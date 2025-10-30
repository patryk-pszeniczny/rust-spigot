package uniquecode.rust.essential.structure.anvil;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import uniquecode.rust.Main;
import uniquecode.rust.api.manager.Manager;
import uniquecode.rust.api.manager.ManagerService;
import uniquecode.rust.api.message.MessageCache;
import uniquecode.rust.builder.ItemBuilder;
import uniquecode.rust.essential.structure.anvil.config.AnvilConfig;
import uniquecode.rust.essential.structure.enchant.EnchantEnum;
import uniquecode.rust.helper.InventoryHelper;
import uniquecode.rust.util.SchedulerUtil;
import uniquecode.rust.util.TextUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AnvilManager implements Manager {
    private final static int RIGHT_SLOT = 33;
    private final static int LEFT_SLOT = 29;
    private final Main main;
    private final ManagerService managerService;
    private MessageCache messageCache;
    public AnvilManager(Main main, ManagerService managerService) {
        this.main = main;
        this.managerService = managerService;
        this.managerService.register("anvil-manager", this);
    }
    @Override
    public void setupCache() {
        this.messageCache = (MessageCache) this.managerService.get("message-cache");
    }
    public void handleEvent(InventoryClickEvent event){
        Inventory clickedInventory = InventoryHelper.getClicked(event);
        if (clickedInventory == null) {
            event.setCancelled(true);
            return;
        }
        Inventory inventory = event.getView().getTopInventory();
        InventoryType inventoryType = clickedInventory.getType();
        ClickType clickType = event.getClick();
        if(!clickType.isLeftClick() && !clickType.isRightClick() && !clickType.isShiftClick()) {
            event.setCancelled(true);
            return;
        }
        if(inventoryType == InventoryType.PLAYER){
            if(event.getClick()==ClickType.DOUBLE_CLICK){
                event.setCancelled(true);
                return;
            }
            if(!event.isShiftClick()){
                return;
            }
            ItemStack target = event.getCurrentItem();
            if(target == null) return;

            ItemStack leftItem = left(inventory);
            ItemStack rightItem = right(inventory);

            int isLeft = isLeft(inventory, target);
            if(isLeft == -1){
                event.setCancelled(true);
                return;
            }
            //Lewo
            if(isLeft == 0){
                int append = target.getAmount();
                if(leftItem != null && leftItem.getType() == target.getType()
                        && leftItem.getMaxStackSize() > 1
                        && leftItem.getAmount() != leftItem.getMaxStackSize()) {
                    append += leftItem.getAmount();
                }
                ItemStack newItem = target.clone();
                if(append > target.getMaxStackSize()) {
                    append = target.getMaxStackSize();
                }
                newItem.setAmount(append);
                this.setGlass(inventory, newItem, rightItem);
                return;
            }
            //Prawo
            if(isLeft == 1){
                int append = target.getAmount();
                if(rightItem != null && rightItem.getType() == target.getType()
                        && rightItem.getMaxStackSize() > 1
                        && rightItem.getAmount() != rightItem.getMaxStackSize()) {
                    append += rightItem.getAmount();
                }
                ItemStack newItem = target.clone();
                if(append > target.getMaxStackSize()) {
                    append = target.getMaxStackSize();
                }
                newItem.setAmount(append);
                this.setGlass(inventory, leftItem, newItem);
                return;
            }
            //Oba
            if(isLeft == 2){
                this.setGlass(inventory, target, target);
                return;
            }
            event.setCancelled(true);
            return;
        }
        if(inventoryType == InventoryType.CHEST) {
            if(event.getRawSlot()==49){
                SchedulerUtil.close((Player) event.getWhoClicked());
                return;
            }
            if(event.getRawSlot() == 22){
                Player player = (Player) event.getWhoClicked();
                event.setCancelled(true);
                ItemStack left = left(inventory);
                ItemStack right = right(inventory);

                boolean isValidLeft=isValidItemEnchanted(left);
                boolean isValidRightStaff = isValidItemStaff(left,right);
                boolean isValidRight=isValidItemEnchanted(right)||isValidRightStaff;
                if(!isValidLeft || !isValidRight) {
                    return;
                }
                if(isValidRightStaff){
                    int repairCost = repairCost(left.getDurability(), left.getType().getMaxDurability());
                    if(right.getAmount() < repairCost){
                        repairCost = right.getAmount();
                    }
                    if(player.getLevel() < repairCost * 10){
                        this.messageCache.sendMessage(player, "anvil.no-enough-level", "&cNie masz wystarczającej ilości poziomów do naprawy tego przedmiotu.");
                        return;
                    }
                    player.setLevel(player.getLevel() - repairCost * 10);
                    ItemStack compareItem = compareRepairItem(left, right);
                    if(compareItem == null){
                        return;
                    }
                    ItemStack cursor = event.getCursor();
                    if (cursor != null) {
                        InventoryHelper.giveItemsPlayer((Player) event.getWhoClicked(), cursor);
                    }
                    if(right.getAmount() > repairCost){
                        right.setAmount(right.getAmount() - repairCost);
                    }else {
                        inventory.setItem(RIGHT_SLOT, null);
                    }
                    inventory.setItem(LEFT_SLOT, null);
                    setGlass(inventory, left(inventory), right(inventory));
                    event.setCursor(compareItem);
                    player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 0.3f, 0.3f);
                    return;
                }
                int comparePrice = compareTwoItemPrice(left, right);
                if(player.getLevel() < comparePrice){
                    this.messageCache.sendMessage(player, "anvil.no-enough-level", "&cNie masz wystarczającej ilości poziomów do ulepszenia tego przedmiotu.");
                    return;
                }
                player.setLevel(player.getLevel() - comparePrice);
                ItemStack compareItem = compareTwoItem(left, right);
                if(compareItem == null){
                    return;
                }
                ItemStack cursor = event.getCursor();
                if (cursor != null) {
                    InventoryHelper.giveItemsPlayer((Player) event.getWhoClicked(), cursor);
                }
                inventory.setItem(LEFT_SLOT, null);
                inventory.setItem(RIGHT_SLOT, null);
                setGlass(inventory, left(inventory), right(inventory));
                event.setCursor(compareItem);
                player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 0.3f, 0.3f);
                return;
            }
            if(event.isShiftClick()) {
                if (event.getRawSlot() == LEFT_SLOT) {
                    this.setGlass(inventory, null, right(inventory));
                    return;
                }
                if (event.getRawSlot() == RIGHT_SLOT) {
                    this.setGlass(inventory, left(inventory), null);
                    return;
                }
            }
            if (event.getRawSlot() == LEFT_SLOT) {
                ItemStack target = event.getCurrentItem();
                ItemStack cursor = event.getCursor();
                if(cursor == null || cursor.getType().isAir()) {
                    if(target != null && event.getClick().isRightClick() && target.getAmount() > 1){
                        int halfAmount = target.getAmount() / 2;
                        ItemStack newItem = target.clone();
                        newItem.setAmount(halfAmount);
                        this.setGlass(inventory, target, right(inventory));
                        return;
                    }
                    this.setGlass(inventory, null, right(inventory));
                    return;
                }
                ItemStack newItem = cursor.clone();
                if(event.getClick().isRightClick()){
                    newItem.setAmount(1);
                }
                if(target == null || target.getType().isAir()) {
                    this.setGlass(inventory, cursor, right(inventory));
                    return;
                }
                if(!target.isSimilar(cursor)){
                    this.setGlass(inventory, newItem, right(inventory));
                    return;
                }
                int amount = target.getAmount() + cursor.getAmount();
                newItem.setAmount(Math.min(amount, target.getMaxStackSize()));
                this.setGlass(inventory, newItem, right(inventory));
                return;
            }
            if (event.getRawSlot() == RIGHT_SLOT) {
                ItemStack target = event.getCurrentItem();
                ItemStack cursor = event.getCursor();
                if(cursor == null || cursor.getType().isAir()) {
                    if(target != null && event.getClick().isRightClick() && target.getAmount() > 1){
                        int halfAmount = target.getAmount() / 2;
                        ItemStack newItem = target.clone();
                        newItem.setAmount(halfAmount);
                        this.setGlass(inventory, left(inventory), newItem);
                        return;
                    }
                    this.setGlass(inventory, left(inventory), null);
                    return;
                }
                ItemStack newItem = cursor.clone();
                if(event.getClick().isRightClick()){
                    newItem.setAmount(1);
                }
                if(target == null || target.getType().isAir()) {
                    this.setGlass(inventory, left(inventory), newItem);
                    return;
                }
                if(!target.isSimilar(cursor)){
                    this.setGlass(inventory, left(inventory), newItem);
                    return;
                }
                int amount = target.getAmount() + newItem.getAmount();
                newItem.setAmount(Math.min(amount, target.getMaxStackSize()));
                this.setGlass(inventory, left(inventory), newItem);
                return;
            }
            event.setCancelled(true);
            return;
        }
        event.setCancelled(true);
    }
    public void setGlass(Inventory inventory,ItemStack left,ItemStack right){
        boolean isValidLeft=isValidItemEnchanted(left);
        boolean isValidRightStaff = isValidItemStaff(left,right);
        boolean isValidRight=isValidItemEnchanted(right)||isValidRightStaff;

        int rightColor = right == null ? 0 : (isValidRight ? 1 : 2);
        int leftColor = left == null ? 0 : (isValidLeft ? 1 : 2);

        if (rightColor == 2 && leftColor != 0) leftColor = 2;
        if (leftColor == 2 && rightColor != 0) rightColor = 2;
        new ItemBuilder(getColorGlass(leftColor))
                .setDisplayName("&6Przedmiot do Ulepszenia")
                .addLore("&7Przedmiot, który chcesz ulepszyć, powinien")
                .addLore("&7być umieszczony w slocie po tej stronie.")
                .build(inventory, 12,11,20);
        new ItemBuilder(getColorGlass(rightColor))
                .setDisplayName("&6Przedmiot Poświęcenia")
                .addLore("&7Przedmiot poświęcany w celu")
                .addLore("&7ulepszenia przedmiotu po lewej stronie")
                .addLore("&7powinien zostać umieszczony w slocie")
                .addLore("&7po tej stronie.")
                .build(inventory, 14, 15, 24);
        int colorUnder= rightColor == leftColor ? rightColor : 0;
        new ItemBuilder(getColorGlass(colorUnder))
                .setDisplayName(" ")
                .build(inventory, 45,46,47,48,50,51,52,53);
        if(colorUnder == 1){
            if(isValidRightStaff) {
                ItemStack compareItem = compareRepairItem(left, right);
                if(compareItem == null){
                    return;
                }
                ItemMeta itemMeta = compareItem.getItemMeta();
                List<String> lore = new ArrayList<>();
                if(itemMeta.hasLore()){
                    lore.addAll(itemMeta.getLore());
                }
                lore.add(TextUtil.color("&8&m━━━━━━━━━━━━━━━━━━"));
                lore.add(TextUtil.color("&aTen przedmiot możesz otrzymać."));
                lore.add(TextUtil.color("&aKlinij na &cKOWADŁO NIŻEJ &aaby złączyć."));
                itemMeta.setLore(lore);
                compareItem.setItemMeta(itemMeta);
                inventory.setItem(13, compareItem);

                int cost = repairCost(left.getDurability(), left.getType().getMaxDurability());
                if(right.getAmount() < cost){
                    cost = right.getAmount();
                }
                cost *= 10;

                new ItemBuilder(Material.ANVIL)
                        .setDisplayName("&aŁączenie Przedmiotów")
                        .addLore("&7Połącz przedmiot w slotach")
                        .addLore("&7po lewiej i prawej stronie poniżej.")
                        .addLore(" ")
                        .addLore("&7Koszt")
                        .addLore("&3"+cost+" Exp Level")
                        .addLore(" ")
                        .addLore("&eKliknij, Aby połączyć!")
                        .addEnchantment(Enchantment.DURABILITY, 10)
                        .addItemFlag(ItemFlag.HIDE_ENCHANTS)
                        .build(22, inventory);
                return;
            }
            ItemStack compareItem = compareTwoItem(left, right);
            if(compareItem == null){
                return;
            }
            ItemMeta itemMeta = compareItem.getItemMeta();
            List<String> lore = new ArrayList<>();
            if(itemMeta.hasLore()){
                lore.addAll(itemMeta.getLore());
            }
            lore.add(TextUtil.color("&8&m━━━━━━━━━━━━━━━━━━"));
            lore.add(TextUtil.color("&aTen przedmiot możesz otrzymać."));
            lore.add(TextUtil.color("&aKlinij na &cKOWADŁO NIŻEJ &aaby złączyć."));
            itemMeta.setLore(lore);
            compareItem.setItemMeta(itemMeta);
            inventory.setItem(13, compareItem);
            int comparePrice = compareTwoItemPrice(left, right);
            new ItemBuilder(Material.ANVIL)
                    .setDisplayName("&aŁączenie Przedmiotów")
                    .addLore("&7Połącz przedmiot w slotach")
                    .addLore("&7po lewiej i prawej stronie poniżej.")
                    .addLore(" ")
                    .addLore("&7Koszt")
                    .addLore("&3"+comparePrice+" Exp Level")
                    .addLore(" ")
                    .addLore("&eKliknij, Aby połączyć!")
                    .addEnchantment(Enchantment.DURABILITY, 10)
                    .addItemFlag(ItemFlag.HIDE_ENCHANTS)
                    .build(22, inventory);
            return;
        }
        new ItemBuilder(Material.BARRIER)
                .setDisplayName("&cAnvil")
                .addLore("&7Umieść przedmiot docelowy w lewym slocie")
                .addLore("&7i przedmiot poświęcenia w prawym slocie,")
                .addLore("&7Aby je połączyć")
                .build(13, inventory);
        new ItemBuilder(Material.ANVIL)
                .setDisplayName("&aŁączenie Przedmiotów")
                .addLore("&7Połącz przedmiot w slotach")
                .addLore("&7po lewiej i prawej stronie poniżej.")
                .build(22, inventory);
    }
    public ItemStack compareRepairItem(ItemStack itemStack, ItemStack targetItem){
        if (itemStack == null || itemStack.getType().isAir()) {
            return null;
        }
        if (targetItem == null || targetItem.getType().isAir()) {
            return null;
        }
        short durability = itemStack.getDurability();
        short maxDurability = itemStack.getType().getMaxDurability();
        int cost = repairCost(durability, maxDurability);
        int unitRepair = unitRepair(durability, maxDurability);
        short newDurability;
        if(targetItem.getAmount() >= cost){
            newDurability = 0;
        }else {
            short durabilityReduction = (short) (targetItem.getAmount() * unitRepair);
            newDurability = (short) (durability - durabilityReduction);
            if(newDurability < 0) {
                newDurability = 0;
            }
        }
        ItemStack newItem = itemStack.clone();
        newItem.setDurability(newDurability);
        return newItem;

    }
    public int compareTwoItemPrice(ItemStack itemStack, ItemStack targetItem) {
        int cost = 0;
        if (itemStack == null || itemStack.getType().isAir()) {
            return cost;
        }
        if (targetItem == null || targetItem.getType().isAir()) {
            return cost;
        }
        if (itemStack.getType() != targetItem.getType()) {
            return cost;
        }
        ItemStack newItem = itemStack.clone();
        short durabilityItem = itemStack.getDurability();
        if(durabilityItem != 0){
            short durabilityTarget = targetItem.getDurability();
            short maxDurability = targetItem.getType().getMaxDurability();
            short reduction = (short) (maxDurability - durabilityTarget);
            short mathDurability = (short) (durabilityItem - reduction);

            double repair = (double) mathDurability / (double) maxDurability;
            cost += (int) (50 * (1 - repair));
        }
        if(!targetItem.getEnchantments().isEmpty()){
            for(Map.Entry<Enchantment, Integer> entry : targetItem.getEnchantments().entrySet()) {
                Enchantment enchantment = entry.getKey();
                int level = entry.getValue();
                if(newItem.containsEnchantment(enchantment)) {
                    int currentLevel = newItem.getEnchantmentLevel(enchantment);
                    if(currentLevel < level) {
                        cost += 5;
                    }else if(currentLevel == level) {
                        EnchantEnum enchantEnum = EnchantEnum.getEnchantEnum(enchantment);
                        if(enchantEnum == null) {
                            continue;
                        }
                        if(enchantEnum.getMaxLevel() < currentLevel+1){
                            continue;
                        }
                        cost += 5;
                    }
                } else {
                    cost += 5;
                }
            }
        }
        return cost;
    }
    public ItemStack compareTwoItem(ItemStack itemStack, ItemStack targetItem) {
        if (itemStack == null || itemStack.getType().isAir()) {
            return null;
        }
        if (targetItem == null || targetItem.getType().isAir()) {
            return null;
        }
        if (itemStack.getType() != targetItem.getType()) {
            return null;
        }
        ItemStack newItem = itemStack.clone();
        short durabilityItem = itemStack.getDurability();
        if(durabilityItem != 0){
            short durabilityTarget = targetItem.getDurability();
            short maxDurability = targetItem.getType().getMaxDurability();
            short reduction = (short) (maxDurability - durabilityTarget);
            short mathDurability = (short) (durabilityItem - reduction);
            if(mathDurability < 0) {
                mathDurability = 0;
            }
            newItem.setDurability(mathDurability);
        }
        ItemMeta itemMeta = newItem.getItemMeta();
        if(!targetItem.getEnchantments().isEmpty()){
            for(Map.Entry<Enchantment, Integer> entry : targetItem.getEnchantments().entrySet()) {
                Enchantment enchantment = entry.getKey();
                int level = entry.getValue();
                if(newItem.containsEnchantment(enchantment)) {
                    int currentLevel = newItem.getEnchantmentLevel(enchantment);
                    if(currentLevel < level) {
                        itemMeta.addEnchant(enchantment, level, true);
                    }else if(currentLevel == level) {
                        EnchantEnum enchantEnum = EnchantEnum.getEnchantEnum(enchantment);
                        if(enchantEnum == null) {
                            continue;
                        }
                        if(enchantEnum.getMaxLevel() < currentLevel+1){
                            itemMeta.addEnchant(enchantment, currentLevel, true);
                            continue;
                        }
                        itemMeta.addEnchant(enchantment, currentLevel + 1, true);
                    }
                } else {
                    itemMeta.addEnchant(enchantment, level, true);
                }
            }
        }
        newItem.setItemMeta(itemMeta);
        return newItem;
    }
    public int unitRepair(short durability, short maxDurability){
        if(durability <= 0 || maxDurability <= 0) {
            return 0;
        }
        return maxDurability / 5;
    }
    public int repairCost(short durability, short maxDurability){
        if(durability <= 0 || maxDurability <= 0) {
            return 0;
        }
        int costPerUnit = unitRepair(durability, maxDurability);
        int cost = durability / costPerUnit;
        if(cost % costPerUnit != 0) {
            cost++;
        }
        return Math.min(cost, 5);
    }
    public Material getColorGlass(int valid){
        return switch(valid) {
            case 1->Material.LIME_STAINED_GLASS_PANE;
            case 2->Material.ORANGE_STAINED_GLASS_PANE;
            default->Material.RED_STAINED_GLASS_PANE;
        };
    }
    public ItemStack right(Inventory inventory){
        ItemStack item = inventory.getItem(RIGHT_SLOT);
        if(item == null || item.getType().isAir()){
            return null;
        }
        return item;
    }
    public ItemStack left(Inventory inventory){
        ItemStack item = inventory.getItem(LEFT_SLOT);
        if(item == null || item.getType().isAir()){
            return null;
        }
        return item;
    }
    public int isLeft(Inventory inventory, ItemStack itemStack){
        ItemStack leftItem = inventory.getItem(LEFT_SLOT);
        if(leftItem == null || leftItem.getType().isAir()){
            return 0;
        }
        ItemStack rightItem = inventory.getItem(RIGHT_SLOT);
        if(leftItem.isSimilar(itemStack)
                && leftItem.getMaxStackSize() > 1
                && leftItem.getAmount() != leftItem.getMaxStackSize()) {
            if(leftItem.getAmount()+itemStack.getAmount() > leftItem.getMaxStackSize()) {
                if(rightItem == null || rightItem.getType().isAir()) {
                    return 2;
                }
                if(rightItem.isSimilar(itemStack)
                        && rightItem.getMaxStackSize() > 1
                        && rightItem.getAmount() != rightItem.getMaxStackSize()) {
                    return 2;
                }
                return 0;
            }
            return 0;
        }
        if(rightItem == null || rightItem.getType().isAir()){
            return 1;
        }
        if(rightItem.isSimilar(itemStack)
                && rightItem.getMaxStackSize() > 1
                && rightItem.getAmount() != rightItem.getMaxStackSize()) {
            if(rightItem.getAmount()+ itemStack.getAmount() > rightItem.getMaxStackSize()) {
                return 2;
            }
            if(leftItem.isSimilar(itemStack)
                    && leftItem.getMaxStackSize() > 1
                    && leftItem.getAmount() != leftItem.getMaxStackSize()) {
                return 2;
            }
            return 1;
        }
        return -1;
    }
    public boolean isValidItemEnchanted(ItemStack itemStack) {
        if (itemStack == null || itemStack.getType().isAir()) {
            return false;
        }
        AnvilConfig anvilConfig = AnvilConfig.getByMaterialLeft(itemStack.getType());
        return anvilConfig != null;
    }
    public boolean isValidItemStaff(ItemStack itemStack, ItemStack targetItem) {
        if (itemStack == null || itemStack.getType().isAir()) {
            return false;
        }
        if(targetItem == null || targetItem.getType().isAir()) {
            return false;
        }
        Material anvilConfig = AnvilConfig.toRepair(itemStack.getType());
        return anvilConfig == targetItem.getType();
    }
    public void openInventory(Player player){
        Inventory inventory = InventoryHelper.create(6, "&8Anvil");

        new ItemBuilder(Material.BARRIER)
                .setDisplayName("&cAnvil")
                .addLore("&7Umieść przedmiot docelowy w lewym slocie")
                .addLore("&7i przedmiot poświęcenia w prawym slocie,")
                .addLore("&7Aby je połączyć")
                .build(13, inventory);
        new ItemBuilder(Material.ANVIL)
                .setDisplayName("&aŁączenie Przedmiotów")
                .addLore("&7Połącz przedmiot w slotach")
                .addLore("&7po lewiej i prawej stronie poniżej.")
                .build(22, inventory);
        new ItemBuilder(Material.RED_STAINED_GLASS_PANE)
                .setDisplayName("&6Przedmiot do Ulepszenia")
                .addLore("&7Przedmiot, który chcesz ulepszyć, powinien")
                .addLore("&7być umieszczony w slocie po tej stronie.")
                .build(inventory, 12, 11, 20);

        new ItemBuilder(Material.RED_STAINED_GLASS_PANE)
                .setDisplayName("&6Przedmiot Poświęcenia")
                .addLore("&7Przedmiot poświęcany w celu")
                .addLore("&7ulepszenia przedmiotu po lewej stronie")
                .addLore("&7powinien zostać umieszczony w slocie")
                .addLore("&7po tej stronie.")
                .build(inventory, 14, 15, 23, 24);


        new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE)
                .setDisplayName(" ")
                .build(inventory, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 16, 17, 18, 19, 21, 23, 25, 26,
                27, 28, 30, 31, 32, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44);
        new ItemBuilder(Material.RED_STAINED_GLASS_PANE)
                .setDisplayName(" ")
                .build(inventory, 45, 46, 47, 48, 50, 51, 52, 53);
        new ItemBuilder(Material.BARRIER)
                .setDisplayName("&cZamknij")
                .build(49, inventory);
        player.openInventory(inventory);
    }
}
