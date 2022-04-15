package org.zim.client.common.scan.pipeline;

import org.zim.client.common.ClientHandler;
import org.zim.client.common.scan.ScanHandler;
import org.zim.common.EchoHelper;

public class ExitHandler implements ScanHandler {
    public static final String EXIT_COMMAND = "exit";

    private boolean processing = false;

    private final ClientHandler clientHandler;

    public ExitHandler(ClientHandler clientHandler) {
        this.clientHandler = clientHandler;
    }

    @Override
    public boolean handle(String command) {
        if (processing && "Y".equalsIgnoreCase(command)) {
            clientHandler.closeForce();
            return false;
        }
        else if (EXIT_COMMAND.equals(command)) {
            EchoHelper.printSystem("确认要退出？Y/N");
            processing = true;
            return false;
        }
        processing = false;
        return true;
    }
}
