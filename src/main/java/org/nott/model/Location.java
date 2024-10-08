package org.nott.model;

import lombok.Data;
import org.nott.global.KeyWord;

/**
 * @author Nott
 * @date 2024-9-9
 */
@Data
public class Location {

    private String x;

    private String y;

    private String z;

    public Location(String x, String y, String z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public String toString() {
        return " [" + x + KeyWord.COMMON.WHITER_SPACE + y + KeyWord.COMMON.WHITER_SPACE + z + " ]";
    }
}
