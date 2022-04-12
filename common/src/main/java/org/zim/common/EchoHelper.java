package org.zim.common;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.helpers.MessageFormatter;

import java.time.LocalDate;
import java.time.LocalTime;

@Slf4j
public class EchoHelper {

    public static void print(String line, Object... args) {
        String message = MessageFormatter.arrayFormat(line, args).getMessage();
        System.out.println(message);
    }

    public static void printMessage(String sender, String message) {
        String msg = String.format("[%s]%s: %s%n", date(), Color.BLUE.display(sender, "4"), message);
        System.out.print(msg);
    }

    public static void printGroupMessage(String sender, String message) {
        String msg = String.format("[%s]%s%s: %s%n", date(), Color.YELLOW.display("[GROUP]"), Color.BLUE.display(sender, "4"), message);
        System.out.print(msg);
    }

    public static void printSystem(String line, Object... args) {
        String message = MessageFormatter.arrayFormat(line, args).getMessage();
        System.out.printf("%s : %s%n", Color.GREEN.display("[SYSTEM]"), message);
    }

    public static void printSystemError(String line, Object... args) {
        String message = MessageFormatter.arrayFormat(line, args).getMessage();
        System.out.printf("%s : %s%n", Color.RED.display("[SYSTEM]"), message);
    }

    private static String date() {
        return LocalDate.now().toString() + " " + LocalTime.now().withNano(0).toString();
    }

    public static void main(String[] args) {
        System.out.println("\033[30;1mxxxxx\033[0m");
        System.out.println("\033[31;1mxxxxx\033[0m");
        System.out.println("\033[32;3mxxxxx\033[0m");
        System.out.println("\033[33;2mxxxxx\033[0m");
        System.out.println("\033[34;4mxxxxx\033[0m");
        System.out.println("\033[35;4mxxxxx\033[0m");
        System.out.println("\033[36;4mxxxxx\033[0m");
    }

    private static enum Color {
        BLACK("30"),
        RED("31"),
        GREEN("32"),
        YELLOW("33"),
        BLUE("34"),
        PURPLE("35"),
        ;

        private final String code;

        Color(String code) {
            this.code = code;
        }

        // 1加粗；2正常；3斜体；4下划线；
        public String display(String s) {
            return display(s, "2");
        }

        public String display(String s, String style) {
            return String.format("\033[%s;%sm%s\033[0m", code, style, s);
        }
    }
}
