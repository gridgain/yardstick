package org.yardstickframework.runners;

import org.yardstickframework.BenchmarkUtils;

public class StartServWorker extends StartNodeWorker {



    public StartServWorker(RunContext runCtx, StartNodeWorkContext workCtx) {
        super(runCtx, workCtx);
    }

    @Override public void beforeWork() {
        super.beforeWork();

    }

    @Override public WorkResult doWork(String ip, int cnt) {
        final String servStartTime = BenchmarkUtils.hms();

        StartNodeWorkContext startCtx = (StartNodeWorkContext)getWorkCtx();

        String javaHome = runCtx.getRemJavaHome();

        if(startCtx.getDockerInfo() != null && startCtx.getDockerInfo().getJavaHome() != null)
            javaHome = startCtx.getDockerInfo().getJavaHome();

        BenchmarkUtils.println(String.format("Starting server node on the host %s with id %d", ip, cnt));

//        System.out.println(String.format("full str = %s", getCfgFullStr()));
//        System.out.println(String.format("prop path = %s", getPropPath()));

        String mkdirCmd = String.format("ssh -o StrictHostKeyChecking=no %s mkdir -p %s", ip, servLogDirFullName);

        runCmd(mkdirCmd);

//        String descr = getDescription(startCtx.getFullCfgStr());
        String descr = "TO_DO";

        String logFileName = String.format("%s/%s-id%d-%s-%s.log",
            servLogDirFullName,
            servStartTime,
            cnt,
            ip,
            descr);

        String jvmOptsStr = runCtx.getProps().getProperty("JVM_OPTS") != null ?
            runCtx.getProps().getProperty("JVM_OPTS"):
            "";

        String nodeJvmOptsProp = String.format("%s_JVM_OPTS", "SERVER");

        String nodeJvmOptsStr = runCtx.getProps().getProperty(nodeJvmOptsProp) != null ?
            runCtx.getProps().getProperty(nodeJvmOptsProp):
            "";

        String concJvmOpts = jvmOptsStr + " " + nodeJvmOptsStr;

        String gcJvmOpts = concJvmOpts.contains("PrintGC") ?
            String.format(" -Xloggc:%s/gc-%s-server-id%d-%s-%s.log",
                servLogDirFullName,
                servStartTime,
                cnt,
                ip,
                descr):
            "";

        String fullJvmOpts = (concJvmOpts + " " + gcJvmOpts).replace("\"", "");

        String startCmd = String.format("%s/bin/java %s -Dyardstick.server%d -cp :%s/libs/* %s -id %d %s --config %s " +
            "--logsFolder %s --remoteuser %s --currentFolder %s --scriptsFolder %s/bin > %s 2>& 1 &",
            javaHome,
            fullJvmOpts,
            cnt,
            runCtx.getRemWorkDir(),
            mainClass,
            cnt,
            startCtx.getFullCfgStr(),
            runCtx.getPropPath(),
            servLogDirFullName,
            runCtx.getRemUser(),
            runCtx.getRemWorkDir(),
            runCtx.getRemWorkDir(),
            logFileName);

        NodeInfo nodeInfo = new NodeInfo(NodeType.SERVER, ip, null, String.valueOf(cnt),
            startCtx, startCmd, logFileName );

        NodeStarter starter = runCtx.getNodeStarter(nodeInfo);

        return starter.startNode(nodeInfo);
    }
}
