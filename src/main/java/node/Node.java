package node;

import com.google.gson.GsonBuilder;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.*;
import com.google.gson.Gson;


import java.beans.PropertyChangeListener;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class Node {
    private static Node node;
    public static ArrayList<Node> networkObjects;

    private int nodeId;
    public HashMap<Integer, ArrayList<Integer>> connections;
    private ArrayList<ArrayList<Integer>> routing;

    public int getNodeId() {
        return nodeId;
    }

    public void setNodeId(int nodeId) {
        this.nodeId = nodeId;
    }

    public HashMap<Integer, ArrayList<Integer>> getConnections() {
        return connections;
    }

    public void setConnections(HashMap<Integer, ArrayList<Integer>> connections) {
        this.connections = connections;
    }

    public ArrayList<ArrayList<Integer>> getRouting() {
        return routing;
    }

    public void setRouting(ArrayList<ArrayList<Integer>> routing) {
        this.routing = routing;
    }

    private ENodeColor color;

    public String getNodeColorMessage() {
        return this.getNodeId() + ":" + this.color.toString();
    }

    public Node(String[] args) {
        if (args.length > 0) {
            this.nodeId = Integer.parseInt(args[0]);
        } else {
            this.nodeId = 0;
        }

        this.connections = new HashMap<Integer, ArrayList<Integer>>();
        this.connections.put(nodeId, new ArrayList<>());
        for (int i = 1; i < args.length; i++) {
            this.connections.get(nodeId).add(Integer.parseInt(args[i]));
        }

        this.routing = new ArrayList<ArrayList<Integer>>();
        List<Integer> connectedNodes = this.connections.get(nodeId);
        for (Integer node : connectedNodes) {
            ArrayList<Integer> routes = new ArrayList<>();
            routes.add(node); // destination
            routes.add(1); // distance
            routes.add(nodeId); // where to go to reach destination
            this.routing.add(routes);
        }
    }

    public void log(String string) {
        String stringToPrint = "nodeId: " + this.getNodeId() + ": " + string;
        System.out.println(stringToPrint);
    }

    private static Connection establishConnection() throws Exception {
        // TODO Read credential from a file
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("rat.rmq2.cloudamqp.com");
        factory.setPort(5672);
        factory.setUsername("sgbdexna");
        factory.setVirtualHost("sgbdexna");
        factory.setPassword("HLRzRamxhUobw5vEnZRXAHNNluy6aNwQ");
        return factory.newConnection();
    }

    public void joinClient(String jsonString) throws Exception {
        final String QUEUE_NAME = "join";

        try (Connection connection = establishConnection();
             Channel channel = connection.createChannel()) {
            channel.queueDeclare(QUEUE_NAME, false, false, false, null);
            channel.confirmSelect(); // Enable publisher confirms
            channel.basicPublish("", QUEUE_NAME, null, jsonString.getBytes(StandardCharsets.UTF_8));
            channel.waitForConfirmsOrDie(5_000);
            if (channel.waitForConfirms()) {
                this.log("[x] Request for new node '" + node.getNodeId() + "' sent and confirmed by RabbitMQ");
            } else {
                this.log("[!] Request for new node '" + node.getNodeId() + "' sent but not confirmed by RabbitMQ");
            }
        } catch (RuntimeException | InterruptedException e) {
            System.out.println(" [.] " + e);
        }
    }

    public void handleMessage(String message) {
        boolean me = false;
        boolean neigbour = false;
        int farNeigbourDistance = 0;

        Gson gson = new Gson();
        Node receivedNode = gson.fromJson(message, Node.class);


        if (receivedNode.getNodeId() == this.getNodeId()) {
            me = true;
        }
        if (!networkObjects.contains(receivedNode)) {
            networkObjects.add(receivedNode);
        }

        if (!me) {
            for (ArrayList<Integer> receivedNodeNeigbour : receivedNode.routing) {
                if (receivedNodeNeigbour.get(0) == this.getNodeId() && receivedNodeNeigbour.get(1) == 1) {
                    neigbour = true;
                } else if (receivedNodeNeigbour.get(0) == this.getNodeId()) {
                    farNeigbourDistance = receivedNodeNeigbour.get(1);
                }
            }
        }

        if (neigbour || farNeigbourDistance > 1) {
            for (ArrayList<Integer> receivedNodeNeigbour : receivedNode.routing) {
                boolean potencialConnection = false;
                for (ArrayList<Integer> nodeNeigbour : this.routing) {
                    int receivedNodeId = receivedNodeNeigbour.get(0);
                    int nodeNeigbourId = nodeNeigbour.get(0);
                    if (receivedNodeId == nodeNeigbourId || receivedNodeId == this.getNodeId()) {
                        this.log("Having already this connection " + receivedNodeNeigbour);
                        potencialConnection = false;
                        break;
                    } else {
                        this.log("Found potential connection: " + receivedNodeNeigbour + " but still voting");
                        potencialConnection = true;
                    }
                }
                if (potencialConnection) {
                    ArrayList<Integer> routes = new ArrayList<>();
                    routes.add(receivedNodeNeigbour.get(0)); // destination
                    if (farNeigbourDistance == 0) {
                        routes.add(receivedNodeNeigbour.get(1) + 1);
                    } else {
                        routes.add(farNeigbourDistance + 1);
                    }
                    routes.add(receivedNode.getNodeId()); // where to go to reach destination
                    this.log("New routes " + routes + " added to " + this.getNodeId());
                    this.routing.add(routes);
                    System.out.println(this.routing);
                    String json = gson.toJson(node);
                    try {
                        Thread.sleep(1000);
                        this.fanout(json);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
//        if (this.getNodeId() == 1) {
//            this.sendMessage(1, 2, "Some message");
//        }
    }

    public void sendMessage(int nodeSource, int nodeDestination, String message) {
        boolean me = nodeSource == this.getNodeId();
        if (nodeDestination == this.getNodeId()) {
            this.log("Message received: " + message);
            return;
        }
        int nextNode = nodeDestination;
        boolean foundRoute = false;


        this.log("Sending message from: " + nodeSource + " to " + nodeDestination + " with message: " + message);
        for (ArrayList<Integer> routes : this.routing) {
            int destination = runtimeMagicGetInteger(routes.get(0));

            int distance = runtimeMagicGetInteger(routes.get(1));

            int whereToGo = runtimeMagicGetInteger(routes.get(2));

            if (nodeDestination == destination && distance == 1) {
                this.log("Sending message directly to node: " + destination);
                nextNode = destination;
                foundRoute = true;
                break;
            } else if (distance > 1) {
                this.log("Sending message through node: " + whereToGo);
                nextNode = whereToGo;
                foundRoute = true;
                break;
            }
        }
        if (!foundRoute) {
            this.log("Too little routes, could not find connection. Try again");
            try {
                this.fanout("routing");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            return;
        }
        MessageObject messageObj = new MessageObject(nodeSource, nodeDestination, message);
        Gson gson = new Gson();
        String json = gson.toJson(messageObj);
        String nodeIdAsString = String.valueOf(nextNode);
        try {
            this.sendToNode(json, nodeIdAsString);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    // I literally have any idea why the integers becomes double in runtime.
    // This is kind of magic.
    public int runtimeMagicGetInteger(Object item) {
        Double magicDouble;

        if (item instanceof Double) {
            magicDouble = (Double) item;
        } else if (item instanceof Integer) {
            magicDouble = ((Integer) item).doubleValue();
        } else {
            throw new RuntimeException("Unknown type");
        }

        return magicDouble.intValue();
    }

    public void listen() throws Exception {

        final String EXCHANGE_NAME = "nodesConnections";

        Connection connection = establishConnection();
        Channel channel = connection.createChannel();

        channel.exchangeDeclare(EXCHANGE_NAME, "fanout");
        String queueName = channel.queueDeclare().getQueue();
        channel.queueBind(queueName, EXCHANGE_NAME, "");

        this.log("[*] Waiting for nodes messages. To exit press CTRL+C");
        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), "UTF-8");
            this.log("[x] Received '" + message + "'");
            if (message.equals("routing")) {
                this.log("Receive routing request, propagating this node");
                Gson gson = new Gson();
                String json = gson.toJson(node);
                try {
                    this.fanout(json);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            } else {
                node.handleMessage(message);
            }
        };
        channel.basicConsume(queueName, true, deliverCallback, consumerTag -> {
        });

    }

    public void fanout(String message) throws Exception {

        final String EXCHANGE_NAME = "nodesConnections";

        try (Connection connection = establishConnection();
             Channel channel = connection.createChannel()) {
            channel.exchangeDeclare(EXCHANGE_NAME, "fanout");
            channel.basicPublish(EXCHANGE_NAME, "", null, message.getBytes("UTF-8"));
            this.log("[x] Message propagated '" + message + "'");
        }
    }

    public static void sendToNode(String message, String QUEUE_NAME) throws Exception {

        try (Connection connection = establishConnection();
             Channel channel = connection.createChannel()) {
            channel.queueDeclare(QUEUE_NAME, false, false, false, null);
            channel.basicPublish("", QUEUE_NAME, null, message.getBytes(StandardCharsets.UTF_8));
            node.log("[x] Message to node  '" + QUEUE_NAME + "' sent");
        } catch (RuntimeException e) {
            System.out.println(" [.] " + e);
        }
    }

    public void sendNodeColor() {
        String QUEUE_NAME = "nodeColor";

        try {
            try (Connection connection = establishConnection();
                 Channel channel = connection.createChannel()) {
                channel.queueDeclare(QUEUE_NAME, false, false, false, null);
                channel.basicPublish("", QUEUE_NAME, null, this.getNodeColorMessage().getBytes(StandardCharsets.UTF_8));
                node.log("[x] Node color to node  '" + QUEUE_NAME + "' sent");
            } catch (RuntimeException e) {
                System.out.println(" [.] " + e);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void receiveOnNode() throws Exception {
        String nodeIdAsString = String.valueOf(this.getNodeId());
        String QUEUE_NAME = nodeIdAsString;

        Connection connection = establishConnection();
        Channel channel = connection.createChannel();

        channel.queueDeclare(QUEUE_NAME, false, false, false, null);
        this.log("[*] Waiting for direct messages. To exit press CTRL+C");

        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            String receivedMessage = new String(delivery.getBody(), "UTF-8");
            Gson gson = new Gson();
            MessageObject messageObj = gson.fromJson(receivedMessage, MessageObject.class);
            if (messageObj.getNodeDestination() == this.getNodeId()) {
                this.log("Message has been received: " + messageObj.getMessage());
                this.color = ENodeColor.RECEIVED;
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } else {
                this.sendMessage(this.getNodeId(), messageObj.getNodeDestination(), messageObj.getMessage());
                this.color = ENodeColor.TRANSPORTING;
            }

            this.sendNodeColor();

        };
        channel.basicConsume(QUEUE_NAME, true, deliverCallback, consumerTag -> {
        });

    }

    public static void main(String[] args) throws Exception {

        node = new Node(args);
        networkObjects = new ArrayList<Node>();
        networkObjects.add(node);
        Gson gson = new Gson();
        String json = gson.toJson(node);
        System.out.println(node);

        try {
            node.log("Born of node number " + node.getNodeId());
            node.joinClient(json);
        } catch (NumberFormatException e) {
            System.out.println("Not an integer");
        }
        node.listen();
        node.receiveOnNode();
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Node ID: ").append(nodeId).append("\n");
        sb.append("Connections: ").append(connections).append("\n");
        sb.append("Routing: ").append(routing).append("\n");
        return sb.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        Node other = (Node) obj;
        return nodeId == other.nodeId && Objects.equals(connections, other.connections) && Objects.equals(routing, other.routing);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nodeId, connections, routing);
    }
}