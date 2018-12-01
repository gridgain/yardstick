package org.yardstickframework.runners;

import java.io.IOException;
import java.util.Properties;
import org.yardstickframework.BenchmarkConfiguration;
import org.yardstickframework.BenchmarkUtils;

import static org.yardstickframework.BenchmarkUtils.jcommander;

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
        super.beforeWork();

        dateTime = runCtx.getMainDateTime();

        resDirName = String.format("results-%s", dateTime);

        logDirName = String.format("logs-%s", dateTime);

        baseLogDirFullName = String.format("%s/output/%s", runCtx.getRemWorkDir(), logDirName);

        servLogDirFullName = String.format("%s/log_servers", baseLogDirFullName);

        drvrLogDirFullName = String.format("%s/log_drivers", baseLogDirFullName);
    }

    @Override public WorkResult doWork(String ip, int cnt) {
        final String nodeStartTime = BenchmarkUtils.dateTime();

        StartNodeWorkContext startCtx = (StartNodeWorkContext)getWorkCtx();

        String nodeTypeLowCase = startCtx.getNodeType().toString().toLowerCase();

        BenchmarkUtils.println(String.format("Starting %s node on the host %s with id %d",
            nodeTypeLowCase,
            ip,
            cnt));

        String logDirFullName = getLogDirFullName(startCtx);

        CommandHandler hndl = new CommandHandler(runCtx);

        try {
            hndl.runMkdirCmd(ip, logDirFullName);
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
            ip,
            descr);

        String drvrResDir = String.format("%s/output/result-%s", runCtx.getRemWorkDir(), runCtx.getMainDateTime());

        String outputFolderParam = getWorkCtx().getHostList().size() > 1 ?
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
                logDirFullName,
                nodeStartTime,
                nodeTypeLowCase,
                cnt,
                ip,
                descr):
            "";

        String fullJvmOpts = (concJvmOpts + " " + gcJvmOpts).replace("\"", "");

        String startCmd = String.format("%s -Dyardstick.%s%d -cp :%s/libs/* %s -id %d %s %s --config %s " +
                "--logsFolder %s --remoteuser %s --currentFolder %s --scriptsFolder %s/bin",
            fullJvmOpts,
            nodeTypeLowCase,
            cnt,
            runCtx.getRemWorkDir(),
            getMainClass(startCtx),
            cnt,
            outputFolderParam,
            startCtx.getFullCfgStr(),
            runCtx.getPropPath(),
            logDirFullName,
            runCtx.getRemUser(),
            runCtx.getRemWorkDir(),
            runCtx.getRemWorkDir());

//        String startCmd = String.format("%s/bin/java %s -Dyardstick.%s%d -cp :%s/libs/* %s -id %d %s %s --config %s " +
//                "--logsFolder %s --remoteuser %s --currentFolder %s --scriptsFolder %s/bin &",
//            javaHome,
//            fullJvmOpts,
//            nodeTypeLowCase,
//            cnt,
//            runCtx.getRemWorkDir(),
//            getMainClass(startCtx),
//            cnt,
//            outputFolderParam,
//            startCtx.getFullCfgStr(),
//            runCtx.getPropPath(),
//            logDirFullName,
//            runCtx.getRemUser(),
//            runCtx.getRemWorkDir(),
//            runCtx.getRemWorkDir());

        NodeInfo nodeInfo = new NodeInfo(startCtx.getNodeType(), ip, null, String.valueOf(cnt),
            startCtx, startCmd, logFileName );

        NodeStarter starter = runCtx.getNodeStarter(nodeInfo);

        return starter.startNode(nodeInfo);
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
