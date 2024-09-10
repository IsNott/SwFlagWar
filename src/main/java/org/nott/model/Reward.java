package org.nott.model;

import lombok.Data;

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
