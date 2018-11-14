package org.yardstickframework.runners;

import java.util.Properties;

public class PlainNodeStarter extends AbstractRunner implements NodeStarter  {
    public PlainNodeStarter(Properties runProps) {
        super(runProps);
    }

    @Override public void startNode(String ip, String startCmd) {
        String cmd = String.format("ssh -o StrictHostKeyChecking=no %s nohup %s", ip, startCmd);

        runCmd(cmd);
    }
}
