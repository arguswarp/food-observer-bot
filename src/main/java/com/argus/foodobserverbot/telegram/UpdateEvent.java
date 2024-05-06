package com.argus.foodobserverbot.telegram;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class UpdateEvent extends ApplicationEvent {

    private final Type type;

    public UpdateEvent(Object source, Type type) {
        super(source);
        this.type = type;
    }

    public enum Type {
        RECEIVED, PROCESSED
    }
}
