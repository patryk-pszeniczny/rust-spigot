package uniquecode.rust.building.structure;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.block.Block;
import uniquecode.rust.database.structure.Database;
import uniquecode.rust.database.wrapper.DatabaseDelete;
import uniquecode.rust.database.wrapper.DatabaseInsert;
import uniquecode.rust.util.BlockUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
public abstract class BuildStructure {
    private UUID uuid;
    private Block mainBlock;
    private BuildEnum buildEnum;
    private BuildTypeEnum buildTypeEnum;
    private final List<Block> breakableBlocks = new ArrayList<>();
    private final List<Block> woodBlocks = new ArrayList<>();
    private Integer health;
    private Integer maxHealth;
    private String attribute;
    private String owner;
    public abstract void destroy(boolean gravity);
    public BuildStructure(Database database, String owner, Block mainBlock, BuildEnum buildEnum, BuildTypeEnum buildTypeEnum, int health) {
        this.uuid = UUID.randomUUID();
        this.mainBlock = mainBlock;
        this.buildEnum = buildEnum;
        this.buildTypeEnum = buildTypeEnum;
        this.health = health;
        this.maxHealth = health;
        this.attribute = "";
        this.owner = owner;
        this.insertStructureToBase(database);
    }
    public BuildStructure(Database database, String owner, Block mainBlock, BuildEnum buildEnum, BuildTypeEnum buildTypeEnum, String attribute, int health) {
        this.uuid = UUID.randomUUID();
        this.mainBlock = mainBlock;
        this.buildEnum = buildEnum;
        this.buildTypeEnum = buildTypeEnum;
        this.health = health;
        this.maxHealth = health;
        this.attribute = attribute;
        this.owner = owner;
        this.insertStructureToBase(database);
    }
    public BuildStructure(String owner,String uuid, Block mainBlock, BuildEnum buildEnum, BuildTypeEnum buildTypeEnum, int health) {
        this.uuid = UUID.fromString(uuid);
        this.mainBlock = mainBlock;
        this.buildEnum = buildEnum;
        this.buildTypeEnum = buildTypeEnum;
        this.health = health;
        this.maxHealth = health;
        this.attribute = "";
        this.owner = owner;
    }
    public BuildStructure(String owner, String uuid, Block mainBlock, BuildEnum buildEnum, BuildTypeEnum buildTypeEnum, String attribute, int health) {
        this.uuid = UUID.fromString(uuid);
        this.mainBlock = mainBlock;
        this.buildEnum = buildEnum;
        this.buildTypeEnum = buildTypeEnum;
        this.health = health;
        this.maxHealth = health;
        this.attribute = attribute;
        this.owner = owner;
    }
    public void decrementHealth(Integer damage) {
        this.health -= damage;
    }
    public void insertStructureToBase(Database database){
        new DatabaseInsert(database, "rust_structure")
                .addArguments("uuid", "main_block", "build_enum", "build_type_enum", "attribute", "owner", "health")
                .addValues(this.uuid.toString(), BlockUtil.blockToString(this.mainBlock),
                        this.buildEnum.name(), this.buildTypeEnum.name(),
                        this.attribute, this.owner, this.health.toString())
                .execute(DatabaseInsert.InsertType.ASYNC);
    }
    public void removeStructureFromBase(Database database){
        new DatabaseDelete(database, "rust_structure")
                .setColumn("uuid")
                .setValue(this.uuid.toString())
                .execute(DatabaseDelete.InsertType.ASYNC);
    }
    public void addBreakableBlockStructure(Block block){
        this.breakableBlocks.add(block);
    }
    public void addWoodBlockStructure(Block block){
        this.woodBlocks.add(block);
    }
}
