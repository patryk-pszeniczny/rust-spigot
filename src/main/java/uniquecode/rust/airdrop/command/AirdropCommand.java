package uniquecode.rust.airdrop.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import uniquecode.rust.Main;
import uniquecode.rust.airdrop.structure.AirdropManager;
import uniquecode.rust.api.manager.ManagerService;

public class AirdropCommand implements CommandExecutor {
    private final Main main;
    private final ManagerService managerService;
    private final AirdropManager airdropManager;
    public AirdropCommand(Main main, ManagerService managerService) {
        this.main = main;
        this.managerService = managerService;
        this.airdropManager = (AirdropManager) managerService.get("airdrop-manager");
        this.main.registerCommand("airdrop", this);
    }
    @Override
    public boolean onCommand(@NotNull CommandSender sender,
                             @NotNull Command command,
                             @NotNull String s,
                             @NotNull String[] strings) {
        Player player = (Player) sender;
        this.airdropManager.openInventory(player);
        return false;
    }
}
