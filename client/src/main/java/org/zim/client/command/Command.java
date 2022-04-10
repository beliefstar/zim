package org.zim.client.command;

import lombok.Data;

/**
 * @author zhenxin
 * @program 广州智灵时代研发中心
 * @date 2022/4/8 14:33
 */
@Data
public class Command {

    public static InnerCommand CURRENT_COMMAND;

    private String name;

    private String parameter;

    public static Command parse(String line) {
        line = line.trim();
        Command command = new Command();
        command.setParameter(line);
        int breakPoint = line.indexOf(" ");
        if (breakPoint <= 0) {
            command.setName(line);
            return command;
        }
        command.setName(line.substring(0, breakPoint).trim());
        return command;
    }

    public void tripHead(String command) {
        int i = parameter.indexOf(command) + command.length();
        if (i <= parameter.length()) {
            parameter = parameter.substring(i).trim();
        }
    }
}
