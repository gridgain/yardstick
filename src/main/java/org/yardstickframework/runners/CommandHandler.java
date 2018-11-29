package org.yardstickframework.runners;

import java.io.BufferedReader;
import java.io.File;
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

    public CommandHandler(RunContext runCtx) {
        this.runCtx = runCtx;
    }

    public CommandExecutionResult runCmd(String host, String cmd,
        String args) throws IOException, InterruptedException {

        while(cmd.contains("  "))
            cmd = cmd.replace("  ", " ");

        String fullCmd = isLocal(host) ?
            cmd :
            String.format("%s %s", getFullSSHPref(host), cmd);

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

                    while ((line = errReader.readLine()) != null) {
                        System.out.println(String.format("Command '%s' returned error line: %s:", cmd, line));

                        errStr.add(line);
                    }

                    return null;
                }
            });

            String line = "";

            final List<String> outStr = new ArrayList<>();

            while ((line = reader.readLine()) != null) {
                outStr.add(line);

                if (line.contains("Successfully built "))
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

    public CommandExecutionResult startNode(String host, String cmd, String logPath) throws IOException, InterruptedException {
        String fullCmd = "";

        while(cmd.contains("  "))
            cmd = cmd.replace("  ", " ");

        String[] cmdArr = cmd.split(" ");

        if(isLocal(host)) {
            File logFile = new File(logPath);

            logFile.createNewFile();

            ProcessBuilder pb = new ProcessBuilder()
                .command(cmdArr);

            pb.redirectErrorStream(true);
            pb.redirectOutput(logFile);

            pb.directory(new File(runCtx.getLocWorkDir()));

            pb.start();

        }

//        return runCmd(fullCmd);
        return null;
    }

    public CommandExecutionResult runLocalJava(String args){
        String fullCmd = "";

        while(args.contains("  "))
            args = args.replace("  ", " ");

        String javaHome = System.getProperty("java.home");

        String cmd = String.format("%s/bin/java %s", javaHome, args);

        String[] cmdArr = cmd.split(" ");

            ProcessBuilder pb = new ProcessBuilder()
                .command(cmdArr);

            pb.redirectErrorStream(true);

            pb.directory(new File(runCtx.getLocWorkDir()));

        try {
            pb.start();
        }
        catch (IOException e) {
            e.printStackTrace();
        }

//        return runCmd(fullCmd);
        return null;
    }

    public CommandExecutionResult runMkdirCmd(String host, String path) throws IOException, InterruptedException {
        if(isLocal(host)){
            File dirToMake = new File(path);

            List<String> errStream = new ArrayList<>();

            try {
                dirToMake.mkdirs();
            }
            catch (Exception e){
                BenchmarkUtils.println(e.getMessage());

                errStream.add(e.getMessage());
            }

            return new CommandExecutionResult(errStream.size(), new ArrayList<String>(), errStream);
        }
        else{
            String mkdirCmd = String.format("%s mkdir -p %s",
                getFullSSHPref(host),
                path);

            return runCmd(mkdirCmd);
        }
    }

    public CommandExecutionResult runDockerCmd(String host, String cmd) throws IOException, InterruptedException {
        String fullCmd = String.format("docker %s", cmd);

        if(isLocal(host))
            return runCmd(fullCmd);
        else{
            fullCmd = String.format("%s docker %s", getFullSSHPref(host), cmd);

            return runCmd(fullCmd);
        }
    }

    private boolean isLocal(String host) {
        return host.equalsIgnoreCase("localhost") || host.equals("127.0.0.1");
    }

    private String getFullSSHPref(String host){
        if(runCtx.getRemUser() == null || runCtx.getRemUser().isEmpty())
            return String.format("%s %s", DFLT_SSH_PREF, host);

        return String.format("%s %s@%s", DFLT_SSH_PREF, runCtx.getRemUser(), host);
    }


}
