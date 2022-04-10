package org.zim.common;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.helpers.MessageFormatter;

import java.util.Scanner;

/**
 * @author zhenxin
 * @program 广州智灵时代研发中心
 * @date 2022/4/8 14:17
 */
@Slf4j
public class EchoHelper {

    private static Scanner scanner;

    public static void print(String line, Object... args) {
        String message = MessageFormatter.arrayFormat(line, args).getMessage();
        System.out.println(message);
    }

    public static String nextLine() {
        checkScanner();
        return scanner.nextLine();
    }

    public static Scanner getScanner() {
        checkScanner();
        return scanner;
    }

    private static void checkScanner() {
        if (scanner == null) {
            synchronized (EchoHelper.class) {
                if (scanner == null) {
                    scanner = new Scanner(System.in);
                }
            }
        }
    }
}
