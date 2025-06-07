package me.drownek.datagatherer;

public record StepResult<T>(T value, StepResultType resultType, String message, Runnable runnable) {

    public static <A> StepResult<A> success() {
        return new StepResult<>(null, StepResultType.SUCCESS, null);
    }

    public static <A> StepResult<A> success(A value) {
        return new StepResult<>(value, StepResultType.SUCCESS, null);
    }

    public static <A> StepResult<A> success(Runnable runnable) {
        return new StepResult<>(null, StepResultType.SUCCESS, null, runnable);
    }

    public static <A> StepResult<A> success(A value, Runnable runnable) {
        return new StepResult<>(value, StepResultType.SUCCESS, null, runnable);
    }

    public static <A> StepResult<A> fail(String message) {
        return new StepResult<>(null, StepResultType.FAIL, message);
    }

    public static <A> StepResult<A> exit(String message) {
        return new StepResult<>(null, StepResultType.EXIT, message);
    }

    public static <A> StepResult<A> blank() {
        return new StepResult<>(null, StepResultType.BLANK, null);
    }

    public StepResult(T value, StepResultType resultType, String message) {
        this(value, resultType, message, null);
    }
}
