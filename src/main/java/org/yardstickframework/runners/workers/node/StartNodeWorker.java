package org.yardstickframework.runners.workers.node;

import java.io.IOException;
import java.util.List;
import org.yardstickframework.BenchmarkConfiguration;
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

    /** */
    private String warmup;

    /** */
    private String duration;

    /** Initial duration. */
    protected Long initDuration;

    /**
     * Constructor.
     *
     * @param runCtx Run context.
     * @param nodeList Main list of NodeInfo objects to work with.
     * @param cfgFullStr Config string.
     */
    public StartNodeWorker(RunContext runCtx, List<NodeInfo> nodeList, String cfgFullStr) {
        super(runCtx, nodeList);

        // We need to extract data from config string into BenchmarkConfiguration object, set warmap and durations
        // from that object and than remove --warmap (-w) and -- duration (-d) from config string in order to oveerride
        // those values later if needed.
        parseConfigString(cfgFullStr);
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

        String id = nodeInfo.id();

        NodeType type = nodeInfo.nodeType();

        String mode = "";

        String descript = runCtx.description(cfgFullStr);

        if (nodeInfo.runMode() != RunMode.PLAIN)
            mode = String.format(" Run mode - '%s';", nodeInfo.runMode());

        String wd = nodeInfo.nodeType() == NodeType.DRIVER ?
            String.format("Warmup=%s; Duration=%s; ", warmup, duration) : "";

        log().info(String.format("Starting node '%s' on the host '%s'; %sDescription='%s';%s",
            nodeInfo.toShortStr(),
            host,
            wd,
            descript,
            mode));

        String logDirFullName = logDirFullName(nodeInfo);

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
     * @param nodeInfo Node info.
     * @return Parameter string.
     */
    private String getParamStr(NodeInfo nodeInfo) {
        String host = nodeInfo.host();

        String id = nodeInfo.id();

        NodeType type = nodeInfo.nodeType();

        String drvrResDir = String.format("%s/output/result-%s", runCtx.remoteWorkDirectory(), runCtx.mainDateTime());

        String outputFolderParam = getNodeListSize() > 1 ?
            String.format("--outputFolder %s/%s-%s", drvrResDir, id, host) :
            String.format("--outputFolder %s", drvrResDir);

        String jvmOptsStr = runCtx.properties().getProperty("JVM_OPTS") != null ?
            runCtx.properties().getProperty("JVM_OPTS") :
            "";

        String nodeJvmOptsProp = String.format("%s_JVM_OPTS", type);

        String nodeJvmOptsStr = runCtx.properties().getProperty(nodeJvmOptsProp) != null ?
            runCtx.properties().getProperty(nodeJvmOptsProp) :
            "";

        String concJvmOpts = jvmOptsStr + " " + nodeJvmOptsStr;

        String gcJvmOpts = concJvmOpts.contains("PrintGC") ?
            String.format(" -Xloggc:%s/gc-%s-%s-id%s-%s-%s.log",
                logDirFullName(nodeInfo),
                nodeInfo.nodeStartTime(),
                nodeInfo.typeLow(),
                id,
                host,
                nodeInfo.description()) :
            "";

        String fullJvmOpts = (concJvmOpts + " " + gcJvmOpts).replace("\"", "");

        String propPath = runCtx.propertyPath().replace(runCtx.localeWorkDirectory(), runCtx.remoteWorkDirectory());

        String cfgStr = cfgFullStr.replace(runCtx.localeWorkDirectory(), runCtx.remoteWorkDirectory());

        return String.format("%s -Dyardstick.%s%s -cp :%s/libs/* %s -id %s %s %s --warmup %s --duration %s " +
                "--config %s --logsFolder %s --remoteuser %s --currentFolder %s --scriptsFolder %s/bin",
            fullJvmOpts,
            nodeInfo.typeLow(),
            id,
            runCtx.remoteWorkDirectory(),
            mainClass(type),
            id,
            outputFolderParam,
            cfgStr,
            warmup,
            duration,
            propPath,
            logDirFullName(nodeInfo),
            runCtx.remoteUser(),
            runCtx.remoteWorkDirectory(),
            runCtx.remoteWorkDirectory());
    }

    /**
     * @param type Node type.
     * @return Path to log directory.
     */
    private String logDirFullName(NodeInfo nodeInfo) {
        String path;

        NodeType type = nodeInfo.nodeType();

        switch (type) {
            case SERVER:
                path = servLogDirFullName;

                break;
            case DRIVER:
                path = drvrLogDirFullName;

                break;
            default:
                throw new IllegalArgumentException("Unknown node type");
        }

        // Log directory name suffix for non PLAIN run modes.
        String runModeSuf = "";

        if (nodeInfo.runMode() != RunMode.PLAIN)
            runModeSuf = String.format("_%s", nodeInfo.runMode().toString().toLowerCase());

        return path + runModeSuf;
    }

    /**
     * @param type Node type
     * @return Main class to start node.
     */
    private String mainClass(NodeType type) {
        switch (type) {
            case SERVER:
                return servMainCls;
            case DRIVER:
                return drvrMainCls;
            default:
                throw new IllegalArgumentException("Unknown node type");
        }
    }

    /**
     * @param cfgStr Config string.
     * @return Benchmark configuration.
     */
    private BenchmarkConfiguration parseConfigString(String cfgStr) {
        BenchmarkConfiguration cfg = new BenchmarkConfiguration();

        String[] toNewCfg = cfgStr.split(" ");

        BenchmarkUtils.jcommander(toNewCfg, cfg, "");

        this.warmup = String.valueOf(cfg.warmup());

        this.duration = String.valueOf(cfg.duration());

        this.initDuration = cfg.duration();

        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < toNewCfg.length; i++) {
            if (toNewCfg[i].equals("-w")
                || toNewCfg[i].equals("--warmup")
                || toNewCfg[i].equals("-d")
                || toNewCfg[i].equals("--duration")) {
                i++;

                continue;
            }
            else {
                sb.append(toNewCfg[i]);

                if (i < toNewCfg.length - 1)
                    sb.append(" ");
            }
        }

        String newCfgStr = sb.toString();

        log().debug(String.format("Config string after removing warmap and duration: %s", newCfgStr));

        this.cfgFullStr = newCfgStr;

        return cfg;
    }

    /**
     * @return Warmup.
     */
    public String warmup() {
        return warmup;
    }

    /**
     * @param warmup New warmup.
     */
    public void warmup(String warmup) {
        this.warmup = warmup;
    }

    /**
     * @return Duration.
     */
    public String duration() {
        return duration;
    }

    /**
     * @param duration New duration.
     */
    public void duration(String duration) {
        this.duration = duration;
    }
}
