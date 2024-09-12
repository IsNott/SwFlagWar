package org.nott.global;


import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.SimpleFormatter;

/**
 * @author Nott
 * @date 2024-9-9
 */
public class GlobalFactory {

    public static final String PLUGIN_NAME = "SwFlagWar";

    public static final String MESSAGE_YML = "message.yml";

    public static final String YML_PREFIX = ".yml";

    public static final String FW_COMMAND = "flagwar";

    public static final String COMMON_MSG_SUFFIX = "common";

    public static final String WAR_MESSAGE_SUFFIX = "war";

    public static final String WAR_BASE_DIR = "wars" + File.separator;

    public static List<String> RUNNING_GAME_LOC = Collections.synchronizedList(new ArrayList<String>());
}
