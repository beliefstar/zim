package org.zim.client.common.scan;

import org.zim.client.common.ClientHandler;
import org.zim.client.common.scan.pipline.CommandHandler;
import org.zim.common.pipline.PipLineContext;
import org.zim.common.pipline.PipLineHolder;

import java.util.Scanner;

public class ConsoleScanner {

    private PipLineHolder<String> pipLineHolder;

    private final Scanner scanner = new Scanner(System.in);

    private final ClientHandler clientHandler;

    public ConsoleScanner(ClientHandler clientHandler) {
        this.clientHandler = clientHandler;

        pipLineHolder = new PipLineHolder<>();
        pipLineHolder
                .addLast(clientHandler.getRegisterHandler())
                .addLast(new CommandHandler(clientHandler));
    }

    public void listen() {
        while (scanner.hasNextLine()) {
            String s = scanner.nextLine();

            new PipLineContext<>(pipLineHolder).fireHandle(s);
        }

    }
}
