package me.drownek.datagatherer;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class DataGathererManager {
    public static final Map<Player, DataGatherer> playersInDataGatherer = new HashMap<>();
}
