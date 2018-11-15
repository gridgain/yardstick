package org.yardstickframework.runners;

import java.io.File;
import java.io.IOException;
import java.util.Properties;
import org.yardstickframework.BenchmarkUtils;

public class FullRunner extends AbstractRunner{

    public FullRunner(Properties runProps) {
        super(runProps);
    }

    public static void main(String[] args) {
        for(String a : args)
            System.out.println(a);

        FullRunner runner = new FullRunner(null);

        String arg = args.length == 0 ? "/home/oostanin/yardstick/config/benchmark.properties" :
            args[0];

        try {
            runner.setRunProps(new File(arg));

            System.out.println(String.format("setting arg = %s", arg));

            runner.setPropPath(arg);
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        if(runner.runProps.getProperty("WORK_DIR") == null) {
            runner.mainDir = new File(args[1]).getParent();

            runner.runProps.setProperty("WORK_DIR", new File(args[1]).getParent());
        }

        runner.runProps.setProperty("MAIN_DATE_TIME", BenchmarkUtils.dateTime());

        runner.run();
    }

    /**
     *
     */
    public int run(){
        Worker killWorker = new KillWorker(runProps);

        killWorker.workOnHosts();

        Worker deployWorker = new DeployWorker(runProps);

        deployWorker.workOnHosts();

        for(String cfgStr : runProps.getProperty("CONFIGS").split(",")) {
            StartNodeWorker startWorker = new StartServWorker(runProps, cfgStr.replace("\"", ""));

            startWorker.setPropPath(getPropPath());

            startWorker.workOnHosts();
        }

        return 0;
    }
}
