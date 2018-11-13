package org.yardstickframework.runners;

import java.util.List;
import java.util.Properties;
import org.yardstickframework.BenchmarkUtils;

public class DeployWorker extends Worker{

    public DeployWorker(Properties runProps) {
        super(runProps);
    }

    @Override public void doWork(String ip, String dateTime, int cnt, int total) {

        String createCmd = String.format("ssh -o StrictHostKeyChecking=no %s mkdir -p %s", ip, getMainDir());

        runCmd(createCmd);

        BenchmarkUtils.println(String.format("Deploying on the host %s", ip));

        for(String name : toDeploy) {
            String cpCmd = String.format("scp -o StrictHostKeyChecking=no -rq %s/%s %s:%s",
                getMainDir(), name, ip, getMainDir());

            runCmd(cpCmd);
        }

    }

    @Override public List<String> getHostList() {
        return getFullUniqList();
    }
}
