package Conroller;

import GraphView.GraphView;
import core.OverlayGraph;

import java.util.ArrayList;

public class AppController {

    private GraphView graphView;

    public Integer onAddNode(Integer nodeNb) {
        System.out.println(nodeNb);

        return nodeNb;
    }

    public AppController( OverlayGraph overlayGraph, GraphView graphView) {
        this.graphView = graphView;

        overlayGraph.addPropertyChangeListener(evt -> {
            ArrayList<Integer> nodeList = (ArrayList<Integer>) evt.getNewValue();

            this.graphView.updateVirtualGraph(nodeList);
        });
    }
}
