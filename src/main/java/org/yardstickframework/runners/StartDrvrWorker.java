package org.yardstickframework.runners;

import java.util.List;
import java.util.Properties;
import org.yardstickframework.BenchmarkUtils;

public class StartDrvrWorker extends StartNodeWorker {

    private String drvrLogDirFullName;

    private String mainClass = "org.yardstickframework.BenchmarkDriverStartUp";


    public StartDrvrWorker(Properties runProps) {
        super(runProps);
    }

    public StartDrvrWorker(Properties runProps, String cfgFullStr) {
        super(runProps, cfgFullStr);
    }

    @Override public void beforeWork() {
        super.beforeWork();

        drvrLogDirFullName = String.format("%s/log_servers", baseLogDirFullName);
    }

    @Override public void doWork(String ip, String dateTime, int cnt, int total) {
        final String servStartTime = BenchmarkUtils.hms();

        BenchmarkUtils.println(String.format("Starting driver node on the host %s with id %d", ip, cnt));

        System.out.println(String.format("full str = %s", getCfgFullStr()));
        System.out.println(String.format("prop path = %s", getPropPath()));

        String mkdirCmd = String.format("ssh -o StrictHostKeyChecking=no %s mkdir -p %s", ip, drvrLogDirFullName);

        runCmd(mkdirCmd);

        String logFileName = String.format("%s/%s-id%d-%s.log",
            drvrLogDirFullName,
            servStartTime,
            cnt,
            ip);

        String startCmd = String.format("%s/bin/java -Dyardstick.driver%d -cp :%s/libs/* %s -id %d %s --config %s " +
            "--logsFolder %s --remoteuser %s --currentFolder %s --scriptsFolder %s/bin > %s 2>& 1 &",
            getRemJava(),
            cnt,
            getMainDir(),
            mainClass,
            cnt,
            getCfgFullStr(),
            getPropPath(),
            drvrLogDirFullName,
            getRemUser(),
            getMainDir(),
            getMainDir(),
            logFileName);

        NodeStarter starter = new PlainNodeStarter(runProps);

        System.out.println("Start cmd:");
        System.out.println(startCmd);

        starter.startNode(ip, startCmd);
    }

    @Override public List<String> getHostList() {
        return getServList();
    }
}
