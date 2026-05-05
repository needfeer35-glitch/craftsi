package net.craftsi.mod.gui;

import net.craftsi.mod.node.NodeConnection;
import net.craftsi.mod.node.NodeGraph;
import net.craftsi.mod.node.RecipeNode;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class CraftsiScreen extends Screen {

    private static final int COL_BG          = 0xFF1A1A1A;
    private static final int COL_GRID        = 0xFF262626;
    private static final int COL_NODE_BG     = 0xFF2D2D2D;
    private static final int COL_NODE_BORDER = 0xFF555555;
    private static final int COL_NODE_SEL    = 0xFF5B9BD5;
    private static final int COL_NODE_DIS    = 0xFF4A3030;
    private static final int COL_HEADER      = 0xFF383838;
    private static final int COL_WHITE       = 0xFFFFFFFF;
    private static final int COL_GRAY        = 0xFF888888;
    private static final int COL_GREEN       = 0xFF4CAF50;
    private static final int COL_RED         = 0xFFE53935;
    private static final int COL_YELLOW      = 0xFFFFD600;
    private static final int COL_CONN_OUT    = 0xFF4FC3F7;
    private static final int COL_CONN_IN     = 0xFFA5D6A7;
    private static final int COL_BEZIER      = 0xFF78909C;
    private static final int COL_TOOLBAR     = 0xFF232323;
    private static final int COL_BTN_BG      = 0xFF3C3C3C;
    private static final int COL_BTN_HVR     = 0xFF4A4A4A;
    private static final int COL_BTN_BORDER  = 0xFF606060;
    private static final int COL_SEPARATOR   = 0xFF404040;
    private static final int COL_ITEM_BG     = 0xFF1E1E1E;
    private static final int COL_ITEM_SEL    = 0xFF0D47A1;

    private static final int TOOLBAR_H   = 24;
    private static final int NODE_W      = RecipeNode.NODE_W;
    private static final int NODE_H      = RecipeNode.NODE_H;
    private static final int CONNECTOR_R = 4;

    private final NodeGraph graph = NodeGraph.getInstance();

    private RecipeNode draggingNode = null;
    private int dragOffsetX, dragOffsetY;

    private RecipeNode connectingFrom = null;
    private int mouseX, mouseY;

    private RecipeNode selectedNode = null;

    private boolean showContextMenu = false;
    private int contextMenuX, contextMenuY;
    private RecipeNode contextNode = null;

    private boolean showRecipeEditor = false;
    private RecipeNode editingNode = null;
    private int recipeEditorX, recipeEditorY;
    private static final int RE_W = 220;
    private static final int RE_H = 180;
    private int selectedGridSlot = -1;

    private boolean showItemPicker = false;
    private int pickerX, pickerY;
    private static final int PICKER_W    = 200;
    private static final int PICKER_H    = 160;
    private static final int PICKER_COLS = 8;
    private static final int PICKER_ROWS = 6;
    private static final int PICKER_SLOT = 18;
    private final List<Item> allItems = new ArrayList<>();
    private int pickerScroll = 0;
    private boolean pickingForOutput = false;

    private int offsetX = 0, offsetY = 0;
    private boolean panning = false;
    private int panStartX, panStartY, panOffsetStartX, panOffsetStartY;

    public CraftsiScreen() {
        super(Text.literal("Craftsi — Node Editor"));
        Registries.ITEM.stream()
            .filter(i -> i != Items.AIR)
            .forEach(allItems::add);
    }

    @Override
    protected void init() {
        addDrawableChild(ButtonWidget.builder(
            Text.literal("+ Add Node"), btn -> addNewNode())
            .dimensions(4, 2, 72, 20)
            .build());
    }

    @Override
    public void render(DrawContext ctx, int mx, int my, float delta) {
        mouseX = mx; mouseY = my;
        ctx.fill(0, 0, width, height, COL_BG);
        drawGrid(ctx);
        drawConnections(ctx);
        if (connectingFrom != null) {
            drawBezier(ctx,
                connectingFrom.getOutputX() + offsetX,
                connectingFrom.getOutputY() + offsetY,
                mx, my, COL_YELLOW);
        }
        for (RecipeNode node : graph.getNodes()) drawNode(ctx, node, mx, my);
        ctx.fill(0, 0, width, TOOLBAR_H, COL_TOOLBAR);
        ctx.fill(0, TOOLBAR_H - 1, width, TOOLBAR_H, COL_SEPARATOR);
        ctx.drawText(textRenderer, "Nodes: " + graph.getNodes().size(), 84, 8, COL_GRAY, false);
        ctx.drawText(textRenderer, "Conn: " + graph.getConnections().size(), 160, 8, COL_GRAY, false);
        super.render(ctx, mx, my, delta);
        if (showContextMenu && contextNode != null) drawContextMenu(ctx, mx, my);
        if (showRecipeEditor && editingNode != null) drawRecipeEditor(ctx, mx, my);
        if (showItemPicker) drawItemPicker(ctx, mx, my);
    }

    private void drawGrid(DrawContext ctx) {
        int s = 24;
        for (int x = offsetX % s; x < width; x += s)
            for (int y = (offsetY + TOOLBAR_H) % s + TOOLBAR_H; y < height; y += s)
                ctx.fill(x, y, x + 1, y + 1, COL_GRID);
    }

    private void drawNode(DrawContext ctx, RecipeNode node, int mx, int my) {
        int nx = node.getX() + offsetX;
        int ny = node.getY() + offsetY;
        if (ny + NODE_H < TOOLBAR_H) return;
        boolean sel = node == selectedNode;
        ctx.fill(nx + 2, ny + 2, nx + NODE_W + 2, ny + NODE_H + 2, 0x44000000);
        ctx.fill(nx, ny, nx + NODE_W, ny + NODE_H, node.isEnabled() ? COL_NODE_BG : COL_NODE_DIS);
        ctx.fill(nx, ny, nx + NODE_W, ny + 14, node.isEnabled() ? COL_HEADER : 0xFF3A2020);
        drawBorder(ctx, nx, ny, NODE_W, NODE_H, sel ? COL_NODE_SEL : COL_NODE_BORDER);
        String name = textRenderer.getWidth(node.getName()) > NODE_W - 20
            ? textRenderer.trimToWidth(node.getName(), NODE_W - 24) + "…"
            : node.getName();
        ctx.drawText(textRenderer, name, nx + 4, ny + 3, COL_WHITE, false);
        ctx.fill(nx + NODE_W - 10, ny + 4, nx + NODE_W - 4, ny + 10,
            node.isEnabled() ? COL_GREEN : COL_RED);
        if (node.getOutput() != null && node.getOutput() != Items.AIR) {
            ctx.fill(nx + NODE_W/2 - 9, ny + 20, nx + NODE_W/2 + 9, ny + 38, COL_ITEM_BG);
            drawBorder(ctx, nx + NODE_W/2 - 9, ny + 20, 18, 18, 0xFF505050);
            ctx.drawItem(new net.minecraft.item.ItemStack(node.getOutput()), nx + NODE_W/2 - 8, ny + 21);
            ctx.drawText(textRenderer, "x" + node.getOutputCount(), nx + NODE_W/2 + 10, ny + 26, COL_GRAY, false);
        } else {
            ctx.drawText(textRenderer, "No output", nx + 4, ny + 24, COL_GRAY, false);
        }
        long ing = 0;
        for (Item i : node.getGrid()) if (i != null && i != Items.AIR) ing++;
        if (ing > 0) ctx.drawText(textRenderer, ing + " ing.", nx + 4, ny + NODE_H - 12, COL_GRAY, false);
        String status = node.isEnabled() ? "ON" : "OFF";
        ctx.drawText(textRenderer, status,
            nx + NODE_W - textRenderer.getWidth(status) - 4, ny + NODE_H - 12,
            node.isEnabled() ? COL_GREEN : COL_RED, false);
        drawConnector(ctx, nx, ny + NODE_H / 2, COL_CONN_IN, mx, my);
        drawConnector(ctx, nx + NODE_W, ny + NODE_H / 2, COL_CONN_OUT, mx, my);
    }

    private void drawConnector(DrawContext ctx, int cx, int cy, int color, int mx, int my) {
        int r = CONNECTOR_R;
        boolean hov = Math.abs(mx - cx) <= r + 2 && Math.abs(my - cy) <= r + 2;
        ctx.fill(cx - r, cy - r, cx + r, cy + r, hov ? COL_WHITE : color);
        ctx.fill(cx - r + 1, cy - r + 1, cx + r - 1, cy + r - 1, hov ? color : COL_NODE_BG);
    }

    private void drawConnections(DrawContext ctx) {
        for (NodeConnection conn : graph.getConnections()) {
            RecipeNode from = graph.findById(conn.getFromNodeId()).orElse(null);
            RecipeNode to   = graph.findById(conn.getToNodeId()).orElse(null);
            if (from == null || to == null) continue;
            drawBezier(ctx,
                from.getOutputX() + offsetX, from.getOutputY() + offsetY,
                to.getInputX() + offsetX,   to.getInputY() + offsetY,
                COL_BEZIER);
        }
    }

    private void drawBezier(DrawContext ctx, int x1, int y1, int x2, int y2, int color) {
        int d = Math.abs(x2 - x1) / 2;
        int cp1x = x1 + d, cp2x = x2 - d;
        int px = x1, py = y1;
        for (int i = 1; i <= 20; i++) {
            float t = i / 20f, it = 1 - t;
            int nx = (int)(it*it*it*x1 + 3*it*it*t*cp1x + 3*it*t*t*cp2x + t*t*t*x2);
            int ny = (int)(it*it*it*y1 + 3*it*it*t*y1  + 3*it*t*t*y2  + t*t*t*y2);
            drawLine(ctx, px, py, nx, ny, color);
            px = nx; py = ny;
        }
    }

    private void drawLine(DrawContext ctx, int x1, int y1, int x2, int y2, int color) {
        int dx = Math.abs(x2-x1), dy = Math.abs(y2-y1);
        int sx = x1<x2?1:-1, sy = y1<y2?1:-1, err = dx-dy;
        while (true) {
            ctx.fill(x1, y1, x1+2, y1+2, color);
            if (x1==x2 && y1==y2) break;
            int e2 = 2*err;
            if (e2>-dy){err-=dy;x1+=sx;}
            if (e2< dx){err+=dx;y1+=sy;}
        }
    }

    private static final int CM_W      = 130;
    private static final int CM_ITEM_H = 16;
    private static final String[] CM_OPTIONS = {"Toggle Enable","Edit Recipe","Delete Node"};

    private void drawContextMenu(DrawContext ctx, int mx, int my) {
        int cmH = CM_OPTIONS.length * CM_ITEM_H + 4;
        ctx.fill(contextMenuX, contextMenuY, contextMenuX+CM_W, contextMenuY+cmH, COL_NODE_BG);
        drawBorder(ctx, contextMenuX, contextMenuY, CM_W, cmH, COL_BTN_BORDER);
        for (int i = 0; i < CM_OPTIONS.length; i++) {
            int iy = contextMenuY + 2 + i * CM_ITEM_H;
            boolean hov = mx>=contextMenuX && mx<=contextMenuX+CM_W && my>=iy && my<=iy+CM_ITEM_H;
            if (hov) ctx.fill(contextMenuX+1, iy, contextMenuX+CM_W-1, iy+CM_ITEM_H, COL_BTN_HVR);
            ctx.drawText(textRenderer, CM_OPTIONS[i], contextMenuX+6, iy+4, COL_WHITE, false);
        }
    }

    private void handleContextMenuClick(int mx, int my) {
        int cmH = CM_OPTIONS.length * CM_ITEM_H + 4;
        if (mx<contextMenuX||mx>contextMenuX+CM_W||my<contextMenuY||my>contextMenuY+cmH) {
            showContextMenu = false; return;
        }
        int idx = (my - contextMenuY - 2) / CM_ITEM_H;
        if (idx < 0 || idx >= CM_OPTIONS.length) { showContextMenu = false; return; }
        switch (idx) {
            case 0 -> contextNode.setEnabled(!contextNode.isEnabled());
            case 1 -> openRecipeEditor(contextNode);
            case 2 -> { graph.removeNode(contextNode); selectedNode = null; }
        }
        showContextMenu = false;
    }

    private void openRecipeEditor(RecipeNode node) {
        editingNode = node;
        showRecipeEditor = true;
        recipeEditorX = width/2 - RE_W/2;
        recipeEditorY = height/2 - RE_H/2;
        selectedGridSlot = -1;
    }

    private void drawRecipeEditor(DrawContext ctx, int mx, int my) {
        ctx.fill(recipeEditorX, recipeEditorY, recipeEditorX+RE_W, recipeEditorY+RE_H, COL_NODE_BG);
        drawBorder(ctx, recipeEditorX, recipeEditorY, RE_W, RE_H, COL_NODE_SEL);
        ctx.fill(recipeEditorX, recipeEditorY, recipeEditorX+RE_W, recipeEditorY+14, COL_HEADER);
        ctx.drawText(textRenderer, "Recipe: "+editingNode.getName(), recipeEditorX+4, recipeEditorY+3, COL_WHITE, false);
        int cx = recipeEditorX+RE_W-14, cy = recipeEditorY+1;
        boolean closeHov = mx>=cx&&mx<=cx+12&&my>=cy&&my<=cy+12;
        ctx.fill(cx, cy, cx+12, cy+12, closeHov?COL_RED:COL_BTN_BG);
        ctx.drawText(textRenderer, "X", cx+3, cy+2, COL_WHITE, false);
        int gx = recipeEditorX+8, gy = recipeEditorY+20;
        for (int row=0;row<3;row++) for (int col=0;col<3;col++) {
            int slot=row*3+col, sx=gx+col*20, sy=gy+row*20;
            boolean ssel=slot==selectedGridSlot, shov=mx>=sx&&mx<=sx+18&&my>=sy&&my<=sy+18;
            ctx.fill(sx,sy,sx+18,sy+18,COL_ITEM_BG);
            drawBorder(ctx,sx,sy,18,18,ssel?COL_NODE_SEL:shov?COL_BTN_BORDER:0xFF404040);
            Item item=editingNode.getGrid()[slot];
            if(item!=null&&item!=Items.AIR) ctx.drawItem(new net.minecraft.item.ItemStack(item),sx+1,sy+1);
        }
        ctx.drawText(textRenderer,"→",gx+66,gy+20,COL_GRAY,false);
        int ox=gx+82,oy=gy+14;
        boolean ohov=mx>=ox&&mx<=ox+18&&my>=oy&&my<=oy+18;
        ctx.fill(ox,oy,ox+18,oy+18,COL_ITEM_BG);
        drawBorder(ctx,ox,oy,18,18,ohov?COL_NODE_SEL:0xFF505050);
        if(editingNode.getOutput()!=Items.AIR)
            ctx.drawItem(new net.minecraft.item.ItemStack(editingNode.getOutput()),ox+1,oy+1);
        ctx.drawText(textRenderer,"Out",ox,oy+20,COL_GRAY,false);
        int cy2=gy+82, bx=recipeEditorX+52;
        ctx.drawText(textRenderer,"Count:",recipeEditorX+8,cy2,COL_GRAY,false);
        boolean mhov=mx>=bx&&mx<=bx+14&&my>=cy2-2&&my<=cy2+10;
        ctx.fill(bx,cy2-2,bx+14,cy2+10,mhov?COL_BTN_HVR:COL_BTN_BG);
        drawBorder(ctx,bx,cy2-2,14,12,COL_BTN_BORDER);
        ctx.drawText(textRenderer,"-",bx+4,cy2,COL_WHITE,false);
        ctx.drawText(textRenderer,""+editingNode.getOutputCount(),bx+18,cy2,COL_WHITE,false);
        int bx2=bx+30;
        boolean phov=mx>=bx2&&mx<=bx2+14&&my>=cy2-2&&my<=cy2+10;
        ctx.fill(bx2,cy2-2,bx2+14,cy2+10,phov?COL_BTN_HVR:COL_BTN_BG);
        drawBorder(ctx,bx2,cy2-2,14,12,COL_BTN_BORDER);
        ctx.drawText(textRenderer,"+",bx2+3,cy2,COL_WHITE,false);
        int svx=recipeEditorX+RE_W-52, svy=recipeEditorY+RE_H-18;
        boolean svhov=mx>=svx&&mx<=svx+44&&my>=svy&&my<=svy+14;
        ctx.fill(svx,svy,svx+44,svy+14,svhov?COL_GREEN:COL_BTN_BG);
        drawBorder(ctx,svx,svy,44,14,COL_BTN_BORDER);
        ctx.drawText(textRenderer,"Save",svx+10,svy+3,COL_WHITE,false);
    }

    private boolean handleRecipeEditorClick(int mx, int my, int button) {
        if(!showRecipeEditor) return false;
        int cx=recipeEditorX+RE_W-14, cy=recipeEditorY+1;
        if(mx>=cx&&mx<=cx+12&&my>=cy&&my<=cy+12){showRecipeEditor=false;return true;}
        int svx=recipeEditorX+RE_W-52,svy=recipeEditorY+RE_H-18;
        if(mx>=svx&&mx<=svx+44&&my>=svy&&my<=svy+14){showRecipeEditor=false;return true;}
        int gx=recipeEditorX+8,gy=recipeEditorY+20;
        for(int row=0;row<3;row++) for(int col=0;col<3;col++){
            int slot=row*3+col,sx=gx+col*20,sy=gy+row*20;
            if(mx>=sx&&mx<=sx+18&&my>=sy&&my<=sy+18){
                if(button==GLFW.GLFW_MOUSE_BUTTON_RIGHT) editingNode.setGridSlot(slot,Items.AIR);
                else{selectedGridSlot=slot;pickingForOutput=false;openItemPicker(sx,sy+20);}
                return true;
            }
        }
        int ox=gx+82,oy=gy+14;
        if(mx>=ox&&mx<=ox+18&&my>=oy&&my<=oy+18){
            if(button==GLFW.GLFW_MOUSE_BUTTON_RIGHT) editingNode.setOutput(Items.AIR);
            else{pickingForOutput=true;selectedGridSlot=-1;openItemPicker(ox,oy+20);}
            return true;
        }
        int cy2=gy+82,bx=recipeEditorX+52;
        if(mx>=bx&&mx<=bx+14&&my>=cy2-2&&my<=cy2+10){editingNode.setOutputCount(Math.max(1,editingNode.getOutputCount()-1));return true;}
        int bx2=bx+30;
        if(mx>=bx2&&mx<=bx2+14&&my>=cy2-2&&my<=cy2+10){editingNode.setOutputCount(Math.min(64,editingNode.getOutputCount()+1));return true;}
        if(mx>=recipeEditorX&&mx<=recipeEditorX+RE_W&&my>=recipeEditorY&&my<=recipeEditorY+RE_H) return true;
        return false;
    }

    private void openItemPicker(int x, int y) {
        showItemPicker=true;
        pickerX=Math.min(x,width-PICKER_W-4);
        pickerY=Math.min(y,height-PICKER_H-4);
        pickerScroll=0;
    }

    private void drawItemPicker(DrawContext ctx, int mx, int my) {
        ctx.fill(pickerX,pickerY,pickerX+PICKER_W,pickerY+PICKER_H,COL_NODE_BG);
        drawBorder(ctx,pickerX,pickerY,PICKER_W,PICKER_H,COL_NODE_SEL);
        ctx.drawText(textRenderer,"Select Item",pickerX+4,pickerY+3,COL_WHITE,false);
        ctx.fill(pickerX,pickerY+12,pickerX+PICKER_W,pickerY+13,COL_SEPARATOR);
        int si=pickerScroll*PICKER_COLS,cx2=pickerX+2,cy2=pickerY+14;
        for(int row=0;row<PICKER_ROWS;row++) for(int col=0;col<PICKER_COLS;col++){
            int idx=si+row*PICKER_COLS+col;
            if(idx>=allItems.size()) break;
            int sx=cx2+col*PICKER_SLOT,sy=cy2+row*PICKER_SLOT;
            if(mx>=sx&&mx<=sx+16&&my>=sy&&my<=sy+16) ctx.fill(sx,sy,sx+16,sy+16,COL_ITEM_SEL);
            ctx.drawItem(new net.minecraft.item.ItemStack(allItems.get(idx)),sx,sy);
        }
    }

    private boolean handleItemPickerClick(int mx, int my) {
        if(!showItemPicker) return false;
        if(mx<pickerX||mx>pickerX+PICKER_W||my<pickerY||my>pickerY+PICKER_H){showItemPicker=false;return false;}
        int si=pickerScroll*PICKER_COLS,cx2=pickerX+2,cy2=pickerY+14;
        for(int row=0;row<PICKER_ROWS;row++) for(int col=0;col<PICKER_COLS;col++){
            int idx=si+row*PICKER_COLS+col;
            if(idx>=allItems.size()) break;
            int sx=cx2+col*PICKER_SLOT,sy=cy2+row*PICKER_SLOT;
            if(mx>=sx&&mx<=sx+16&&my>=sy&&my<=sy+16){
                if(pickingForOutput) editingNode.setOutput(allItems.get(idx));
                else if(selectedGridSlot>=0){editingNode.setGridSlot(selectedGridSlot,allItems.get(idx));selectedGridSlot=-1;}
                showItemPicker=false;return true;
            }
        }
        return true;
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        int x=(int)mx,y=(int)my;
        if(showItemPicker){handleItemPickerClick(x,y);return true;}
        if(showContextMenu){handleContextMenuClick(x,y);return true;}
        if(showRecipeEditor){if(handleRecipeEditorClick(x,y,button))return true;showRecipeEditor=false;return true;}
        if(y<TOOLBAR_H) return super.mouseClicked(mx,my,button);
        if(button==GLFW.GLFW_MOUSE_BUTTON_RIGHT){
            RecipeNode node=getNodeAt(x-offsetX,y-offsetY);
            if(node!=null){contextNode=node;contextMenuX=x;contextMenuY=y;showContextMenu=true;return true;}
        }
        if(button==GLFW.GLFW_MOUSE_BUTTON_LEFT){
            RecipeNode cn=getNodeNearOutputConnector(x-offsetX,y-offsetY);
            if(cn!=null){connectingFrom=cn;return true;}
            RecipeNode node=getNodeAt(x-offsetX,y-offsetY);
            if(node!=null){selectedNode=node;draggingNode=node;dragOffsetX=x-offsetX-node.getX();dragOffsetY=y-offsetY-node.getY();return true;}
            selectedNode=null;panning=true;panStartX=x;panStartY=y;panOffsetStartX=offsetX;panOffsetStartY=offsetY;
        }
        return super.mouseClicked(mx,my,button);
    }

    @Override
    public boolean mouseReleased(double mx, double my, int button) {
        int x=(int)mx,y=(int)my;
        if(connectingFrom!=null){
            RecipeNode t=getNodeNearInputConnector(x-offsetX,y-offsetY);
            if(t!=null&&t!=connectingFrom) graph.addConnection(connectingFrom.getId(),t.getId());
            connectingFrom=null;
        }
        draggingNode=null;panning=false;
        return super.mouseReleased(mx,my,button);
    }

    @Override
    public boolean mouseDragged(double mx, double my, int button, double dx, double dy) {
        int x=(int)mx,y=(int)my;
        mouseX=x;mouseY=y;
        if(draggingNode!=null){draggingNode.setX(x-offsetX-dragOffsetX);draggingNode.setY(Math.max(TOOLBAR_H+2,y-offsetY-dragOffsetY));return true;}
        if(panning){offsetX=panOffsetStartX+(x-panStartX);offsetY=panOffsetStartY+(y-panStartY);return true;}
        return super.mouseDragged(mx,my,button,dx,dy);
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double hAmount, double vAmount) {
        if(showItemPicker){
            int totalRows=(allItems.size()+PICKER_COLS-1)/PICKER_COLS;
            pickerScroll=Math.max(0,Math.min(totalRows-PICKER_ROWS,pickerScroll-(int)vAmount));
            return true;
        }
        return super.mouseScrolled(mx,my,hAmount,vAmount);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if(keyCode==GLFW.GLFW_KEY_ESCAPE){
            if(showItemPicker){showItemPicker=false;return true;}
            if(showRecipeEditor){showRecipeEditor=false;return true;}
            if(showContextMenu){showContextMenu=false;return true;}
        }
        if(keyCode==GLFW.GLFW_KEY_DELETE&&selectedNode!=null&&!showRecipeEditor){
            graph.removeNode(selectedNode);selectedNode=null;return true;
        }
        return super.keyPressed(keyCode,scanCode,modifiers);
    }

    private void addNewNode() {
        RecipeNode node=new RecipeNode("Node "+(graph.getNodes().size()+1),
            width/2-offsetX-NODE_W/2, height/2-offsetY-NODE_H/2);
        graph.addNode(node);selectedNode=node;
    }

    private RecipeNode getNodeAt(int cx,int cy){
        List<RecipeNode> nodes=graph.getNodes();
        for(int i=nodes.size()-1;i>=0;i--) if(nodes.get(i).containsPoint(cx,cy)) return nodes.get(i);
        return null;
    }

    private RecipeNode getNodeNearOutputConnector(int cx,int cy){
        for(RecipeNode n:graph.getNodes()) if(Math.abs(cx-n.getOutputX())+Math.abs(cy-n.getOutputY())<=8) return n;
        return null;
    }

    private RecipeNode getNodeNearInputConnector(int cx,int cy){
        for(RecipeNode n:graph.getNodes()) if(Math.abs(cx-n.getInputX())+Math.abs(cy-n.getInputY())<=8) return n;
        return null;
    }

    private void drawBorder(DrawContext ctx,int x,int y,int w,int h,int color){
        ctx.fill(x,y,x+w,y+1,color);
        ctx.fill(x,y+h-1,x+w,y+h,color);
        ctx.fill(x,y,x+1,y+h,color);
        ctx.fill(x+w-1,y,x+w,y+h,color);
    }

    @Override public boolean shouldPause(){return false;}

    @Override
    public boolean shouldCloseOnEsc(){
        return !showItemPicker&&!showRecipeEditor&&!showContextMenu;
    }
}