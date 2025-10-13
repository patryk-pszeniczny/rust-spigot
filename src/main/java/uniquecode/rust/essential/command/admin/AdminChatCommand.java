package uniquecode.rust.essential.command.admin;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import uniquecode.rust.Main;
import uniquecode.rust.api.manager.ManagerService;
import uniquecode.rust.api.message.MessageCache;

public class AdminChatCommand implements CommandExecutor {
    Main main;
    ManagerService managerService;
    MessageCache messageCache;
    String permission;
    public AdminChatCommand(Main main, ManagerService managerService) {
        this.main=main;
        this.managerService=managerService;
        this.messageCache=(MessageCache)managerService.get("message-cache");
        this.permission=messageCache.message("command.adminchat.permission", "command.uniquecode.rust.adminchat");
        this.main.registerCommand("adminchat", this);
    }
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        Player player=(Player)sender;
        if(!sender.hasPermission(permission)){
            messageCache.sendMessage(player, "command.adminchat.permission.message", "brak uprawnien do tej komendy");
            return false;
        }
        return true;
    }
}
