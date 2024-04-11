package core;

import com.rabbitmq.client.*;

public class ServerSocket {
    static OverlayGraph overlay;
    private static ServerSocket server;
    private final static String QUEUE_NAME = "join";

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


    public void join() throws Exception {
        String QUEUE_NAME = "join";

        OverlayGraph overlay = new OverlayGraph();

        Connection connection = establishConnection();
        Channel channel = connection.createChannel();

        channel.queueDeclare(QUEUE_NAME, false, false, false, null);
        System.out.println(" [*] Waiting for nodes to join. To exit press CTRL+C");


        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            String receivedMessage = new String(delivery.getBody(), "UTF-8");
            String[] receivedArray = receivedMessage.split(",");
            String nodeString = receivedArray[0];
            int nodeInt = Integer.parseInt(nodeString);
            System.out.println("Joining node: " + nodeInt);
            overlay.addNode(nodeInt);
            //overlay.printNodeList();
            overlay.printGraph();
            System.out.println("Propagating node");
            try {
                Thread.sleep(1000);
                server.fanout(receivedMessage);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };
        channel.basicConsume(QUEUE_NAME, true, deliverCallback, consumerTag -> {
        });

    }

    public void fanout(String message) throws Exception{

        final String EXCHANGE_NAME = "nodesConnections";

        try (Connection connection = establishConnection();
             Channel channel = connection.createChannel()) {
            channel.exchangeDeclare(EXCHANGE_NAME, "fanout");
            channel.basicPublish(EXCHANGE_NAME, "", null, message.getBytes("UTF-8"));
            System.out.println(" [x] Message propagated '" + message + "'");
        }
    }


    public static void main(String main[]) {

        server = new ServerSocket();
        overlay = new OverlayGraph();
        try {
            server.join();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }
}

