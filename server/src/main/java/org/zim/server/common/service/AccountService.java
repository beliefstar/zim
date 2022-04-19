package org.zim.server.common.service;


import lombok.extern.slf4j.Slf4j;
import org.zim.common.SnowFlakeGenerator;
import org.zim.common.channel.ZimChannel;
import org.zim.protocol.RemoteCommand;
import org.zim.server.common.model.ServerClientInfo;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class AccountService {

    private final SnowFlakeGenerator snowFlakeGenerator = new SnowFlakeGenerator();

    private final Map<Long, ServerClientInfo> userIdMap = new ConcurrentHashMap<>();
    private final Map<ZimChannel, ServerClientInfo> channelMap = new ConcurrentHashMap<>();
    private final Set<String> userNames = new HashSet<>();

    public boolean register(ServerClientInfo serverClientInfo) {
        if (channelMap.containsKey(serverClientInfo.getZimChannel())) {
            return false;
        }
        Long userId = snowFlakeGenerator.nextId();
        String userName = serverClientInfo.getUserName();
        if (!userNames.add(userName)) {
            return false;
        }
        serverClientInfo.setUserId(userId);
        ZimChannel channel = serverClientInfo.getZimChannel();

        userIdMap.put(userId, serverClientInfo);
        channelMap.put(channel, serverClientInfo);
        return true;
    }

    public boolean rename(ServerClientInfo clientInfo) {
        ServerClientInfo info = userIdMap.get(clientInfo.getUserId());
        if (info == null) {
            return false;
        }
        if (info.getZimChannel() != clientInfo.getZimChannel()) {
            return false;
        }
        if (info.getUserName().equals(clientInfo.getUserName())) {
            return false;
        }
        if (!checkUserName(clientInfo.getUserName())) {
            return false;
        }
        userNames.add(clientInfo.getUserName());
        userNames.remove(info.getUserName());
        info.setUserName(clientInfo.getUserName());
        return true;
    }

    public boolean checkUserName(String name) {
        return !userNames.contains(name);
    }

    public List<ServerClientInfo> queryAllUser() {
        List<ServerClientInfo> list = new ArrayList<>(userIdMap.size());
        for (Map.Entry<Long, ServerClientInfo> entry : userIdMap.entrySet()) {
            list.add(entry.getValue());
        }
        return list;
    }

    public void broadcast(RemoteCommand command, ServerClientInfo... excludes) {
        Set<Long> excludeId = new HashSet<>();
        if (excludes != null && excludes.length > 0) {
            for (ServerClientInfo exclude : excludes) {
                excludeId.add(exclude.getUserId());
            }
        }

        List<ServerClientInfo> infos = queryAllUser();
        for (ServerClientInfo info : infos) {
            if (!excludeId.contains(info.getUserId())) {
                info.getZimChannel().write(command);
            }
        }
    }

    public ServerClientInfo queryById(Long userId) {
        return userIdMap.get(userId);
    }

    public ServerClientInfo queryByChannel(ZimChannel zimChannel) {
        return channelMap.get(zimChannel);
    }

    public void removeClient(ServerClientInfo clientInfo) {
        userIdMap.remove(clientInfo.getUserId());
        userNames.remove(clientInfo.getUserName());
        channelMap.remove(clientInfo.getZimChannel());
    }
}
