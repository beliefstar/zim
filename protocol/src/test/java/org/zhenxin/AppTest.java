package org.zhenxin;

import org.junit.Test;

import java.nio.ByteBuffer;

/**
 * Unit test for simple App.
 */
public class AppTest 
{
    /**
     * Rigorous Test :-)
     */
    @Test
    public void shouldAnswerWithTrue()
    {
        ByteBuffer buffer = ByteBuffer.allocate(64);
        for (int i = 0; i < 30; i++) {
            buffer.put((byte) i);
        }
        buffer.flip();
        buffer.get(new byte[10]);
        ByteBuffer slice = buffer.slice();
        System.out.println(slice);
    }
}
