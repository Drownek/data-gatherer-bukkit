package me.drownek.datagatherer.step;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.event.Event;
import me.drownek.datagatherer.StepResult;

import java.util.function.Function;

@RequiredArgsConstructor
@Getter
public class Step<T, E extends Event> {

    private final Class<? extends E> eventClass;
    private final Function<E, StepResult<T>> eventConsumer;
    private final String info;
    private Runnable startAction = () -> {};
    private Function<T, String> toStringMapper = Object::toString;
    private boolean displaySetValue = true;
    private boolean confirmAction = true;

    public Step<T, E> withoutConfirmAction() {
        confirmAction = false;
        return this;
    }

    public Step<T, E> withoutDisplaySetValue() {
        displaySetValue = false;
        return this;
    }

    public Step<T, E> withStartAction(Runnable runnable) {
        startAction = runnable;
        return this;
    }

    public Step<T, E> withToStringMapper(Function<T, String> mapper) {
        toStringMapper = mapper;
        return this;
    }
}
