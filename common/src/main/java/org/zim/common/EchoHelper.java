package org.zim.common;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.helpers.MessageFormatter;

@Slf4j
public class EchoHelper {

    public static void print(String line, Object... args) {
        String message = MessageFormatter.arrayFormat(line, args).getMessage();
        System.out.println(message);
    }

    public static void printMessage(String sender, String message) {
        System.out.printf("[PMSG]%s: %s%n", sender, message);
    }

    public static void printSystem(String line, Object... args) {
        String message = MessageFormatter.arrayFormat(line, args).getMessage();
        System.out.println("[SYSTEM]: " + message);
    }

    public static void printSystemError(String line, Object... args) {
        String message = MessageFormatter.arrayFormat(line, args).getMessage();
        System.err.println("[SYSTEM]: " + message);
    }

}
