package org.lorewriter.lorewriter.log;

public class LogEntry {
    private final String player;
    private final String action;
    private final String world;
    private final int x, y, z;
    private final String blockType;
    private final long timestamp;
    private int quantity = 0;

    public LogEntry(String player, String action, String world, int x, int y, int z, String blockType, long timestamp) {
        this.player = player;
        this.action = action;
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
        this.blockType = blockType; 
        this.timestamp = timestamp;
    }

    public LogEntry(String player, String action, String world, int x, int y, int z, String blockType, long timestamp, int quantity) {
        this.player = player;
        this.action = action;
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
        this.blockType = blockType; 
        this.timestamp = timestamp;
        this.quantity = quantity; 
    }

    public String getPlayer() {
        return player;
    }

    public String getAction() {
        return action;
    }

    public String getWorld() {
        return world;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getZ() {
        return z;
    }

    public String getBlockType() { 
        return blockType;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public int getQuantity() {
        return quantity;
    }
}
