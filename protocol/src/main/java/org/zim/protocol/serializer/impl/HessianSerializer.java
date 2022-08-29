/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.zim.protocol.serializer.impl;


import com.caucho.hessian.io.Hessian2Input;
import com.caucho.hessian.io.Hessian2Output;
import com.caucho.hessian.io.SerializerFactory;
import org.zim.protocol.RemoteCommand;
import org.zim.protocol.serializer.Serializer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public class HessianSerializer implements Serializer {
    
    private final SerializerFactory serializerFactory = new SerializerFactory();
    
    @Override
    public byte[] serialize(RemoteCommand remoteCommand) {
        ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
        Hessian2Output output = new Hessian2Output(byteArray);
        output.setSerializerFactory(serializerFactory);
        try {
            output.writeObject(remoteCommand);
            output.close();
        } catch (IOException e) {
            throw new RuntimeException("IOException occurred when Hessian serializer encode!", e);
        }

        byte[] bytes = byteArray.toByteArray();

        ByteBuffer buffer = ByteBuffer.allocate(bytes.length + 4).putInt(bytes.length);
        buffer.put(bytes);

        return buffer.array();
    }

    @Override
    public RemoteCommand deserialize(byte[] data) {
        if (data == null || data.length == 0) {
            return null;
        }

        Hessian2Input input = new Hessian2Input(new ByteArrayInputStream(data));
        input.setSerializerFactory(serializerFactory);
        Object resultObject;
        try {
            resultObject = input.readObject();
            input.close();
        } catch (IOException e) {
            throw new RuntimeException("IOException occurred when Hessian serializer decode!", e);
        }
        return (RemoteCommand) resultObject;
    }

    @Override
    public RemoteCommand deserialize(ByteBuffer data) {
        byte[] copy = new byte[data.limit()];
        data.get(copy);
        return deserialize(copy);
    }
}
