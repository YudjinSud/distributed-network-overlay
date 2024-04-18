package node;

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

        // We are converting a string of array to string, need to separate on the receiving side
        //String serializedMessage = String.join(",", args);

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
                System.out.println(receivedNodeNeigbour.get(0));
                if (receivedNodeNeigbour.get(0) == node.getNodeId()) {
                    System.out.println("Founded sasiada");
                    neigbour = true;
                }
            }
        }

        // To debug
        if (neigbour) {
            for (ArrayList<Integer> receivedNodeNeigbour : receivedNode.routing) {
                boolean potencialConnection = false;
                for (ArrayList<Integer> nodeNeigbour : this.routing) {
                    System.out.println(receivedNodeNeigbour.get(0));
                    System.out.println(nodeNeigbour.get(0));
                    if (receivedNodeNeigbour.get(0) != nodeNeigbour.get(0) && receivedNodeNeigbour.get(0) != node.getNodeId()) {
                        System.out.println("Having already this connexion");
                        continue;
                    } else {
                        System.out.println("Founded potencial connetion" + receivedNodeNeigbour);
                        potencialConnection = true;
                    }
                }
                if (potencialConnection) {
                    System.out.println("Gonna add this" + receivedNodeNeigbour + "to my node " + node.getNodeId());
                }
            }
        }


        //check for the connections
        if (me == false && neigbour == true) {
            System.out.println("Checking for new routing ");

        }


//
//        if (!nodeExists) {
//            HashMap<Integer, ArrayList<Integer>> connectionsNeighbour = new HashMap<Integer, ArrayList<Integer>>();
//            connectionsNeighbour.put(nodeInt, new ArrayList<>());
//            for (int i = 1; i < receivedArray.length; i++) {
//                connectionsNeighbour.get(nodeInt).add(Integer.parseInt(receivedArray[i]));
//            }
//            NetworkNode newNode = new NetworkNode(nodeInt, connectionsNeighbour);
//            networkObjects.add(newNode);
//            System.out.println("handleMessage(): Got new routing from node" + newNode.toString());
//        }


    }

    public void listen() throws Exception {

        final String EXCHANGE_NAME = "nodesConnections";

        Connection connection = establishConnection();
        Channel channel = connection.createChannel();

        channel.exchangeDeclare(EXCHANGE_NAME, "fanout");
        String queueName = channel.queueDeclare().getQueue();
        channel.queueBind(queueName, EXCHANGE_NAME, "");

        node.log("[*] Waiting for messages. To exit press CTRL+C");
        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), "UTF-8");
            node.log("[x] Received '" + message + "'");
            this.handleMessage(message);
        };
        channel.basicConsume(queueName, true, deliverCallback, consumerTag -> {
        });


//        for (int i = 0; i < networkObjects.size() - 1; i++) {
//            HashMap<Integer, Integer> currentHopDistance = networkObjects.get(i).getHopDistance();
//            for (Map.Entry<Integer, Integer> entry : currentHopDistance.entrySet()) {
//                int key = entry.getKey();
//                int value = entry.getValue();
//
//                for (int j = i + 1; j < networkObjects.size(); j++) {
//                    HashMap<Integer, Integer> otherHopDistance = networkObjects.get(j).getHopDistance();
//                    for (Map.Entry<Integer, Integer> entry2 : otherHopDistance.entrySet()) {
//                        int otherKey = entry2.getKey();
//                        int otherValue = entry2.getValue();
//
//                        if (key == otherKey && value == otherValue) {
//                            System.out.println("Matching hopDistance found:");
//                            System.out.println("Node " + i + " hopDistance: " + currentHopDistance);
//                            System.out.println("Node " + j + " hopDistance: " + otherHopDistance);
//                            // Do something here
//                        }
//                    }
//                }
//            }
//        }

    }


    public static void main(String[] args) throws Exception {

        node = new Node(args);
        networkObjects = new ArrayList<Node>();
        // NetworkNode nodeNetwork = new NetworkNode(node.getNodeId(), node.connections);
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

    // hashCode method should be overridden along with equals method
    @Override
    public int hashCode() {
        return Objects.hash(nodeId, connections, routing);
    }

}