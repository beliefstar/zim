package org.zim.client.common;

import org.zim.common.EchoHelper;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ReconnectHelper {

    private final ScheduledExecutorService scheduledExecutorService = new ScheduledThreadPoolExecutor(1);;

    private final ReconnectAction action;
    private int reconnectCount = 0;

    public ReconnectHelper(ReconnectAction action) {
        this.action = action;
    }

    public static void handleReconnect(ReconnectAction action) {
        new ReconnectHelper(action).reconnect();
    }

    private void reconnect() {
        EchoHelper.printSystemError("reconnect..." + (++reconnectCount));
        try {
            action.run();
            return;
        } catch (Exception ignore) {
        }
        System.out.println("--");
        scheduledExecutorService.schedule(this::reconnect, 10, TimeUnit.SECONDS);
    }

    public interface ReconnectAction {
        void run() throws Exception;
    }
}
