package uniquecode.rust.api.human.module.type;

import com.mojang.datafixers.util.Pair;
import net.minecraft.network.protocol.game.ClientboundMoveEntityPacket;
import net.minecraft.network.protocol.game.ClientboundRotateHeadPacket;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.server.network.ServerPlayerConnection;
import net.minecraft.world.item.ItemStack;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.craftbukkit.v1_18_R2.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import uniquecode.rust.Main;
import uniquecode.rust.api.human.module.HumanModule;
import uniquecode.rust.api.human.module.ModuleCache;
import uniquecode.rust.api.human.structure.Human;
import uniquecode.rust.api.human.structure.HumanCache;
import uniquecode.rust.api.manager.ManagerService;
import uniquecode.rust.api.message.MessageCache;
import uniquecode.rust.api.packet.type.PacketDamage;
import uniquecode.rust.api.time.TimeAPI;
import uniquecode.rust.combat.structure.sleeping.SleepingHuman;
import uniquecode.rust.combat.structure.sleeping.SleepingManager;
import uniquecode.rust.combat.structure.statistic.StatisticEnum;
import uniquecode.rust.essential.structure.user.User;
import uniquecode.rust.essential.structure.user.UserCache;
import uniquecode.rust.essential.structure.user.UserState;
import uniquecode.rust.helper.MathHelper;
import uniquecode.rust.util.ItemUtil;
import uniquecode.rust.util.LevelUtil;
import uniquecode.rust.util.RoundUtil;

import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

