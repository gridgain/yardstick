package org.yardstickframework.runners;

import java.io.IOException;
import java.util.List;
import java.util.Properties;

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
