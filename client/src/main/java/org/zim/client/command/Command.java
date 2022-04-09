package org.zim.client.command;

import lombok.Data;

/**
 * @author zhenxin
 * @program 广州智灵时代研发中心
 * @date 2022/4/8 14:33
 */
@Data
public class Command {

    private String name;

    private String parameter;

    public static Command parse(String line) {
        line = line.trim();
        Command command = new Command();
        int breakPoint = line.indexOf(" ");
        if (breakPoint <= 0) {
            command.setName(line);
            return command;
        }
        command.setName(line.substring(0, breakPoint).trim());
        command.setParameter(line.substring(breakPoint + 1).trim());
        return command;
    }
}
