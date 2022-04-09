package org.zim.server.common.model;

import lombok.Data;
import org.zim.server.common.channel.ZimChannel;

/**
 * @author zhenxin
 * @program 广州智灵时代研发中心
 * @date 2022/4/8 16:40
 */
@Data
public class ClientInfo {
    private Long userId;
    private String userName;

    private ZimChannel zimChannel;
}
