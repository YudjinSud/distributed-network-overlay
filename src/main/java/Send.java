import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.nio.charset.StandardCharsets;

// TODO: Classes to be exported as a Docker image in the future. This represents a node
public class Send {

    private final static String QUEUE_NAME = "join";

    public static void main(String[] args) throws Exception {


        int argAsInt = 0;
        if (args.length != 1) {
            System.out.println("Please provide exactly one argument.");
        } else {
            // Parse the argument as an integer and print it
            try {
                argAsInt = Integer.parseInt(args[0]);
                System.out.println("Born of node number " + argAsInt);
            } catch (NumberFormatException e) {
                System.out.println("Not an integer");
            }
        }

        String message = Integer.toString(argAsInt);

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("rat.rmq2.cloudamqp.com");
        factory.setPort(5672);
        factory.setUsername("sgbdexna");
        factory.setVirtualHost("sgbdexna");
        factory.setPassword("6JG_KIUYfXlkjSFU1lvov_G1ePwxZ9x0");
        System.out.println("Trying to joing node... ");
        try (Connection connection = factory.newConnection(); Channel channel = connection.createChannel()) {
            channel.queueDeclare(QUEUE_NAME, false, false, false, null);
            channel.basicPublish("", QUEUE_NAME, null, message.getBytes(StandardCharsets.UTF_8));
            System.out.println(" [x] Request for node sent " + message);
        }
    }
}