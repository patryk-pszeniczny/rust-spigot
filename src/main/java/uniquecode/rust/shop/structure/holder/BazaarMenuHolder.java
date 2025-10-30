package uniquecode.rust.shop.structure.holder;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemFlag;
import org.jetbrains.annotations.NotNull;
import uniquecode.rust.Main;
import uniquecode.rust.api.manager.ManagerService;
import uniquecode.rust.builder.ItemBuilder;
import uniquecode.rust.shop.structure.bazaar.BazaarCache;
import uniquecode.rust.shop.structure.bazaar.BazaarProduct;
import uniquecode.rust.shop.structure.bazaar.BazaarProductMapper;
import uniquecode.rust.shop.structure.bazaar.BazaarType;
import uniquecode.rust.shop.util.BazaarUtil;
import uniquecode.rust.util.ItemUtil;
import uniquecode.rust.util.NumberUtil;
import uniquecode.rust.util.TextUtil;

import java.util.List;
@Getter
public class BazaarMenuHolder implements InventoryHolder {
    private final static List<Integer> glassPaneSlots = List.of(
            1, 2, 3, 4, 5, 6, 7, 8,
            10, 17, 19, 26, 28, 35, 37, 44,
            46, 48, 53);
    private final static List<Integer> productSlots = List.of(
            11, 12, 13, 14, 15, 16,
            20, 21, 22, 23, 24, 25,
            29, 30, 31, 32, 33, 34,
            38, 39, 40, 41, 42, 43
    );
    private final Main main;
    private final ManagerService managerService;
    private final Inventory inventory;
    private final BazaarType bazaarType;
    private final BazaarCache bazaarCache;
    public BazaarMenuHolder(Main main, ManagerService managerService, BazaarType bazaarType){
        this.main = main;
        this.managerService = managerService;
        this.bazaarCache = (BazaarCache) this.managerService.get("bazaar-cache");
        this.bazaarType = bazaarType;
        this.inventory = this.renderMenu();
    }
    @Override
    public @NotNull Inventory getInventory() {
        return this.inventory;
    }
    public Inventory renderMenu(){
        Inventory inventoryMenu = this.main.getServer().createInventory(this, 54, TextUtil.color("&8Bazaar ➜ "+this.bazaarType.getDisplayName()));
        new ItemBuilder(this.bazaarType.getGlassColor())
                .setDisplayName(" ")
                .build(inventoryMenu, glassPaneSlots);
        new ItemBuilder(Material.CHEST)
                .setDisplayName("&aSell Inventory Now")
                .addLore("&7Instantly sell any items in your")
                .addLore("&7inventory that can be sold on the")
                .addLore("&7Bazaar.")
                .addLore(" ")
                .addLore("&cYou don't have anything to sell!")
                .build(47, inventoryMenu);
        new ItemBuilder(Material.BARRIER)
                .setDisplayName("&cClose")
                .build(49, inventoryMenu);
        new ItemBuilder(Material.BOOK)
                .setDisplayName("&aManage Orders")
                .addLore("&7You don't have any ongoing orders.")
                .addLore(" ")
                .addLore("&eClick to manage!")
                .build(50, inventoryMenu);
        new ItemBuilder(Material.FILLED_MAP)
                .setDisplayName("&aTransaction History")
                .addLore("&7You have no transactions yet.")
                .build(51, inventoryMenu);
        new ItemBuilder(Material.REDSTONE_TORCH)
                .setDisplayName("&aBazaar Settings")
                .addLore("&7View and edit your Bazaar settings.")
                .addLore(" ")
                .addLore("&eClick to open!")
                .build(52, inventoryMenu);
        int slot = 0;
        for(BazaarType bazaarType : BazaarType.values()){
            if(bazaarType == BazaarType.SEARCH){
                new ItemBuilder(bazaarType.getMaterial())
                        .setDisplayName(bazaarType.getColorDisplay()+bazaarType.getDisplayName())
                        .addLore("&7Find products by name!")
                        .addLore(" ")
                        .addLore("&eClick to search!")
                        .addNbt("bazaar-type", bazaarType.name())
                        .build(inventoryMenu, slot);
                continue;
            }
            new ItemBuilder(bazaarType.getMaterial())
                    .setDisplayName(bazaarType.getColorDisplay()+bazaarType.getDisplayName())
                    .addLore("&8Category")
                    .addLore(" ")
                    .addLore(bazaarType == this.bazaarType, "&aCurrently viewing!", "&eClick to view!")
                    .addItemFlag(ItemFlag.HIDE_ATTRIBUTES)
                    .addItemFlag(ItemFlag.HIDE_ENCHANTS)
                    .addEnchantment(bazaarType == this.bazaarType, Enchantment.DURABILITY, 10)
                    .addNbt("bazaar-type", bazaarType.name())
                    .build(inventoryMenu, slot);
            slot+=9;
        }
        slot = 0;
        for(BazaarProductMapper bazaarProductMapper : this.bazaarCache.getProducts(this.bazaarType)){
            if(productSlots.isEmpty()) break;
            if(bazaarProductMapper.getProducts().isEmpty()) continue;
            BazaarProduct firstProduct = bazaarProductMapper.getProducts().get(0);
            Material material = firstProduct.getItem().getItemStack().getType();
            if(material == Material.AIR) material = Material.BARRIER;
            int size = bazaarProductMapper.getProducts().size();
            ItemBuilder itemBuilder = new ItemBuilder(material)
                    .setDisplayName(bazaarType.getColorDisplay()+bazaarProductMapper.getDisplayName())
                    .addLore("&8"+size+" product")
                    .addLore(" ");
            if(size == 1){
                itemBuilder.addLore("&7Buy price: &6"+ NumberUtil.formatBazaar(firstProduct.getBuy()))
                        .addLore("&7Sell price: &6"+ NumberUtil.formatBazaar(firstProduct.getSell()))
                        .addLore(" ")
                        .addLore("&eClick to view product!");
            }else {
                for (BazaarProduct bazaarProduct : bazaarProductMapper.getProducts()) {
                    String regularName = bazaarProduct.getItem().getItemBuilder().getOrgFormat();
                    String productName = ItemUtil.getNameByText(BazaarUtil.fabricateString(BazaarUtil.orgName(regularName)));
                    itemBuilder.addLore("&f⏵ &7" + productName);
                }
                itemBuilder.addLore(" ")
                        .addLore("&eClick to view products!");
            }
            itemBuilder.addItemFlag(ItemFlag.HIDE_ATTRIBUTES)
                    .addItemFlag(ItemFlag.HIDE_ENCHANTS)
                    .build(inventoryMenu, productSlots.get(slot++));

        }
        return inventoryMenu;
    }
    public void productClicked(int index){
        Bukkit.broadcastMessage("Clicked product index: "+index);
    }
}
