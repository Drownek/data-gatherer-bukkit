package pl.drownek.util.data.step.impl;

import me.drownek.util.TextUtil;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.plugin.Plugin;
import pl.drownek.util.data.DataGatherer;
import pl.drownek.util.data.step.Step;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class ClickedBlockLocationStep extends Step<Location> {

    public ClickedBlockLocationStep(String info, Consumer<Location> consumer) {
        super(info, consumer);
    }

    @Override
    public Listener handle(Plugin plugin, DataGatherer dataGatherer, Player targetPlayer, BiConsumer<Listener, Location> callback) {
        return new Listener() {
            @EventHandler
            public void handle(PlayerInteractEvent event) {
                if (!event.getPlayer().equals(targetPlayer) || event.getHand() == null || !event.getHand().equals(EquipmentSlot.HAND) || event.getClickedBlock() == null) {
                    return;
                }

                Location location = event.getClickedBlock().getLocation();
                callback.accept(this, location);
            }
        };
    }

    @Override
    public String toString(Location value) {
        return TextUtil.prettyFormatLocation(value);
    }
}
