package org.zim.server.common.service;


import org.zim.server.common.EchoHelper;
import org.zim.server.common.channel.ZimChannel;
import org.zim.server.common.channel.ZimChannelListener;
import org.zim.server.common.model.ClientInfo;

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

    private final Map<Long, ClientInfo> userId2UserNameMap = new ConcurrentHashMap<>();
    private final Map<String, ClientInfo> userName2UserIdMap = new ConcurrentHashMap<>();
    private final Map<ZimChannel, ClientInfo> channelClientInfoMap = new ConcurrentHashMap<>();

    public boolean register(Long userId, String userName, ZimChannel zimChannel) {
        if (userId2UserNameMap.containsKey(userId) || userName2UserIdMap.containsKey(userName)) {
            return false;
        }
        ClientInfo clientInfo = new ClientInfo();
        clientInfo.setUserId(userId);
        clientInfo.setUserName(userName);
        clientInfo.setZimChannel(zimChannel);

        userId2UserNameMap.put(userId, clientInfo);
        userName2UserIdMap.put(userName, clientInfo);
        channelClientInfoMap.put(zimChannel, clientInfo);

        zimChannel.registerListener(new ZimChannelListener() {
            @Override
            public void onClose(ZimChannel zimChannel) {
                ClientInfo info = channelClientInfoMap.get(zimChannel);
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

    public List<String> queryAllUser() {
        return new ArrayList<>(userName2UserIdMap.keySet());
    }

    public ClientInfo queryByName(String userName) {
        return userName2UserIdMap.get(userName);
    }

    public ClientInfo queryByChannel(ZimChannel zimChannel) {
        return channelClientInfoMap.get(zimChannel);
    }
}
