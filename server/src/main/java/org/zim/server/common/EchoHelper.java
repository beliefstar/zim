package org.zim.server.common;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.helpers.MessageFormatter;

/**
 * @author zhenxin
 * @program 广州智灵时代研发中心
 * @date 2022/4/8 14:17
 */
@Slf4j
public class EchoHelper {

    public static void print(String line, Object... args) {
        String message = MessageFormatter.arrayFormat(line, args).getMessage();
        System.out.println(message);
    }

}
