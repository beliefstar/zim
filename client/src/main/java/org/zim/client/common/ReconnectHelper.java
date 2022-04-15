package org.zim.client.common;

import org.zim.common.EchoHelper;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

public class ReconnectHelper {

    private final ScheduledExecutorService scheduledExecutorService = new ScheduledThreadPoolExecutor(1, new ThreadFactory() {
        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(r);
            t.setName("zim-reconnect-exec-single");
            return t;
        }
    });

    private final ReconnectAction action;
    private int reconnectCount = 0;

    public ReconnectHelper(ReconnectAction action) {
        this.action = action;
    }

    public static void handleReconnect(ReconnectAction action) {
        new ReconnectHelper(action).handleReconnect();
    }

    private void handleReconnect() {
        scheduledExecutorService.schedule(this::reconnect, 1, TimeUnit.SECONDS);
    }

    private void reconnect() {
        EchoHelper.printSystemError("reconnect..." + (++reconnectCount));
        try {
            action.run();
            return;
        } catch (Exception ignore) {
        }
        scheduledExecutorService.schedule(this::reconnect, 10, TimeUnit.SECONDS);
    }

    public interface ReconnectAction {
        void run() throws Exception;
    }
}
