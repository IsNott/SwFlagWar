package org.nott.global;

/**
 * @author Nott
 * @date 2024-9-11
 */
public interface KeyWord {

    public interface WAR {
        public static final String LOC = "loc";
        public static final String REMOVE = "move";
        public static final String RESET = "reset";
        public static final String ADD = "add";
        public static final String CREATE = "create";
        public static final String LIST = "list";
        public static final String LOCATIONS = "locations";
        public static final String NAME = "name";
        public static final String UUID = "UUID";
        public static final String WORLD = "world";
        public static final String START = "start";
        public static final String END = "end";
        public static final String DAYS = "days";
        public static final String TYPES = "types";
        public static final String REWARDS = "rewards";
        public static final String REWARDS_TYPE = "rewards.type";
        public static final String REWARDS_COMMAND  = "rewards.command";
        public static final String REWARDS_MATERIAL  = "rewards.material";
        public static final String REWARDS_LEVEL_UP  = "rewards.level-up";
        public static final String REWARDS_AMOUNT  = "rewards.amount";
        public static final String REWARDS_EFFECT  = "rewards.effect";
        public static final String REWARDS_PERIOD = "rewards.period";
        public static final String REWARDS_PERIOD_VAL  = "rewards.period-val";
        public static final String REWARDS_MUST_STAND_ON  = "rewards.must-stand-on";
        public static final String FW_HELP = "/fw help";
    }

    public interface CONFIG {
        public static final String WAR_OPEN_TIP = "war.war_open_tip";
        public static final String FLAG_ENABLE = "flag_war.enable";
        public static final String OFFER_ENABLE = "offer.enable";
        public static final String DROP_ENABLE = "drop.enable";
        public static final String DROP_INVENTORY = "death_drop.inventory";
        public static final String DROP_HEAD = "death_drop.head";
        public static final String DROP_HEAD_PROB = "drop.head.probability";
        public static final String DROP_STEAL_PROB = "drop.steal.probability";
        public static final String DROP_STEAL_MAX = "drop.steal.max";
        public static final String WAR_STARTING_TIP = "war.war_starting_tip";
        public static final String REG_OFFER = "registry.offer_success";
        public static final String REG_DEATH = "registry.death_success";
        public static final String REG_FLAG = "registry.flag_success";
    }

    public interface COMMON{
        public static final String HELP = "help";
        public static final String NEXT = "next";
        public static final String UNKNOWN_COMMAND = "unknown_command";
        public static final String OP = "op";
        public static final String PLAYER = "player";
        public static final String RELOAD = "reload";
        public static final String WHITER_SPACE = " ";
        public static final String COMMA = ",";
        public static final String COLON = ":";
    }

    public interface PERMISSION{
        public static final String WAR_OP_PERM = "flagwar.op";
        public static final String WAR_PLAYER_PERM = "flagwar.player";
    }
}
