package node;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.*;


import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

// TODO: Classes to be exported as a Docker image in the future. This represents a node
public class Node {

    private int nodeId;
    private static Node node;
    private static ArrayList<NetworkNode> networkObjects;
    public static HashMap<Integer, ArrayList<Integer>> connections;

    public Node(String[] args) {
        if (args.length > 0) {
            this.nodeId = Integer.parseInt(args[0]);
        } else {
            this.nodeId = 0;
        }

        connections = new HashMap<Integer, ArrayList<Integer>>();
        connections.put(nodeId, new ArrayList<>());
        for (int i = 1; i < args.length; i++) {
            connections.get(nodeId).add(Integer.parseInt(args[i]));
        }

    }

    public int getNodeId() {
        return nodeId;
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
        factory.setUsername("sgbdexna");
        factory.setVirtualHost("sgbdexna");
        factory.setPassword("HLRzRamxhUobw5vEnZRXAHNNluy6aNwQ");
        return factory.newConnection();
    }

    public void joinClient(String[] args) throws Exception {
        final String QUEUE_NAME = "join";

        // We are converting a string of array to string, need to separate on the receiving side
        String serializedMessage = String.join(",", args);

        try (Connection connection = establishConnection();
             Channel channel = connection.createChannel()) {
            channel.queueDeclare(QUEUE_NAME, false, false, false, null);
            channel.basicPublish("", QUEUE_NAME, null, serializedMessage.getBytes(StandardCharsets.UTF_8));
            node.log("[x] Request for new node '" + node.getNodeId() + "' sent");
        } catch (RuntimeException e) {
            System.out.println(" [.] " + e);
        }
    }

    public void handleMessage(String message) {
        boolean nodeExists = false;

        System.out.println("handleMessage(): " + message);
        String[] receivedArray = message.split(",");
        String nodeString = receivedArray[0];
        int nodeInt = Integer.parseInt(nodeString);


        for (NetworkNode obj : networkObjects) {
            if (obj.getNodeId() == nodeInt) {
                // If the node exists, set nodeExists to true and break the loop
                nodeExists = true;
                node.log(obj.toString());
                break;
            }
        }

        if (!nodeExists) {
            HashMap<Integer, ArrayList<Integer>> connectionsNeighbour = new HashMap<Integer, ArrayList<Integer>>();
            connectionsNeighbour.put(nodeInt, new ArrayList<>());
            for (int i = 1; i < receivedArray.length; i++) {
                connectionsNeighbour.get(nodeInt).add(Integer.parseInt(receivedArray[i]));
            }
            NetworkNode newNode = new NetworkNode(nodeInt, connectionsNeighbour);
            networkObjects.add(newNode);
            System.out.println("handleMessage(): Got new routing from node" + newNode.toString());
        }


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
            node.handleMessage(message);
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
        networkObjects = new ArrayList<NetworkNode>();
        NetworkNode nodeNetwork = new NetworkNode(node.getNodeId(), connections);
        networkObjects.add(nodeNetwork);


        try {
            node.log("Born of node number " + node.getNodeId());
            node.joinClient(args);
        } catch (NumberFormatException e) {
            System.out.println("Not an integer");
        }
        node.listen();
    }
}