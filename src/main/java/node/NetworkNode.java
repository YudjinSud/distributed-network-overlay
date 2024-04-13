package node;

import java.util.ArrayList;
import java.util.HashMap;

public class NetworkNode {
    private static Integer nodeId;
    private HashMap<Integer, ArrayList<Integer>>  connections;
    private HashMap<Integer, Integer> hopDistance;

    public NetworkNode(Integer nodeId, HashMap<Integer, ArrayList<Integer>> connections){
        this.nodeId = nodeId;
        this.connections = connections;
        initialHop(connections);
    }
    public int getNodeId(){
        return nodeId;
    }
    public HashMap<Integer, Integer> getHopDistance() {
        return this.hopDistance;
    }
    public void initialHop(HashMap<Integer, ArrayList<Integer>>  connections){

        hopDistance = new HashMap<Integer, Integer>();
        for (Integer value : connections.get(nodeId)) {
            hopDistance.put(value, 1);
        }
    }

    @Override
    public String toString() {
        return "NetworkObject{" +
                "nodeId=" + getNodeId() +
                ", connection=" + connections +
                ", hopDistance=" + hopDistance +
                '}';
    }


}
