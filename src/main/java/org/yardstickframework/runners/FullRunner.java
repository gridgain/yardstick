package org.yardstickframework.runners;

import java.io.File;
import java.io.IOException;

public class FullRunner extends AbstractRunner{

    public static void main(String[] args) {
        for(String a : args)
            System.out.println(a);

        FullRunner runner = new FullRunner();

        String arg = args.length == 0 ? "/home/oostanin/yardstick/config/benchmark.properties" :
            args[0];

        try {
            runner.setRunProps(new File(arg));
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        if(runner.runProps.getProperty("WORK_DIR") == null)
            runner.mainDir = new File(args[1]).getParent();

        runner.run();
    }

    /**
     *
     */
    public int run(){

        String createCmd = String.format("ssh -o StrictHostKeyChecking=no 172.25.1.33 mkdir -p %s", mainDir);

        runCmd(createCmd);

        for(String name : toDeploy) {
            String copyCmd = String.format("scp -o StrictHostKeyChecking=no -rq %s/%s 172.25.1.33:%s",
                mainDir, name, mainDir);

            runCmd(copyCmd);
        }
//        System.out.println(runProps.getProperty("SERVER_HOSTS"));
//        System.out.println(runProps.getProperty("CONFIGS"));
//        System.out.println(mainDir);
//

        return 0;
    }
}
