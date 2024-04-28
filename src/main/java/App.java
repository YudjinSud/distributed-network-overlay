import Conroller.AppController;
import GraphView.GraphView;
import core.ServerSocket;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class App extends Application {
    TextArea logArea;

    AppController appController;

    @Override
    public void start(Stage stage)  {
        try {
            GraphView graphView = new GraphView();

            this.setupGUI(stage, graphView);

            graphView.initGraph();

            ServerSocket server = new ServerSocket();

            appController = new AppController(server.overlay, graphView);
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

        Button btn = new Button("Send message");

        btn.setOnAction(event -> openDialog(stage));

        HBox buttonBox = new HBox(10, btn);
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

    private void openDialog(Stage owner) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.initModality(Modality.APPLICATION_MODAL);
        alert.initOwner(owner);
        alert.setTitle("Send Message");
        alert.setHeaderText(null);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        TextField fromField = new TextField();
        fromField.setPromptText("From Node");
        TextField toField = new TextField();
        toField.setPromptText("To Node");
        TextField messageField = new TextField();
        messageField.setPromptText("Message");

        grid.add(new Label("From:"), 0, 0);
        grid.add(fromField, 1, 0);
        grid.add(new Label("To:"), 0, 1);
        grid.add(toField, 1, 1);
        grid.add(new Label("Message:"), 0, 2);
        grid.add(messageField, 1, 2);

        alert.getDialogPane().setContent(grid);

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                handleSendMessage(fromField.getText(), toField.getText(), messageField.getText());
            }
        });
    }

    private void handleSendMessage(String from, String to, String message) {
        System.out.println("Sending Message from " + from + " to " + to + ": " + message);
        logMessage("Sending Message from " + from + " to " + to + ": " + message);

        this.appController.sendMessage(Integer.parseInt(from), Integer.parseInt(to), message);

    }



    public static void main(String[] args) {
        launch(args);
    }
}
