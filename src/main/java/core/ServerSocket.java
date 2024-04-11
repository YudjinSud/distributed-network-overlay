package core;

import com.rabbitmq.client.*;

public class ServerSocket {
    static OverlayGraph overlay;

    private final static String QUEUE_NAME = "join";

    private static Connection establishConnection() throws Exception {
        // TODO Read credential from a file
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("rat.rmq2.cloudamqp.com");
        factory.setPort(5672);
        factory.setUsername("sgbdexna");
        factory.setVirtualHost("sgbdexna");
        factory.setPassword("6JG_KIUYfXlkjSFU1lvov_G1ePwxZ9x0");
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
            String message = new String(delivery.getBody(), "UTF-8");
            int messageInt = Integer.parseInt(message);
            System.out.println("Joining node: " + messageInt);
            overlay.addNode(messageInt);
            //overlay.printNodeList();
            overlay.printGraph();
        };
        channel.basicConsume(QUEUE_NAME, true, deliverCallback, consumerTag -> {
        });

        // TODO: Will each node have its own assigned queue for communication / RPC?
    }

    public void awaitRPC() throws Exception {

        String RPC_QUEUE_NAME = "rpc_queue";

        Connection connection = establishConnection();
        Channel channel = connection.createChannel();
        channel.queueDeclare(RPC_QUEUE_NAME, false, false, false, null);
        channel.queuePurge(RPC_QUEUE_NAME);

        channel.basicQos(1); // number of server process

        System.out.println(" [x] Awaiting RPC requests");

        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            AMQP.BasicProperties replyProps = new AMQP.BasicProperties
                    .Builder()
                    .correlationId(delivery.getProperties().getCorrelationId())
                    .build();

            String response = "Example of computed answer";
            try {
                String message = new String(delivery.getBody(), "UTF-8");
                System.out.println("Receive following route request:" + message);
            } catch (RuntimeException e) {
                System.out.println(" [.] " + e);
            } finally {
                channel.basicPublish("", delivery.getProperties().getReplyTo(), replyProps, response.getBytes("UTF-8"));
                channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
            }
        };

        channel.basicConsume(RPC_QUEUE_NAME, false, deliverCallback, (consumerTag -> {
        }));
    }


    public static void main(String main[]) {

        ServerSocket server = new ServerSocket();
        overlay = new OverlayGraph();

        try {
            server.join();
            // server.awaitRPC();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }
}

