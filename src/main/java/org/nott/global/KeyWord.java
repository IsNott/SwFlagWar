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
    }

    public interface COMMON{
        public static final String HELP = "help";
        public static final String NEXT = "next";
        public static final String OP = "op";
        public static final String PLAYER = "player";
        public static final String RELOAD = "reload";
    }

    public interface PERMISSION{
        public static final String WAR_OP_PERM = "flagwar.op";
        public static final String WAR_PLAYER_PERM = "flagwar.player";
    }
}
