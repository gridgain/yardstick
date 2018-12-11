package org.yardstickframework.runners.checkers;

import java.io.IOException;
import org.yardstickframework.runners.AbstractRunner;
import org.yardstickframework.runners.CommandHandler;
import org.yardstickframework.runners.context.NodeInfo;
import org.yardstickframework.runners.context.RunContext;

public class PlainNodeChecker extends AbstractRunner implements NodeChecker {
    private CommandHandler hndl;

    public PlainNodeChecker(RunContext runCtx) {
        super(runCtx);

        hndl = new CommandHandler(runCtx);
    }

    @Override public NodeInfo checkNode(NodeInfo nodeInfo) throws InterruptedException{
        try {
            return hndl.checkPlainNode(nodeInfo);
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        return nodeInfo;
    }
}
