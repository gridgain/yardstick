package org.yardstickframework.runners;

import java.util.List;
import java.util.Properties;
import org.yardstickframework.BenchmarkUtils;

public class StartServWorker extends StartNodeWorker {

    private String servLogDirFullName;

    private String mainClass = "org.yardstickframework.BenchmarkServerStartUp";


    public StartServWorker(Properties runProps) {
        super(runProps);
    }

    public StartServWorker(Properties runProps, String cfgFullStr) {
        super(runProps, cfgFullStr);
    }

    @Override public void beforeWork() {
        super.beforeWork();

        servLogDirFullName = String.format("%s/log_servers", baseLogDirFullName);
    }

    @Override public void doWork(String ip, String dateTime, int cnt, int total) {
        final String servStartTime = BenchmarkUtils.hms();

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
            getCfgFullStr(),
            getPropPath(),
            servLogDirFullName,
            getRemUser(),
            getMainDir(),
            getMainDir(),
            logFileName);

//        String startCmd = String.format("%s/bin/java -Dyardstick.server%d -cp :%s/libs/* %s -id %d %s --config %s " +
//                "--logsFolder %s --remoteuser %s --currentFolder %s --scriptsFolder %s/bin ",
//            getRemJava(),
//            cnt,
//            getMainDir(),
//            mainClass,
//            cnt,
//            getCfgFullStr(),
//            getPropPath(),
//            servLogDirFullName,
//            getRemUser(),
//            getMainDir(),
//            getMainDir());

        NodeStarter starter = new InDockerNodeStarter(runProps);

//        System.out.println("Start cmd:");
//        System.out.println(startCmd);

        starter.startNode(ip, startCmd);
    }

    @Override public List<String> getHostList() {
        return getServList();
    }
}
