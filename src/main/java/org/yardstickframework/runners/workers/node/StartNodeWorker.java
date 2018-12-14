package org.yardstickframework.runners.workers.node;

import java.io.IOException;
import java.util.List;
import org.yardstickframework.BenchmarkConfiguration;
import org.yardstickframework.BenchmarkUtils;
import org.yardstickframework.runners.CommandHandler;
import org.yardstickframework.runners.context.NodeInfo;
import org.yardstickframework.runners.starters.NodeStarter;
import org.yardstickframework.runners.context.NodeType;
import org.yardstickframework.runners.context.RunMode;
import org.yardstickframework.runners.context.RunContext;

public class StartNodeWorker extends NodeWorker {
    /** */
    protected String dateTime;

    /** */
    protected String resDirName;

    /** */
    protected String logDirName;

    /** */
    protected String baseLogDirFullName;

    /** */
    protected String cfgFullStr;

    protected String servLogDirFullName;

    private String servMainClass = "org.yardstickframework.BenchmarkServerStartUp";

    protected String drvrLogDirFullName;

    private String drvrMainClass = "org.yardstickframework.BenchmarkDriverStartUp";

    /** */
    private BenchmarkConfiguration iterCfg;

    public StartNodeWorker(RunContext runCtx, List<NodeInfo> nodeList, String cfgFullStr) {
        super(runCtx, nodeList);
        this.cfgFullStr = cfgFullStr;
    }

    public String getCfgFullStr() {
        return cfgFullStr;
    }

    @Override public void beforeWork() {
        dateTime = runCtx.mainDateTime();

        resDirName = String.format("results-%s", dateTime);

        logDirName = String.format("logs-%s", dateTime);

        baseLogDirFullName = String.format("%s/output/%s", runCtx.remoteWorkDirectory(), logDirName);

        servLogDirFullName = String.format("%s/log_servers", baseLogDirFullName);

        drvrLogDirFullName = String.format("%s/log_drivers", baseLogDirFullName);
    }

    @Override public void afterWork() {
        //NO_OP
    }

    @Override public NodeInfo doWork(NodeInfo nodeInfo) throws InterruptedException {
        return startNode(nodeInfo);
    }

    protected NodeInfo startNode(NodeInfo nodeInfo) throws InterruptedException {
        final String nodeStartTime = BenchmarkUtils.dateTime();

        nodeInfo.nodeStartTime(nodeStartTime);

        String host = nodeInfo.host();

        String id  = nodeInfo.id();

        NodeType type = nodeInfo.nodeType();

        String mode = "";

        if(nodeInfo.runMode() != RunMode.PLAIN)
            mode = String.format(" (Run mode - %s)", nodeInfo.runMode());

        log().info(String.format("Starting node '%s' on the host '%s'.%s",
            nodeInfo.toShortStr(),
            host,
            mode));

        String logDirFullName = getLogDirFullName(type);

        CommandHandler hndl = new CommandHandler(runCtx);

        try {
            hndl.runMkdirCmd(host, logDirFullName);
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        String descript = runCtx.description(cfgFullStr);

        nodeInfo.description(descript);

        String logFileName = String.format("%s/%s-id%s-%s-%s.log",
            logDirFullName,
            nodeStartTime,
            id,
            host,
            descript);

        String paramStr = getParamStr(nodeInfo);

        nodeInfo.parameterString(paramStr);

        nodeInfo.logPath(logFileName);

        NodeStarter starter = runCtx.nodeStarter(nodeInfo);

        return starter.startNode(nodeInfo);
    }

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
                getLogDirFullName(type),
                nodeInfo.nodeStartTime(),
                nodeInfo.typeLow(),
                id,
                host,
                nodeInfo.description()):
            "";

        String fullJvmOpts = (concJvmOpts + " " + gcJvmOpts).replace("\"", "");

        String propPath = runCtx.propertyPath().replace(runCtx.localeWorkDirectory(), runCtx.remoteWorkDirectory());

        String cfgStr = cfgFullStr.replace(runCtx.localeWorkDirectory(), runCtx.remoteWorkDirectory());


        String paramStr = String.format("%s -Dyardstick.%s%s -cp :%s/libs/* %s -id %s %s %s --config %s " +
                "--logsFolder %s --remoteuser %s --currentFolder %s --scriptsFolder %s/bin",
            fullJvmOpts,
            nodeInfo.typeLow(),
            id,
            runCtx.remoteWorkDirectory(),
            getMainClass(type),
            id,
            outputFolderParam,
            cfgStr,
            propPath,
            getLogDirFullName(type),
            runCtx.remoteUser(),
            runCtx.remoteWorkDirectory(),
            runCtx.remoteWorkDirectory());

        return paramStr;
    }

    private String getLogDirFullName(NodeType type){
        switch (type) {
            case SERVER:
                return servLogDirFullName;
            case DRIVER:
                return drvrLogDirFullName;
            default:
                throw new IllegalArgumentException("Unknown node type");
        }
    }

    private String getMainClass(NodeType type){
        switch (type) {
            case SERVER:
                return servMainClass;
            case DRIVER:
                return drvrMainClass;
            default:
                throw new IllegalArgumentException("Unknown node type");
        }
    }
}
