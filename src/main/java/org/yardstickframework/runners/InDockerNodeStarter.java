package org.yardstickframework.runners;

import java.io.File;
import java.util.List;
import java.util.Properties;
import java.util.Random;

public class InDockerNodeStarter extends AbstractRunner implements NodeStarter  {
    private StartNodeWorkContext workCtx;

    public InDockerNodeStarter(Properties runProps, StartNodeWorkContext workCtx) {
        super(runProps);
        this.workCtx = workCtx;
    }

    @Override public NodeInfo startNode(NodeInfo nodeInfo) {


        String docImageName = workCtx.getDockerInfo().getImageName();

        String docImageVer = workCtx.getDockerInfo().getImageVer();

        String docContName = String.format("%s-%s-%s", nodeInfo.getNodeType(), nodeInfo.getHost(), nodeInfo.getId());

        String checkCmd = String.format("ssh -o StrictHostKeyChecking=no %s docker ps -a", nodeInfo.getHost());

        List<String> resList = runCmd(checkCmd);

        String contId = getContId(resList, docContName);

        if(contId.equals("Unknown")) {
            String sleepCmd = String.format("ssh -o StrictHostKeyChecking=no %s docker run -d --name %s " +
                    " --network host %s:%s sleep infinity",
                nodeInfo.getHost(),
                docContName,
                docImageName,
                docImageVer);

            runCmd(sleepCmd);
        }

        resList = runCmd(checkCmd);

        contId = getContId(resList, docContName);

        String cmd = String.format("ssh -o StrictHostKeyChecking=no %s docker exec %s %s",
            nodeInfo.getHost(),
            docContName,
            nodeInfo.getStartCmd());

        System.out.println(cmd);

        runCmd(cmd);

        nodeInfo.setDockerInfo(new DockerInfo(docImageName, docImageVer, docContName, contId));

        return nodeInfo;
    }

    private String getContId(List<String> resList, String docContName){
        for (String str : resList){
            System.out.println("String is " + str);

            if(str.contains(docContName)) {
                System.out.println("Returning " + str.substring(0, str.indexOf(' ')));

                return str.substring(0, str.indexOf(' '));


            }
        }

        return "Unknown";
    }

}
