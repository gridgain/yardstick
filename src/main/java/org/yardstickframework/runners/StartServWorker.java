package org.yardstickframework.runners;

import java.util.List;
import java.util.Properties;

public class StartServWorker extends StartNodeWorker {
    public StartServWorker(Properties runProps) {
        super(runProps);
    }

    @Override public void doWork(String ip, String dateTime, int cnt, int total) {

    }

    @Override public List<String> getHostList() {
        return getServList();
    }
}
