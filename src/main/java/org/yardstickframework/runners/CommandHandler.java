/*
 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */

package org.yardstickframework.runners;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
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

import static org.yardstickframework.BenchmarkUtils.getJava;

/**
 * Command handler.
 */
public class CommandHandler {
    /** */
    private static CommandHandler instance;

    /** */
    private static final String DFLT_SSH_PREF = "ssh -o StrictHostKeyChecking=no";

    /** */
    private RunContext runCtx;

    /** Field to store last executed command to avoid repetition in log. */
    private String lastCmd = "";

    /** */
    private static final String[] excluded = new String[] {
        "image has dependent child images"
    };

    /**
     * Constructor.
     *
     * @param runCtx Run context.
     */
    private CommandHandler(RunContext runCtx) {
        this.runCtx = runCtx;
    }

    /**
     * @param runCtx Run context.
     * @return Command handler.
     */
    public static CommandHandler getCommandHandler(RunContext runCtx) {
        if (instance == null) {
            synchronized (CommandHandler.class) {
                if (instance == null)
                    instance = new CommandHandler(runCtx);
            }
        }

        return instance;
    }

    /**
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
     * @param host Host.
     * @param path File path.
     * @param keyWords Key words to look for.
     * @return Command execution result.
     * @throws IOException If failed.
     * @throws InterruptedException If interrupted.
     */
    public CommandExecutionResult runGrepCmd(String host, String path, List<String> keyWords)
        throws IOException, InterruptedException {
        if (isLocal(host))
            return runGrepCmdLocale(path, keyWords);

        StringBuilder sb = new StringBuilder(String.format("head -20 %s | grep", path));

        for (String keyWord : keyWords)
            sb.append(String.format(" -e '%s'", keyWord));

        return runCmd(host, sb.toString());
    }

    /**
     * @param path File path.
     * @param keyWords Key words to look for.
     * @return Command execution result.
     * @throws IOException If failed.
     * @throws InterruptedException If interrupted.
     */
    public CommandExecutionResult runGrepCmdLocale(String path, List<String> keyWords)
        throws IOException, InterruptedException {
        List<String> outRes = new ArrayList<>();
        List<String> errRes = new ArrayList<>();

        int exitCode = 0;

        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            String line;

            while ((line = br.readLine()) != null)
                for (String keyWord : keyWords) {
                    if (line.contains(keyWord))
                        outRes.add(line);
                }

        }
        catch (IOException e) {
            log().error(String.format("Failed to check file '%s'.", path), e);

            errRes.add(e.getMessage());

            exitCode = 1;
        }

