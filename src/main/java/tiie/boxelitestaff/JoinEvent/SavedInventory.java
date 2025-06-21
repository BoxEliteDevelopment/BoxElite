package tiie.boxelitestaff.JoinEvent;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class SavedInventory {

    private final ItemStack[] contents;
    private final ItemStack[] armor;
    private final ItemStack[] extra;

    public SavedInventory(ItemStack[] contents, ItemStack[] armor, ItemStack[] extra) {
        this.contents = contents;
        this.armor = armor;
        this.extra = extra;
    }

    public ItemStack[] getContents() {
        return contents;
    }

    public ItemStack[] getArmor() {
        return armor;
    }

    public ItemStack[] getExtra() {
        return extra;
    }

    //Save to file
    public static void saveToFile(File file, SavedInventory inv) throws IOException {
        YamlConfiguration config = new YamlConfiguration();
        config.set("contents", Arrays.asList(inv.getContents()));
        config.set("armor", Arrays.asList(inv.getArmor()));
        config.set("extra", Arrays.asList(inv.getExtra()));
        config.save(file);
    }

    //Load from file
    public static SavedInventory loadFromFile(File file) {
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        try {
            List<ItemStack> contentsList = (List<ItemStack>) config.getList("contents");
            List<ItemStack> armorList = (List<ItemStack>) config.getList("armor");
            List<ItemStack> extraList = (List<ItemStack>) config.getList("extra");

            if (contentsList == null || armorList == null || extraList == null) {
                Bukkit.getLogger().warning("[SavedInventory] Missing section in: " + file.getName());
                return null;
            }

            ItemStack[] contents = contentsList.toArray(new ItemStack[0]);
            ItemStack[] armor = armorList.toArray(new ItemStack[0]);
            ItemStack[] extra = extraList.toArray(new ItemStack[0]);

            return new SavedInventory(contents, armor, extra);

        } catch (Exception e) {
            Bukkit.getLogger().severe("[SavedInventory] Failed to load file: " + file.getName());
            e.printStackTrace();
            return null;
        }
    }

    //File Path
    public static File getFile(File dir, UUID uuid) {
        return new File(dir, uuid.toString() + ".yml");
    }
}
