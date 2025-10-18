package uniquecode.rust;

import lombok.Getter;
import org.bukkit.command.CommandExecutor;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import uniquecode.rust.airdrop.AirdropPlugin;
import uniquecode.rust.api.bossbar.BossbarPlugin;
import uniquecode.rust.api.hologram.HologramException;
import uniquecode.rust.api.human.HumanPlugin;
import uniquecode.rust.api.item.ItemException;
import uniquecode.rust.api.manager.ManagerService;
import uniquecode.rust.api.message.MessagePlugin;
import uniquecode.rust.api.nametag.NameTagException;
import uniquecode.rust.api.packet.PacketException;
import uniquecode.rust.api.sidebar.SidebarPlugin;
import uniquecode.rust.api.sign.SignPlugin;
import uniquecode.rust.api.time.TimeException;
import uniquecode.rust.building.BuildPlugin;
import uniquecode.rust.casino.CasinoPlugin;
import uniquecode.rust.codelock.CodelockPlugin;
import uniquecode.rust.combat.CombatPlugin;
import uniquecode.rust.crafting.CraftingPlugin;
import uniquecode.rust.cuboid.CuboidPlugin;
import uniquecode.rust.cupboard.CupboardPlugin;
import uniquecode.rust.database.DatabasePlugin;
import uniquecode.rust.disco.DiscoPlugin;
import uniquecode.rust.drop.DropPlugin;
import uniquecode.rust.enderchest.EnderchestPlugin;
import uniquecode.rust.essential.EssentialPlugin;
import uniquecode.rust.evolution.EvolutionPlugin;
import uniquecode.rust.flare.FlarePlugin;
import uniquecode.rust.limbo.LimboPlugin;
import uniquecode.rust.loadout.LoadoutPlugin;
import uniquecode.rust.lootbox.LootboxPlugin;
import uniquecode.rust.recive.RecivePlugin;
import uniquecode.rust.recycle.RecyclePlugin;
import uniquecode.rust.rock.RockPlugin;
import uniquecode.rust.schematic.SchematicPlugin;
import uniquecode.rust.shop.ShopPlugin;
import uniquecode.rust.tablist.TablistPlugin;
import uniquecode.rust.tree.TreePlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.logging.Logger;

@Getter
public class Main extends JavaPlugin {
    public static volatile Main INSTANCE;

    private static final String LOG_PREFIX = "[UniqueCode] ";
    public static final Logger LOGGER = Logger.getLogger(LOG_PREFIX);

    private ManagerService managerService;

    private DatabasePlugin databasePlugin;
    private TimeException timeException;
    private PacketException packetException;
    private HologramException hologramException;
    private HumanPlugin humanPlugin;
    private ItemException itemException;
    private NameTagException nameTagException;
    private MessagePlugin messagePlugin;
    private DropPlugin dropPlugin;
    private EssentialPlugin essentialPlugin;
    private CraftingPlugin craftingPlugin;
    private BuildPlugin buildPlugin;
    private CupboardPlugin cupboardPlugin;
    private EnderchestPlugin enderchestPlugin;
    private RecivePlugin recivePlugin;
    private DiscoPlugin discoPlugin;
    private BossbarPlugin bossbarPlugin;
    private CodelockPlugin codelockPlugin;
    private RockPlugin rockPlugin;
    private TreePlugin treePlugin;
    private SchematicPlugin schematicPlugin;
    private CuboidPlugin cuboidPlugin;
    private LootboxPlugin lootboxPlugin;
    private FlarePlugin flarePlugin;
    private LimboPlugin limboPlugin;
    private CombatPlugin combatPlugin;
    private TablistPlugin tablistPlugin;
    private RecyclePlugin recyclePlugin;
    private EvolutionPlugin evolutionPlugin;
    private LoadoutPlugin loadoutPlugin;
    private SidebarPlugin sidebarPlugin;
    private AirdropPlugin airdropPlugin;
    private CasinoPlugin casinoPlugin;
    private SignPlugin signPlugin;
    private ShopPlugin shopPlugin;