        return new CommandExecutionResult(exitCode, outRes, errRes, null);
    }

    /**
     * @param cmd Command to execute.
     * @return Command execution result.
     * @throws IOException If failed.
     * @throws InterruptedException If interrupted.
     */
    private CommandExecutionResult runLocCmd(String cmd) throws IOException, InterruptedException {
        while (cmd.contains("  "))
            cmd = cmd.replace("  ", " ");

        if (!cmd.equals(lastCmd))
            log().debug(String.format("Running local cmd '%s'", cmd));

        lastCmd = cmd;

        String[] cmdArr = null;

        boolean redirect = false;

        String logPath = null;

        if(!cmd.endsWith("'echo $JAVA_HOME'") && !cmd.endsWith(" 2>& 1 &"))
            cmdArr = cmd.split(" ");
        else if(cmd.endsWith("'echo $JAVA_HOME'")) {
            String cmdTemp = cmd.replace(" 'echo $JAVA_HOME'", "");

            String[] cmdArrTemp = cmdTemp.split(" ");

            cmdArr = Arrays.copyOf(cmdArrTemp, cmdArrTemp.length + 1);

            cmdArr[cmdArr.length - 1] = "echo $JAVA_HOME";
        }
        else if(cmd.endsWith(" 2>& 1 &")) {
            redirect = true;

            String cmdTemp = cmd.replace(" 2>& 1 &", "");

            String[] cmdAndLog = cmdTemp.split(" > ");

            String cmd0 = cmdAndLog[0];

            logPath = cmdAndLog[1];

            cmdArr = cmd0.split(" ");
        }

        final ProcessBuilder pb = new ProcessBuilder().command(cmdArr);

        if(redirect) {
            File logFile = new File(logPath);

            if (!logFile.exists())
                logFile.createNewFile();

            pb.redirectErrorStream(true);

            pb.redirectOutput(logFile);
        }

        pb.directory(new File(runCtx.localeWorkDirectory()));

        final Process p = pb.start();

        int exitCode = 0;

        if(!redirect)
            exitCode = p.waitFor();

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

        while ((lineO = reader.readLine()) != null)
            outStr.add(lineO);

        return new CommandExecutionResult(exitCode, outStr, errList, p);
    }

    /**
     * @param cmd Command to execute.
     * @return Command execution result.
     * @throws IOException If failed.
     * @throws InterruptedException If interrupted.
     */
    private CommandExecutionResult runRmtCmd(final String cmd) throws IOException, InterruptedException {
        if (!cmd.equals(lastCmd))
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
            if (!checkIfExcluded(lineE))
                log().error(String.format("Command '%s' returned error line: %s:", cmd, lineE));

            errStr.add(lineE);
        }

        errStreamPrinter.shutdown();

        String lineO;

        final List<String> outStr = new ArrayList<>();

        while ((lineO = outReader.readLine()) != null)
            outStr.add(lineO);

        return new CommandExecutionResult(exitCode, outStr, errStr, proc);
    }

    /**
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
            return startNodeLocale(cmd, logPath);

        String startNodeCmd = String.format("%s cd %s; nohup %s > %s 2>& 1 &",
            getFullSSHPref(host),
            runCtx.remoteWorkDirectory(),
            cmd,
            logPath);

        return runRmtCmd(startNodeCmd);
    }

    /**
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
     * @param cmd Command to execute.
     * @param logPath Log file path.
     * @return Command execution result.
     * @throws IOException If failed.
     */
    private CommandExecutionResult startNodeLocale(String cmd,
        String logPath) throws IOException {

        while (cmd.contains("  "))
            cmd = cmd.replace("  ", " ");

        String[] cmdArr = cmd.split(" ");

        String java = cmdArr[0];

        if(!new File(java).exists()){
            log().error(String.format("Failed to find java '%s'.", java));

            return CommandExecutionResult.emptyFailedResult();
        }

        File logFile = new File(logPath);

        if (!logFile.exists())
            logFile.createNewFile();

        final ProcessBuilder pb = new ProcessBuilder().command(cmdArr);

        pb.redirectErrorStream(true);

        pb.redirectOutput(logFile);

        pb.directory(new File(runCtx.localeWorkDirectory()));

        final Process proc = pb.start();

        ExecutorService exec = Executors.newSingleThreadExecutor();

        exec.submit(new Callable<Object>() {
            @Override public Process call() throws InterruptedException {
                if(!runCtx.startServersEndExit())
                    proc.waitFor();

                return proc;
            }
        });

        exec.shutdown();

        return new CommandExecutionResult(0, new ArrayList<>(), new ArrayList<>(), proc);
    }

    /**
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

        String toLook = String.format("-Dyardstick.%s ", nodeInfo.toShortStr());

        if (checkList(res.outputList(), toLook))
            nodeInfo.nodeStatus(NodeStatus.RUNNING);
        else
            nodeInfo.nodeStatus(NodeStatus.NOT_RUNNING);

        return nodeInfo;
    }

    /**
     * Checks if at least one line in the list contains a given string.
     *
     * @param toCheck List to check.
     * @param toLook String to look for.
     * @return
     */
    public boolean checkList(List<String> toCheck, String toLook){
        boolean found = false;

        for (String str : toCheck)
            if (str.contains(toLook))
                found = true;

        return found;
    }

    /**
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

        String killCmd = String.format("pkill -9 -f Dyardstick.%s ", nodeInfo.toShortStr());

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
     * @param args Arguments.
     * @return Command execution result.
     */
    public CommandExecutionResult runLocalJava(String args) {
        while (args.contains("  "))
            args = args.replace("  ", " ");

        String cmd = String.format("%s %s", getJava(), args);

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

            return new CommandExecutionResult(errStream.size(), new ArrayList<>(), errStream, null);
        }
        else {
            String mkdirCmd = String.format("%s mkdir -p %s",
                getFullSSHPref(host),
                path);

            return runRmtCmd(mkdirCmd);
        }
    }

    /**
     * @param host Host.
     * @param cmd Command to execute.
     * @return Command execution result.
     * @throws IOException If failed.
     * @throws InterruptedException If interrupted.
     */
    public CommandExecutionResult runDockerCmd(String host, String cmd) throws IOException, InterruptedException {
        String fullCmd = String.format("docker %s", cmd);

        if (isLocal(host)) {
            if(fullCmd.endsWith("'echo $JAVA_HOME'") || fullCmd.contains(">"))
                return runLocCmd(fullCmd);
            else
                return runRmtCmd(fullCmd);
        }
        else {
            fullCmd = String.format("%s docker %s", getFullSSHPref(host), cmd);

            return runRmtCmd(fullCmd);
        }
    }

    /**
     * @param host Host.
     * @return {@code true} if host address is "localhost" or "127.0.0.1" or {@code false} otherwise.
     */
    private boolean isLocal(String host) {
        return host.equalsIgnoreCase("localhost") || host.equals("127.0.0.1");
    }

    /**
     * @param host Host.
     * @return Full ssh prefix.
     */
    private String getFullSSHPref(String host) {
        if (runCtx.remoteUser() == null || runCtx.remoteUser().isEmpty())
            return String.format("%s %s", DFLT_SSH_PREF, host);

        return String.format("%s %s@%s", DFLT_SSH_PREF, runCtx.remoteUser(), host);
    }

    /**
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
     * @param host Host.
     * @return {@code true} if connection is established or {@code false} otherwise.
     */
    public boolean checkConn(String host) {
        if (isLocal(host))
            return true;

        CommandExecutionResult res;

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
     * @param host Host.
     * @param javaHome Path to Java home.
     * @return {@code true} if Java actually exists at the specified path or {@code false} otherwise.
     */
    public boolean checkJava(String host, String javaHome) {
        if (isLocal(host))
            return new File(javaHome + "/bin/java").exists();

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
     * @param host Host.
     * @param path Path.
     * @return {@code true} if file actually exists at the specified path or {@code false} otherwise.
     */
    public boolean checkRemFile(String host, String path) {
        if (isLocal(host))
            return new File(path).exists();

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
     * @param lineE Source string.
     * @return {@code true} if  source string contains excluded patern or {@code false} otherwise.
     */
    private boolean checkIfExcluded(String lineE) {
        for (String excl : excluded)
            if (lineE.contains(excl))
                return true;

        return false;
    }

    /**
     * @return Logger.
     */
    protected Logger log() {
        return LogManager.getLogger(getClass().getSimpleName());
    }
}
