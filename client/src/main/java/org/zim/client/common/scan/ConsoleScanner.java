package org.zim.client.common.scan;

import org.zim.client.common.ClientHandler;
import org.zim.client.common.scan.pipeline.CommandHandler;
import org.zim.common.pipeline.ZimPipeline;

import java.util.Scanner;

public class ConsoleScanner {

    private final ZimPipeline<String> zimPipeline;

    private final Scanner scanner = new Scanner(System.in);

    private final ClientHandler clientHandler;

    public ConsoleScanner(ClientHandler clientHandler) {
        this.clientHandler = clientHandler;

        zimPipeline = new ZimPipeline<>();
        zimPipeline
                .addLast(clientHandler.getRegisterHandler())
                .addLast(new CommandHandler(clientHandler));
    }

    public void listen() {
        while (scanner.hasNextLine()) {
            String s = scanner.nextLine();

            zimPipeline.fireHandle(s);
        }

    }
}
