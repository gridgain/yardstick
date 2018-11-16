package org.yardstickframework.runners;

import java.util.Properties;
import java.util.Random;

public class InDockerNodeStarter extends AbstractRunner implements NodeStarter  {
    public InDockerNodeStarter(Properties runProps) {
        super(runProps);
    }

    @Override public NodeInfo startNode(NodeInfo nodeInfo) {

        Random r = new Random();

        int idx = r.nextInt(1000);

        String cmd = String.format("ssh -o StrictHostKeyChecking=no %s docker run --name Test%d " +
            "-v %s/output:/%s/output --network host yardstick:1.1 %s", nodeInfo.getHost(), idx, getMainDir(),
            getMainDir(), nodeInfo.getStartCmd());

        System.out.println(cmd);

        runCmd(cmd);
    }
}
