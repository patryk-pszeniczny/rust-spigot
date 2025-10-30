package uniquecode.rust.shop.structure.bazaar;

import uniquecode.rust.Main;
import uniquecode.rust.api.item.Item;
import uniquecode.rust.api.item.ItemCache;
import uniquecode.rust.api.manager.Manager;
import uniquecode.rust.api.manager.ManagerService;
import uniquecode.rust.database.structure.Database;
import uniquecode.rust.util.ParserUtil;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BazaarCache implements Manager {
    private final Main main;
    private final ManagerService managerService;
    private final Map<BazaarType, List<BazaarProductMapper>> cache = new HashMap<>();
    private Database database;
    private ItemCache itemCache;
    public BazaarCache(Main main, ManagerService managerService){
        this.main = main;
        this.managerService = managerService;
        this.managerService.register("bazaar-cache", this);

    }
    @Override
    public void setupCache() {
        this.database = (Database) this.managerService.get("database");
        this.itemCache = (ItemCache) this.managerService.get("item-cache");
        this.load();

    }
    public void load(){
        try(ResultSet resultSet = this.database.select("rust_bazaar")) {
            while (resultSet.next()) {
                BazaarType bazaarType = BazaarType.valueOf(resultSet.getString("category"));
                String displayName = resultSet.getString("displayName");
                List<String> attributes = ParserUtil.getListValue(resultSet.getString("attribute"));
                BazaarProductMapper bazaarProductMapper = new BazaarProductMapper(bazaarType, displayName);
                for(String attribute : attributes){
                    Map<String, String> configuration = ParserUtil.getMap(attribute);
                    Item item = ParserUtil.getMapValue(configuration, "item", this.itemCache::getItem);
                    double buyPrice = ParserUtil.getMapValue(configuration, "buy", Double::parseDouble);
                    double sellPrice = ParserUtil.getMapValue(configuration, "sell", Double::parseDouble);
                    BazaarProduct bazaarProduct = new BazaarProduct(item, buyPrice, sellPrice);
                    this.addProduct(bazaarType, bazaarProductMapper, bazaarProduct);
                }

            }
        }catch (SQLException e){
            e.printStackTrace();
        }
    }
    public List<BazaarProductMapper> getProducts(BazaarType bazaarType){
        return this.cache.getOrDefault(bazaarType, new ArrayList<>());
    }
    public void addProduct(BazaarType bazaarType, BazaarProductMapper bazaarProductMapper, BazaarProduct bazaarProduct){
        if(!this.cache.containsKey(bazaarType)){
            this.cache.put(bazaarType, new ArrayList<>());
        }
        List<BazaarProductMapper> mappers = this.cache.get(bazaarType);
        if(!mappers.contains(bazaarProductMapper)){
            mappers.add(bazaarProductMapper);
        }
        bazaarProductMapper.addProduct(bazaarProduct);

    }
}
