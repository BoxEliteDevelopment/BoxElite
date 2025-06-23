package tiie.boxelitestaff.spleef.Arena;

import org.bukkit.Location;

public class Region {

    private final Location pos1;
    private final Location pos2;

    public Region(Location pos1, Location pos2) {
        this.pos1 = pos1;
        this.pos2 = pos2;
    }

    public boolean contains(Location loc) {
        int x = loc.getBlockX();
        int y = loc.getBlockY();
        int z = loc.getBlockZ();

        return x >= getMinX() && x <= getMaxX()
                && y >= getMinY() && y <= getMaxY()
                && z >= getMinZ() && z <= getMaxZ();
    }

    public boolean isInside(Location loc) {
        if (!loc.getWorld().equals(pos1.getWorld())) return false;

        double x = loc.getX(), y = loc.getY(), z = loc.getZ();

        return x >= getMinX() && x <= getMaxX()
                && y >= getMinY() && y <= getMaxY()
                && z >= getMinZ() && z <= getMaxZ();
    }

    // === New utility methods for easy iteration ===
    public int getMinX() { return Math.min(pos1.getBlockX(), pos2.getBlockX()); }
    public int getMaxX() { return Math.max(pos1.getBlockX(), pos2.getBlockX()); }

    public int getMinY() { return Math.min(pos1.getBlockY(), pos2.getBlockY()); }
    public int getMaxY() { return Math.max(pos1.getBlockY(), pos2.getBlockY()); }

    public int getMinZ() { return Math.min(pos1.getBlockZ(), pos2.getBlockZ()); }
    public int getMaxZ() { return Math.max(pos1.getBlockZ(), pos2.getBlockZ()); }

    // Optional: expose positions if needed
    public Location getPos1() { return pos1; }
    public Location getPos2() { return pos2; }

}
