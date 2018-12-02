package org.yardstickframework.runners;

import java.io.IOException;
import java.util.List;
import java.util.Properties;

public class PlainNodeChecker extends AbstractRunner implements NodeChecker {
    public PlainNodeChecker(RunContext runCtx) {
        super(runCtx);
    }

    @Override public WorkResult checkNode(NodeInfo nodeInfo) {
        CommandHandler hndl = new CommandHandler(runCtx);

        NodeCheckResult res = null;

        try {
             res = hndl.checkPlainNode(nodeInfo);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }

        return res;
    }
}
