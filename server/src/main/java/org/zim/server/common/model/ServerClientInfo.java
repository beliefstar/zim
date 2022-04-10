package org.zim.server.common.model;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.zim.common.channel.ZimChannel;
import org.zim.common.model.ClientInfo;

/**
 * @author zhenxin
 * @program 广州智灵时代研发中心
 * @date 2022/4/8 16:40
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ServerClientInfo extends ClientInfo {

    @JSONField(serialize = false, deserialize = false)
    private ZimChannel zimChannel;
}
