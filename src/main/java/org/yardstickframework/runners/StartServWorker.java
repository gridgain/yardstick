package org.yardstickframework.runners;

import java.util.List;
import java.util.Properties;
import org.yardstickframework.BenchmarkUtils;

public class StartServWorker extends StartNodeWorker {

    private String servLogDirFullName;

    private String mainClass = "org.yardstickframework.BenchmarkServerStartUp";


    public StartServWorker(Properties runProps, WorkContext workCtx) {
        super(runProps, workCtx);
    }

    @Override public void beforeWork() {
        super.beforeWork();

        servLogDirFullName = String.format("%s/log_servers", baseLogDirFullName);
    }

    @Override public WorkResult doWork(String ip, int cnt) {
        final String servStartTime = BenchmarkUtils.hms();

        StartNodeWorkContext startCtx = (StartNodeWorkContext)getWorkContext();

        BenchmarkUtils.println(String.format("Starting server node on the host %s with id %d", ip, cnt));

//        System.out.println(String.format("full str = %s", getCfgFullStr()));
//        System.out.println(String.format("prop path = %s", getPropPath()));

        String mkdirCmd = String.format("ssh -o StrictHostKeyChecking=no %s mkdir -p %s", ip, servLogDirFullName);

        runCmd(mkdirCmd);

        String logFileName = String.format("%s/%s-id%d-%s.log",
            servLogDirFullName,
            servStartTime,
            cnt,
            ip);

        String startCmd = String.format("%s/bin/java -Dyardstick.server%d -cp :%s/libs/* %s -id %d %s --config %s " +
            "--logsFolder %s --remoteuser %s --currentFolder %s --scriptsFolder %s/bin > %s 2>& 1 &",
            getRemJava(),
            cnt,
            getMainDir(),
            mainClass,
            cnt,
            startCtx.getFullCfgStr(),
            startCtx.getPropPath(),
            servLogDirFullName,
            getRemUser(),
            getMainDir(),
            getMainDir(),
            logFileName);

        NodeStarter starter = startCtx.getStartMode() == StartMode.PLAIN ?
            new PlainNodeStarter(runProps):
            new InDockerNodeStarter(runProps, startCtx);

        NodeInfo nodeInfo = new NodeInfo(NodeType.SERVER, ip, null, String.valueOf(cnt), startCmd, logFileName );

        return starter.startNode(nodeInfo);
    }
}
