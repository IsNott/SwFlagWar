package org.nott.model;

import lombok.Data;

import java.util.List;

/**
 * @author Nott
 * @date 2024-9-9
 */
@Data
public class War {

    private String UUID;

    private String name;

    private String world;

    private List<Location> locations;

    private String start;

    private String end;

    private List<Integer> days;

    private List<Integer> types;

    private Reward rewards;

    public War() {
    }

    public War(String UUID, String name, String world, List<Location> locations, String start, String end, List<Integer> days, List<Integer> types, Reward rewards) {
        this.UUID = UUID;
        this.name = name;
        this.world = world;
        this.locations = locations;
        this.start = start;
        this.end = end;
        this.days = days;
        this.types = types;
        this.rewards = rewards;
    }
}
