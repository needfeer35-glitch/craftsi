package net.craftsi.mod.node;

import net.minecraft.item.Item;
import net.minecraft.item.Items;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class RecipeNode {
    private final String id;
    private String name;
    private int x, y;
    private boolean enabled;
    private Item[] grid;
    private Item output;
    private int outputCount;
    private final List<String> inputNodeIds = new ArrayList<>();

    public static final int NODE_W = 120;
    public static final int NODE_H = 60;

    public RecipeNode(String name, int x, int y) {
        this.id = UUID.randomUUID().toString().substring(0, 8);
        this.name = name;
        this.x = x;
        this.y = y;
        this.enabled = true;
        this.grid = new Item[9];
        for (int i = 0; i < 9; i++) grid[i] = Items.AIR;
        this.output = Items.AIR;
        this.outputCount = 1;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getX() { return x; }
    public void setX(int x) { this.x = x; }
    public int getY() { return y; }
    public void setY(int y) { this.y = y; }
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public Item[] getGrid() { return grid; }
    public void setGridSlot(int slot, Item item) { grid[slot] = item; }
    public Item getOutput() { return output; }
    public void setOutput(Item output) { this.output = output; }
    public int getOutputCount() { return outputCount; }
    public void setOutputCount(int count) { this.outputCount = count; }
    public List<String> getInputNodeIds() { return inputNodeIds; }

    public void addInput(String nodeId) {
        if (!inputNodeIds.contains(nodeId)) inputNodeIds.add(nodeId);
    }

    public void removeInput(String nodeId) {
        inputNodeIds.remove(nodeId);
    }

    public boolean containsPoint(int px, int py) {
        return px >= x && px <= x + NODE_W && py >= y && py <= y + NODE_H;
    }

    public int getOutputX() { return x + NODE_W; }
    public int getOutputY() { return y + NODE_H / 2; }
    public int getInputX() { return x; }
    public int getInputY() { return y + NODE_H / 2; }
}