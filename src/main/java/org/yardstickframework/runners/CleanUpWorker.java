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

//        BenchmarkUtils.println(String.format("Docker ps -a result from %s: %s", ip, conts));

        if(conts.size() > 1){
            for(int i = 1; i < conts.size(); i++){
                String contId = conts.get(i).split(" ")[0];

                String stopContCmd = String.format("ssh -o StrictHostKeyChecking=no %s docker stop %s",
                        ip, contId);

                BenchmarkUtils.println(String.format("Running stop cmd on the host %s: %s", ip, stopContCmd));

                runCmd(stopContCmd);
            }
        }

//        String getImages = String.format("ssh -o StrictHostKeyChecking=no %s docker images", ip);
//
//        List<String> images = runCmd(getImages);
//
//        BenchmarkUtils.println(String.format("Docker images result from %s: %s", ip, images));
//
//
//        if(images.size() > 1){
//            for(int i = 1; i < images.size(); i++){
//                String[] lineArr = images.get(i).split(" ");
//
//                String line0 = images.get(i).split(" ")[0];
//                String line2 = images.get(i).split(" ")[2];
//
//                for(int j = 0; j<lineArr.length; j++){
//                    System.out.println(j + " +++ " + lineArr[j]);
//                }
//
//                if(line0.startsWith("yardstickserver") || line0.startsWith("yardstickdriver")) {
//                    String rmImageCmd = String.format("ssh -o StrictHostKeyChecking=no %s docker rmi %s",
//                            ip, line2);
//
//                    BenchmarkUtils.println(String.format("Running rmi cmd on the host %s: %s", ip, rmImageCmd));
//
//                    runCmd(rmImageCmd);
//                }
//            }
//        }

        return null;
    }

    @Override public String getWorkerName() {
        return getClass().getSimpleName();
    }
}
