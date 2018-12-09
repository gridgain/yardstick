package org.yardstickframework.runners;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class PlainNodeStarter extends AbstractRunner implements NodeStarter {

    private static Map<String, String> hostJavaHomeMap = new HashMap<>();

    public PlainNodeStarter(RunContext runCtx) {
        super(runCtx);
    }

    @Override public NodeInfo startNode(NodeInfo nodeInfo) throws InterruptedException{
        CommandHandler hndl = new CommandHandler(runCtx);

        String host = nodeInfo.getHost();

        String cmd = nodeInfo.getParamStr();

        String javaHome = runCtx.getHostJava(host);

        String withJavaHome = String.format("%s/bin/java %s", javaHome, cmd);

        //        log().info("Running start node cmd: " + cmd);
//        log().info("Running start node cmd: " + cmd.replaceAll(runCtx.getRemWorkDir(), "<MAIN_DIR>"));

        CommandExecutionResult res = null;

        try {
            res = hndl.startNode(host, withJavaHome, nodeInfo.getLogPath());
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        nodeInfo.setCmdExRes(res);

        return nodeInfo;
    }

    private String getHostJavaHome(String host) {
        return new CommandHandler(runCtx).getHostJavaHome(host);
    }

    private void printNoJavaError(String host) {
        log().info(String.format("Failed to get default JAVA_HOME variable from the host %s", host));
        log().info(String.format("Will not start node on the host %s", host));

    }

    private boolean checked(String host) {
        return hostJavaHomeMap.containsKey(host);
    }
}
