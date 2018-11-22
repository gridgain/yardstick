package org.yardstickframework.runners;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.yardstickframework.BenchmarkUtils;

public class CommandHandler {
    private static final String DFLT_SSH_PREF = "ssh -o StrictHostKeyChecking=no";

    private RunContext runCtx;


    public CommandHandler(RunContext runCtx){
        this.runCtx = runCtx;
    }

    public CommandExecutionResult runCmd(String host, String cmd, String args) throws IOException, InterruptedException {
        String sshPref = isLocal(host) ? "" : DFLT_SSH_PREF;

        String fullCmd = String.format("%s %s %s", sshPref, cmd, args);

        return runCmd(fullCmd);
    }

    protected CommandExecutionResult runCmd(final String cmd) throws IOException, InterruptedException {
        ExecutorService errStreamPrinter = Executors.newSingleThreadExecutor();

        try {
            final List<String> errStr = new ArrayList<>();

            final Process p = Runtime.getRuntime().exec(cmd);

            int exitCode = p.waitFor();

            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));

            errStreamPrinter.submit(new Callable<Object>() {
                @Override public Object call() throws IOException {
                    String line = "";


                    BufferedReader errReader = new BufferedReader(new InputStreamReader(p.getErrorStream()));

                    while ((line = errReader.readLine())!= null) {
                        System.out.println(String.format("Command '%s' returned error line: %s:", cmd, line));

                        errStr.add(line);
                    }

                    return null;
                }
            });

            String line = "";

            final List<String> outStr = new ArrayList<>();

            while ((line = reader.readLine())!= null) {
                outStr.add(line);

                if(line.contains("Successfully built "))
                    BenchmarkUtils.println(line);
            }

            return new CommandExecutionResult(exitCode, outStr, errStr);
        }
        catch (Exception e) {
            e.printStackTrace();

            throw e;
        }
        finally {
            errStreamPrinter.shutdown();
        }
    }

    private boolean isLocal(String host){
        return host.equalsIgnoreCase("localhost") || host.equals("127.0.0.1");
    }
}