public class AttackModule implements HumanModule {
    private final Main main;
    private final ManagerService managerService;
    private final ModuleCache moduleCache;
    private final UserCache userCache;
    private final MessageCache messageCache;
    private final HumanCache humanCache;
    private final SleepingManager sleepingManager;
    public AttackModule(Main main){
        this.main=main;
        this.managerService=main.getManagerService();
        this.moduleCache= (ModuleCache) this.managerService.get("module-cache");
        this.humanCache = (HumanCache) managerService.get("human-cache");
        this.userCache = (UserCache) managerService.get("user-cache");
        this.sleepingManager = (SleepingManager) this.managerService.get("sleeping-manager");
        this.messageCache= (MessageCache) this.managerService.get("message-cache");
        this.moduleCache.register("attack", this);
    }
    @Override
    public void execute(Player player, Human human, ServerboundInteractPacket.ActionType type, String argument) {
        if(type==ServerboundInteractPacket.ActionType.INTERACT){
            return;
        }
        if(argument.equals("sleeping_human")){
            human.getPlayerList().stream()
                    .filter(Objects::nonNull)
                    .forEach(playerStream ->{
                        playerStream.playSound(playerStream.getLocation(), Sound.ENTITY_PLAYER_ATTACK_STRONG, 1f, 1f);
                        ServerPlayerConnection playerConnection = ((CraftPlayer) playerStream).getHandle().connection;
                        PacketDamage.send(playerConnection, human.getEntityPlayer());
                    });
            double attackDamage = ItemUtil.getDamage(player.getInventory().getItemInMainHand());
            double armorDamage = ItemUtil.armorCalculator(human.getEquipmentList().stream()
                    .map(Pair::getSecond)
                    .filter(Objects::nonNull)
                    .map(ItemStack::getBukkitStack)
                    .toList());
            double health = human.getEntityPlayer().getHealth();
            double percent = 1-(armorDamage/21);
            double damage = attackDamage * percent;
            if (human.getAttackCooldown() > TimeAPI.time) {
                long math = human.getAttackCooldown() - TimeAPI.time;
                double cooldown =  1- ((double) math / 1000);
                damage = damage * cooldown;
            }
            human.setAttackCooldown(TimeAPI.time + 1000L);
            if(health - damage > 0) {
                human.getEntityPlayer().setHealth((float) (health - damage));
                human.getHologramAPI().update(1,
                        RoundUtil.round(human.getEntityPlayer().getHealth(), 2, 2)+" &c❤");
                this.messageCache.sendActionbar(player, "attack.module.sleeping", "&a&lZadałeś &c&l{0} &c❤ obrażeń", RoundUtil.round(damage, 2, 2));
                return;
            }
            User victim = userCache.getUserBaseId(human.getId());
            User attacker = userCache.getUser(player.getEntityId());

            this.humanCache.removeHuman(human);

            if(victim==null || attacker==null){
                Main.LOGGER.warning("Victim or attacker is null!");
                return;
            }
            SleepingHuman sleepingHuman = sleepingManager.getSleepingHuman(victim);
            if(sleepingHuman==null){
                Main.LOGGER.warning("Sleeping human is null!");
                return;
            }
            handlePlayerDeath(human.getLocation(), victim, attacker);
            sleepingHuman.drop(human.getLocation());

            sleepingManager.removeSleepingHuman(victim, sleepingHuman);
            this.sleepingManager.deleteHuman(victim);
            return;
        }
        human.setTarget(player);
        human.getPlayerList().stream()
                .filter(Objects::nonNull)
                .forEach(playerStream ->{
                    playerStream.playSound(playerStream.getLocation(), Sound.ENTITY_PLAYER_ATTACK_STRONG, 1f, 1f);
                    ServerPlayerConnection playerConnection = ((CraftPlayer) playerStream).getHandle().connection;

                    Vector difference = player.getLocation().subtract(human.getLocation()).toVector().normalize();
                    byte angleYaw = (byte) MathHelper.d((float) Math.toDegrees(Math.atan2(-difference.getX(), difference.getZ())) * 256.0F / 360.0F);
                    byte anglePitch = (byte) MathHelper.d((float) Math.toDegrees(Math.atan2(-difference.getY(), Math.sqrt(difference.getX() * difference.getX() + difference.getZ() * difference.getZ()))) * 256.0F / 360.0F);

                    human.setPacketPlayOutEntityLook(new ClientboundMoveEntityPacket.Rot(human.getEntityPlayer().getId(), angleYaw, anglePitch, false));
                    human.setPacketPlayOutEntityHeadRotation(new ClientboundRotateHeadPacket(human.getEntityPlayer(), angleYaw));
                    human.sendPlayOutEntityHeadRotation();
                    PacketDamage.send(playerConnection, human.getEntityPlayer());
                });
    }
    private void handlePlayerDeath(Location location, User user, User combat){
        if(user.getUserState().isDeath()){
            Main.LOGGER.warning(user.getName() + " died while already in 'DEAD' mode!");
            return;
        }
        user.setUserState(UserState.DEATH);
        int POINT_COMBAT_PLAYER = user.getStatisticPlayer().getPoint();
        int POINT_PLUS = (int) (POINT_COMBAT_PLAYER * 0.10);
        int POINT_MINUS = (int) (POINT_COMBAT_PLAYER * 0.07);
        if(combat.getStatisticPlayer().getPoint() < user.getStatisticPlayer().getPoint()){
            double mathM = POINT_MINUS * 1.15;
            double mathP = POINT_PLUS * 0.95;
            POINT_MINUS = (int) mathM;
            POINT_PLUS = (int) mathP;
        }
        combat.getStatisticPlayer().merge(StatisticEnum.POINT, POINT_PLUS);
        user.getStatisticPlayer().merge(StatisticEnum.POINT, POINT_MINUS);

        Player playerCombat = combat.getPlayer();
        if(playerCombat != null){
            playerCombat.getWorld().playSound(playerCombat.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f);
            int experience_random = ThreadLocalRandom.current().nextInt(50,
                    LevelUtil.getMax(50, playerCombat, false));
            messageCache.sendTitle(
                    playerCombat,
                    "player.death.killer.title",
                    "&cEliminacja! &f{0}",
                    "player.death.killer.subtitle",
                    "&a+{1}&F pkt | &a+{2} &eExp",
                    1, 30, 1,
                    user.getName(), POINT_PLUS, experience_random
            );
            LevelUtil.play(playerCombat, combat, experience_random);
        }
        messageCache.broadcast("player.death.sleeping.by.player",
                "&f☠ &7{0} ({1}) &f{2} &7{3} &bŚpioch &7(-{4})",
                combat.getName(),
                POINT_PLUS+"",
                "&fzabił",
                user.getName(),
                POINT_MINUS+"");
        combat.getStatisticPlayer().merge(StatisticEnum.KILL, 1);
        strikeLightning(location);
        location.getWorld().playSound(location, Sound.ENTITY_WITHER_SPAWN, 0.3f, 0.3f);

    }
    public void strikeLightning(Location location) {
        location.getWorld().strikeLightningEffect(location);
        if (location.getBlock().getType() == Material.FIRE) {
            location.getBlock().setType(Material.AIR);
        }
    }
}
