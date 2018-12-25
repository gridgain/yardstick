package org.yardstickframework.runners.workers.node;

import java.io.IOException;
import java.util.List;
import org.yardstickframework.BenchmarkUtils;
import org.yardstickframework.runners.context.NodeInfo;
import org.yardstickframework.runners.starters.NodeStarter;
import org.yardstickframework.runners.context.NodeType;
import org.yardstickframework.runners.context.RunMode;
import org.yardstickframework.runners.context.RunContext;

/**
 * Starts node.
 */
public class StartNodeWorker extends NodeWorker {
    /** */
    private String dateTime;

    /** */
    private String logDirName;

    /** */
    protected String baseLogDirFullName;

    /** */
    private String cfgFullStr;

    /** */
    protected String servLogDirFullName;

    /** */
    private String servMainCls = "org.yardstickframework.BenchmarkServerStartUp";

    /** */
    protected String drvrLogDirFullName;

    /** */
    private String drvrMainCls = "org.yardstickframework.BenchmarkDriverStartUp";

    /**
     * Constructor.
     *
     * @param runCtx Run context.
     * @param nodeList Main list of NodeInfo objects to work with.
     * @param cfgFullStr Config string.
     */
    public StartNodeWorker(RunContext runCtx, List<NodeInfo> nodeList, String cfgFullStr) {
        super(runCtx, nodeList);
        this.cfgFullStr = cfgFullStr;
    }

    /** {@inheritDoc} */
    @Override public void beforeWork() {
        dateTime = runCtx.mainDateTime();

        logDirName = String.format("logs-%s", dateTime);

        baseLogDirFullName = String.format("%s/output/%s", runCtx.remoteWorkDirectory(), logDirName);

        servLogDirFullName = String.format("%s/log_servers", baseLogDirFullName);

        drvrLogDirFullName = String.format("%s/log_drivers", baseLogDirFullName);
    }

    /** {@inheritDoc} */
    @Override public NodeInfo doWork(NodeInfo nodeInfo) throws InterruptedException {
        return startNode(nodeInfo);
    }

    /**
     * Starts node.
     *
     * @param nodeInfo Node info.
     * @return Node info.
     * @throws InterruptedException if interrupted.
     */
    NodeInfo startNode(NodeInfo nodeInfo) throws InterruptedException {
        final String nodeStartTime = BenchmarkUtils.dateTime();

        nodeInfo.nodeStartTime(nodeStartTime);

        String host = nodeInfo.host();

        String id  = nodeInfo.id();

        NodeType type = nodeInfo.nodeType();

        String mode = "";

        String descript = runCtx.description(cfgFullStr);

        if(nodeInfo.runMode() != RunMode.PLAIN)
            mode = String.format(" Run mode - %s.", nodeInfo.runMode());

        log().info(String.format("Starting node '%s' on the host '%s' (%s).%s",
            nodeInfo.toShortStr(),
            host,
            descript,
            mode));

        String logDirFullName = logDirFullName(type);

        try {
            runCtx.handler().runMkdirCmd(host, logDirFullName);
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        nodeInfo.description(descript);

        String paramStr = getParamStr(nodeInfo);

        nodeInfo.parameterString(paramStr);

        String logFileName = String.format("%s/%s-id%s-%s-%s.log",
            logDirFullName,
            nodeStartTime,
            id,
            host,
            descript);

        nodeInfo.logPath(logFileName);

        NodeStarter starter = runCtx.nodeStarter(nodeInfo);

        return starter.startNode(nodeInfo);
    }

    /**
     *
     * @param nodeInfo Node info.
     * @return Parameter string.
     */
    private String getParamStr(NodeInfo nodeInfo){
        String host = nodeInfo.host();

        String id  = nodeInfo.id();

        NodeType type = nodeInfo.nodeType();

        String drvrResDir = String.format("%s/output/result-%s", runCtx.remoteWorkDirectory(), runCtx.mainDateTime());

        String outputFolderParam = getNodeListSize() > 1 ?
            String.format("--outputFolder %s/%s-%s", drvrResDir, id, host) :
            String.format("--outputFolder %s", drvrResDir);

        String jvmOptsStr = runCtx.properties().getProperty("JVM_OPTS") != null ?
            runCtx.properties().getProperty("JVM_OPTS"):
            "";

        String nodeJvmOptsProp = String.format("%s_JVM_OPTS", type);

        String nodeJvmOptsStr = runCtx.properties().getProperty(nodeJvmOptsProp) != null ?
            runCtx.properties().getProperty(nodeJvmOptsProp):
            "";

        String concJvmOpts = jvmOptsStr + " " + nodeJvmOptsStr;

        String gcJvmOpts = concJvmOpts.contains("PrintGC") ?
            String.format(" -Xloggc:%s/gc-%s-%s-id%s-%s-%s.log",
                logDirFullName(type),
                nodeInfo.nodeStartTime(),
                nodeInfo.typeLow(),
                id,
                host,
                nodeInfo.description()):
            "";

        String fullJvmOpts = (concJvmOpts + " " + gcJvmOpts).replace("\"", "");

        String propPath = runCtx.propertyPath().replace(runCtx.localeWorkDirectory(), runCtx.remoteWorkDirectory());

        String cfgStr = cfgFullStr.replace(runCtx.localeWorkDirectory(), runCtx.remoteWorkDirectory());


        return String.format("%s -Dyardstick.%s%s -cp :%s/libs/* %s -id %s %s %s --config %s " +
                "--logsFolder %s --remoteuser %s --currentFolder %s --scriptsFolder %s/bin",
            fullJvmOpts,
            nodeInfo.typeLow(),
            id,
            runCtx.remoteWorkDirectory(),
            mainClass(type),
            id,
            outputFolderParam,
            cfgStr,
            propPath,
            logDirFullName(type),
            runCtx.remoteUser(),
            runCtx.remoteWorkDirectory(),
            runCtx.remoteWorkDirectory());
    }

    /**
     *
     * @param type Node type.
     * @return Path to log directory.
     */
    private String logDirFullName(NodeType type){
        switch (type) {
            case SERVER:
                return servLogDirFullName;
            case DRIVER:
                return drvrLogDirFullName;
            default:
                throw new IllegalArgumentException("Unknown node type");
        }
    }

    /**
     *
     * @param type Node type
     * @return Main class to start node.
     */
    private String mainClass(NodeType type){
        switch (type) {
            case SERVER:
                return servMainCls;
            case DRIVER:
                return drvrMainCls;
            default:
                throw new IllegalArgumentException("Unknown node type");
        }
    }
}
