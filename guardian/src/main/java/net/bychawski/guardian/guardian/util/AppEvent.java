package net.bychawski.guardian.guardian.util;

import net.bychawski.guardian.guardian.models.User;

import java.util.EventObject;

/**
 * Created by marcin on 5/12/14.
 */
public class AppEvent extends EventObject {
    private AppEventType _type = null;
    private User _user = null;
    public AppEvent(Object source, AppEventType type ) {
        super(source);
        _type = type;
    }

    public AppEvent(Object source, AppEventType type, User user){
        this(source,type);
        _user = user;
    }

    public AppEventType getType() {
        return _type;
    }

    public User getUser() {
        return _user;
    }
}
