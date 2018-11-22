package org.yardstickframework.runners;

import org.yardstickframework.BenchmarkUtils;

public class StartDrvrWorker extends StartNodeWorker {

    private String drvrLogDirFullName;

    private String mainClass = "org.yardstickframework.BenchmarkDriverStartUp";

    public StartDrvrWorker(RunContext runCtx, StartNodeWorkContext workCtx) {
        super(runCtx, workCtx);
    }

    @Override public void beforeWork() {
        super.beforeWork();

        drvrLogDirFullName = String.format("%s/log_drivers", baseLogDirFullName);
    }

    @Override public WorkResult doWork(String ip, int cnt) {
        final String drvrStartTime = BenchmarkUtils.dateTime();

        StartNodeWorkContext startCtx = (StartNodeWorkContext)getWorkCtx();

        String javaHome = runCtx.getRemJavaHome();

        if(startCtx.getDockerInfo() != null && startCtx.getDockerInfo().getJavaHome() != null)
            javaHome = startCtx.getDockerInfo().getJavaHome();

        BenchmarkUtils.println(String.format("Starting driver node on the host %s with id %d", ip, cnt));

        String mkdirCmd = String.format("ssh -o StrictHostKeyChecking=no %s mkdir -p %s", ip, drvrLogDirFullName);

        runCmd(mkdirCmd);

//        String descr = getDescription(startCtx.getFullCfgStr());
        String descr = "TO_DO";

        String logFileName = String.format("%s/%s-id%d-%s-%s.log",
            drvrLogDirFullName,
            drvrStartTime,
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

        String nodeJvmOptsProp = String.format("%s_JVM_OPTS", "DRIVER");

        String nodeJvmOptsStr = runCtx.getProps().getProperty(nodeJvmOptsProp) != null ?
            runCtx.getProps().getProperty(nodeJvmOptsProp):
            "";

        String concJvmOpts = jvmOptsStr + " " + nodeJvmOptsStr;

        String gcJvmOpts = concJvmOpts.contains("PrintGC") ?
            String.format(" -Xloggc:%s/gc-%s-driver-id%d-%s-%s.log",
                drvrLogDirFullName,
                drvrStartTime,
                cnt,
                ip,
                descr):
            "";

        String fullJvmOpts = (concJvmOpts + " " + gcJvmOpts).replace("\"", "");

        String startCmd = String.format("%s/bin/java %s -Dyardstick.driver%d -cp :%s/libs/* %s -id %d %s %s --config %s " +
            "--logsFolder %s --remoteuser %s --currentFolder %s --scriptsFolder %s/bin > %s 2>& 1 &",
            javaHome,
            fullJvmOpts,
            cnt,
            runCtx.getRemWorkDir(),
            mainClass,
            cnt,
            outputFolderParam,
            startCtx.getFullCfgStr(),
            runCtx.getPropPath(),
            drvrLogDirFullName,
            runCtx.getRemUser(),
            runCtx.getRemWorkDir(),
            runCtx.getRemWorkDir(),
            logFileName);

        NodeInfo nodeInfo = new NodeInfo(NodeType.DRIVER, ip, null, String.valueOf(cnt),
            startCtx, startCmd, logFileName );

        NodeStarter starter = runCtx.getNodeStarter(nodeInfo);

        return starter.startNode(nodeInfo);
    }

}
