package org.yardstickframework.runners;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import org.yardstickframework.BenchmarkUtils;
import org.yardstickframework.runners.docker.DockerContext;

public class InDockerNodeStarter extends AbstractRunner implements NodeStarter  {
    private StartNodeWorkContext workCtx;

    public InDockerNodeStarter(RunContext runCtx, StartNodeWorkContext workCtx) {
        super(runCtx);
        this.workCtx = workCtx;
    }

    @Override public NodeInfo startNode(NodeInfo nodeInfo) {
        String contName = String.format("YARDSTICK_%s_%s", nodeInfo.getNodeType(), nodeInfo.getId());

        String nodeLogDir = new File(nodeInfo.getLogPath()).getParent();

        CommandHandler hndl = new CommandHandler(runCtx);

        String javaParams = nodeInfo.getStartCmd();

        NodeType type = nodeInfo.getNodeType();

        String javaHome = type == NodeType.SERVER ?
            runCtx.getDockerContext().getServerDockerJavaHome() :
            runCtx.getDockerContext().getDriverDockerJavaHome() ;

        String startNodeCmd = String.format("%s/bin/java %s", javaHome, javaParams);

        try {
            String mkdirCmd = String.format("exec %s mkdir -p %s", contName, nodeLogDir);

            String cmd = String.format("exec %s nohup %s", contName, startNodeCmd);

            hndl.runDockerCmd(nodeInfo.getHost(), mkdirCmd);

//            BenchmarkUtils.println("Running start node cmd: " + cmd);

            hndl.runDockerCmd(nodeInfo.getHost(), cmd);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }

        nodeInfo.setDockerInfo(new DockerInfo(null, null, contName, null));

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
