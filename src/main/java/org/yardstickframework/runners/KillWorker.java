package org.yardstickframework.runners;

import java.util.List;
import java.util.Properties;
import org.yardstickframework.BenchmarkUtils;

public class KillWorker extends Worker{

    public KillWorker(Properties runProps) {
        super(runProps);
    }

    @Override public void doWork(String ip, String dateTime, int cnt, int total) {

        String killCmd = String.format("ssh -o StrictHostKeyChecking=no %s pkill -9 -f \"Dyardstick.server\"", ip);

        runCmd(killCmd);
    }

    @Override public List<String> getHostList() {
        return getFullUniqList();
    }
}