    private final List<NamedStep> enableSteps = new ArrayList<>();
    private final List<NamedStep> disableSteps = new ArrayList<>();

    @Override
    public void onLoad() {
        INSTANCE = this;
        saveDefaultConfig();

        managerService = new ManagerService(this);

        databasePlugin = new DatabasePlugin(this);

        timeException = new TimeException(this);
        packetException = new PacketException(this);
        messagePlugin = new MessagePlugin(this);

        itemException = new ItemException(this);
        hologramException = new HologramException(this);
        humanPlugin = new HumanPlugin(this);
        nameTagException = new NameTagException(this);

        dropPlugin = new DropPlugin(this);
        essentialPlugin = new EssentialPlugin(this);
        craftingPlugin = new CraftingPlugin(this);
        buildPlugin = new BuildPlugin(this);
        cupboardPlugin = new CupboardPlugin(this);
        enderchestPlugin = new EnderchestPlugin(this);
        recivePlugin = new RecivePlugin(this);
        discoPlugin = new DiscoPlugin(this);
        bossbarPlugin = new BossbarPlugin(this);
        codelockPlugin = new CodelockPlugin(this);
        rockPlugin = new RockPlugin(this);
        treePlugin = new TreePlugin(this);
        schematicPlugin = new SchematicPlugin(this);
        cuboidPlugin = new CuboidPlugin(this);
        lootboxPlugin = new LootboxPlugin(this);
        flarePlugin = new FlarePlugin(this);
        limboPlugin = new LimboPlugin(this);
        combatPlugin = new CombatPlugin(this);
        tablistPlugin = new TablistPlugin(this);
        recyclePlugin = new RecyclePlugin(this);
        evolutionPlugin = new EvolutionPlugin(this);
        loadoutPlugin = new LoadoutPlugin(this);
        sidebarPlugin = new SidebarPlugin(this);
        airdropPlugin = new AirdropPlugin(this);
        casinoPlugin = new CasinoPlugin(this);
        signPlugin = new SignPlugin(this);
        shopPlugin = new ShopPlugin(this);
        registerLifecycleSteps();
    }

    @Override
    public void onEnable() {
        info("Enabling UniqueCode Rust...");
        safeRun("ManagerService.load", () -> managerService.load());
        runAll(enableSteps, NamedStep::run);

        info("UniqueCode Rust enabled.");
    }

    @Override
    public void onDisable() {
        info("Disabling UniqueCode Rust...");

        for (int i = disableSteps.size() - 1; i >= 0; i--) {
            disableSteps.get(i).run();
        }

        INSTANCE = null;
        info("UniqueCode Rust disabled.");
    }

    public void registerListener(Listener listener) {
        getServer().getPluginManager().registerEvents(listener, this);
    }

