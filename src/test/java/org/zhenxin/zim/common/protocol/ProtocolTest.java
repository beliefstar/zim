package org.zhenxin.zim.common.protocol;

import org.junit.Assert;
import org.junit.Test;
import org.zhenxin.zim.common.protocol.command.GroupChatMessageCommand;
import org.zhenxin.zim.common.protocol.command.PrivateChatMessageCommand;
import org.zhenxin.zim.common.protocol.command.RegisterCommand;

import java.nio.charset.StandardCharsets;

/**
 * @author zhenxin
 * @program 广州智灵时代研发中心
 * @date 2022/4/8 11:27
 */
public class ProtocolTest {

    @Test
    public void testRegisterCommand() {
        RegisterCommand command = new RegisterCommand();
        command.setUserId(System.currentTimeMillis());
        command.setUserName(System.currentTimeMillis() + "");
        System.out.println(command);

        byte[] bytes = command.encode();
        RemoteCommand decode = RemoteCommand.decode(bytes);

        Assert.assertEquals(command.toString(), decode.toString());
        System.out.println(decode);
    }

    @Test
    public void testMessageCommandGroupChatMessage() {
        GroupChatMessageCommand gcm = new GroupChatMessageCommand();
        gcm.setBody((System.currentTimeMillis() + "").getBytes(StandardCharsets.UTF_8));
        System.out.println(gcm);

        byte[] bytes = gcm.encode();
        RemoteCommand decode = RemoteCommand.decode(bytes);

        Assert.assertEquals(gcm.toString(), decode.toString());
        System.out.println(decode);
    }

    @Test
    public void testMessageCommandPrivateChatMessage() {
        PrivateChatMessageCommand pcm = new PrivateChatMessageCommand();
        pcm.setFrom(System.currentTimeMillis());
        pcm.setTo(System.currentTimeMillis() - 5555);
        pcm.setBody((System.currentTimeMillis() + "").getBytes(StandardCharsets.UTF_8));
        System.out.println(pcm);

        byte[] bytes = pcm.encode();
        RemoteCommand decode = RemoteCommand.decode(bytes);

        Assert.assertEquals(pcm.toString(), decode.toString());
        System.out.println(decode);
    }


    @Test
    public void testResponseCommand() {
        RemoteCommand pcm = RemoteCommand.createResponseCommand(CommandResponseType.OK);
        System.out.println(pcm);

        byte[] bytes = pcm.encode();
        RemoteCommand decode = RemoteCommand.decode(bytes);

        Assert.assertEquals(pcm.toString(), decode.toString());
        System.out.println(decode);


        pcm = RemoteCommand.createResponseCommand(CommandResponseType.ERROR, "system error");
        System.out.println(pcm);

        bytes = pcm.encode();
        decode = RemoteCommand.decode(bytes);

        Assert.assertEquals(pcm.toString(), decode.toString());
        System.out.println(decode);
    }
}
