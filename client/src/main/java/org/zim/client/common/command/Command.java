package org.zim.client.common.command;

import lombok.Data;
import org.zim.common.StringTokenHelper;

@Data
public class Command {

    public static final String COMMAND_PREFIX = ":";

    private String name;

    private String parameter;

    public static Command parse(String line) {
        line = line.trim();
        Command command = new Command();

        if (!line.startsWith(COMMAND_PREFIX)) {
            command.setName(line);
            return command;
        }

        line = line.substring(1);

        StringTokenHelper tokenHelper = new StringTokenHelper(line);
        if (!tokenHelper.hasNext()) {
            return command;
        }

        command.setName(tokenHelper.next());
        command.setParameter(tokenHelper.remaining());
        return command;
    }

    public void stripHead(String command) {
        int i = parameter.indexOf(command) + command.length();
        if (i <= parameter.length()) {
            parameter = parameter.substring(i).trim();
        }
    }
}
