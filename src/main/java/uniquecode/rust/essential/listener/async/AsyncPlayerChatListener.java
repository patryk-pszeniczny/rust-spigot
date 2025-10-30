package uniquecode.rust.essential.listener.async;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import uniquecode.rust.Main;
import uniquecode.rust.api.manager.ManagerService;
import uniquecode.rust.api.message.MessageCache;
import uniquecode.rust.api.time.TimeAPI;
import uniquecode.rust.essential.structure.chat.ChatEnum;
import uniquecode.rust.essential.structure.chat.ChatOption;
import uniquecode.rust.essential.structure.guild.Guild;
import uniquecode.rust.essential.structure.mute.Mute;
import uniquecode.rust.essential.structure.user.User;
import uniquecode.rust.essential.structure.user.UserCache;
import uniquecode.rust.helper.MessageHelper;
import uniquecode.rust.util.RoundUtil;
import uniquecode.rust.util.TextUtil;
import uniquecode.rust.util.TimerUtil;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AsyncPlayerChatListener implements Listener {
    private final Main main;
    private final ManagerService managerService;
    private final UserCache userCache;
    private final Pattern linkPattern;
    private final MessageCache messageCache;
    private final LegacyComponentSerializer legacySerializer = LegacyComponentSerializer.legacyAmpersand();

    public AsyncPlayerChatListener(Main main, ManagerService managerService) {
        this.main = main;
        this.managerService = managerService;
        this.userCache = (UserCache) managerService.get("user-cache");
        this.messageCache = (MessageCache) managerService.get("message-cache");
        this.linkPattern = Pattern.compile("((?:(?:https?):\\/\\/)?(?:[-\\w_\\.]{2,}\\.[a-z]{2,4}.*?(?=[\\.\\?!,;:]?(?:["
                + ChatColor.COLOR_CHAR + " \\n]|$))))");
        this.main.registerListener(this);
    }

    @EventHandler
    private void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        User user = userCache.getUser(player.getEntityId());

        if (shouldCancelForCooldown(player, user, event)) return;
        if (shouldCancelForRepeatedMessage(player, user, event)) return;
        if (shouldCancelForChatRestrictions(player, event)) return;
        if (handleMutedPlayer(player, user, event)) return;

        processChatMessage(event, player, user);
    }

    private boolean shouldCancelForCooldown(Player player, User user, AsyncPlayerChatEvent event) {
        if (!player.hasPermission("uniquecode.admin") && user.getCooldown().isCooldown("async.chat.cooldown")) {
            event.setCancelled(true);
            messageCache.sendMessage(player, "playerchat.cooldown",
                    "&cMusisz poczekac &f{0} &csekund przed napisaniem kolejnej wiadomosci.",
                    RoundUtil.round((double) (user.getCooldown().getCooldown("async.chat.cooldown") - TimeAPI.INSTANCE.getTime()) / 1000, 3, 3));
            return true;
        }
        user.getCooldown().setCooldown("async.chat.cooldown", 1500L);
        return false;
    }

    private boolean shouldCancelForRepeatedMessage(Player player, User user, AsyncPlayerChatEvent event) {
        if (!player.hasPermission("playerchat.cooldown") && event.getMessage().equalsIgnoreCase(user.getLast_message())) {
            event.setCancelled(true);
            messageCache.sendMessage(player, "playerchat.similar", "&cNie mozesz wyslac takiej samej wiadomosci.");
            return true;
        }
        user.setLast_message(event.getMessage());
        return false;
    }

    private boolean shouldCancelForChatRestrictions(Player player, AsyncPlayerChatEvent event) {
        if (ChatOption.chatEnum == ChatEnum.ADMIN && !player.hasPermission("uniquecode.admin")) {
            event.setCancelled(true);
            messageCache.sendMessage(player, "async.chat_for_admin", "&cMusisz posiadac range &6ADMIN &caby pisac na tym kanale.");
            return true;
        }
        if (ChatOption.chatEnum == ChatEnum.PREMIUM && !player.hasPermission("uniquecode.premiumchat")) {
            event.setCancelled(true);
            messageCache.sendMessage(player, "async.chat_for_premium", "&cMusisz posiadac range &6PREMIUM &caby pisac na tym kanale.");
            return true;
        }
        return false;
    }

    private boolean handleMutedPlayer(Player player, User user, AsyncPlayerChatEvent event) {
        if (user.getMute() != null) {
            Mute mute = user.getMute();
            MessageHelper.sendMessage(player, "&c&lNie mozesz tego zrobic, jestes zablokowany.");
            MessageHelper.sendMessage(player, "&fPowód blokady: &7" + mute.getReason());
            MessageHelper.sendMessage(player, "&7Blokada nadana przez: &f" + mute.getAdmin());
            MessageHelper.sendMessage(player, "&2Pozostaly czas blokady&8: &a&l" + TimerUtil.getTimeChatHour(mute.getTime()));
            event.setCancelled(true);
            return true;
        }
        return false;
    }

    private void processChatMessage(AsyncPlayerChatEvent event, Player player, User user) {
        event.setCancelled(true);
        sendMessageToAppropriateChannel(event, player, user);
    }

    private void sendMessageToAppropriateChannel(AsyncPlayerChatEvent event, Player player, User user) {
        if (event.getMessage().startsWith("$") && user.getGuild() != null) {
            sendGuildMessage(event, player, user);
        } else {
            sendMessageToAllPlayers(event, player, user);
        }
    }

    private void sendGuildMessage(AsyncPlayerChatEvent event, Player player, User user) {
        Guild party = user.getGuild();
        Component messageComponent = buildChatComponent(user, player, event.getMessage(), true);
        String legacyMessage = legacySerializer.serialize(messageComponent);
        for (User member : party.getMembers()) {
            if (member.getPlayer() == null) continue;
            member.getPlayer().sendMessage(messageComponent);
        }
    }

    private void sendMessageToAllPlayers(AsyncPlayerChatEvent event, Player player, User user) {
        Component messageComponent = buildChatComponent(user, player, event.getMessage(), false);
        String legacyMessage = legacySerializer.serialize(messageComponent);
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.sendMessage(messageComponent);
        }
    }
    private Component buildChatComponent(User user, Player player, String message, boolean isPartyMessage) {
        String prefix = isPartyMessage ? TextUtil.color("&9[GILDIA] ") : "";
        String displayName = TextUtil.color("&7" + player.getDisplayName());
        //Component playerPrefixComponent = parseColoredPlayerPrefix(user.prefixChatAsync());
        Component baseComponent = Component.text(prefix)
                .append(Component.text(TextUtil.color(user.prefixChat())))
                .append(Component.text(displayName)
                        .hoverEvent(HoverEvent.showText(Component.text("/msg " + player.getDisplayName())))
                        .clickEvent(ClickEvent.suggestCommand("/msg " + player.getName())))
                .append(Component.text(TextUtil.color(" &8» &f")));

        Component finalComponent = baseComponent;
        Matcher matcher = linkPattern.matcher(message);
        int lastIndex = 0;
        while (matcher.find()) {
            String beforeLink = message.substring(lastIndex, matcher.start());
            finalComponent = finalComponent.append(Component.text(TextUtil.color(beforeLink)));
            String link = matcher.group();
            if (player.hasPermission("playchest.links")) {
                Component linkComponent = Component.text(TextUtil.color("&8[&a&nLINK&r&8]&f"))
                        .hoverEvent(HoverEvent.showText(Component.text(TextUtil.color("\n&fLink&8: &a" + link +
                                "\n\n" +
                                "&fKliknij, Aby otworzyć link.\n\n" +
                                "&4&lUWAGA! &cAdministracja nie odpowiada za scam linki.\n"))))
                        .clickEvent(ClickEvent.openUrl(link.startsWith("http") ? link : "https://" + link));
                finalComponent = finalComponent.append(linkComponent);
            } else {
                Component linkComponent = Component.text(TextUtil.color("&8[&c&nLINK&r&8]&f"))
                        .hoverEvent(HoverEvent.showText(Component.text(TextUtil.color("\n&fAby &c&nlink&r&f dzialal na chacie\n" +
                                " &ftrzeba posiadac range &6PREMIUM&f.\n"))))
                        .clickEvent(ClickEvent.openUrl(link.startsWith("http") ? link : "https://" + link));
                finalComponent = finalComponent.append(linkComponent);
            }
            lastIndex = matcher.end();
        }
        String remaining = message.substring(lastIndex);
        finalComponent = finalComponent.append(Component.text(TextUtil.color(remaining)));

        return finalComponent;
    }
    public static Component parseColoredPlayerPrefix(String input) {
        Pattern pattern = Pattern.compile("(&#[A-Fa-f0-9]{6})((?:&[0-9a-z])*)([^&]+)");
        Matcher matcher = pattern.matcher(input);
        Component result = Component.empty();

        while (matcher.find()) {
            String hexCode = matcher.group(1); // np. "&#480C8D"
            String formatCodes = matcher.group(2); // np. "&l" (może zawierać więcej kodów)
            String text = matcher.group(3); // tekst, np. "DE"

            String hex = hexCode.substring(1);
            Component segment = Component.text(text)
                    .color(TextColor.fromHexString(hex));
            if (formatCodes.contains("&l")) {
                segment = segment.decoration(TextDecoration.BOLD, true);
            }

            result = result.append(segment);
        }

        return result;
    }
}
