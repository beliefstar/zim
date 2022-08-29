package org.zim.protocol;

public class RemoteCommandFactory {

    public static RemoteCommand createByRequestType(CommandRequestType requestType) {
        Class<? extends RemoteCommand> type = requestType.getType();

        try {
            return type.newInstance();
        } catch (Exception e) {
            throw new RuntimeException("create class error! type: " + type.getName());
        }
    }

    public static RemoteCommand create(byte flag, short code) {
        RemoteCommand rc;
        if (flag == RemoteCommand.REQUEST_FLAG) {
            CommandRequestType commandType = CommandRequestType.valueOf(code);
            if (commandType == null) {
                throw new IllegalArgumentException();
            }
            rc = createByRequestType(commandType);
        } else {
            rc = new RemoteCommand();
        }
        rc.setFlag(flag);
        rc.setCode(code);
        return rc;
    }
}
