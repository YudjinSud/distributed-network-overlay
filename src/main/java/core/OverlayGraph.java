package core;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class OverlayGraph {
    private HashMap<Integer, ArrayList<Integer>> adjList;

    private ArrayList<Integer> nodeList;

    public int size() {
        return adjList.size();
    }

    public OverlayGraph() {

        this.adjList = new HashMap<Integer, ArrayList<Integer>>();
        this.nodeList = new ArrayList<Integer>();
    }

    public void buildGraph(){
        int numNodes = nodeList.size();

        for (int i = 0; i < numNodes; i++) {
            int currentNode = nodeList.get(i);
            int nextNodeIndex = (i + 1) % numNodes; // Get the index of the next node, wrapping around to 0 if we reach the end
            int nextNode = nodeList.get(nextNodeIndex);

            // get connections for currentNode if not create a new ArrayList
            //ArrayList<Integer> connections = adjList.getOrDefault(currentNode, new ArrayList<>());
            //
            ArrayList<Integer> connections = new ArrayList<>();
            connections.add(currentNode);
            connections.add(nextNode);
            adjList.put(currentNode, connections);
        }

    }

    public void printNodeList(){
        for (Integer node : nodeList){
            System.out.println(node);
        }
        System.out.println();
    }
    public void addNode(int nodeNb) {
        if (!nodeList.contains(nodeNb)) {
            nodeList.add(nodeNb);
            if (nodeList.size() > 1 ){
                this.buildGraph();
            }
        }
    }
    public HashMap<Integer, ArrayList<Integer>> getAdjList() {
        return adjList;
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

}