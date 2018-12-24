package org.yardstickframework.runners.starters;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.yardstickframework.runners.Runner;
import org.yardstickframework.runners.CommandExecutionResult;
import org.yardstickframework.runners.context.NodeInfo;
import org.yardstickframework.runners.context.RunContext;

/**
 * Starts nodes.
 */
public class PlainNodeStarter extends Runner implements NodeStarter {
    /** */
    private static Map<String, String> hostJavaHomeMap = new HashMap<>();

    /**
     *
     * @param runCtx Run context.
     */
    public PlainNodeStarter(RunContext runCtx) {
        super(runCtx);
    }

    /** {@inheritDoc} */
    @Override public NodeInfo startNode(NodeInfo nodeInfo) throws InterruptedException{
        String host = nodeInfo.host();

        String param = nodeInfo.parameterString();

        String javaHome = runCtx.getHostJava(host);

        CommandExecutionResult res = null;

        try {
            String withJavaHome = String.format("%s/bin/java %s", javaHome, param);

            res = runCtx.handler().startNode(host, withJavaHome, nodeInfo.logPath());
        }
        catch (IOException e) {
            log().error(String.format("Failed to start node '%s' on the host '%s'", nodeInfo.toShortStr(), host), e);
        }

        nodeInfo.commandExecutionResult(res);

        return nodeInfo;
    }

    /**
     *
     * @param host Host.
     * @return Host Java home path.
     */
    private String getHostJavaHome(String host) {
        return runCtx.handler().getHostJavaHome(host);
    }

    /**
     *
     * @param host Host.
     */
    private void printNoJavaError(String host) {
        log().info(String.format("Failed to get default JAVA_HOME variable from the host %s", host));
        log().info(String.format("Will not start node on the host %s", host));

    }

    /**
     *
     * @param host Host.
     * @return {@code boolean} true if host already been checked or {@code false} otherwise.
     */
    private boolean checked(String host) {
        return hostJavaHomeMap.containsKey(host);
    }
}
