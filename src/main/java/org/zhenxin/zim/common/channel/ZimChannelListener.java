package org.zhenxin.zim.common.channel;

import java.nio.ByteBuffer;

/**
 * @author zhenxin
 * @program 广州智灵时代研发中心
 * @date 2022/4/8 18:19
 */
public interface ZimChannelListener {

    default void onRead(ByteBuffer buffer, int readSize) {}

    default void onWrite(ByteBuffer buffer) {}

    default void onClose(ZimChannel zimChannel) {}
}
