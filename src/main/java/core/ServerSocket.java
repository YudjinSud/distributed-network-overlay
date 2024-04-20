package core;

import com.google.gson.GsonBuilder;
import com.rabbitmq.client.*;
import node.Node;
import com.google.gson.Gson;
import node.NodeDeserializer;


public class ServerSocket {
    public OverlayGraph overlay;

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


    public void join() throws Exception {
        String QUEUE_NAME = "join";

        Connection connection = establishConnection();
        Channel channel = connection.createChannel();

        channel.queueDeclare(QUEUE_NAME, false, false, false, null);
        System.out.println(" [*] Waiting for nodes to join. To exit press CTRL+C");

        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            String receivedMessage = new String(delivery.getBody(), "UTF-8");

            System.out.println(receivedMessage);

            GsonBuilder builder = new GsonBuilder();
            builder.registerTypeAdapter(Node.class, new NodeDeserializer());
            Gson gson = builder.create();

            builder.registerTypeAdapter(Node.class, new NodeDeserializer());

            Node receivedNode = gson.fromJson(receivedMessage, Node.class);

            int nodeInt = receivedNode.getNodeId();
            System.out.println("Joining node: " + nodeInt);
            overlay.addNode(nodeInt);
            //overlay.printNodeList();
            overlay.printGraph();

            System.out.println("Propagating node");
            try {
                Thread.sleep(1000);
                this.fanout(receivedMessage);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };
        channel.basicConsume(QUEUE_NAME, false, deliverCallback, consumerTag -> {
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

    public ServerSocket() {

        this.overlay = new OverlayGraph();

        this.overlay.addPropertyChangeListener(evt -> {
            Object obj = evt.getNewValue();
            System.out.println(obj);
        });

        try {
            join();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

