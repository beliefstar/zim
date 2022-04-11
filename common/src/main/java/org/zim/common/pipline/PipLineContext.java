package org.zim.common.pipline;

public class PipLineContext<R> {

    private final PipLineHolder<R> pipLineHolder;
    private int idx = 0;

    public PipLineContext(PipLineHolder<R> pipLineHolder) {
        this.pipLineHolder = pipLineHolder;
    }

    public void fireHandle(R command) {
        if (idx >= pipLineHolder.size()) {
            return;
        }
        pipLineHolder.get(idx++).handle(command, this);
    }
}
