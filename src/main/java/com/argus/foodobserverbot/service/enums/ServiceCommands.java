package com.argus.foodobserverbot.service.enums;

public enum ServiceCommands {
    HELP("/help"),
    DAY("/day"),
    FOOD_RECORD("/food"),
    IS_BLOOD("/blood"),
    IS_PIMPLE("/pimple"),
    CANCEL("/cancel"),
    START("/start");
    private final String command;

    ServiceCommands(String command) {
        this.command = command;
    }

    public static ServiceCommands getServiceCommandByValue(String command) {
        for (ServiceCommands c : ServiceCommands.values()) {
            if (c.equals(command)) return c;
        }
        return null;
    }

    public boolean equals(String command) {
        return this.command.equals(command);
    }
}
