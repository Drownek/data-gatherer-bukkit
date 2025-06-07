package me.drownek.datagatherer.step;

import me.drownek.datagatherer.StepResult;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.function.Consumer;
import java.util.function.Predicate;

public class MsgStep extends Step<String, AsyncPlayerChatEvent> {

    public MsgStep(String info, Consumer<String> consumer) {
        this(info, consumer, s -> true);
    }

    public MsgStep(String info, Consumer<String> consumer, Predicate<String> predicate) {
        super(AsyncPlayerChatEvent.class, event -> {
            String message = event.getMessage();
            if (!predicate.test(message)) {
                return StepResult.fail("Zły format!");
            }
            return StepResult.success(message, () -> consumer.accept(message));
        }, info);
    }
}
