package me.drownek.datagatherer;

import me.drownek.util.CommandUtil;
import me.drownek.util.EventRegistration;
import me.drownek.util.TextUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.Nullable;
import me.drownek.datagatherer.step.Step;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class DataGatherer {

    public static final Component CONFIRM_MESSAGE = MiniMessage.miniMessage().deserialize(
        "Kliknij F aby potwierdzić " +
            "[" +
            "<click:run_command:/datagatherer-no><hover:show_text:Kliknij, aby ponownie ustawić>Powtórz krok</hover></click>" +
            "]"
    );
    public static final String TIMEOUT_MESSAGE = "Czas na akcje wygasł!";
    public static final String CANCEL_MESSAGE = "<click:run_command:/datagatherer-exit><hover:show_text:Kliknij, by anulować><dark_green>[Anuluj]</hover></click>";

    private final Plugin plugin;

    private final List<Step<?, ?>> steps;
    private final @Nullable Runnable startAction;
    private final @Nullable Runnable endAction;
    private final @Nullable Consumer<Integer> afterEachStepAction;
    private final List<Listener> registeredListeners = new ArrayList<>();
    private int currentStep;
    private final boolean confirmActions;
    private final @Nullable Runnable cancelAction;
    private final boolean displaySetValues;
    private final boolean displaySuccessMessage;
    private final Duration timeoutDuration;
    private Instant startTime;
    public BukkitTask timeoutTask;

    DataGatherer(Plugin plugin,
                 List<Step<?, ?>> steps,
                 @Nullable Runnable startAction,
                 @Nullable Runnable endAction,
                 @Nullable Consumer<Integer> afterEachStepAction,
                 boolean confirmActions,
                 @Nullable Runnable cancelAction,
                 boolean displaySetValues,
                 boolean displaySuccessMessage,
                 Duration timeoutDuration) {
        this.plugin = plugin;
        this.steps = steps;
        this.startAction = startAction;
        this.endAction = endAction;
        this.afterEachStepAction = afterEachStepAction;
        this.confirmActions = confirmActions;
        this.displaySetValues = displaySetValues;
        this.displaySuccessMessage = displaySuccessMessage;
        this.cancelAction = cancelAction;
        this.timeoutDuration = timeoutDuration;
    }

    public static DataGathererBuilder builder(Plugin plugin) {
        return new DataGathererBuilder(plugin);
    }

    public static DataGathererBuilder builder() {
        return new DataGathererBuilder(JavaPlugin.getProvidingPlugin(DataGatherer.class));
    }

    public void start(Player player) {
        if (DataGathererManager.playersInDataGatherer.containsKey(player)) {
            TextUtil.message(player, "&cJesteś już w trakcie tworzenia!");
            return;
        }

        DataGathererManager.playersInDataGatherer.put(player, this);

        if (startAction != null) {
            startAction.run();
        }

        if (timeoutDuration != ChronoUnit.FOREVER.getDuration()) {
            timeoutTask = plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
                if (Instant.now().isAfter(startTime.plus(timeoutDuration))) {
                    endGatherer(player, true, false);
                }
            }, 20L, 20L);
        }

        registerListener(new Listener() {
            @EventHandler
            public void onCommand(PlayerCommandPreprocessEvent event) {
                if (!event.getPlayer().equals(player)) {
                    return;
                }

                if (event.getMessage().equalsIgnoreCase("/datagatherer-exit")) {
                    event.setCancelled(true);

                    unregister();
                    TextUtil.message(player, "&cAnulowano!");
                    if (cancelAction != null) {
                        cancelAction.run();
                    }
                    DataGathererManager.playersInDataGatherer.remove(player);
                }
            }
        });

        handleCurrentStep(player);
    }

    public void unregister() {
        for (Listener registeredListener : registeredListeners) {
            HandlerList.unregisterAll(registeredListener);
        }
    }

    public void registerListener(Listener listener) {
        registeredListeners.add(listener);
        plugin.getServer().getPluginManager().registerEvents(listener, plugin);
    }

    public <T, E extends Event> void handleCurrentStep(Player player) {
        startTime = Instant.now();

        @SuppressWarnings("unchecked")
        Step<T, E> step = (Step<T, E>) steps.get(currentStep);

        TextUtil.adventure.player(player).sendMessage(MiniMessage.miniMessage().deserialize("<green>" + step.getInfo() + " " + CANCEL_MESSAGE));

        Listener listener = new Listener() {
        };
        Consumer<StepResult<T>> callback = result -> {
            HandlerList.unregisterAll(listener);
            if (result.message() != null) {
                TextUtil.message(player, result.message());
            }
            switch (result.resultType()) {
                case FAIL -> {
                    handleCurrentStep(player);
                    return;
                }
                case EXIT -> {
                    endGatherer(player, false, false);
                    return;
                }
                case BLANK -> {
                    return;
                }
            }

            T value = result.value();
            if (displaySetValues && step.isDisplaySetValue()) {
                if (value != null) {
                    TextUtil.message(player, "Ustawiono na: " + step.getToStringMapper().apply(value));
                }
            }

            if (confirmActions && step.isConfirmAction()) {
                TextUtil.adventure.player(player).sendMessage(CONFIRM_MESSAGE);
                registerListener(new Listener() {
                    @EventHandler
                    public void onCommand(PlayerCommandPreprocessEvent event) {
                        if (!event.getPlayer().equals(player)) {
                            return;
                        }

                        if (event.getMessage().equalsIgnoreCase("/datagatherer-no")) {
                            event.setCancelled(true);
                            handleCurrentStep(player);
                            HandlerList.unregisterAll(this);
                        }
                    }

                    @EventHandler
                    public void handle(PlayerSwapHandItemsEvent event) {
                        if (!event.getPlayer().equals(player)) {
                            return;
                        }
                        event.setCancelled(true);
                        confirmStep(player, result);
                        HandlerList.unregisterAll(this);
                    }
                });
            } else {
                confirmStep(player, result);
            }

            HandlerList.unregisterAll(listener);
        };
        EventRegistration registration = EventRegistration.register(listener,
            step.getEventClass(),
            event -> {
                if (event instanceof PlayerEvent playerEvent && !playerEvent.getPlayer().equals(player)) {
                    return;
                }
                if (event instanceof Cancellable) {
                    ((Cancellable) event).setCancelled(true);
                }
                callback.accept(step.getEventConsumer().apply(event));
            },
            EventPriority.LOWEST);
        registerListener(registration.getListener());
        step.getStartAction().run();
    }

    private void confirmStep(Player player, StepResult<?> result) {
        Runnable runnable = result.runnable();
        if (runnable != null) {
            runnable.run();
        }
        if (afterEachStepAction != null) {
            afterEachStepAction.accept(currentStep);
        }
        if (++currentStep >= steps.size()) {
            endGatherer(player, false, true);
        } else {
            handleCurrentStep(player);
        }
    }

    public void endGatherer(Player player, boolean timeout, boolean success) {
        if (timeoutTask != null) {
            timeoutTask.cancel();
        }
        unregister();
        if (timeout) {
            TextUtil.message(player, TIMEOUT_MESSAGE);
        } else {
            if (endAction != null) {
                Bukkit.getScheduler().runTask(plugin, endAction);
            }
            if (displaySuccessMessage && success) {
                TextUtil.message(player, CommandUtil.SUCCESS_MESSAGE);
            }
        }
        DataGathererManager.playersInDataGatherer.remove(player);
    }

    public static class DataGathererBuilder {

        private final Plugin plugin;
        private List<Step<?, ?>> steps;
        private Runnable startAction;
        private Runnable endAction;
        private Consumer<Integer> afterEachStepAction;
        private Runnable cancelAction;
        private boolean confirmActions = true;
        private boolean displaySetValues = true;
        private boolean displaySuccessMessage = true;
        private Duration timeoutDuration = ChronoUnit.FOREVER.getDuration();

        DataGathererBuilder(Plugin plugin) {
            this.plugin = plugin;
        }

        public DataGathererBuilder timeout(Duration timeoutDuration) {
            this.timeoutDuration = timeoutDuration;
            return this;
        }

        public DataGathererBuilder withoutConfirm() {
            confirmActions = false;
            return this;
        }

        public DataGathererBuilder steps(List<Step<?, ?>> steps) {
            this.steps = steps;
            return this;
        }

        public DataGathererBuilder steps(Step<?, ?>... steps) {
            this.steps = List.of(steps);
            return this;
        }

        public DataGathererBuilder startAction(Runnable startAction) {
            this.startAction = startAction;
            return this;
        }

        public DataGathererBuilder endAction(Runnable endAction) {
            this.endAction = endAction;
            return this;
        }

        public DataGathererBuilder afterEachStepAction(Consumer<Integer> afterEachStepAction) {
            this.afterEachStepAction = afterEachStepAction;
            return this;
        }

        public DataGathererBuilder cancelAction(Runnable cancelAction) {
            this.cancelAction = cancelAction;
            return this;
        }

        public DataGathererBuilder withoutDisplaySetValues() {
            displaySetValues = false;
            return this;
        }

        public DataGathererBuilder withoutSuccessMessage() {
            displaySuccessMessage = false;
            return this;
        }

        public DataGatherer build() {
            return new DataGatherer(plugin,
                steps,
                startAction,
                endAction,
                afterEachStepAction,
                confirmActions,
                cancelAction,
                displaySetValues,
                displaySuccessMessage,
                timeoutDuration);
        }
    }
}
