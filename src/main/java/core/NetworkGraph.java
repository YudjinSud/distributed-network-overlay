package core;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class NetworkGraph {

    private HashMap<Integer, ArrayList<Integer>> adjList;

    public int size() {
        return adjList.size();
    }

    public NetworkGraph() {
        adjList = new HashMap<Integer, ArrayList<Integer>>();
    }

    public void addEdge(int node1, int node2) {
        if (adjList.get(node1) == null) {
            adjList.put(node1, new ArrayList<>());
        }

        adjList.get(node1).add(node2);
    }

    public void readGraphFromFile(String filename) {
        try(BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(":");
                int node1 = Integer.parseInt(parts[0].trim());
                String[] edges = parts[1].trim().split(" ");
                for (String edge : edges) {
                    int node2 = Integer.parseInt(edge);
                    addEdge(node1, node2);
                }
            }
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    public List<Integer> getAdjNodes(int node) {
        return adjList.get(node);
    }

    public void printGraph() {
        for(int node = 1; node <= adjList.size(); node++) {
            List<Integer> adjNodes = this.getAdjNodes(node);
            System.out.print("Node " + node + " is connected to: ");
            for(int adjNode : adjNodes) {
                System.out.print(adjNode + " ");
            }
            System.out.println();
        }
    }

    public static NetworkGraph createGraph() {
        NetworkGraph g = new NetworkGraph();
        g.readGraphFromFile("graph.txt");
        g.printGraph();

        return g;
    }

}
