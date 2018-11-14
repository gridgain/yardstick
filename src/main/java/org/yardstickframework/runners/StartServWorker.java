package org.yardstickframework.runners;

import java.util.List;
import java.util.Properties;
import org.yardstickframework.BenchmarkUtils;

public class StartServWorker extends StartNodeWorker {

    private String servLogDirFullName;


    public StartServWorker(Properties runProps) {
        super(runProps);
    }



    @Override public void beforeWork() {
        super.beforeWork();

        servLogDirFullName = String.format("%s/log_servers", baseLogDirFullName);
    }

    @Override public void doWork(String ip, String dateTime, int cnt, int total) {
        final String servStartTime = BenchmarkUtils.hms();

        BenchmarkUtils.println(String.format("Starting server node on the host %s with id %d", ip, cnt));



        NodeStarter starter = new PlainNodeStarter(runProps);





    }

    @Override public List<String> getHostList() {
        return getServList();
    }
}
