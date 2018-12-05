package org.yardstickframework.runners;

import java.io.IOException;
import org.yardstickframework.BenchmarkConfiguration;
import org.yardstickframework.BenchmarkUtils;

public class StartNodeWorker extends Worker {
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

    private String servLogDirFullName;

    private String servMainClass = "org.yardstickframework.BenchmarkServerStartUp";

    private String drvrLogDirFullName;

    private String drvrMainClass = "org.yardstickframework.BenchmarkDriverStartUp";

    /** */
    private BenchmarkConfiguration iterCfg;


    public StartNodeWorker(RunContext runCtx, WorkContext workCtx) {
        super(runCtx, workCtx);
    }

    public String getCfgFullStr() {
        return cfgFullStr;
    }

    @Override public void beforeWork() {
        dateTime = runCtx.getMainDateTime();

        resDirName = String.format("results-%s", dateTime);

        logDirName = String.format("logs-%s", dateTime);

        baseLogDirFullName = String.format("%s/output/%s", runCtx.getRemWorkDir(), logDirName);

        servLogDirFullName = String.format("%s/log_servers", baseLogDirFullName);

        drvrLogDirFullName = String.format("%s/log_drivers", baseLogDirFullName);
    }

    @Override public void afterWork() {
        //NO_OP
    }

    @Override public WorkResult doWork(String host, int cnt) {
        final String nodeStartTime = BenchmarkUtils.dateTime();

        StartNodeWorkContext startCtx = (StartNodeWorkContext)getWorkCtx();

        String mode = "";

        if(startCtx.getRunMode() != RunMode.PLAIN)
            mode = String.format(" (Run mode - %s)", startCtx.getRunMode());

        BenchmarkUtils.println(String.format("Starting %s node on the host %s with id %d.%s",
            getNodeTypeLowCase(startCtx),
            host,
            cnt,
            mode));

        String logDirFullName = getLogDirFullName(startCtx);

        CommandHandler hndl = new CommandHandler(runCtx);

        try {
            hndl.runMkdirCmd(host, logDirFullName);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }

        String descr = runCtx.getDescription(startCtx.getFullCfgStr());

        String logFileName = String.format("%s/%s-id%d-%s-%s.log",
            logDirFullName,
            nodeStartTime,
            cnt,
            host,
            descr);

        String paramStr = getParamStr(host, cnt, nodeStartTime, startCtx, descr);

        NodeInfo nodeInfo = new NodeInfo(startCtx.getNodeType(), host, null, String.valueOf(cnt),
            startCtx, paramStr, logFileName );

        NodeStarter starter = runCtx.getNodeStarter(nodeInfo);

        return starter.startNode(nodeInfo);
    }

    private String getParamStr(String ip, int cnt, String nodeStartTime, StartNodeWorkContext startCtx, String descr){
        String drvrResDir = String.format("%s/output/result-%s", runCtx.getRemWorkDir(), runCtx.getMainDateTime());

        String outputFolderParam = getWorkCtx().getList().size() > 1 ?
            String.format("--outputFolder %s/%d-%s", drvrResDir, cnt, ip) :
            String.format("--outputFolder %s", drvrResDir);

        String jvmOptsStr = runCtx.getProps().getProperty("JVM_OPTS") != null ?
            runCtx.getProps().getProperty("JVM_OPTS"):
            "";

        String nodeJvmOptsProp = String.format("%s_JVM_OPTS", startCtx.getNodeType());

        String nodeJvmOptsStr = runCtx.getProps().getProperty(nodeJvmOptsProp) != null ?
            runCtx.getProps().getProperty(nodeJvmOptsProp):
            "";

        String concJvmOpts = jvmOptsStr + " " + nodeJvmOptsStr;

        String gcJvmOpts = concJvmOpts.contains("PrintGC") ?
            String.format(" -Xloggc:%s/gc-%s-%s-id%d-%s-%s.log",
                getLogDirFullName(startCtx),
                nodeStartTime,
                getNodeTypeLowCase(startCtx),
                cnt,
                ip,
                descr):
            "";

        String fullJvmOpts = (concJvmOpts + " " + gcJvmOpts).replace("\"", "");

        String propPath = runCtx.getPropPath().replace(runCtx.getLocWorkDir(), runCtx.getRemWorkDir());

        String cfgStr = startCtx.getFullCfgStr().replace(runCtx.getLocWorkDir(), runCtx.getRemWorkDir());


        String paramStr = String.format("%s -Dyardstick.%s%d -cp :%s/libs/* %s -id %d %s %s --config %s " +
                "--logsFolder %s --remoteuser %s --currentFolder %s --scriptsFolder %s/bin",
            fullJvmOpts,
            getNodeTypeLowCase(startCtx),
            cnt,
            runCtx.getRemWorkDir(),
            getMainClass(startCtx),
            cnt,
            outputFolderParam,
            cfgStr,
            propPath,
            getLogDirFullName(startCtx),
            runCtx.getRemUser(),
            runCtx.getRemWorkDir(),
            runCtx.getRemWorkDir());

        return paramStr;
    }

    private String getNodeTypeLowCase(StartNodeWorkContext startCtx){
        return startCtx.getNodeType().toString().toLowerCase();
    }

    private String getLogDirFullName(StartNodeWorkContext startCtx){
        switch (startCtx.getNodeType()) {
            case SERVER:
                return servLogDirFullName;
            case DRIVER:
                return drvrLogDirFullName;
            default:
                throw new IllegalArgumentException("Unknown node type");
        }
    }

    private String getMainClass(StartNodeWorkContext startCtx){
        switch (startCtx.getNodeType()) {
            case SERVER:
                return servMainClass;
            case DRIVER:
                return drvrMainClass;
            default:
                throw new IllegalArgumentException("Unknown node type");
        }
    }

    @Override public String getWorkerName() {
        return getClass().getSimpleName();
    }
}
