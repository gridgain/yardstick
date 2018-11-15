package org.yardstickframework.runners;

import java.util.Properties;
import java.util.Random;

public class InDockerNodeStarter extends AbstractRunner implements NodeStarter  {
    public InDockerNodeStarter(Properties runProps) {
        super(runProps);
    }

    @Override public void startNode(String ip, String startCmd) {

        String buildDockerCmd = String.format("ssh -o StrictHostKeyChecking=no %s %s/bin/build-docker.sh",
            ip, getMainDir());

        runCmd(buildDockerCmd);

        Random r = new Random();

        int idx = r.nextInt(1000);

        String cmd = String.format("ssh -o StrictHostKeyChecking=no %s docker run --name Test%d " +
            "-v %s/output:/%s/output --network host yardstick:1.1 %s", ip, idx, getMainDir() , getMainDir(),startCmd);

        System.out.println(cmd);

        runCmd(cmd);
    }
}
