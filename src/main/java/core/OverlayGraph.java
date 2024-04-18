package core;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Function;

public class OverlayGraph {
    private HashMap<Integer, ArrayList<Integer>> adjList;

    private ArrayList<Integer> nodeList;

    public int size() {
        return adjList.size();
    }

    public Function<Integer, Integer> onAddNodeCallback;

    public void setOnAddNodeCallback(Function<Integer, Integer> onAddNodeCallback) {
        this.onAddNodeCallback = onAddNodeCallback;

        System.out.println("Callback set");
    }

    public OverlayGraph() {

        this.adjList = new HashMap<Integer, ArrayList<Integer>>();
        this.nodeList = new ArrayList<Integer>();
    }

    public void buildGraph() {
        int numNodes = nodeList.size();

        for (int i = 0; i < numNodes; i++) {
            int currentNode = nodeList.get(i);
            int nextNodeIndex = (i + 1) % numNodes; // Get the index of the next node, wrapping around to 0 if we reach the end
            int nextNode = nodeList.get(nextNodeIndex);

            int prevNodeIndex = (i - 1 + numNodes) % numNodes;
            int prevNode = nodeList.get(prevNodeIndex);

            ArrayList<Integer> connections = new ArrayList<>();
            if (nextNode != prevNode) {
                connections.add(prevNode);
            }
            connections.add(currentNode);
            connections.add(nextNode);
            this.adjList.put(currentNode, connections);
        }
    }

    public void printNodeList() {
        for (Integer node : nodeList) {
            System.out.println(node);
        }
        System.out.println();
    }

    public void addNode(int nodeNb) {
        if (!nodeList.contains(nodeNb)) {
            nodeList.add(nodeNb);

//            this.onAddNodeCallback.apply(nodeNb);

            if (nodeList.size() > 1) {
                this.buildGraph();
            }
        } else {
            System.out.println("Node: '" + nodeNb + "' already in the scope, just propagating the message");
        }
    }

    public HashMap<Integer, ArrayList<Integer>> getAdjList() {
        return adjList;
    }

    public List<Integer> getAdjNodes(int node) {
        return adjList.get(node);
    }

    public void printGraph() {
        if (nodeList.size() <= 1) {
            return;
        }
        System.out.println("adjList:" + adjList);
        for (int index = 0; index <= this.adjList.size() - 1; index++) {
            int node = nodeList.get(index);
            List<Integer> adjNodes = this.getAdjNodes(node);
            System.out.print("Node " + node + " is connected to: ");
            for (int adjNode : adjNodes) {
                System.out.print(adjNode + " ");
            }
            System.out.println();
        }

    }

}
