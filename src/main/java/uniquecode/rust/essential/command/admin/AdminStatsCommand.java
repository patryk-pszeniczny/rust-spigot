package uniquecode.rust.essential.command.admin;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import uniquecode.rust.Main;
import uniquecode.rust.api.manager.ManagerService;
import uniquecode.rust.api.message.MessageCache;
import uniquecode.rust.essential.structure.user.User;
import uniquecode.rust.essential.structure.user.UserCache;

public class AdminStatsCommand implements CommandExecutor {
    private final Main main;
    private final ManagerService managerService;
    private final UserCache userCache;
    private final MessageCache messageCache;
    private final String permission;
    public AdminStatsCommand(Main main, ManagerService managerService) {
        this.main=main;
        this.managerService = managerService;
        this.userCache=(UserCache)managerService.get("user-cache");
        this.messageCache=(MessageCache)managerService.get("message-cache");
        this.permission=messageCache.message("command.adminstats.permission", "command.adminstats.permission");
        this.main.registerCommand("adminstats", this);
    }
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (!sender.hasPermission(permission)) {
            messageCache.sendMessage(sender, "command.adminstats.permission.message", "brak uprawnien do tej komendy");
            return false;
        }
        if (args.length < 3) {
            messageCache.sendMessage(sender, "command.adminstats.use", "adminstats (nick) (type) (integer)");
            return false;
        }
        User user = userCache.getUser(args[0]);
        if (user == null) {
            messageCache.sendMessage(sender, "command.adminstats.wrong.player", "nie ma takiego gracza");
            return false;
        }
        int num;
        try {
            num = Integer.parseInt(args[2]);
        } catch (Exception exception) {
            messageCache.sendMessage(sender, "command.adminstats.wrong.num", "wartość musi być liczbą");
            return false;
        }
        if (num < 1) {
            messageCache.sendMessage(sender, "command.adminstats.wrong.num", "wartość musi być większa od 0");
            return false;
        }
        messageCache.sendMessage(sender, "command.adminstats.use", "adminstats (nick) (type) (integer)");
        return false;
    }
}
