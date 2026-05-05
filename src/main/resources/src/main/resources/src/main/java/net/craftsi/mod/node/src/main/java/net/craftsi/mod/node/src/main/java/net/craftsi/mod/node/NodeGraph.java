package net.craftsi.mod.node;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class NodeGraph {
    private static final NodeGraph INSTANCE = new NodeGraph();
    private final List<RecipeNode> nodes = new ArrayList<>();
    private final List<NodeConnection> connections = new ArrayList<>();

    private NodeGraph() {}

    public static NodeGraph getInstance() { return INSTANCE; }

    public List<RecipeNode> getNodes() { return nodes; }
    public List<NodeConnection> getConnections() { return connections; }

    public void addNode(RecipeNode node) { nodes.add(node); }

    public void removeNode(RecipeNode node) {
        nodes.remove(node);
        connections.removeIf(c ->
            c.getFromNodeId().equals(node.getId()) ||
            c.getToNodeId().equals(node.getId())
        );
    }

    public void addConnection(String fromId, String toId) {
        NodeConnection conn = new NodeConnection(fromId, toId);
        if (!connections.contains(conn)) connections.add(conn);
    }

    public void removeConnection(NodeConnection conn) { connections.remove(conn); }

    public Optional<RecipeNode> findById(String id) {
        return nodes.stream().filter(n -> n.getId().equals(id)).findFirst();
    }
}