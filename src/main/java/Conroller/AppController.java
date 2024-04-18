package Conroller;

import GUI.GUI;
import core.OverlayGraph;

public class AppController {

    private OverlayGraph overlayGraph;
    private GUI graphView;

    public Integer onAddNode(Integer nodeNb) {
        System.out.println(nodeNb);

        return nodeNb;
    }

    public AppController( OverlayGraph overlayGraph) {
        this.overlayGraph = overlayGraph;
//        this.graphView = graphView;

//        this.overlayGraph.setOnAddNodeCallback(this::onAddNode);
    }
}
