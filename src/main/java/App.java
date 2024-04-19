import Conroller.AppController;
import GraphView.GraphView;
import core.ServerSocket;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Separator;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class App extends Application {
    TextArea logArea;

    @Override
    public void start(Stage stage)  {
        try {
            GraphView graphView = new GraphView();

            this.setupGUI(stage, graphView);

            graphView.initGraph();

            ServerSocket server = new ServerSocket();

            AppController appController = new AppController(server.overlay, graphView);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void setupGUI(Stage stage, GraphView gui) {
        HBox graphBox = new HBox();

        graphBox.getChildren().addAll(gui.physicalGraph, new Separator(), gui.virtualGraph);

        HBox.setHgrow(gui.physicalGraph, Priority.ALWAYS);
        HBox.setHgrow(gui.virtualGraph, Priority.ALWAYS);

        gui.physicalGraph.maxWidthProperty().bind(graphBox.widthProperty().multiply(0.5));
        gui.virtualGraph.maxWidthProperty().bind(graphBox.widthProperty().multiply(0.5));

        graphBox.setStyle("-fx-padding: 10;");

        logArea = new TextArea();
        logArea.setPrefHeight(100);

        Button btnLeft = new Button("Msg left");
        btnLeft.setOnAction(event -> logMessage("Message sent to the left graph"));

        Button btnRight = new Button("Msg right");
        btnRight.setOnAction(event -> logMessage("Message sent to the right graph"));

        HBox buttonBox = new HBox(10, btnLeft, btnRight);
        buttonBox.setAlignment(Pos.CENTER);


        VBox root = new VBox();
        root.getChildren().addAll(graphBox, buttonBox, logArea);

        VBox.setVgrow(graphBox, Priority.ALWAYS);
        logArea.maxWidthProperty().bind(stage.widthProperty());

        Scene scene = new Scene(root, 1024, 768);
        stage.setTitle("Distributed Overlay Network");
        stage.setScene(scene);
        stage.show();

        root.heightProperty().addListener((observable, oldValue, newValue) -> {
            double totalHeight = newValue.doubleValue();
            graphBox.setPrefHeight(totalHeight * 0.8);  // Graph takes 80% of height
            logArea.setPrefHeight(totalHeight * 0.2 - buttonBox.getHeight());  // Log area takes 20% minus buttonBox height
        });
    }

    private void logMessage(String msg) {
        logArea.appendText(msg + "\n");
    }


    public static void main(String[] args) {
        launch(args);
    }
}
