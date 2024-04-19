package node;

import com.google.gson.GsonBuilder;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.*;
import com.google.gson.Gson;


import java.nio.charset.StandardCharsets;
import java.util.*;

// TODO: Classes to be exported as a Docker image in the future. This represents a node
public class Node {
    private static Node node;
    private static ArrayList<Node> networkObjects;

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
        String stringToPrint = "nodeId: " + node.getNodeId() + ": " + string;
        System.out.println(stringToPrint);
    }

    private static Connection establishConnection() throws Exception {
        // TODO Read credential from a file
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("rat.rmq2.cloudamqp.com");
        factory.setPort(5672);
        factory.setUsername("wqnhzlhb");
        factory.setVirtualHost("wqnhzlhb");
        factory.setPassword("o9EdvwoKVGxNTfIEjVSqF9UUKPSrD8EJ");
        return factory.newConnection();
    }

    public void joinClient(String jsonString) throws Exception {
        final String QUEUE_NAME = "join";

        try (Connection connection = establishConnection();
             Channel channel = connection.createChannel()) {
            channel.queueDeclare(QUEUE_NAME, false, false, false, null);
            channel.basicPublish("", QUEUE_NAME, null, jsonString.getBytes(StandardCharsets.UTF_8));
            node.log("[x] Request for new node '" + node.getNodeId() + "' sent");
        } catch (RuntimeException e) {
            System.out.println(" [.] " + e);
        }
    }

    public void handleMessage(String message) {
        boolean nodeExists = false;
        boolean me = false;
        boolean neigbour = false;
        int farNeigbourDistance = 0;

        Gson gson = new Gson();
        Node receivedNode = gson.fromJson(message, Node.class);


        if (receivedNode.getNodeId() == node.getNodeId()) {
            me = true;
        }

        for (Node obj : networkObjects) {
            if (obj.getNodeId() == receivedNode.getNodeId()) {
                nodeExists = true;
                break;
            }
        }
        if (!networkObjects.contains(receivedNode)) {
            networkObjects.add(receivedNode);
        }

        if (!me) {
            for (ArrayList<Integer> receivedNodeNeigbour : receivedNode.routing) {
                if (receivedNodeNeigbour.get(0) == node.getNodeId() && receivedNodeNeigbour.get(1) == 1) {
                    neigbour = true;
                } else if (receivedNodeNeigbour.get(0) == node.getNodeId()){
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
                    if (receivedNodeId == nodeNeigbourId || receivedNodeId == node.getNodeId()) {
                        node.log("Having already this connection " + receivedNodeNeigbour);
                        potencialConnection = false;
                        break;
                    } else {
                        node.log("Found potential connection: " + receivedNodeNeigbour + " but still voting");
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
                    node.log("New routes " + routes + " added to " + node.getNodeId());
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
        if (this.getNodeId() == 1 ){
            this.sendMessage(1, 2, "suka");
        }
    }

    public void sendMessage(int nodeSource, int nodeDestination, String message) {
        boolean me = nodeSource == this.getNodeId();
        if (nodeDestination == this.getNodeId()) {node.log("Message received: " + message); return;}
        int nextNode = nodeDestination;
        boolean foundRoute = false;


        node.log("Sending message from: " + nodeSource + " to " + nodeDestination + " with message: " + message);
        for (ArrayList<Integer> routes : this.routing) {
            int destination = routes.get(0); // destination
            int distance = routes.get(1); // distance
            int whereToGo = routes.get(2); // where to go to reach destination
            if (nodeDestination == destination && distance == 1){
                node.log("Sending message directly to node: " + destination);
                nextNode = destination;
                foundRoute = true;
                break;
            } else if (distance > 1) {
                node.log("Sending message through node: " + whereToGo);
                nextNode = whereToGo;
                foundRoute = true;
                break;
            }
        }
        if (!foundRoute) {
            node.log("Too little routes, could not find connection");
            return;
            // fanout message to ask others to send their routes ?
        }
        MessageObject messageObj = new MessageObject(nodeSource,nodeDestination,nextNode,message);
        Gson gson = new Gson();
        String json = gson.toJson(messageObj);
        String nodeIdAsString = String.valueOf(nextNode);
        try {
            this.sendToNode(json, nodeIdAsString);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    public void listen() throws Exception {

        final String EXCHANGE_NAME = "nodesConnections";

        Connection connection = establishConnection();
        Channel channel = connection.createChannel();

        channel.exchangeDeclare(EXCHANGE_NAME, "fanout");
        String queueName = channel.queueDeclare().getQueue();
        channel.queueBind(queueName, EXCHANGE_NAME, "");

        node.log("[*] Waiting for nodes messages. To exit press CTRL+C");
        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), "UTF-8");
            node.log("[x] Received '" + message + "'");
            node.handleMessage(message);
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
            System.out.println(" [x] Message propagated '" + message + "'");
        }
    }

    public void sendToNode(String message, String QUEUE_NAME) throws Exception {

        try (Connection connection = establishConnection();
             Channel channel = connection.createChannel()) {
            channel.queueDeclare(QUEUE_NAME, false, false, false, null);
            channel.basicPublish("", QUEUE_NAME, null, message.getBytes(StandardCharsets.UTF_8));
            node.log("[x] Message to node  '" +  QUEUE_NAME + "' sent");
        } catch (RuntimeException e) {
            System.out.println(" [.] " + e);
        }
    }

    public void receiveOnNode() throws Exception {
        String nodeIdAsString = String.valueOf(node.getNodeId());
        String QUEUE_NAME = nodeIdAsString;

        Connection connection = establishConnection();
        Channel channel = connection.createChannel();

        channel.queueDeclare(QUEUE_NAME, false, false, false, null);
        node.log(" [*] Waiting for direct messages. To exit press CTRL+C");

        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            String receivedMessage = new String(delivery.getBody(), "UTF-8");
            Gson gson = new Gson();
            MessageObject messageObj = gson.fromJson(receivedMessage, MessageObject.class);
            if (messageObj.getNodeDestination() == this.getNodeId()){
                node.log("Message has been received: " + messageObj.getMessage());
            } else {
                this.sendMessage(this.getNodeId(), messageObj.getNodeDestination(), messageObj.getMessage());
            }
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