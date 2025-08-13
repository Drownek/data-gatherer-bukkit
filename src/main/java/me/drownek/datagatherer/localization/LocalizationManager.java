package me.drownek.datagatherer.localization;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class LocalizationManager {
    private static final Map<Locale, Map<MessageKey, String>> messages = new HashMap<>();

    private static Locale DEFAULT_LOCALE = new Locale("pl", "PL");

    static {
        var polishMessages = new HashMap<MessageKey, String>();
        polishMessages.put(MessageKey.CONFIRM_MESSAGE, "Kliknij F aby potwierdzić [<click:run_command:/datagatherer-no><hover:show_text:Kliknij, aby ponownie ustawić>Powtórz krok</hover></click>]");
        polishMessages.put(MessageKey.TIMEOUT_MESSAGE, "Czas na akcje wygasł!");
        polishMessages.put(MessageKey.ALREADY_IN_GATHERER, "<red>Jesteś już w trakcie tworzenia!");
        polishMessages.put(MessageKey.CANCELLED, "<red>Anulowano!");
        polishMessages.put(MessageKey.CURRENT_STEP_INFO, "<green>{STEP_INFO} <click:run_command:/datagatherer-exit><hover:show_text:Kliknij, by anulować><dark_green>[Anuluj]</hover></click>");
        polishMessages.put(MessageKey.VALUE_SET, "Ustawiono na: {VALUE}");
        polishMessages.put(MessageKey.SUCCESS, "<green>Sukces!");
        messages.put(DEFAULT_LOCALE, polishMessages);

        var englishMessages = new HashMap<MessageKey, String>();
        englishMessages.put(MessageKey.CONFIRM_MESSAGE, "Click F to confirm [<click:run_command:/datagatherer-no><hover:show_text:Click to reset>Repeat step</hover></click>]");
        englishMessages.put(MessageKey.TIMEOUT_MESSAGE, "Action timeout!");
        englishMessages.put(MessageKey.ALREADY_IN_GATHERER, "<red>You are already creating!");
        englishMessages.put(MessageKey.CANCELLED, "<red>Cancelled!");
        englishMessages.put(MessageKey.CURRENT_STEP_INFO, "<green>{STEP_INFO} <click:run_command:/datagatherer-exit><hover:show_text:Click to cancel><dark_green>[Cancel]</hover></click>");
        englishMessages.put(MessageKey.VALUE_SET, "Set to: {VALUE}");
        englishMessages.put(MessageKey.SUCCESS, "<green>Success!");
        messages.put(Locale.ENGLISH, englishMessages);
    }

    public static String getMessage(MessageKey key) {
        return getMessage(key, Map.of());
    }

    public static String getMessage(MessageKey key, Map<String, Object> placeholders) {
        return getMessage(DEFAULT_LOCALE, key, placeholders);
    }

    public static String getMessage(Locale locale, MessageKey key) {
        return getMessage(locale, key, Map.of());
    }

    public static String getMessage(Locale locale, MessageKey key, Map<String, Object> placeholders) {
        String message = messages.get(locale).getOrDefault(key, key.toString());

        for (Map.Entry<String, Object> entry : placeholders.entrySet()) {
            String placeholder = entry.getKey();
            Object value = entry.getValue();
            message = message.replace(placeholder, String.valueOf(value));
        }

        return message;
    }

    public static void setMessage(MessageKey key, String value) {
        setMessage(DEFAULT_LOCALE, key, value);
    }

    public static void setMessage(Locale locale, MessageKey key, String value) {
        messages.computeIfAbsent(locale, locale1 -> new HashMap<>()).put(key, value);
    }

    public static void setDefaultLocale(Locale locale) {
        if (!locale.equals(Locale.ENGLISH) && !locale.equals(new Locale("pl", "PL"))) {
            throw new IllegalArgumentException("Only Polish (pl_PL) and English (en) locales are supported");
        }
        DEFAULT_LOCALE = locale;
    }
}