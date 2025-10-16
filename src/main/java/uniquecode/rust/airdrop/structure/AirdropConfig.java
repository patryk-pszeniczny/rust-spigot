package uniquecode.rust.airdrop.structure;

import lombok.Getter;
import uniquecode.rust.flare.structure.FlareEnumType;

import java.util.concurrent.ThreadLocalRandom;

@Getter
public enum AirdropConfig {
    WOOD(FlareEnumType.WOOD_FLARE, 52.42, "&ez Drewnem i Kamieniem"), //2.99zł
    SILVER(FlareEnumType.SILVER_FLARE, 26.16, "&7z Rozbudową"), //5.99zł
    GOLD(FlareEnumType.GOLD_FLARE, 10.45, "&6z Zdrowiem"), //14.99zł
    DIAMOND(FlareEnumType.DIAMOND_FLARE, 6.27, "&bdo Walki"),//24.99zł
    EMERALD(FlareEnumType.EMERALD_FLARE, 3.13, "&ado Rajdu"),//49.99zł
    NETHERITE(FlareEnumType.NETHERITE_FLARE, 1.57, "&c&lSPECJALNY"); //99.99zł
    private final FlareEnumType flareEnumType;
    private final double chance;
    private final String color;
    AirdropConfig(FlareEnumType flareEnumType, double chance, String color) {
        this.flareEnumType = flareEnumType;
        this.chance = chance;
        this.color = color;
    }
    public static AirdropConfig randomDrop() {
        int random = ThreadLocalRandom.current().nextInt(1, 10000);
        return getDrop(random);
    }
    public static AirdropConfig getDrop(int index) {
        int startOn = 1;
        for(AirdropConfig airdropConfig:AirdropConfig.values()){
            int endOn = (int) (startOn + (airdropConfig.getChance() * 100))-1;
            if(index >= startOn && index <= endOn) {
                return airdropConfig;
            }
            startOn += (int) (airdropConfig.getChance() * 100);
        }
        return null;
    }
}
