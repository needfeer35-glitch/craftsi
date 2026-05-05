package net.craftsi.mod.node;

public class NodeConnection {
    private final String fromNodeId;
    private final String toNodeId;

    public NodeConnection(String fromNodeId, String toNodeId) {
        this.fromNodeId = fromNodeId;
        this.toNodeId = toNodeId;
    }

    public String getFromNodeId() { return fromNodeId; }
    public String getToNodeId() { return toNodeId; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NodeConnection nc)) return false;
        return fromNodeId.equals(nc.fromNodeId) && toNodeId.equals(nc.toNodeId);
    }

    @Override
    public int hashCode() {
        return 31 * fromNodeId.hashCode() + toNodeId.hashCode();
    }
}