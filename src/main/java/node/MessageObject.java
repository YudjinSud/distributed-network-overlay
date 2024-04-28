package node;

public class MessageObject {
    private int nodeSource;
    private int nodeDestination;
    private String message;

    // Constructor
    public MessageObject(int nodeSource, int nodeDestination, String message) {
        this.nodeSource = nodeSource;
        this.nodeDestination = nodeDestination;
        this.message = message;
    }

    // Getters and setters
    public int getNodeSource() {
        return nodeSource;
    }

    public void setNodeSource(int nodeSource) {
        this.nodeSource = nodeSource;
    }

    public int getNodeDestination() {
        return nodeDestination;
    }

    public void setNodeDestination(int nodeDestination) {
        this.nodeDestination = nodeDestination;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "MessageObject{" +
                "nodeSource=" + nodeSource +
                ", nodeDestination=" + nodeDestination +
                ", message='" + message + '\'' +
                '}';
    }
}

