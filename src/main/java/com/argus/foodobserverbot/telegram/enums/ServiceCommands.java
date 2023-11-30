package com.argus.foodobserverbot.telegram.enums;

import com.argus.foodobserverbot.exception.UnknownServiceCommandException;

public enum ServiceCommands {
    START("/start", "Starts the bot"),
    HELP("/help", "Shows commands"),
    CANCEL("/cancel", "Aborts selected command"),
    DAY("/day", "Starts today record"),
    FOOD_RECORD("/food", "Adds food record"),
    IS_BLOOD("/blood", "Sets bloody rating"),
    IS_PIMPLE("/pimple", "Sets if there are pimples"),
    PIMPLE_FACE("/face", "Sets face pimples rating"),
    PIMPLE_BOOTY("/booty", "Sets booty pimples rating");
    private final String command;

    private final String description;

    ServiceCommands(String command, String description) {
        this.command = command;
        this.description = description;
    }

    public String getCommand() {
        return command;
    }

    public String getDescription() {
        return description;
    }

    public static ServiceCommands getServiceCommandByValue(String command) {
        for (ServiceCommands c : ServiceCommands.values()) {
            if (c.equals(command)) return c;
        }
        throw new UnknownServiceCommandException("Unknown command: " + command);
    }

    public boolean equals(String command) {
        return this.command.equals(command);
    }
}
