package core;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

public class ServerSocket {

    private final static String QUEUE_NAME = "join";

    public static void main(String[] args) throws Exception {

        OverlayGraph overlay = new OverlayGraph();

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("rat.rmq2.cloudamqp.com");
        factory.setPort(5672);
        factory.setUsername("sgbdexna");
        factory.setVirtualHost("sgbdexna");
        factory.setPassword("6JG_KIUYfXlkjSFU1lvov_G1ePwxZ9x0");
        Connection connection = factory.newConnection();
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
}

