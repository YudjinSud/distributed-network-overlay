package core;

import node.ENodeColor;
import node.Node;
import node.NodeColor;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
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

    public ArrayList<NodeColor> nodeColors = new ArrayList<>();

    public ArrayList<Node> networkObjects = new ArrayList<>();

    public int size() {
        return adjList.size();
    }

    private PropertyChangeSupport pcs = new PropertyChangeSupport(this);

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

    public void addNode(int nodeNb, Node node) {
        if (!nodeList.contains(nodeNb)) {
            nodeList.add(nodeNb);

            this.networkObjects.add(node);

            if (nodeList.size() > 1) {
                this.buildGraph();
            }

            pcs.firePropertyChange("nodeList", null, nodeList);


        } else {
            System.out.println("Node: '" + nodeNb + "' already in the scope, just propagating the message");
        }
    }

    public void processNodeColor(String input) {
        String[] parts = input.split(":");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid input format");
        }

        int id = Integer.parseInt(parts[0]);
        ENodeColor color = ENodeColor.fromString(parts[1]);

        NodeColor nodeColor = new NodeColor(id, color);
        nodeColors.add(nodeColor);

        pcs.firePropertyChange("nodeColors", null, nodeColors);

        if (color.getValue() == ENodeColor.RECEIVED.getValue()) {
            nodeColors.clear();


            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            pcs.firePropertyChange("nodeColors", null, nodeColors);

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

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(listener);
    }
}
