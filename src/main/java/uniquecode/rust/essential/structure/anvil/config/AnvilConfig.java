package uniquecode.rust.essential.structure.anvil.config;

import lombok.Getter;
import org.bukkit.Material;
import uniquecode.rust.util.MapUtil;

import java.util.Map;
@Getter
public enum AnvilConfig {
    FISHING_ROD(MapUtil.of(
            Material.FISHING_ROD, Material.STRING
    )),
    WEAPON(MapUtil.of(
            Material.WOODEN_SWORD, Material.OAK_WOOD,
            Material.STONE_SWORD, Material.COBBLESTONE,
            Material.IRON_SWORD, Material.IRON_INGOT,
            Material.GOLDEN_SWORD, Material.GOLD_INGOT,
            Material.DIAMOND_SWORD, Material.DIAMOND,
            Material.NETHERITE_SWORD, Material.NETHERITE_INGOT
    )),
    TOOL(MapUtil.of(
            Material.WOODEN_PICKAXE, Material.WOODEN_PICKAXE,
            Material.STONE_PICKAXE, Material.COBBLESTONE,
            Material.IRON_PICKAXE, Material.IRON_INGOT,
            Material.GOLDEN_PICKAXE, Material.GOLD_INGOT,
            Material.DIAMOND_PICKAXE, Material.DIAMOND,
            Material.NETHERITE_PICKAXE, Material.NETHERITE_INGOT,
            Material.WOODEN_AXE, Material.OAK_WOOD,
            Material.STONE_AXE, Material.COBBLESTONE,
            Material.IRON_AXE, Material.IRON_INGOT,
            Material.GOLDEN_AXE, Material.GOLD_INGOT,
            Material.DIAMOND_AXE, Material.DIAMOND,
            Material.NETHERITE_AXE, Material.NETHERITE_INGOT
    )),
    BOW(MapUtil.of(
            Material.BOW, Material.STRING
    )),
    CROSSBOW(MapUtil.of(
            Material.CROSSBOW, Material.STRING
    )),
    TRIDENT(MapUtil.of(
            Material.TRIDENT, Material.NETHERITE_INGOT
    )),
    ARMOR(MapUtil.of(
            Material.LEATHER_HELMET, Material.OAK_LOG,
            Material.CHAINMAIL_HELMET, Material.COBBLESTONE,
            Material.IRON_HELMET, Material.IRON_INGOT,
            Material.GOLDEN_HELMET, Material.GOLD_INGOT,
            Material.DIAMOND_HELMET, Material.DIAMOND,
            Material.NETHERITE_HELMET, Material.NETHERITE_INGOT,
            Material.LEATHER_CHESTPLATE, Material.OAK_LOG,
            Material.CHAINMAIL_CHESTPLATE, Material.COBBLESTONE,
            Material.IRON_CHESTPLATE, Material.IRON_INGOT,
            Material.GOLDEN_CHESTPLATE, Material.GOLD_INGOT,
            Material.DIAMOND_CHESTPLATE, Material.DIAMOND,
            Material.NETHERITE_CHESTPLATE, Material.NETHERITE_INGOT,
            Material.LEATHER_LEGGINGS, Material.OAK_LOG,
            Material.CHAINMAIL_LEGGINGS, Material.COBBLESTONE,
            Material.IRON_LEGGINGS, Material.IRON_INGOT,
            Material.GOLDEN_LEGGINGS, Material.GOLD_INGOT,
            Material.DIAMOND_LEGGINGS, Material.DIAMOND,
            Material.NETHERITE_LEGGINGS, Material.NETHERITE_INGOT,
            Material.LEATHER_BOOTS, Material.OAK_LOG,
            Material.CHAINMAIL_BOOTS, Material.COBBLESTONE,
            Material.IRON_BOOTS, Material.IRON_INGOT,
            Material.GOLDEN_BOOTS, Material.GOLD_INGOT,
            Material.DIAMOND_BOOTS, Material.DIAMOND,
            Material.NETHERITE_BOOTS, Material.NETHERITE_INGOT
    ));
    private final Map<Material, Material> materialMap;
    AnvilConfig(Map<Material, Material> materialMap) {
        this.materialMap = materialMap;
    }
    public static AnvilConfig getByMaterialLeft(Material material) {
        for (AnvilConfig config : values()) {
            if (config.materialMap.containsKey(material)) {
                return config;
            }
        }
        return null;
    }
    public static Material toRepair(Material material) {
        AnvilConfig config = getByMaterialLeft(material);
        if (config != null) {
            return config.getMaterialMap().get(material);
        }
        return null;
    }
}
