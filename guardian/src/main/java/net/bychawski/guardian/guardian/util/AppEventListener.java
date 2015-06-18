package net.bychawski.guardian.guardian.util;

import java.util.EventListener;

/**
 * Created by marcin on 5/12/14.
 */
public interface AppEventListener extends EventListener {
    void onAppEvent(AppEvent event);
}
