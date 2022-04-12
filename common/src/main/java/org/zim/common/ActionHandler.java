package org.zim.common;

import java.io.IOException;
import java.nio.channels.SelectionKey;


@FunctionalInterface
public interface ActionHandler {

    void action(SelectionKey key) throws IOException;
}
