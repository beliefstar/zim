package org.zim.server.common.service;


import org.zim.common.EchoHelper;
import org.zim.common.channel.ZimChannel;
import org.zim.common.channel.ZimChannelListener;
import org.zim.server.common.model.ServerClientInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author zhenxin
 * @program 广州智灵时代研发中心
 * @date 2022/4/8 16:34
 */
public class AccountService {

    private final Map<Long, ServerClientInfo> userId2UserNameMap = new ConcurrentHashMap<>();
    private final Map<String, ServerClientInfo> userName2UserIdMap = new ConcurrentHashMap<>();
    private final Map<ZimChannel, ServerClientInfo> channelClientInfoMap = new ConcurrentHashMap<>();

    public boolean register(Long userId, String userName, ZimChannel zimChannel) {
        if (userId2UserNameMap.containsKey(userId) || userName2UserIdMap.containsKey(userName)) {
            return false;
        }
        ServerClientInfo serverClientInfo = new ServerClientInfo();
        serverClientInfo.setUserId(userId);
        serverClientInfo.setUserName(userName);
        serverClientInfo.setZimChannel(zimChannel);

        userId2UserNameMap.put(userId, serverClientInfo);
        userName2UserIdMap.put(userName, serverClientInfo);
        channelClientInfoMap.put(zimChannel, serverClientInfo);

        zimChannel.registerListener(new ZimChannelListener() {
            @Override
            public void onClose(ZimChannel zimChannel) {
                ServerClientInfo info = channelClientInfoMap.get(zimChannel);
                if (info != null) {
                    EchoHelper.print("user [{}] offline", info.getUserName());
                    userId2UserNameMap.remove(info.getUserId());
                    userName2UserIdMap.remove(info.getUserName());
                    channelClientInfoMap.remove(zimChannel);
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
