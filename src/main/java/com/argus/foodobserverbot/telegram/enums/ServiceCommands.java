package com.argus.foodobserverbot.telegram.enums;

import com.argus.foodobserverbot.exception.UnknownServiceCommandException;

public enum ServiceCommands {
    HELP("/help"),
    DAY("/day"),
    FOOD_RECORD("/food"),
    IS_BLOOD("/blood"),
    IS_PIMPLE("/pimple"),
    PIMPLE_FACE("/face"),
    PIMPLE_BOOTY("/booty"),
    CANCEL("/cancel"),
    START("/start");
    private final String command;

    ServiceCommands(String command) {
        this.command = command;
    }

    public String getCommand() {
        return command;
    }

    public static ServiceCommands getServiceCommandByValue(String command) {
        for (ServiceCommands c : ServiceCommands.values()) {
            if (c.equals(command)) return c;
        }
        throw new UnknownServiceCommandException("Unknown command");
    }

    public boolean equals(String command) {
        return this.command.equals(command);
    }
}
