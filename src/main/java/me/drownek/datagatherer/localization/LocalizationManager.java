package me.drownek.datagatherer.localization;

import java.util.HashMap;
import java.util.Map;

public class LocalizationManager {
    private static final Map<MessageKey, String> messages = new HashMap<>();

    static {
        messages.put(MessageKey.CONFIRM_MESSAGE, "Kliknij F aby potwierdzić [<click:run_command:/datagatherer-no><hover:show_text:Kliknij, aby ponownie ustawić>Powtórz krok</hover></click>]");
        messages.put(MessageKey.TIMEOUT_MESSAGE, "Czas na akcje wygasł!");
        messages.put(MessageKey.ALREADY_IN_GATHERER, "<red>Jesteś już w trakcie tworzenia!");
        messages.put(MessageKey.CANCELLED, "<red>Anulowano!");
        messages.put(MessageKey.CURRENT_STEP_INFO, "<green>{STEP_INFO} <click:run_command:/datagatherer-exit><hover:show_text:Kliknij, by anulować><dark_green>[Anuluj]</hover></click>");
        messages.put(MessageKey.VALUE_SET, "Ustawiono na: {VALUE}");
        messages.put(MessageKey.SUCCESS, "<green>Sukces!");
    }

    public static String getMessage(MessageKey key) {
        return getMessage(key, Map.of());
    }

    public static String getMessage(MessageKey key, Map<String, Object> placeholders) {
        String message = messages.getOrDefault(key, key.toString());

        for (Map.Entry<String, Object> entry : placeholders.entrySet()) {
            String placeholder = entry.getKey();
            Object value = entry.getValue();
            message = message.replace(placeholder, String.valueOf(value));
        }

        return message;
    }

    public static void setMessage(MessageKey key, String value) {
        messages.put(key, value);
    }
}