package org.yardstickframework.runners;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.yardstickframework.BenchmarkUtils;

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
                     BenchmarkUtils.println(line);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        errStreamPrinter.shutdown();

        return res;
    }

    protected List<String> getHosts(NodeType type){
        return type == NodeType.SERVER ? runCtx.getServList() : runCtx.getDrvrList();
    }

    protected List<String> getUniqHosts(NodeType type){
        return type == NodeType.SERVER ? runCtx.getServUniqList() : runCtx.getDrvrUniqList();
    }




}
