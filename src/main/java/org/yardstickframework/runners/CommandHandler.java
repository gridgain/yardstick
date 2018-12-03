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

        if (isLocal(host))
            return runLocCmd(cmd);

        String fullCmd = String.format("%s %s", getFullSSHPref(host), cmd);

        return runRmtCmd(fullCmd);
    }

    private CommandExecutionResult runLocCmd(String cmd) throws IOException, InterruptedException {
//        System.out.println(String.format("Running local cmd %s", cmd));

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

        CommandExecutionResult res = new CommandExecutionResult(exitCode, outStr, errStr, p);

//        System.out.println(res.toString());

        return res;
    }

    protected CommandExecutionResult runRmtCmd(final String cmd) throws IOException, InterruptedException {
//        System.out.println(String.format("Running cmd %s", cmd));

        ExecutorService errStreamPrinter = Executors.newSingleThreadExecutor();

        final Process proc = Runtime.getRuntime().exec(cmd);

        int exitCode = proc.waitFor();

        BufferedReader outReader = new BufferedReader(new InputStreamReader(proc.getInputStream()));

        String lineE = "";

        List<String> errStr = new ArrayList<>();

        BufferedReader errReader = new BufferedReader(new InputStreamReader(proc.getErrorStream()));

        while ((lineE = errReader.readLine()) != null) {
            System.out.println(String.format("Command '%s' returned error line: %s:", cmd, lineE));

            errStr.add(lineE);
        }

        errStreamPrinter.shutdown();

        String lineO = "";

        final List<String> outStr = new ArrayList<>();

        while ((lineO = outReader.readLine()) != null) {
            outStr.add(lineO);

            if (lineO.contains("Successfully built "))
                BenchmarkUtils.println(lineO);
        }

        CommandExecutionResult res = new CommandExecutionResult(exitCode, outStr, errStr, proc);

//        System.out.println(res.toString());

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

        while (cmd.contains("  "))
            cmd = cmd.replace("  ", " ");

        String[] cmdArr = cmd.split(" ");

        File logFile = new File(logPath);

        if (!logFile.exists())
            logFile.createNewFile();

        final ProcessBuilder pb = new ProcessBuilder().command(cmdArr);

        pb.redirectErrorStream(true);
        pb.redirectOutput(logFile);

        pb.directory(new File(runCtx.getLocWorkDir()));

        final Process proc = pb.start();

        ExecutorService nodeServ = Executors.newSingleThreadExecutor();

        nodeServ.submit(new Runnable() {
            @Override public void run() {
                try {
                    proc.waitFor();
                }
                catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        nodeServ.shutdown();

        return new CommandExecutionResult(0, null, null, proc);
    }

    public NodeCheckResult checkPlainNode(NodeInfo nodeInfo) throws IOException, InterruptedException {
        String host = nodeInfo.getHost();

        if (isLocal(host)) {
            Process proc = nodeInfo.getCmdExRes().getProc();

            if (proc != null && proc.isAlive())
                return new NodeCheckResult(NodeStatus.RUNNING);
            else
                return new NodeCheckResult(NodeStatus.NOT_RUNNING);
        }

        String checkCmd = String.format("%s ps -ax|grep 'java'", getFullSSHPref(host));

        CommandExecutionResult res = runRmtCmd(checkCmd);

        boolean found = false;

        String toLook = String.format("-Dyardstick.%s%s ",
            nodeInfo.getNodeType().toString().toLowerCase(),
            nodeInfo.getId());

        for (String str : res.getOutStream())
            if (str.contains(toLook))
                found = true;

        return found ?
            new NodeCheckResult(NodeStatus.RUNNING) :
            new NodeCheckResult(NodeStatus.NOT_RUNNING);
    }

    public WorkResult killNode(NodeInfo nodeInfo) throws IOException, InterruptedException {
        BenchmarkUtils.println(String.format("Killing node -Dyardstick.%s%s",
            nodeInfo.getNodeType().toString().toLowerCase(), nodeInfo.getId()));

        String host = nodeInfo.getHost();

        RunMode runMode = nodeInfo.getStartCtx().getRunMode();

        if (isLocal(host) && runMode == RunMode.PLAIN) {
            Process proc = nodeInfo.getCmdExRes().getProc();

            proc.destroyForcibly();

            return nodeInfo;
        }

        String killCmd = String.format("pkill -9 -f \"Dyardstick.%s%s \"",
            nodeInfo.getNodeType().toString().toLowerCase(), nodeInfo.getId());

        if (runMode == RunMode.DOCKER) {
            String contName = nodeInfo.getDockerInfo().getContName();

            String docKillCmd = String.format("exec %s %s", contName, killCmd);

            runDockerCmd(host, docKillCmd);

            return nodeInfo;
        }

        runCmd(host, killCmd);

        return nodeInfo;
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

            return new CommandExecutionResult(errStream.size(), new ArrayList<String>(), errStream, null);
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
//        if (host.equals(runCtx.getCurrentHost()))
//            return "";

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

    public boolean checkRemFile(String host, String path) {
        String checkCmd = String.format("%s test -f %s", getFullSSHPref(host), path);

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
