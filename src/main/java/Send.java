import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

// TODO: Classes to be exported as a Docker image in the future. This represents a node
public class Send {
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

    public void joinClient(Integer argAsInt) throws Exception {
        final String QUEUE_NAME = "join";

        String message = Integer.toString(argAsInt);

        try (Connection connection = establishConnection();
             Channel channel = connection.createChannel()) {
            channel.queueDeclare(QUEUE_NAME, false, false, false, null);
            channel.basicPublish("", QUEUE_NAME, null, message.getBytes(StandardCharsets.UTF_8));
            System.out.println(" [x] Request for node sent " + message);
        } catch (RuntimeException e) {
            System.out.println(" [.] " + e);
        }
    }

    public String computeRoute(String message) throws IOException, InterruptedException, ExecutionException, Exception {
        String requestQueueName = "rpc_queue";
        Connection connection = establishConnection();
        Channel channel = connection.createChannel();

        final String corrId = UUID.randomUUID().toString();

        String replyQueueName = channel.queueDeclare().getQueue();
        AMQP.BasicProperties props = new AMQP.BasicProperties
                .Builder()
                .correlationId(corrId)
                .replyTo(replyQueueName)
                .build();

        channel.basicPublish("", requestQueueName, props, message.getBytes("UTF-8"));

        final CompletableFuture<String> response = new CompletableFuture<>();

        String ctag = channel.basicConsume(replyQueueName, true, (consumerTag, delivery) -> {
            if (delivery.getProperties().getCorrelationId().equals(corrId)) {
                response.complete(new String(delivery.getBody(), "UTF-8"));
            }
        }, consumerTag -> {
        });

        String result = response.get();
        channel.basicCancel(ctag);
        return result;
    }

    public static void main(String[] args) throws Exception {

        Send send = new Send();

        int argAsInt = 0;
        if (args.length != 1) {
            System.out.println("Please provide exactly one argument.");
        } else {
            // Parse the argument as an integer and print it
            try {
                argAsInt = Integer.parseInt(args[0]);
                System.out.println("Born of node number " + argAsInt);
                send.joinClient(argAsInt);
            } catch (NumberFormatException e) {
                System.out.println("Not an integer");
            }
        }

        Scanner scanner = new Scanner(System.in);
        System.out.println("Write direction (r/l) and the node to compute the route ex. r 1 ");
        while (true) {
            String input = scanner.nextLine();

            try {
                System.out.println(" [x] Requesting route for(" + input + ")");
                String response = send.computeRoute(input);
                System.out.println(" [.] Got '" + response + "'");

            } catch (IOException | TimeoutException | InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
    }
}