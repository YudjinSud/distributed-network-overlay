package node;

import com.google.gson.*;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;

public class NodeDeserializer implements JsonDeserializer<Node> {
    @Override
    public Node deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();

        Node node = new Node(new String[0]);
        node.setNodeId(jsonObject.get("nodeId").getAsInt());

        // Deserialize connections
        HashMap<Integer, ArrayList<Integer>> connections = new HashMap<>();
        JsonObject connectionsObj = jsonObject.getAsJsonObject("connections");
        for (String key : connectionsObj.keySet()) {
            ArrayList<Integer> list = context.deserialize(connectionsObj.get(key), ArrayList.class);
            connections.put(Integer.parseInt(key), list);
        }
        node.setConnections(connections);

        // Deserialize routing
        JsonArray routingArray = jsonObject.getAsJsonArray("routing");
        ArrayList<ArrayList<Integer>> routing = new ArrayList<>();
        for (int i = 0; i < routingArray.size(); i++) {

            ArrayList<Integer> sublist = context.deserialize(routingArray.get(i), ArrayList.class);
            routing.add(sublist);
        }
        node.setRouting(routing);

        return node;
    }

}

