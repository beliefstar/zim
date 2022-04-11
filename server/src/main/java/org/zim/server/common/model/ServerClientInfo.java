package org.zim.server.common.model;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.zim.common.channel.ZimChannel;
import org.zim.common.model.ClientInfo;

@EqualsAndHashCode(callSuper = true)
@Data
public class ServerClientInfo extends ClientInfo {

    @JSONField(serialize = false, deserialize = false)
    private ZimChannel zimChannel;
}
