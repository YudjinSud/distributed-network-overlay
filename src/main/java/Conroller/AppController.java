package Conroller;

import GraphView.GraphView;
import com.google.gson.Gson;
import core.OverlayGraph;
import node.MessageObject;
import node.Node;
import node.NodeColor;

import java.util.ArrayList;

public class AppController {

    private GraphView graphView;

    private OverlayGraph overlayGraph;

    public AppController(OverlayGraph overlayGraph, GraphView graphView) {
        this.graphView = graphView;

        this.overlayGraph = overlayGraph;

        overlayGraph.addPropertyChangeListener(evt -> {

            if (evt.getPropertyName() == "nodeList") {
                ArrayList<Integer> nodeList = (ArrayList<Integer>) evt.getNewValue();
                this.graphView.updateVirtualGraph(nodeList);


            } else if (evt.getPropertyName() == "nodeColors") {
                ArrayList<NodeColor> nodeColors = (ArrayList<NodeColor>) evt.getNewValue();
                this.graphView.updateNodeColors(nodeColors);
            }

        });
    }

    public void sendMessage(int fromNode, int toNode, String message) {

        try {
            MessageObject messageObj = new MessageObject(fromNode, toNode, message);
            Gson gson = new Gson();
            String json = gson.toJson(messageObj);
            String nodeIdAsString = String.valueOf(fromNode);

            Node.sendToNode(json, nodeIdAsString);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
