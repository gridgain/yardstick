package org.yardstickframework.runners;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.yardstickframework.runners.context.NodeType;
import org.yardstickframework.runners.context.RunContext;
import org.yardstickframework.runners.workers.CheckWorkResult;
import org.yardstickframework.runners.workers.WorkResult;
import org.yardstickframework.runners.workers.host.HostWorker;

public class AbstractRunner {
    protected static final long DFLT_TIMEOUT = 300_000L;

    protected RunContext runCtx;

    protected String[] toDeploy = new String[]{"bin", "config", "libs"};

    protected String[] toClean = new String[]{"bin", "config", "libs", "output", "work"};

    protected String mainDir;

    public AbstractRunner(RunContext runCtx) {
        this.runCtx = runCtx;
    }

    protected List<String> runCmd(final String cmd){

        List<String> res = new ArrayList<>();

        final Process p;

        ExecutorService errStreamPrinter = Executors.newSingleThreadExecutor();


        try {
            p = Runtime.getRuntime().exec(cmd);
            p.waitFor();
            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));


            errStreamPrinter.submit(new Callable<Object>() {
                @Override public Object call() throws IOException {
                    String line = "";


                    BufferedReader errReader = new BufferedReader(new InputStreamReader(p.getErrorStream()));

                    while ((line = errReader.readLine())!= null)
                        System.out.println(String.format("Command '%s' returned error line: %s:", cmd, line));

                    return null;
                }
            });

            String line = "";

            while ((line = reader.readLine())!= null) {
                res.add(line);

                if(line.contains("Successfully built "))
                     log().info(line);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        errStreamPrinter.shutdown();

        return res;
    }

    protected void checkPlain(HostWorker checkWorker) {
        List<WorkResult> checks = checkWorker.workOnHosts();

        for (WorkResult check : checks) {
            CheckWorkResult res = (CheckWorkResult)check;

            if (res.exit())
                System.exit(1);
        }
    }

    protected List<String> getHosts(NodeType type){
        return type == NodeType.SERVER ? runCtx.getServList() : runCtx.getDrvrList();
    }

    protected List<String> getUniqHosts(NodeType type){
        return type == NodeType.SERVER ? runCtx.getServUniqList() : runCtx.getDrvrUniqList();
    }

    protected Logger log(){
        Logger log = LogManager.getLogger(getClass().getSimpleName());

        return log;
    }




}
