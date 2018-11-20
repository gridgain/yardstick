package org.yardstickframework.runners;

import java.io.File;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import org.yardstickframework.BenchmarkUtils;

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
//            BenchmarkUtils.println(String.format("No running container on the host %s for node %s-%s.",
//                nodeInfo.getHost(), nodeInfo.getNodeType(), nodeInfo.getId()));

            BenchmarkUtils.println(String.format("Starting docker container on the host %s for node %s-%s.",
                nodeInfo.getHost(), nodeInfo.getNodeType(), nodeInfo.getId()));

            String sleepCmd = String.format("ssh -o StrictHostKeyChecking=no %s docker run %s -d --name %s " +
                    " --network host %s:%s sleep 365d",
                nodeInfo.getHost(),
                docContName,
                docImageName,
                docImageVer);

//            BenchmarkUtils.println("Running start docker cmd: " + sleepCmd);

            runCmd(sleepCmd);
        }

        resList = runCmd(checkCmd);

        contId = getContId(resList, docContName);

        String nodeLogDir = new File(nodeInfo.getLogPath()).getParent();

        String mkdirCmd = String.format("ssh -o StrictHostKeyChecking=no %s docker exec %s mkdir -p %s",
            nodeInfo.getHost(), docContName, nodeLogDir);

        runCmd(mkdirCmd);

        String cmd = String.format("ssh -o StrictHostKeyChecking=no %s docker exec %s %s",
            nodeInfo.getHost(),
            docContName,
            nodeInfo.getStartCmd());

//        BenchmarkUtils.println("Running start node cmd: " + cmd);
        BenchmarkUtils.println("Running start node cmd: " + cmd.replaceAll(getMainDir(), "<MAIN_DIR>"));

        runCmd(cmd);

        nodeInfo.setDockerInfo(new DockerInfo(docImageName, docImageVer, docContName, contId));

        return nodeInfo;
    }

    private String getContId(List<String> resList, String docContName){
        for (String str : resList){
//            System.out.println("String is " + str);

            if(str.contains(docContName)) {
//                System.out.println("Returning " + str.substring(0, str.indexOf(' ')));

                return str.substring(0, str.indexOf(' '));


            }
        }

        return "Unknown";
    }

}
