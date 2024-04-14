package com.argus.foodobserverbot.telegram.enums;

import com.argus.foodobserverbot.exception.UnknownServiceCommandException;

import java.util.Arrays;

public enum ServiceCommands {
    START("/start", "Starts the bot", "Start"),
    HELP("/help", "Shows commands", "Help"),
    MENU("/menu", "Call menu to make day record", "Menu"),
    NOTE("/note", "Add note to day record", "Note"),
    SHOW("/show", "Show today records","Show food"),
    SHOW_NOTES("/shownotes", "Show today notes","Show notes"),
    CANCEL("/cancel", "Aborts selected command", "Cancel"),
    MODE("/mode", "Change record mode", "Mode"),
    DAY_TODAY("/today","Saves today records", "Today"),
    DAY_YESTERDAY("/yesterday","Saves yesterday records", "Yesterday"),
    FOOD_RECORD("/food", "Adds food record", "Food"),
    IS_BLOOD("/blood", "Sets bloody rating", "Blood"),
    IS_PIMPLE("/pimples", "Sets if there are pimples", "Pimples"),
    PIMPLE_FACE("/face", "Sets face pimples rating", "Face"),
    PIMPLE_BOOTY("/booty", "Sets booty pimples rating", "Booty"),
    EXCEL_USER_DATA("/excel","Saves user records to excel file" , "Excel"),
    EXCEL_ALL_DATA("/excelall", "Saves all records to excel file", "Excel all data");

    private final String command;
    private final String description;
    private final String buttonName;

    ServiceCommands(String command, String description, String buttonName) {
        this.command = command;
        this.description = description;
        this.buttonName = buttonName;
    }

    public String getCommand() {
        return command;
    }

    public String getDescription() {
        return description;
    }

    public String getButtonName() {
        return buttonName;
    }

    public static ServiceCommands getServiceCommandByValue(String command) {
        for (ServiceCommands c : ServiceCommands.values()) {
            if (c.equals(command)) return c;
        }
        throw new UnknownServiceCommandException("Unknown command: " + command);
    }

    public static boolean isCommand(String text) {
       return Arrays.stream(ServiceCommands.values())
               .anyMatch(serviceCommands -> serviceCommands.getCommand().equals(text));
    }

    public boolean equals(String command) {
        return this.command.equals(command);
    }
}
