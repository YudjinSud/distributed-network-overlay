package node;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.*;


import java.nio.charset.StandardCharsets;

// TODO: Classes to be exported as a Docker image in the future. This represents a node
public class Node {

    private int nodeId;
    private static Node node;
    public Node(String[] args) {
        if (args.length > 0) {
            this.nodeId = Integer.parseInt(args[0]);
        } else {
            // If no argument is provided, set the nodeId to a default value
            this.nodeId = 0;
        }
    }
    public int getNodeId(){
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
    public void listen() throws Exception{

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
        };
        channel.basicConsume(queueName, true, deliverCallback, consumerTag -> { });

    }

    public static void main(String[] args) throws Exception {

        node = new Node(args);

//        System.out.println("Number of arguments: " + args.length);
//        for (int i = 0; i < args.length; i++) {
//            System.out.println("Argument " + i + ": " + args[i]);
//        }

        try {
            node.log("Born of node number " + node.getNodeId());
            node.joinClient(args);
        } catch (NumberFormatException e) {
            System.out.println("Not an integer");
        }
        node.listen();
    }
}