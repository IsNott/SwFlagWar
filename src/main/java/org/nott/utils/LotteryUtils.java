package org.nott.utils;

import org.apache.commons.lang3.RandomUtils;
import org.nott.model.BaseLotteryAward;

import java.util.List;

/**
 * @author Nott
 * @date 2024-9-13
 */
public class LotteryUtils {

    /**
     * 抽奖方法
     *
     * @param awardList 奖品列表，这些是备选的奖品（一定有库存的）
     * @param <T>       具体奖品类型
     * @return 返回一个抽中的奖品
     */
    public static <T extends BaseLotteryAward> T draw(List<T> awardList) {
        if (SwUtil.isEmpty(awardList)) {
            return null;
        }

        // 获取总概率，当奖品总概率正好为1时，奖品的 probability 就是真实的概率，否则会按新的比例作为概率
        double sumProbability = awardList.stream()
                .map(BaseLotteryAward::getProbability)
                .reduce(0.0, Double::sum);

        // 一共会尝试 awardList.size() 次，确保能返回一个奖品
        for (T t : awardList) {

            // 使用随机值，左闭右开（包含0，不包含1）
            if (t.getProbability() > RandomUtils.nextDouble(0.0, 1.0) * sumProbability) {
                return t;
            }
            sumProbability = sumProbability - t.getProbability();
        }

        return null;
    }

}
