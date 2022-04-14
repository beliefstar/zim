package org.zim.client.common.scan;

import org.zim.client.common.ClientHandler;
import org.zim.client.common.scan.pipeline.CommandHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class ConsoleScanner {

    private final Scanner scanner = new Scanner(System.in);

    private final List<ScanHandler> scanHandlers = new ArrayList<>();

    public ConsoleScanner(ClientHandler clientHandler) {

        scanHandlers.add(clientHandler.getRegisterHandler());
        scanHandlers.add(new CommandHandler(clientHandler));
    }

    public void listen() {
        while (scanner.hasNextLine()) {
            String s = scanner.nextLine();

            triggerCommand(s);
        }

    }

    private void triggerCommand(String command) {
        for (ScanHandler scanHandler : scanHandlers) {
            if (!scanHandler.handle(command)) {
                break;
            }
        }
    }
}
