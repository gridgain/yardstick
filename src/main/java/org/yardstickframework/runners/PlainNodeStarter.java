package org.yardstickframework.runners;

import java.util.Properties;
import org.yardstickframework.BenchmarkUtils;

public class PlainNodeStarter extends AbstractRunner implements NodeStarter  {
    public PlainNodeStarter(Properties runProps) {
        super(runProps);
    }

    @Override public NodeInfo startNode(NodeInfo nodeInfo) {
        String cmd = String.format("ssh -o StrictHostKeyChecking=no %s nohup %s",
            nodeInfo.getHost(), nodeInfo.getStartCmd());

        //        BenchmarkUtils.println("Running start node cmd: " + cmd);
//        BenchmarkUtils.println("Running start node cmd: " + cmd.replaceAll(getMainDir(), "<MAIN_DIR>"));

        runCmd(cmd);

        return nodeInfo;
    }
}
