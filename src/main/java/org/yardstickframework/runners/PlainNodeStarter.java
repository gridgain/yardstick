package org.yardstickframework.runners;

import java.io.IOException;

public class PlainNodeStarter extends AbstractRunner implements NodeStarter  {
    public PlainNodeStarter(RunContext runCtx) {
        super(runCtx);
    }

    @Override public NodeInfo startNode(NodeInfo nodeInfo) {
        CommandHandler hndl = new CommandHandler(runCtx);

        String host = nodeInfo.getHost();

        String cmd = nodeInfo.getStartCmd();

        String withJavaHome = String.format("%s/bin/java %s", runCtx.getRemJavaHome(), cmd);

        //        BenchmarkUtils.println("Running start node cmd: " + cmd);
//        BenchmarkUtils.println("Running start node cmd: " + cmd.replaceAll(runCtx.getRemWorkDir(), "<MAIN_DIR>"));

        try {
            hndl.runCmd(host, withJavaHome, "");
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }

        return nodeInfo;
    }
}
