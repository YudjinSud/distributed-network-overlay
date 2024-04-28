package GraphView;

import com.brunomnsilva.smartgraph.graph.Graph;
import com.brunomnsilva.smartgraph.graph.GraphEdgeList;
import com.brunomnsilva.smartgraph.graphview.SmartCircularSortedPlacementStrategy;
import com.brunomnsilva.smartgraph.graphview.SmartGraphPanel;
import com.brunomnsilva.smartgraph.graphview.SmartPlacementStrategy;

import core.NetworkGraph;
import node.ENodeColor;
import node.NodeColor;

import java.util.ArrayList;


public class GraphView {

    public Graph<String, String> physicalGraphEdgeList = new GraphEdgeList<>();
    public Graph<String, String> virtualGraphEdgeList = new GraphEdgeList<>();

    public SmartGraphPanel<String, String> physicalGraph;
    public SmartGraphPanel<String, String> virtualGraph;

    NetworkGraph graph = NetworkGraph.createGraph();

    public GraphView() {
        SmartPlacementStrategy initialPlacement = new SmartCircularSortedPlacementStrategy();

        this.createPhysicalGraph();

        physicalGraph = new SmartGraphPanel<>(physicalGraphEdgeList, initialPlacement);
        virtualGraph = new SmartGraphPanel<>(virtualGraphEdgeList, initialPlacement);
    }

    public void initGraph() {
        this.physicalGraph.init();
        this.virtualGraph.init();
        this.physicalGraph.setAutomaticLayout(true);
        this.virtualGraph.setAutomaticLayout(true);
    }

    public void updateNodeColors(ArrayList<NodeColor> nodeColors) {
        // update the colors of the nodes in the physical graph

        System.out.println("Updating node colors");

        graph.adjList.forEach((k, v) -> {
            physicalGraph.getStylableVertex(String.valueOf(k)).setStyleClass("vertex");
        });

        nodeColors.forEach(nodeColor -> {
            if (nodeColor.nodeColor.getValue() == ENodeColor.TRANSPORTING.getValue()) {
                physicalGraph.getStylableVertex(String.valueOf(nodeColor.nodeId)).setStyleClass("vertexTransporting");
            } else {
                physicalGraph.getStylableVertex(String.valueOf(nodeColor.nodeId)).setStyleClass("vertexReceived");
            }
        });
    }

    public void updateVirtualGraph(ArrayList<Integer> nodeList) {
        // create a virtual graph in a ring topology

        virtualGraphEdgeList.vertices().forEach(v -> {
            virtualGraphEdgeList.removeVertex(v);
        });

        virtualGraphEdgeList.edges().forEach(e -> {
            virtualGraphEdgeList.removeEdge(e);
        });

        for (int i = 0; i < nodeList.size(); i++) {
            virtualGraphEdgeList.insertVertex(String.valueOf(nodeList.get(i)));
        }

        if (nodeList.size() == 1) {
            this.virtualGraph.updateAndWait();
            return;
        }

        for (int i = 0; i < nodeList.size(); i++) {
            if (i == nodeList.size() - 1) {
                virtualGraphEdgeList.insertEdge(String.valueOf(nodeList.get(i)), String.valueOf(nodeList.get(0)), String.valueOf(nodeList.get(i)) + "-" + String.valueOf(nodeList.get(0)));
            } else {
                virtualGraphEdgeList.insertEdge(String.valueOf(nodeList.get(i)), String.valueOf(nodeList.get(i + 1)), String.valueOf(nodeList.get(i)) + "-" + String.valueOf(nodeList.get(i + 1)));
            }
        }

        this.virtualGraph.updateAndWait();
    }


    public void createPhysicalGraph() {
        for (int i = 1; i  <= this.graph.size(); i++) {
            physicalGraphEdgeList.insertVertex(String.valueOf(i));
        }

        for (int i = 1; i <= this.graph.size(); i++) {
            for (int j : this.graph.getAdjNodes(i)) {
                if (i < j) {
                    physicalGraphEdgeList.insertEdge(String.valueOf(i), String.valueOf(j), String.valueOf(i) + "-" + String.valueOf(j));
                }
            }
        }
    }

}