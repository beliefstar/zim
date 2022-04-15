package org.zim.client.common.scan;

import org.zim.client.common.ClientHandler;
import org.zim.client.common.scan.pipeline.CommandHandler;
import org.zim.client.common.scan.pipeline.ExitHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicBoolean;

public class ConsoleScanner {

    private final Scanner scanner = new Scanner(System.in);

    private final List<ScanHandler> scanHandlers = new ArrayList<>();

    private final AtomicBoolean state = new AtomicBoolean(true);

    public ConsoleScanner(ClientHandler clientHandler) {

        scanHandlers.add(new ExitHandler(clientHandler));
        scanHandlers.add(clientHandler.getRegisterHandler());
        scanHandlers.add(new CommandHandler(clientHandler));
    }

    public void listen() {
        while (state.get()) {
            if (scanner.hasNextLine()) {
                String s = scanner.nextLine();

                triggerCommand(s);
            }
        }
    }

    public void close() {
        state.set(false);
    }

    private void triggerCommand(String command) {
        for (ScanHandler scanHandler : scanHandlers) {
            if (!scanHandler.handle(command)) {
                break;
            }
        }
    }
}
