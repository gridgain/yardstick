package org.yardstickframework.runners.checkers;

import java.util.List;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.yardstickframework.runners.context.NodeInfo;
import org.yardstickframework.runners.context.RunContext;

/**
 * Node checker.
 */
public abstract class NodeChecker {
    /** */
    protected RunContext runCtx;

    /**
     *
     * @param runCtx Run context.
     */
    public NodeChecker(RunContext runCtx) {
        this.runCtx = runCtx;
    }


    /**
     * Sets {@code NodeInfo.nodeStatus} to NodeStatus.RUNNING if node is running or NodeStatus.NOT_RUNNING otherwise.
     *
     * @param nodeInfo {@code Nodeinfo} object.
     * @return {@code Nodeinfo} object.
     * @throws InterruptedException if interrupted.
     */
    public abstract NodeInfo checkNode(NodeInfo nodeInfo) throws InterruptedException;

    /**
     *
     * @return Logger.
     */
    protected Logger log(){
        return LogManager.getLogger(getClass().getSimpleName());
    }
}
