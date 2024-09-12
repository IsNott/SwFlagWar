package org.nott.model;

import lombok.Data;
import org.nott.global.Period;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author Nott
 * @date 2024-9-9
 */
@Data
public class Reward {

    private List<Integer> type;

    private String command;

    private String material;

    private Integer levelUp;

    private Integer amount;

    private String effect;

    private String period;

    private Integer periodVal;

    private boolean mustStandOn;

    public Reward() {
    }

    public static Reward defVal(){
        Reward reward = new Reward();
        reward.setType(Collections.singletonList(1));
        reward.setLevelUp(0);
        reward.setAmount(0);
        reward.setEffect("");
        reward.setCommand("");
        reward.setPeriodVal(1);
        reward.setMustStandOn(true);
        reward.setPeriod(Period.DAY.name());
        return reward;
    }

    public Reward(List<Integer> type, String command, String material, Integer levelUp, Integer amount, String effect, String period, Integer periodVal, boolean mustStandOn) {
        this.type = type;
        this.command = command;
        this.material = material;
        this.levelUp = levelUp;
        this.amount = amount;
        this.effect = effect;
        this.period = period;
        this.periodVal = periodVal;
        this.mustStandOn = mustStandOn;
    }
}
