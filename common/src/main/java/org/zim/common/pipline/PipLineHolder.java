package org.zim.common.pipline;

import java.util.ArrayList;
import java.util.List;

public class PipLineHolder<R> {

    private final List<PipLineHandler<R>> handlers = new ArrayList<>();

    public List<PipLineHandler<R>> getHandlers() {
        return handlers;
    }

    public PipLineHolder<R> addLast(PipLineHandler<R> handler) {
        handlers.add(handler);
        return this;
    }

    public PipLineHandler<R> get(int idx) {
        return handlers.get(idx);
    }

    public int size() {
        return handlers.size();
    }
}
