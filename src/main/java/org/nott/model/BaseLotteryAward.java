package org.nott.model;

import lombok.Data;

/**
 * @author Nott
 * @date 2024-9-13
 */
@Data
public abstract class BaseLotteryAward {

    private Integer amount;

    private String loreName;

    private double probability;

    private Integer stock;
}
