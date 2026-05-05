package net.craftsi.mod.logic;

import net.craftsi.mod.node.NodeGraph;
import net.craftsi.mod.node.RecipeNode;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.CraftingScreenHandler;

import java.util.HashMap;
import java.util.Map;

public class CraftExecutor {

    private static int tickCounter = 0;
    private static final int TICK_INTERVAL = 20;

    public static void tick(NodeGraph graph, PlayerEntity player) {
        tickCounter++;
        if (tickCounter < TICK_INTERVAL) return;
        tickCounter = 0;

        if (!(player.currentScreenHandler instanceof CraftingScreenHandler)) return;

        for (RecipeNode node : graph.getNodes()) {
            if (!node.isEnabled()) continue;
            if (node.getOutput() == Items.AIR) continue;
            if (!hasRequiredMaterials(player.getInventory(), node)) continue;
            consumeMaterials(player.getInventory(), node);
            giveOutput(player.getInventory(), node);
        }
    }

    private static boolean hasRequiredMaterials(PlayerInventory inv, RecipeNode node) {
        Map<Item, Integer> required = getRequirements(node);
        for (Map.Entry<Item, Integer> entry : required.entrySet()) {
            if (countItem(inv, entry.getKey()) < entry.getValue()) return false;
        }
        return true;
    }

    private static void consumeMaterials(PlayerInventory inv, RecipeNode node) {
        Map<Item, Integer> required = getRequirements(node);
        for (Map.Entry<Item, Integer> entry : required.entrySet()) {
            removeItems(inv, entry.getKey(), entry.getValue());
        }
    }

    private static void giveOutput(PlayerInventory inv, RecipeNode node) {
        inv.offerOrDrop(new ItemStack(node.getOutput(), node.getOutputCount()));
    }

    private static Map<Item, Integer> getRequirements(RecipeNode node) {
        Map<Item, Integer> map = new HashMap<>();
        for (Item item : node.getGrid()) {
            if (item == null || item == Items.AIR) continue;
            map.merge(item, 1, Integer::sum);
        }
        return map;
    }

    private static int countItem(PlayerInventory inv, Item item) {
        int count = 0;
        for (ItemStack stack : inv.main) {
            if (stack.getItem() == item) count += stack.getCount();
        }
        return count;
    }

    private static void removeItems(PlayerInventory inv, Item item, int amount) {
        for (ItemStack stack : inv.main) {
            if (stack.getItem() != item) continue;
            int take = Math.min(stack.getCount(), amount);
            stack.decrement(take);
            amount -= take;
            if (amount <= 0) break;
        }
    }
}