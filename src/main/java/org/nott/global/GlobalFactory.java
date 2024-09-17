package org.nott.global;



import org.bukkit.Material;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author Nott
 * @date 2024-9-9
 */
public class GlobalFactory {

    public static List<String> RUNNING_GAME_LOC = Collections.synchronizedList(new ArrayList<String>());

    public static final String PLUGIN_NAME = "SwFlagWar";

    public static final String MESSAGE_YML = "message.yml";

    public static final String YML_PREFIX = ".yml";

    public static final String FW_COMMAND = "flagwar";
    public static final String SW_COMMAND = "SimepleWorld";

    public static final String OFFER_COMMAND = "offer";

    public static final String COMMON_MSG_SUFFIX = "common";

    public static final String WAR_MESSAGE_SUFFIX = "war";

    public static final String WAR_BASE_DIR = "wars" + File.separator;

    public static final String EXAMPLE_FILE = "example_war.yml";

    public static final List<Material> NOT_DROPS_EQUIP = Arrays.asList(
            Material.DIAMOND_AXE, Material.DIAMOND_BOOTS, Material.DIAMOND_HELMET,
            Material.DIAMOND_CHESTPLATE, Material.DIAMOND_LEGGINGS, Material.DIAMOND_HOE,
            Material.DIAMOND_SWORD, Material.DIAMOND_PICKAXE, Material.DIAMOND_SHOVEL,
            Material.NETHERITE_AXE, Material.NETHERITE_AXE, Material.NETHERITE_BOOTS, Material.NETHERITE_HELMET,
            Material.NETHERITE_CHESTPLATE, Material.NETHERITE_LEGGINGS, Material.NETHERITE_HOE,
            Material.NETHERITE_SWORD, Material.NETHERITE_PICKAXE, Material.NETHERITE_SHOVEL,
            Material.BOW, Material.TRIDENT, Material.SHIELD, Material.CROSSBOW
    );

}
