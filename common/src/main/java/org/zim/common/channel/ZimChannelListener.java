package org.zim.common.channel;

/**
 * @author zhenxin
 * @program 广州智灵时代研发中心
 * @date 2022/4/8 18:19
 */
public interface ZimChannelListener {

    default void onClose(ZimChannel zimChannel) {}
}
