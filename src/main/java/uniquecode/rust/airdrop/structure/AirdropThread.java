package uniquecode.rust.airdrop.structure;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import uniquecode.rust.Main;
import uniquecode.rust.api.manager.Manager;
import uniquecode.rust.api.manager.ManagerService;
import uniquecode.rust.api.message.MessageCache;
import uniquecode.rust.cuboid.structure.CuboidCache;
import uniquecode.rust.cupboard.structure.CupboardManager;
import uniquecode.rust.flare.structure.FlareManager;
import uniquecode.rust.util.LocationUtil;
import uniquecode.rust.util.RandomUtil;
import uniquecode.rust.util.SchedulerUtil;

import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class AirdropThread implements Manager {
    private final Main main;
    private final ManagerService managerService;
    private AirdropManager airdropManager;
    private MessageCache messageCache;
    private CuboidCache cuboidCache;
    private CupboardManager cupboardManager;
    private FlareManager flareManager;
    private final ZoneId ZONE = ZoneId.of("Europe/Warsaw");
    private final int START_HOUR = 12;
    private final int END_HOUR   = 18;
    private final Duration TICK_WINDOW = Duration.ofMinutes(5);
    private final DateTimeFormatter HOUR_FMT = DateTimeFormatter.ofPattern("HH:mm");
    private final Set<ZonedDateTime> fired = ConcurrentHashMap.newKeySet();
    private LocalDate lastReset = LocalDate.now(ZONE);
    public AirdropThread(Main main, ManagerService managerService) {
        this.main = main;
        this.managerService = managerService;
        this.managerService.register("airdrop-thread", this);
    }
    @Override
    public void setupCache() {
        this.airdropManager = (AirdropManager) this.managerService.get("airdrop-manager");
        this.messageCache = (MessageCache) this.managerService.get("message-cache");
        this.cuboidCache = (CuboidCache) this.managerService.get("cuboid-cache");
        this.cupboardManager = (CupboardManager) this.managerService.get("cupboard-manager");
        this.flareManager = (FlareManager) this.managerService.get("flare-manager");
        this.schedule();
    }
    public void schedule(){
        SchedulerUtil.runTaskTimer(() -> {
            ZonedDateTime now = ZonedDateTime.now(ZONE);
            LocalDate today = now.toLocalDate();
            if (!today.equals(lastReset)) {
                fired.clear();
                lastReset = today;
            }
            for (int h = START_HOUR; h <= END_HOUR; h++) {
                ZonedDateTime start = now.withHour(h).withMinute(0).withSecond(0).withNano(0);
                if (isWithin(now, start, TICK_WINDOW) && fired.add(start)) {
                    AirdropConfig cfg = airdropManager.getAirdropConfig(start);
                    String dropName = (cfg != null ? cfg.getColor() : "&7brak");
                    String startLabel = start.format(HOUR_FMT);
                    Location location = findLocation();
                    this.messageCache.broadcast(
                            "ait.drop.spawn",
                            "&6&l[AirDrop] &fWystartował zrzut &dAIRDROP &7(&f{0}&7)&f!\n" +
                                    "&7Drop:&f{1}\n" +"&7Kordy: &f{2}",
                            startLabel,
                            dropName,
                            LocationUtil.toStringPresent(location)
                    );
                    this.flareManager.spawnChest(null, cfg.getFlareEnumType(), true, location);
                }
            }
            this.airdropManager.createInventory();
        }, 0L, 5L * 60L * 20L);
    }
    public Location findLocation(){
        while (true){
            int x= RandomUtil.getRandInt(-1250, 1250);
            int z= RandomUtil.getRandInt(-1250, 1250);
            int y= Bukkit.getWorld("world").getHighestBlockYAt(x, z)+1;
            Block block=Bukkit.getWorld("world").getBlockAt(x, y, z);
            if(!block.getType().isAir()){
                continue;
            }
            if(cuboidCache.isMonument(block.getLocation())!=null){
                continue;
            }
            if(cupboardManager.getCupboard(block.getLocation())!=null){
                continue;
            }
            return block.getLocation().add(0, 2, 0);
        }
    }
    private boolean isWithin(ZonedDateTime now, ZonedDateTime target, Duration window) {
        return !now.isBefore(target) && now.isBefore(target.plus(window));
    }
}
