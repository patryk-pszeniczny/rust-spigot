package uniquecode.rust.airdrop;

import uniquecode.rust.InitializerClass;
import uniquecode.rust.Main;
import uniquecode.rust.airdrop.command.AirdropCommand;
import uniquecode.rust.api.manager.ManagerService;

public class AirdropPlugin implements InitializerClass {
    private final Main main;
    private final ManagerService managerService;
    public AirdropPlugin(Main main) {
        this.main = main;
        this.managerService = main.getManagerService();
    }

    @Override
    public void onLoad() {

    }

    @Override
    public void onEnable() {
        this.managerService.get("airdrop-manager").setupCache();
        this.managerService.get("airdrop-thread").setupCache();
        new AirdropCommand(this.main, this.managerService);
    }

    @Override
    public void onDisable() {

    }
}
