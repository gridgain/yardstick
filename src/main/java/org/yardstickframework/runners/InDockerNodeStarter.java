package org.yardstickframework.runners;

import java.util.Properties;
import java.util.Random;

public class InDockerNodeStarter extends AbstractRunner implements NodeStarter  {
    private StartNodeWorkContext workCtx;

    public InDockerNodeStarter(Properties runProps, WorkContext workCtx) {
        super(runProps);
        this.workCtx = (StartNodeWorkContext)workCtx;
    }

    @Override public NodeInfo startNode(NodeInfo nodeInfo) {

        Random r = new Random();

        int idx = r.nextInt(1000);

        String docImageName = workCtx.getDockerInfo().getImageName();

        String docImageVer = workCtx.getDockerInfo().getImageVer();

        String docContName = String.format("%s-%s-%s", nodeInfo.getNodeType(), nodeInfo.getHost(), nodeInfo.getId());

        String cmd = String.format("ssh -o StrictHostKeyChecking=no %s docker run --name %s " +
            "-v %s/output:/%s/output --network host %s:%s %s",
            nodeInfo.getHost(),
            docContName,
            getMainDir(),
            getMainDir(),
            docImageName,
            docImageVer,
            nodeInfo.getStartCmd());

        System.out.println(cmd);

        runCmd(cmd);

        nodeInfo.setDockerInfo(new DockerInfo(docImageName, docImageVer, docContName));

        return nodeInfo;
    }
}
