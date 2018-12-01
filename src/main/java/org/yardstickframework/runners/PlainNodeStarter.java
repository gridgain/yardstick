package org.yardstickframework.runners;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.yardstickframework.BenchmarkUtils;

public class PlainNodeStarter extends AbstractRunner implements NodeStarter {

    private static Map<String, String> hostJavaHomeMap = new HashMap<>();

    public PlainNodeStarter(RunContext runCtx) {
        super(runCtx);
    }

    @Override public NodeInfo startNode(NodeInfo nodeInfo) {
        CommandHandler hndl = new CommandHandler(runCtx);

        String host = nodeInfo.getHost();

        String cmd = nodeInfo.getStartCmd();

        String javaHome = null;

        if (runCtx.getRemJavaHome() != null)
            javaHome = runCtx.getRemJavaHome();
        else {
            if (!checked(host)) {
                javaHome = getHostJavaHome(host);

                synchronized (this) {
                    hostJavaHomeMap.put(host, javaHome);
                }

                if (javaHome == null || javaHome.isEmpty()) {
                    printNoJavaError(host);

                    return null;
                }
                else{
                    BenchmarkUtils.println(String.format("JAVA_HOME is not defined in property file. Using default " +
                        "JAVA_HOME %s on the host %s", javaHome, host));
                }
            }
            else
                javaHome = hostJavaHomeMap.get(host);

            if (javaHome == null || javaHome.isEmpty()) {
                printNoJavaError(host);

                return null;
            }
        }

        String withJavaHome = String.format("%s/bin/java %s", javaHome, cmd);

        //        BenchmarkUtils.println("Running start node cmd: " + cmd);
//        BenchmarkUtils.println("Running start node cmd: " + cmd.replaceAll(runCtx.getRemWorkDir(), "<MAIN_DIR>"));

        try {
            hndl.runCmd(host, withJavaHome);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }

        return nodeInfo;
    }

    private String getHostJavaHome(String host) {
        return new CommandHandler(runCtx).getHostJavaHome(host);
    }

    private void printNoJavaError(String host) {
        BenchmarkUtils.println(String.format("Failed to get default JAVA_HOME variable from the host %s", host));
        BenchmarkUtils.println(String.format("Will not start node on the host %s", host));

    }

    private boolean checked(String host) {
        return hostJavaHomeMap.containsKey(host);
    }
}
