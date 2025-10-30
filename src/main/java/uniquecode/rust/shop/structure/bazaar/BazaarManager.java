package uniquecode.rust.shop.structure.bazaar;

import org.bukkit.entity.Player;
import uniquecode.rust.Main;
import uniquecode.rust.api.manager.Manager;
import uniquecode.rust.api.manager.ManagerService;
import uniquecode.rust.shop.structure.holder.BazaarMenuHolder;

import java.util.HashMap;
import java.util.Map;

public class BazaarManager implements Manager {
    private final Main main;
    private final ManagerService managerService;
    private final Map<BazaarType, BazaarMenuHolder> bazaars = new HashMap<>();
    public BazaarManager(Main main, ManagerService managerService){
        this.main = main;
        this.managerService = managerService;
        this.managerService.register("bazaar-manager", this);

    }
    @Override
    public void setupCache() {

        this.loadHolder();
    }
    public void loadHolder(){
        for(BazaarType type : BazaarType.values()){
            this.bazaars.put(type, new BazaarMenuHolder(this.main, this.managerService, type));
        }
    }
    public void openBazaar(BazaarType type, Player player){
        if(this.bazaars.containsKey(type)){
            player.openInventory(this.bazaars.get(type).getInventory());
        }
    }
}
