package org.yardstickframework.runners;

import java.util.List;
import java.util.Properties;
import org.yardstickframework.BenchmarkUtils;

public class StartDrvrWorker extends StartNodeWorker {

    private String drvrLogDirFullName;

    private String mainClass = "org.yardstickframework.BenchmarkDriverStartUp";


    public StartDrvrWorker(Properties runProps, WorkContext workContext) {
        super(runProps, workContext);
    }

    @Override public void beforeWork() {
        super.beforeWork();

        drvrLogDirFullName = String.format("%s/log_drivers", baseLogDirFullName);
    }

    @Override public WorkResult doWork(String ip, int cnt) {
        final String drvrStartTime = BenchmarkUtils.dateTime();

        StartNodeWorkContext startCtx = (StartNodeWorkContext)getWorkContext();

        BenchmarkUtils.println(String.format("Starting driver node on the host %s with id %d", ip, cnt));

        String mkdirCmd = String.format("ssh -o StrictHostKeyChecking=no %s mkdir -p %s", ip, drvrLogDirFullName);

        runCmd(mkdirCmd);

        String descr = getDescription(startCtx.getFullCfgStr());

        String logFileName = String.format("%s/%s-id%d-%s-%s.log",
            drvrLogDirFullName,
            drvrStartTime,
            cnt,
            ip,
            descr);

        String drvrResDir = String.format("%s/output/result-%s", getMainDir(), getMainDateTime());

        String outputFolderParam = getWorkContext().getHostList().size() > 1 ?
            String.format("--outputFolder %s/%d-%s", drvrResDir, cnt, ip) :
            String.format("--outputFolder %s", drvrResDir);

        String jvmOptsStr = runProps.getProperty("JVM_OPTS") != null ?
            runProps.getProperty("JVM_OPTS"):
            "";

        String nodeJvmOptsProp = String.format("%s_JVM_OPTS", "DRIVER");

        String nodeJvmOptsStr = runProps.getProperty(nodeJvmOptsProp) != null ?
            runProps.getProperty(nodeJvmOptsProp):
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
            getRemJava(),
            fullJvmOpts,
            cnt,
            getMainDir(),
            mainClass,
            cnt,
            outputFolderParam,
            startCtx.getFullCfgStr(),
            startCtx.getPropPath(),
            drvrLogDirFullName,
            getRemUser(),
            getMainDir(),
            getMainDir(),
            logFileName);

        NodeInfo nodeInfo = new NodeInfo(NodeType.DRIVER, ip, null, String.valueOf(cnt),
            startCtx, startCmd, logFileName );

        NodeStarter starter = getNodeStarter(nodeInfo);

        return starter.startNode(nodeInfo);
    }
}
