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
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.yardstickframework.runners.context.NodeInfo;
import org.yardstickframework.runners.context.NodeStatus;
import org.yardstickframework.runners.context.RunContext;
import org.yardstickframework.runners.context.RunMode;

/**
 * Command handler.
 */
public class CommandHandler {
    /** */
    private static final String DFLT_SSH_PREF = "ssh -o StrictHostKeyChecking=no";

    /** */
    private RunContext runCtx;

    /** Field to store last executed command to avoid repetition in log. */
    private String lastCmd = "";

    /**
     * Constructor.
     * 
     * @param runCtx Run context.
     */
    public CommandHandler(RunContext runCtx) {
        this.runCtx = runCtx;
    }

    /**
     * 
     * @param host Host.
     * @param cmd Command to execute.
     * @return Command execution result.
     * @throws IOException If failed.
     * @throws InterruptedException If interrupted.
     */
    public CommandExecutionResult runCmd(String host, String cmd) throws IOException, InterruptedException {
        while (cmd.contains("  "))
            cmd = cmd.replace("  ", " ");

        if (isLocal(host))
            return runLocCmd(cmd);

        String fullCmd = String.format("%s %s", getFullSSHPref(host), cmd);

        return runRmtCmd(fullCmd);
    }

    /**
     * 
     * @param cmd Command to execute.
     * @return Command execution result.
     * @throws IOException If failed.
     * @throws InterruptedException If interrupted.
     */
    private CommandExecutionResult runLocCmd(String cmd) throws IOException, InterruptedException {
        while (cmd.contains("  "))
            cmd = cmd.replace("  ", " ");

        if(!cmd.equals(lastCmd))
            log().debug(String.format("Running local cmd '%s'", cmd));

        lastCmd = cmd;

        String[] cmdArr = cmd.split(" ");

        final ProcessBuilder pb = new ProcessBuilder()
            .command(cmdArr);

        pb.directory(new File(runCtx.localeWorkDirectory()));

        Process p = pb.start();

        int exitCode = p.waitFor();

        BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));

        String lineE;

        List<String> errList = new ArrayList<>();

        BufferedReader errReader = new BufferedReader(new InputStreamReader(p.getErrorStream()));

        while ((lineE = errReader.readLine()) != null) {
            log().error(String.format("Command '%s' returned error line: %s:", cmd, lineE));

            errList.add(lineE);
        }

        String lineO;

        final List<String> outStr = new ArrayList<>();

        while ((lineO = reader.readLine()) != null) {
            outStr.add(lineO);

            if (lineO.contains("Successfully built "))
                log().info(lineO);
        }

        return new CommandExecutionResult(exitCode, outStr, errList, p);
    }

    /**
     * 
     * @param cmd Command to execute.
     * @return Command execution result.
     * @throws IOException If failed.
     * @throws InterruptedException If interrupted.
     */
    private CommandExecutionResult runRmtCmd(final String cmd) throws IOException, InterruptedException {
        if(!cmd.equals(lastCmd))
            log().debug(String.format("Running remote cmd '%s'", cmd));

        lastCmd = cmd;

        ExecutorService errStreamPrinter = Executors.newSingleThreadExecutor();

        final Process proc = Runtime.getRuntime().exec(cmd);

        int exitCode = proc.waitFor();

        BufferedReader outReader = new BufferedReader(new InputStreamReader(proc.getInputStream()));

        String lineE;

        List<String> errStr = new ArrayList<>();

        BufferedReader errReader = new BufferedReader(new InputStreamReader(proc.getErrorStream()));

        while ((lineE = errReader.readLine()) != null) {
            log().error(String.format("Command '%s' returned error line: %s:", cmd, lineE));

            errStr.add(lineE);
        }

        errStreamPrinter.shutdown();

        String lineO;

        final List<String> outStr = new ArrayList<>();

        while ((lineO = outReader.readLine()) != null) {
            outStr.add(lineO);

            if (lineO.contains("Successfully built "))
                log().info(lineO);
        }

        return new CommandExecutionResult(exitCode, outStr, errStr, proc);
    }

    /**
     * 
     * @param host Host.
     * @param cmd Command to execute.
     * @param logPath Log file path.
     * @return Command execution result.
     * @throws IOException If failed.
     * @throws InterruptedException If interrupted.
     */
    public CommandExecutionResult startNode(String host, String cmd,
        String logPath) throws IOException, InterruptedException {
        if (isLocal(host))
            return startNodeLocal(cmd, logPath);

        String startNodeCmd = String.format("%s nohup %s > %s 2>& 1 &", getFullSSHPref(host), cmd, logPath);

        return runRmtCmd(startNodeCmd);
    }

    /**
     * 
     * @param host Host.
     * @param pathLoc Locale path.
     * @param pathRem Remote path.
     * @return Command execution result.
     * @throws IOException If failed.
     * @throws InterruptedException If interrupted.
     */
    public CommandExecutionResult upload(String host, String pathLoc, String pathRem)
        throws IOException, InterruptedException {
        String cpCmd = String.format("scp -o StrictHostKeyChecking=no -rq %s %s:%s",
            pathLoc, host, pathRem);

        return runRmtCmd(cpCmd);
    }

    /**
     * 
     * @param host Host.
     * @param pathLoc Locale path.
     * @param pathRem Remote path.
     * @return Command execution result.
     * @throws IOException If failed.
     * @throws InterruptedException If interrupted.
     */
    public CommandExecutionResult download(String host, String pathLoc, String pathRem)
        throws IOException, InterruptedException {
        String cpCmd = String.format("scp -o StrictHostKeyChecking=no -rq %s:%s %s",
            host, pathRem, pathLoc);

        return runRmtCmd(cpCmd);
    }

    /**
     * 
     * @param cmd Command to execute.
     * @param logPath Log file path.
     * @return Command execution result.
     * @throws IOException If failed.
     * @throws InterruptedException If interrupted.
     */
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

        pb.directory(new File(runCtx.localeWorkDirectory()));

        final Process proc = pb.start();

        ExecutorService nodeServ = Executors.newSingleThreadExecutor();

        nodeServ.submit(new Callable<Object>() {
            @Override public Process call() throws InterruptedException{
                    proc.waitFor();

                    return proc;
            }
        });

        nodeServ.shutdown();

        return new CommandExecutionResult(0, null, null, proc);
    }

    /**
     *
     * @param nodeInfo Node info.
     * @return Node info.
     * @throws IOException If failed.
     * @throws InterruptedException If interrupted.
     */
    public NodeInfo checkPlainNode(NodeInfo nodeInfo) throws IOException, InterruptedException {
        String host = nodeInfo.host();

        if (isLocal(host)) {
            Process proc = nodeInfo.commandExecutionResult().process();

            if (proc != null && proc.isAlive())
                nodeInfo.nodeStatus(NodeStatus.RUNNING);
            else
                nodeInfo.nodeStatus(NodeStatus.NOT_RUNNING);

            return nodeInfo;
        }

        String checkCmd = String.format("%s ps -ax|grep 'java'", getFullSSHPref(host));

        CommandExecutionResult res = runRmtCmd(checkCmd);

        boolean found = false;

        String toLook = String.format("-Dyardstick.%s ", nodeInfo.toShortStr());

        for (String str : res.outputList())
            if (str.contains(toLook))
                found = true;

        if(found)
            nodeInfo.nodeStatus(NodeStatus.RUNNING);
        else
            nodeInfo.nodeStatus(NodeStatus.NOT_RUNNING);

        return nodeInfo;
    }

    /**
     *
     * @param nodeInfo Node info.
     * @return Node info.
     * @throws IOException If failed.
     * @throws InterruptedException If interrupted.
     */
    public NodeInfo killNode(NodeInfo nodeInfo) throws IOException, InterruptedException {
        String host = nodeInfo.host();

        RunMode runMode = nodeInfo.runMode();

        if (isLocal(host) && runMode == RunMode.PLAIN) {
            Process proc = nodeInfo.commandExecutionResult().process();

            proc.destroyForcibly();

            return nodeInfo;
        }

        String killCmd = String.format("pkill -9 -f \"Dyardstick.%s \"", nodeInfo.toShortStr());

        if (runMode == RunMode.DOCKER) {
            String contName = nodeInfo.dockerInfo().contName();

            String docKillCmd = String.format("exec %s %s", contName, killCmd);

            runDockerCmd(host, docKillCmd);

            return nodeInfo;
        }

        runCmd(host, killCmd);

        return nodeInfo;
    }

    /**
     * 
     * @param args Arguments.
     * @return Command execution result.
     */
    public CommandExecutionResult runLocalJava(String args) {
        while (args.contains("  "))
            args = args.replace("  ", " ");

        String javaHome = System.getProperty("java.home");

        String cmd = String.format("%s/bin/java %s", javaHome, args);

        String[] cmdArr = cmd.split(" ");

        ProcessBuilder pb = new ProcessBuilder()
            .command(cmdArr);

        pb.redirectErrorStream(true);

        pb.directory(new File(runCtx.localeWorkDirectory()));

        try {
            pb.start();
        }
        catch (IOException e) {
            log().error("Failed to start java process.", e);
        }

        return null;
    }

    /**
     * 
     * @param host Host.
     * @param path Directory to create.
     * @return Command execution result.
     * @throws IOException If failed.
     * @throws InterruptedException If interrupted.
     */
    public CommandExecutionResult runMkdirCmd(String host, String path) throws IOException, InterruptedException {
        if (isLocal(host)) {
            List<String> errStream = new ArrayList<>();

            try {
                File dirToMake = new File(path);

                dirToMake.mkdirs();
            }
            catch (Exception e) {
                log().info(e.getMessage());

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

    /**
     * 
     * @param host Host.
     * @param cmd Command to execute.
     * @return Command execution result.
     * @throws IOException If failed.
     * @throws InterruptedException If interrupted.
     */
    public CommandExecutionResult runDockerCmd(String host, String cmd) throws IOException, InterruptedException {
        String fullCmd = String.format("docker %s", cmd);

        if (isLocal(host))
            return runRmtCmd(fullCmd);
        else {
            fullCmd = String.format("%s docker %s", getFullSSHPref(host), cmd);

            return runRmtCmd(fullCmd);
        }
    }

    /**
     *
     * @param host Host.
     * @return {@code true} if host address is "localhost" or "127.0.0.1" or {@code false} otherwise.
     */
    private boolean isLocal(String host) {
        return host.equalsIgnoreCase("localhost") || host.equals("127.0.0.1");
    }

    /**
     *
     * @param host Host.
     * @return Full ssh prefix.
     */
    private String getFullSSHPref(String host) {
        if (runCtx.remoteUser() == null || runCtx.remoteUser().isEmpty())
            return String.format("%s %s", DFLT_SSH_PREF, host);

        return String.format("%s %s@%s", DFLT_SSH_PREF, runCtx.remoteUser(), host);
    }

    /**
     *
     * @param host Host.
     * @return Host Java home.
     */
    public String getHostJavaHome(String host) {
        String javaHome = null;

        if (isLocal(host))
            javaHome = System.getProperty("java.home");
        else {
            CommandExecutionResult res = null;

            try {
                String echoCmd = "echo $JAVA_HOME";

                res = runCmd(host, echoCmd);
            }
            catch (IOException | InterruptedException e) {
                log().error(String.format("Failed to get Java home from the host '%s'", host), e);
            }

            if (!res.outputList().isEmpty())
                javaHome = res.outputList().get(0);
        }

        return javaHome;
    }

    /**
     *
     * @param host Host.
     * @return {@code true} if connection is established or {@code false} otherwise.
     */
    public boolean checkConn(String host) {
        if (isLocal(host))
            return true;

        CommandExecutionResult res = null;

        String checkCmd = String.format("%s echo check", getFullSSHPref(host));

        try {
            res = runRmtCmd(checkCmd);
        }
        catch (IOException | InterruptedException e) {
            log().error(String.format("Failed to check connection to the host '%s'", host), e);

            return false;
        }

        return res != null
            && res.exitCode() == 0
            && !res.outputList().isEmpty()
            && res.outputList().get(0).equals("check");
    }

    /**
     *
     * @param host Host.
     * @param javaHome Path to Java home.
     * @return {@code true} if Java actually exists at the specified path or {@code false} otherwise.
     */
    public boolean checkRemJava(String host, String javaHome) {
        String checkCmd = String.format("%s test -f %s/bin/java", getFullSSHPref(host), javaHome);

        CommandExecutionResult res = null;

        try {
            res = runRmtCmd(checkCmd);
        }
        catch (IOException | InterruptedException e) {
            log().error(String.format("Failed to check Java home on the host '%s'", host), e);
        }

        return res != null && res.exitCode() == 0;
    }

    /**
     *
     * @param host Host.
     * @param path Path.
     * @return {@code true} if file actually exists at the specified path or {@code false} otherwise.
     */
    public boolean checkRemFile(String host, String path) {
        String checkCmd = String.format("%s test -f %s", getFullSSHPref(host), path);

        CommandExecutionResult res = null;

        try {
            res = runRmtCmd(checkCmd);
        }
        catch (IOException | InterruptedException e) {
            log().error(String.format("Failed to check file '%s' home on the host '%s'", path, host), e);
        }

        return res != null && res.exitCode() == 0;
    }

    /**
     *
     * @return Logger.
     */
    protected Logger log(){
        return LogManager.getLogger(getClass().getSimpleName());
    }
}