    public void registerCommand(String command, CommandExecutor executor) {
        var pluginCommand = getCommand(command);
        if (pluginCommand == null) {
            warn("Command '" + command + "' not found in plugin.yml — skipping executor registration.");
            return;
        }
        pluginCommand.setExecutor(executor);
    }
    private void registerLifecycleSteps() {
        addLifecycle("Database", databasePlugin::onEnable, databasePlugin::onDisable);

        addLifecycle("Time", timeException::onEnable, timeException::onDisable);
        addLifecycle("Packet", packetException::onEnable, packetException::onDisable);
        addLifecycle("Message", messagePlugin::onEnable, messagePlugin::onDisable);

        addLifecycle("Hologram", hologramException::onEnable, hologramException::onDisable);
        addLifecycle("Human", humanPlugin::onEnable, humanPlugin::onDisable);
        addLifecycle("Item", itemException::onEnable, itemException::onDisable);
        addLifecycle("NameTag", nameTagException::onEnable, nameTagException::onDisable);

        addLifecycle("Essential", essentialPlugin::onEnable, essentialPlugin::onDisable);
        addLifecycle("Crafting", craftingPlugin::onEnable, craftingPlugin::onDisable);
        addLifecycle("Drop", dropPlugin::onEnable, dropPlugin::onDisable);
        addLifecycle("Build", buildPlugin::onEnable, buildPlugin::onDisable);
        addLifecycle("Cupboard", cupboardPlugin::onEnable, cupboardPlugin::onDisable);
        addLifecycle("Enderchest", enderchestPlugin::onEnable, enderchestPlugin::onDisable);
        addLifecycle("Recive", recivePlugin::onEnable, recivePlugin::onDisable);
        addLifecycle("Disco", discoPlugin::onEnable, discoPlugin::onDisable);
        addLifecycle("Bossbar", bossbarPlugin::onEnable, bossbarPlugin::onDisable);
        addLifecycle("Codelock", codelockPlugin::onEnable, codelockPlugin::onDisable);
        addLifecycle("Rock", rockPlugin::onEnable, rockPlugin::onDisable);
        addLifecycle("Tree", treePlugin::onEnable, treePlugin::onDisable);
        addLifecycle("Schematic", schematicPlugin::onEnable, schematicPlugin::onDisable);
        addLifecycle("Cuboid", cuboidPlugin::onEnable, cuboidPlugin::onDisable);
        addLifecycle("Lootbox", lootboxPlugin::onEnable, lootboxPlugin::onDisable);
        addLifecycle("Flare", flarePlugin::onEnable, flarePlugin::onDisable);
        addLifecycle("Limbo", limboPlugin::onEnable, limboPlugin::onDisable);
        addLifecycle("Combat", combatPlugin::onEnable, combatPlugin::onDisable);
        addLifecycle("Tablist", tablistPlugin::onEnable, tablistPlugin::onDisable);
        addLifecycle("Recycle", recyclePlugin::onEnable, recyclePlugin::onDisable);
        addLifecycle("Evolution", evolutionPlugin::onEnable, evolutionPlugin::onDisable);
        addLifecycle("Loadout", loadoutPlugin::onEnable, loadoutPlugin::onDisable);
        addLifecycle("Sidebar", sidebarPlugin::onEnable, sidebarPlugin::onDisable);
        addLifecycle("Airdrop", airdropPlugin::onEnable, airdropPlugin::onDisable);
        addLifecycle("Casino", casinoPlugin::onEnable, casinoPlugin::onDisable);
        addLifecycle("Sign", signPlugin::onEnable, signPlugin::onDisable);
        addLifecycle("Shop", shopPlugin::onEnable, shopPlugin::onDisable);
    }

    private void addLifecycle(String name, Runnable enable, Runnable disable) {
        enableSteps.add(new NamedStep(name, enable, this::safeRun));
        disableSteps.add(new NamedStep(name, disable, this::safeRun));
    }

    private void runAll(List<NamedStep> steps, Consumer<NamedStep> runner) {
        for (NamedStep step : steps) runner.accept(step);
    }

    private void safeRun(String name, Runnable action) {
        try {
            action.run();
            info("Loaded: " + name);
        } catch (Throwable t) {
            error("Failed: " + name, t);
        }
    }

    private void info(String msg) { LOGGER.info(LOG_PREFIX + msg); }
    private void warn(String msg) { LOGGER.warning(LOG_PREFIX + msg); }
    private void error(String msg, Throwable t) { LOGGER.severe(LOG_PREFIX + msg + " | " + t.getMessage()); }
    private static final class NamedStep {
        private final String name;
        private final Runnable action;
        private final BiSafeRunner safeRunner;

        private NamedStep(String name, Runnable action, BiSafeRunner safeRunner) {
            this.name = name;
            this.action = action;
            this.safeRunner = safeRunner;
        }

        void run() { safeRunner.run(name, action); }
    }

    @FunctionalInterface
    private interface BiSafeRunner {
        void run(String name, Runnable action);
    }
}
