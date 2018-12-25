package org.yardstickframework.runners.checkers;

import java.io.IOException;
import org.yardstickframework.runners.Runner;
import org.yardstickframework.runners.context.NodeInfo;
import org.yardstickframework.runners.context.RunContext;

/**
 * Node checker for plain run.
 */
public class PlainNodeChecker extends NodeChecker {
    /**
     * Constructor.
     *
     * @param runCtx Run context.
     */
    public PlainNodeChecker(RunContext runCtx) {
        super(runCtx);
    }

    /** {@inheritDoc} */
    @Override public NodeInfo checkNode(NodeInfo nodeInfo) throws InterruptedException{
        try {
            return runCtx.handler().checkPlainNode(nodeInfo);
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        return nodeInfo;
    }
}
