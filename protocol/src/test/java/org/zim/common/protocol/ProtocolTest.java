package org.zim.common.protocol;

import org.junit.Assert;
import org.junit.Test;
import org.zim.protocol.CommandResponseType;
import org.zim.protocol.RemoteCommand;
import org.zim.protocol.command.GroupChatMessageCommand;
import org.zim.protocol.command.PrivateChatMessageCommand;
import org.zim.protocol.command.RegisterCommand;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class ProtocolTest {

    @Test
    public void testRegisterCommand() {
        RegisterCommand command = new RegisterCommand();
        command.setUserId(System.currentTimeMillis());
        command.setUserName(System.currentTimeMillis() + "");
        System.out.println(command);

        ByteBuffer bytes = ByteBuffer.wrap(command.encode());
        bytes.getInt();
        RemoteCommand decode = RemoteCommand.decode(bytes);

        Assert.assertEquals(command.toString(), decode.toString());
        System.out.println(decode);
    }

    @Test
    public void testMessageCommandGroupChatMessage() {
        GroupChatMessageCommand gcm = new GroupChatMessageCommand();
        gcm.setBody((System.currentTimeMillis() + "").getBytes(StandardCharsets.UTF_8));
        System.out.println(gcm);

        ByteBuffer bytes = ByteBuffer.wrap(gcm.encode());
        bytes.getInt();
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


        ByteBuffer bytes = ByteBuffer.wrap(pcm.encode());
        bytes.getInt();
        RemoteCommand decode = RemoteCommand.decode(bytes);

        Assert.assertEquals(pcm.toString(), decode.toString());
        System.out.println(decode);
    }


    @Test
    public void testResponseCommand() {
        RemoteCommand pcm = RemoteCommand.createResponseCommand(CommandResponseType.REGISTER_OK);
        System.out.println(pcm);

        ByteBuffer bytes = ByteBuffer.wrap(pcm.encode());
        bytes.getInt();
        RemoteCommand decode = RemoteCommand.decode(bytes);

        Assert.assertEquals(pcm.toString(), decode.toString());
        System.out.println(decode);


        pcm = RemoteCommand.createResponseCommand(CommandResponseType.ERROR, "system error");
        System.out.println(pcm);

        bytes = ByteBuffer.wrap(pcm.encode());
        bytes.getInt();
        decode = RemoteCommand.decode(bytes);

        Assert.assertEquals(pcm.toString(), decode.toString());
        System.out.println(decode);
    }
}
