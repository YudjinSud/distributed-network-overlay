import Conroller.AppController;
import GUI.GUI;
import core.NetworkGraph;
import core.ServerSocket;

public class App {
    public static void main(String[] args) {


        try {

//        GUI gui = new GUI();

//        gui.main(args);

            ServerSocket server = new ServerSocket();
            server.main(args);

            AppController appController = new AppController(server.overlay);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }
}
