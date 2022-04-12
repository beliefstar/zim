package org.zim.server.common.service;


import org.zim.common.EchoHelper;
import org.zim.common.channel.ZimChannel;
import org.zim.common.channel.ZimChannelListener;
import org.zim.protocol.CommandResponseType;
import org.zim.protocol.MessageConstants;
import org.zim.protocol.RemoteCommand;
import org.zim.server.common.model.ServerClientInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AccountService {

    private final Map<Long, ServerClientInfo> userId2UserNameMap = new ConcurrentHashMap<>();
    private final Map<String, ServerClientInfo> userName2UserIdMap = new ConcurrentHashMap<>();
    private final Map<ZimChannel, ServerClientInfo> channelClientInfoMap = new ConcurrentHashMap<>();

    public boolean register(ServerClientInfo serverClientInfo) {
        if (userId2UserNameMap.containsKey(serverClientInfo.getUserId())
                || userName2UserIdMap.containsKey(serverClientInfo.getUserName())) {
            return false;
        }

        userId2UserNameMap.put(serverClientInfo.getUserId(), serverClientInfo);
        userName2UserIdMap.put(serverClientInfo.getUserName(), serverClientInfo);
        channelClientInfoMap.put(serverClientInfo.getZimChannel(), serverClientInfo);

        serverClientInfo.getZimChannel().registerListener(new ZimChannelListener() {
            @Override
            public void onClose(ZimChannel zimChannel) {
                ServerClientInfo info = channelClientInfoMap.get(zimChannel);
                if (info != null) {
                    EchoHelper.print("user [{}] offline", info.getUserName());
                    userId2UserNameMap.remove(info.getUserId());
                    userName2UserIdMap.remove(info.getUserName());
                    channelClientInfoMap.remove(zimChannel);

                    // offline broadcast
                    broadcastOffline(info);
                }
            }
        });
        return true;
    }

    public List<ServerClientInfo> queryAllUser() {
        List<ServerClientInfo> list = new ArrayList<>(userName2UserIdMap.size());
        for (Map.Entry<String, ServerClientInfo> entry : userName2UserIdMap.entrySet()) {
            list.add(entry.getValue());
        }
        return list;
    }

    public void broadcastOnline(ServerClientInfo clientInfo) {
        RemoteCommand remoteCommand = RemoteCommand.createResponseCommand(CommandResponseType.BROADCAST_ONLINE);
        remoteCommand.putExtendField(MessageConstants.TO, Long.toString(clientInfo.getUserId()));
        remoteCommand.putExtendField(MessageConstants.TO_NAME, clientInfo.getUserName());

        List<ServerClientInfo> infos = queryAllUser();
        for (ServerClientInfo info : infos) {
            if (!info.getUserId().equals(clientInfo.getUserId())) {
                info.getZimChannel().write(remoteCommand.encode());
            }
        }
    }

    private void broadcastOffline(ServerClientInfo clientInfo) {
        RemoteCommand command = RemoteCommand.createResponseCommand(CommandResponseType.BROADCAST_OFFLINE);
        command.putExtendField(MessageConstants.TO, Long.toString(clientInfo.getUserId()));
        command.putExtendField(MessageConstants.TO_NAME, clientInfo.getUserName());

        for (ZimChannel zimChannel : channelClientInfoMap.keySet()) {
            if (!zimChannel.equals(clientInfo.getZimChannel())) {
                zimChannel.write(command.encode());
            }
        }
    }

    public ServerClientInfo queryById(Long userId) {
        return userId2UserNameMap.get(userId);
    }

    public ServerClientInfo queryByName(String userName) {
        return userName2UserIdMap.get(userName);
    }

    public ServerClientInfo queryByChannel(ZimChannel zimChannel) {
        return channelClientInfoMap.get(zimChannel);
    }
}
