package uniquecode.rust.airdrop.structure;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import uniquecode.rust.Main;
import uniquecode.rust.api.manager.Manager;
import uniquecode.rust.api.manager.ManagerService;
import uniquecode.rust.builder.ItemBuilder;
import uniquecode.rust.drop.structure.DropEnum;
import uniquecode.rust.drop.structure.DropManager;
import uniquecode.rust.helper.InventoryHelper;
import uniquecode.rust.util.NBTUtil;
import uniquecode.rust.util.SchedulerUtil;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class AirdropManager implements Manager {
    private final Main main;
    private final ManagerService managerService;
    private final ZoneId ZONE = ZoneId.of("Europe/Warsaw");
    private final int START_HOUR = 12;
    private final int END_HOUR = 18;
    private final int GRACE_MIN = 15;
    private final Map<ZonedDateTime, AirdropConfig> AIRDROP_CONFIG_MAP=new HashMap<>();

    private final DateTimeFormatter HOUR_FMT = DateTimeFormatter.ofPattern("HH:mm");
    private final DateTimeFormatter DATE_DAY = DateTimeFormatter.ofPattern("EEEE", new Locale("pl", "PL"));
    private final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd.MM.yyyy", new Locale("pl", "PL"));
    private Inventory inventory;
    private DropManager dropManager;
    public AirdropManager(Main main, ManagerService managerService) {
        this.main = main;
        this.managerService = managerService;
        this.managerService.register("airdrop-manager", this);
    }

    @Override
    public void setupCache() {
        this.dropManager = (DropManager) this.managerService.get("drop-manager");
    }
    public void handleEvent(InventoryClickEvent event){
        event.setCancelled(true);
        Inventory inventory = InventoryHelper.getClicked(event);
        if(inventory==null || inventory.getType()!= InventoryType.CHEST){
            return;
        }
        Player player = (Player) event.getWhoClicked();
        if(event.getRawSlot()==49){
            SchedulerUtil.close(player);
            return;
        }
        if(event.getCurrentItem()==null)return;
        String type = NBTUtil.getString(event.getCurrentItem(), "type");
        if(type==null || type.isEmpty()) {
            return;
        }
        this.dropManager.open(
                player,
                DropEnum.FLARE,
                DropEnum.valueOf(type)
        );


    }
    public void createInventory(){
        Inventory inventory= InventoryHelper.create(6, "&dAirDrop");
        new ItemBuilder(Material.PURPLE_STAINED_GLASS_PANE).setDisplayName(" ").build(inventory, 0, 8, 45, 53, 9, 17, 36, 44);
        new ItemBuilder(Material.MAGENTA_STAINED_GLASS_PANE).setDisplayName(" ").build(inventory, 1, 7, 46, 52);
        new ItemBuilder(Material.BLUE_STAINED_GLASS_PANE).setDisplayName(" ").build(inventory, 2, 6, 47, 51);
        new ItemBuilder(Material.LIGHT_BLUE_STAINED_GLASS_PANE).setDisplayName(" ").build(inventory, 3, 5, 48, 50);
        new ItemBuilder(Material.BARRIER).setDisplayName("&cZamknij").build(inventory, 49);

        List<Integer> airdropLocations = List.of(
                10, 11, 12, 13, 14, 15, 16,
                19, 20, 21, 22, 23, 24, 25,
                28, 29, 30, 31, 32, 33, 34,
                37, 38, 39, 40, 41, 42, 43
        );
        final String title =
                "&#DB0A67☀ "
                        + "&#FF0000A&#F81712I&#F02E24R&#E74536D&#DF5C48R&#D7735AO&#CF8A6CP"
                        + " &8• &dPanel";
        final ZonedDateTime now = ZonedDateTime.now(ZONE);
        List<ZonedDateTime> upcoming = collectUpcomingAirdrops(now, airdropLocations.size());
        final ZonedDateTime next = upcoming.get(0);
        final boolean liveNext = !now.isBefore(next) && !now.isAfter(next.plusMinutes(GRACE_MIN));
        final boolean todayNext = next.toLocalDate().equals(now.toLocalDate());
        final boolean tomorrowNext = next.toLocalDate().equals(now.toLocalDate().plusDays(1));

        final String whenDayPretty = todayNext ? "&aDziś"
                : (tomorrowNext ? "&eJutro" : "&7" + next.format(DATE));
        final String startLabel = next.format(HOUR_FMT);
        final String endLabelLore   = next.plusMinutes(GRACE_MIN).format(HOUR_FMT);

        final long minsToStart = Math.max(0, Duration.between(now, next).toMinutes());
        final long minsToEnd   = Math.max(0, Duration.between(now, next.plusMinutes(GRACE_MIN)).toMinutes());

        final AirdropConfig nextCfg = getAirdropConfig(next);
        final String dropName = (nextCfg != null ? nextCfg.getColor() : "&7brak");
        new ItemBuilder(Material.BEACON)
                .setDisplayName(title)
                .addLore(
                        "&8&m━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━",
                        " &7Następny zrzut&8: " + whenDayPretty + " &8(&f" + startLabel + "&8)",
                        " &7Okno aktywności&8: &a" + startLabel + " &8→ &e" + endLabelLore,
                        liveNext
                                ? " &7Status&8: &a&lTRWA &8(kończy się za &f" + minsToEnd + " min&8)"
                                : " &7Status&8: &eWkrótce &8(za &f" + minsToStart + " min&8)",
                        " &7Wylosowany drop&8: " + dropName,
                        "&8&m━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━",
                        " &7  &d📜 &7&oPlotki mówią o skrzyniach, które &dzakrzywiają los&7,",
                        " &7  prowadząc śmiałków do &fniezwykłych zdobyczy&7.",
                        " &7  &e✨ &7Airdrop trwa &f" + GRACE_MIN + " min&7 od startu — nie spóźnij się.",
                        " &7  &b📍 &7Wypatruj sygnału flary i &fkoorydnatów&7 na czacie.",
                        "&8▸ &7Wybierz termin poniżej, aby podejrzeć zawartość."
                )
                .build(inventory, 4);
        for (int i = 0; i < upcoming.size(); i++) {
            int slot = airdropLocations.get(i);
            ZonedDateTime when = upcoming.get(i);

            boolean live = !now.isBefore(when) && !now.isAfter(when.plusMinutes(GRACE_MIN));
            boolean today = when.toLocalDate().equals(now.toLocalDate());
            boolean tomorrow = when.toLocalDate().equals(now.toLocalDate().plusDays(1));
            String timeLabel = when.format(HOUR_FMT);

            String endLabel = when.plusMinutes(GRACE_MIN).format(HOUR_FMT);
            String statusLine = " &fStatus&8: "+(live
                    ? "&a&lTRWA &8(do &f" + endLabel + "&8)"
                    : (today ? "&eWkrótce"
                    : (tomorrow ? "&6Jutro"
                    : "&7Zaplanowany")));
            AirdropConfig airdropConfig = getAirdropConfig(when);
            new ItemBuilder(getMaterial(today, tomorrow, live))
                    .setDisplayName("&6&l>> &5&l"+when.format(DATE_DAY)+" &f(&a&l" + when.format(DATE) + "&f) &6&l<<")
                    .addLore(
                            " &fWylosowany drop&8: "+airdropConfig.getColor(),
                            " &fCzas trwania&8: &a" + timeLabel + " &8→ &e" + endLabel,
                            statusLine,
                            " ",
                            "&aKliknij, Aby zobaczyć dropy na ten termin!"
                    )
                    .addNbt("type", airdropConfig.getFlareEnumType().name())
                    .build(inventory, slot);

        }
        this.inventory = inventory;
    }
    public void openInventory(Player player){
        if(inventory == null) {
            createInventory();
        }
        player.openInventory(inventory);
    }
    public Material getMaterial(boolean today, boolean tomorrow, boolean live){
        if(live) return Material.LIME_DYE;
        if(today) return Material.YELLOW_DYE;
        if(tomorrow) return Material.ORANGE_DYE;
        return Material.RED_DYE;
    }
    private List<ZonedDateTime> collectUpcomingAirdrops(ZonedDateTime now, int count) {
        List<ZonedDateTime> result = new ArrayList<>(count);
        LocalDate day = now.toLocalDate();
        int scannedDays = 0;
        while (result.size() < count && scannedDays < 365) {
            for (int h = START_HOUR; h <= END_HOUR && result.size() < count; h++) {
                ZonedDateTime dt = ZonedDateTime.of(day, LocalTime.of(h, 0), now.getZone());
                if (!dt.plusMinutes(GRACE_MIN).isBefore(now)) {
                    result.add(dt);
                }
            }
            day = day.plusDays(1);
            scannedDays++;
        }
        while (result.size() < count) {
            for (int h = START_HOUR; h <= END_HOUR && result.size() < count; h++) {
                ZonedDateTime dt = ZonedDateTime.of(day, LocalTime.of(h, 0), now.getZone());
                result.add(dt);
            }
            day = day.plusDays(1);
        }
        return result;
    }
    public AirdropConfig getAirdropConfig(ZonedDateTime date) {
        if(date == null) {
            return null;
        }
        if(AIRDROP_CONFIG_MAP.containsKey(date)) {
            return AIRDROP_CONFIG_MAP.get(date);
        }
        AirdropConfig airdropConfig = AirdropConfig.randomDrop();
        if (airdropConfig == null) {
            return null;
        }
        AIRDROP_CONFIG_MAP.put(date, airdropConfig);
        return airdropConfig;
    }

}
