package org.yardstickframework.runners.starters;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.yardstickframework.runners.context.NodeInfo;
import org.yardstickframework.runners.context.RunContext;

/**
 * Node starter.
 */
public abstract class NodeStarter {
    /** */
    protected RunContext runCtx;

    /**
     *
     * @param runCtx Run context.
     */
    public NodeStarter(RunContext runCtx) {
        this.runCtx = runCtx;
    }

    /**
     *
     * @param nodeInfo Node info.
     * @return Node info.
     * @throws InterruptedException if interrupted.
     */
    public abstract NodeInfo startNode(NodeInfo nodeInfo) throws InterruptedException;

    /**
     *
     * @return Logger.
     */
    protected Logger log(){
        return LogManager.getLogger(getClass().getSimpleName());
    }
}
