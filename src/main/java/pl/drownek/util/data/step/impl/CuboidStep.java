package pl.drownek.util.data.step.impl;

import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.session.SessionManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.plugin.Plugin;
import pl.drownek.util.TextUtil;
import pl.drownek.util.data.DataGatherer;
import pl.drownek.util.data.step.Step;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class CuboidStep extends Step<CuboidRegion> {

    public CuboidStep(String info, Consumer<CuboidRegion> consumer) {
        super(info, consumer);
        this.withoutConfirmAction();
    }

    @Override
    public Listener handle(Plugin plugin, DataGatherer dataGatherer, Player player, BiConsumer<Listener, CuboidRegion> callback) {
        return new Listener() {

            @EventHandler(priority = EventPriority.LOW)
            public void handle(PlayerSwapHandItemsEvent event) {
                if (!event.getPlayer().equals(player)) {
                    return;
                }

                event.setCancelled(true);

                Optional<CuboidRegion> region = getRegion(event.getPlayer());
                if (region.isPresent()) {
                    callback.accept(this, region.get());
                } else {
                    TextUtil.message(player, "&cNie zaznaczyłeś terenu!");
                }
            }
        };
    }

    public static Optional<CuboidRegion> getRegion(Player player) {
        com.sk89q.worldedit.entity.Player adaptedPlayer = BukkitAdapter.adapt(player);
        SessionManager sessionManager = WorldEdit.getInstance().getSessionManager();
        LocalSession localSession = sessionManager.get(adaptedPlayer);
        com.sk89q.worldedit.world.World sessionWorld = localSession.getSelectionWorld();

        try {
            Region region = localSession.getSelection(sessionWorld);

            BlockVector3 minimumPoint = region.getMinimumPoint();
            BlockVector3 maximumPoint = region.getMaximumPoint();

            CuboidRegion polygonal2DRegion = new CuboidRegion(BukkitAdapter.adapt(player.getWorld()),
                minimumPoint,
                maximumPoint);

            return Optional.of(polygonal2DRegion);
        } catch (IncompleteRegionException ex) {
            return Optional.empty();
        }
    }
}
