package org.nott.model;

import lombok.Data;
import org.nott.model.Location;

/**
 * @author Nott
 * @date 2024-9-13
 */
@Data
public class Region {

    private Location x1;

    private Location x2;

    public boolean isIn(Location currentLocation){

    }
}
