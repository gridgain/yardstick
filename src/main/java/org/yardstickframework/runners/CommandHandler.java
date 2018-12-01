package org.yardstickframework.runners;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.yardstickframework.BenchmarkUtils;

public class CommandHandler {
    private static final String DFLT_SSH_PREF = "ssh -o StrictHostKeyChecking=no";

    private RunContext runCtx;

    public CommandHandler(RunContext runCtx) {
        this.runCtx = runCtx;
    }

    public CommandExecutionResult runCmd(String host, String cmd) throws IOException, InterruptedException {

        while (cmd.contains("  "))
            cmd = cmd.replace("  ", " ");

        if(isLocal(host))
            return runLocCmd(cmd);

        String fullCmd = String.format("%s %s", getFullSSHPref(host), cmd);

        return runRmtCmd(fullCmd);
    }

    protected CommandExecutionResult runLocCmd(String cmd) throws IOException, InterruptedException {
        System.out.println(String.format("Running local cmd %s", cmd));

        while (cmd.contains("  "))
            cmd = cmd.replace("  ", " ");

        String[] cmdArr = cmd.split(" ");

        final ProcessBuilder pb = new ProcessBuilder()
            .command(cmdArr);

        pb.directory(new File(runCtx.getLocWorkDir()));

        Process p = pb.start();

        int exitCode = p.waitFor();

        BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));

        String lineE = "";

        List<String> errList = new ArrayList<>();

        BufferedReader errReader = new BufferedReader(new InputStreamReader(p.getErrorStream()));

        while ((lineE = errReader.readLine()) != null) {
            System.out.println(String.format("Command '%s' returned error line: %s:", cmd, lineE));

            errList.add(lineE);
        }

        List<String> errStr = errList;

        String lineO = "";

        final List<String> outStr = new ArrayList<>();

        while ((lineO = reader.readLine()) != null) {
            outStr.add(lineO);

            if (lineO.contains("Successfully built "))
                BenchmarkUtils.println(lineO);
        }

        CommandExecutionResult res = new CommandExecutionResult(exitCode, outStr, errStr);

        System.out.println(res.toString());

        return res;
    }

    protected CommandExecutionResult runRmtCmd(final String cmd) throws IOException, InterruptedException {
        System.out.println(String.format("Running cmd %s", cmd));

        ExecutorService errStreamPrinter = Executors.newSingleThreadExecutor();

        final Process p = Runtime.getRuntime().exec(cmd);

        int exitCode = p.waitFor();

        BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));

        Future<List<String>> resFut = errStreamPrinter.submit(new Callable<List<String>>() {
            @Override public List<String> call() throws IOException {
                String line = "";

                List<String> resList = new ArrayList<>();

                BufferedReader errReader = new BufferedReader(new InputStreamReader(p.getErrorStream()));

                while ((line = errReader.readLine()) != null) {
                    System.out.println(String.format("Command '%s' returned error line: %s:", cmd, line));

                    resList.add(line);
                }

                return resList;
            }
        });

        List<String> errStr = new ArrayList<>();

        try {
            errStr = resFut.get();
        }
        catch (ExecutionException e) {
            e.printStackTrace();
        }

        errStreamPrinter.shutdown();

        String line = "";

        final List<String> outStr = new ArrayList<>();

        while ((line = reader.readLine()) != null) {
            outStr.add(line);

            if (line.contains("Successfully built "))
                BenchmarkUtils.println(line);
        }

        CommandExecutionResult res = new CommandExecutionResult(exitCode, outStr, errStr);

        System.out.println(res.toString());

        return res;
    }

    public CommandExecutionResult startNode(String host, String cmd,
        String logPath) throws IOException, InterruptedException {
        if (isLocal(host))
            return startNodeLocal(cmd, logPath);

        String startNodeCmd = String.format("%s nohup %s > %s 2>& 1 &", getFullSSHPref(host), cmd, logPath);

        return runRmtCmd(startNodeCmd);
    }

    private CommandExecutionResult startNodeLocal(String cmd,
        String logPath) throws IOException, InterruptedException {
//        cmd = "nohup " + cmd;

        while (cmd.contains("  "))
            cmd = cmd.replace("  ", " ");

        String[] cmdArr = cmd.split(" ");

        File logFile = new File(logPath);

        logFile.createNewFile();

        final ProcessBuilder pb = new ProcessBuilder()
            .command(cmdArr);

        pb.redirectErrorStream(true);
        pb.redirectOutput(logFile);

        pb.directory(new File(runCtx.getLocWorkDir()));

        Executors.newSingleThreadExecutor().submit(new Runnable() {
            @Override public void run() {
                try {
                    Process p = pb.start();

                    p.waitFor();
                }
                catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }

            }
        });

        return null;
    }

    public CommandExecutionResult runLocalJava(String args) {
        String fullCmd = "";

        while (args.contains("  "))
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
        if (isLocal(host)) {
            File dirToMake = new File(path);

            List<String> errStream = new ArrayList<>();

            try {
                dirToMake.mkdirs();
            }
            catch (Exception e) {
                BenchmarkUtils.println(e.getMessage());

                errStream.add(e.getMessage());
            }

            return new CommandExecutionResult(errStream.size(), new ArrayList<String>(), errStream);
        }
        else {
            String mkdirCmd = String.format("%s mkdir -p %s",
                getFullSSHPref(host),
                path);

            return runRmtCmd(mkdirCmd);
        }
    }

    public CommandExecutionResult runDockerCmd(String host, String cmd) throws IOException, InterruptedException {
        String fullCmd = String.format("docker %s", cmd);

        if (isLocal(host))
            return runRmtCmd(fullCmd);
        else {
            fullCmd = String.format("%s docker %s", getFullSSHPref(host), cmd);

            return runRmtCmd(fullCmd);
        }
    }

    public static boolean isLocal(String host) {
        return host.equalsIgnoreCase("localhost") || host.equals("127.0.0.1");
    }

    private String getFullSSHPref(String host) {
        if (runCtx.getRemUser() == null || runCtx.getRemUser().isEmpty())
            return String.format("%s %s", DFLT_SSH_PREF, host);

        return String.format("%s %s@%s", DFLT_SSH_PREF, runCtx.getRemUser(), host);
    }

    public String getHostJavaHome(String host) {
        String javaHome = null;

        if (isLocal(host))
            javaHome = System.getProperty("java.home");
        else {
            String echoCmd = "echo $JAVA_HOME";

            CommandExecutionResult res = null;

            try {
                res = runCmd(host, echoCmd);
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }

            if (!res.getOutStream().isEmpty())
                javaHome = res.getOutStream().get(0);
        }

        return javaHome;
    }

    public boolean checkConn(String host) {
        if (isLocal(host))
            return true;

        CommandExecutionResult res = null;

        String checkCmd = String.format("%s echo check", getFullSSHPref(host));

        try {
            res = runRmtCmd(checkCmd);
        }
        catch (IOException | InterruptedException e) {
            e.printStackTrace();

            return false;
        }

        return res != null
            && res.getExitCode() == 0
            && !res.getOutStream().isEmpty()
            && res.getOutStream().get(0).equals("check");
    }

    public boolean checkRemJava(String host, String javaHome) {
        String checkCmd = String.format("%s test -f %s/bin/java", getFullSSHPref(host), javaHome);

        CommandExecutionResult res = null;

        try {
            res = runRmtCmd(checkCmd);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }

        return res != null && res.getExitCode() == 0;
    }
}
