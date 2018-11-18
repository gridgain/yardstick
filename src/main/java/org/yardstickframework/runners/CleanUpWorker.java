package org.yardstickframework.runners;

import org.yardstickframework.BenchmarkUtils;

import java.util.List;
import java.util.Properties;

public class CleanUpWorker extends Worker{

    public CleanUpWorker(Properties runProps, WorkContext workCtx) {
        super(runProps, workCtx);
    }

    @Override public WorkResult doWork(String ip, int cnt) {

        String getConts = String.format("ssh -o StrictHostKeyChecking=no %s docker ps -a", ip);

        List<String> conts = runCmd(getConts);

        if(conts.size() > 1){
            for(int i = 1; i < conts.size(); i++){
                String contId = conts.get(i).split(" ")[0];

                String stopContCmd = String.format("ssh -o StrictHostKeyChecking=no %s docker stop %s",
                        ip, contId);

                runCmd(stopContCmd);
            }
        }

        String getImages = String.format("ssh -o StrictHostKeyChecking=no %s docker ps -a", ip);

        List<String> images = runCmd(getImages);

        if(images.size() > 1){
            for(int i = 1; i < images.size(); i++){
                String line = images.get(i).split(" ")[0];

                if(line.startsWith("yardstickserver") || line.startsWith("yardstickdriver")) {
                    String rmImageCmd = String.format("ssh -o StrictHostKeyChecking=no %s docker rmi %s",
                            ip, line);

                    runCmd(rmImageCmd);
                }
            }
        }

        return null;
    }
}
