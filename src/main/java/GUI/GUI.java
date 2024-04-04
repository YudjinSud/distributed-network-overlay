package GUI;

import com.brunomnsilva.smartgraph.graph.Graph;
import com.brunomnsilva.smartgraph.graph.GraphEdgeList;
import com.brunomnsilva.smartgraph.graphview.SmartCircularSortedPlacementStrategy;
import com.brunomnsilva.smartgraph.graphview.SmartGraphPanel;
import com.brunomnsilva.smartgraph.graphview.SmartPlacementStrategy;
import core.OverlayNode;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import core.NetworkGraph;


public class GUI extends Application {

    Graph<String, String> g = new GraphEdgeList<>();

    SmartGraphPanel<String, String> graphView;

    NetworkGraph graph = NetworkGraph.createGraph();

    @Override
    public void start(Stage stage) {
        SmartPlacementStrategy initialPlacement = new SmartCircularSortedPlacementStrategy();

        this.createGraph();

        graphView = new SmartGraphPanel<>(g, initialPlacement);


        Scene scene = new Scene(graphView, 1024, 768);

        stage.setTitle("Distributed overlay network");
        stage.setScene(scene);
        stage.show();


        graphView.init();
        graphView.setAutomaticLayout(true);
    }

    public void createGraph() {
        for (int i = 1; i  <= this.graph.size(); i++) {
            g.insertVertex(String.valueOf(i));
        }

        for (int i = 1; i <= this.graph.size(); i++) {


            // for each node new java program
            OverlayNode node = new OverlayNode(i);



            for (int j : this.graph.getAdjNodes(i)) {
                if (i < j) {
                    g.insertEdge(String.valueOf(i), String.valueOf(j), String.valueOf(i) + "-" + String.valueOf(j));
                }
            }
        }

    }

    public static void main(String[] args) {
        launch();
    }

}