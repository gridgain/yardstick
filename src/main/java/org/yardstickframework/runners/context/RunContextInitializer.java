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

package org.yardstickframework.runners.context;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.PropertyConfigurator;
import org.yardstickframework.BenchmarkConfiguration;
import org.yardstickframework.BenchmarkUtils;

/**
 * Initializes run context.
 */
public class RunContextInitializer {
    /** */
    private static final Logger LOG = LogManager.getLogger(RunContextInitializer.class);

    /** Instance to initialize */
    private RunContext ctx;

    /**
     * Constructor.
     *
     * @param ctx {@code RunContext} instance to initialize.
     */
    RunContextInitializer(RunContext ctx) {
        this.ctx = ctx;
    }

    /**
     *
     * @param args Command line arguments.
     * @return {@code true} if initialization completed or {@code false} otherwise.
     */
    void initialize(String[] args) {
        ctx.mainDateTime(BenchmarkUtils.dateTime());

        if (!handleArgs(args))
            return;

        setHosts();

        setProps();

        setConfigs();

        setServerName();

        setWarmup();

        setDuration();

        setThreads();

        setJavaHome();

        setRunModes();

        setUser();

        setCfgList();

        setRestartCtx(NodeType.SERVER);

        setRestartCtx(NodeType.DRIVER);

        if(ctx.dockerEnabled())
            setDockerContext();

        handleAdditionalArgs();
    }

    /**
     * @param args Command line arguments.
     * @return {@code true} if '-h' or '--help' was not passed or {@code false} otherwise.
     */
    private boolean handleArgs(String[] args) {
        try {
            BenchmarkUtils.jcommander(args, ctx.config(), "<benchmark-runner>");
        }
        catch (Exception e) {
            BenchmarkUtils.println("Failed to parse command line arguments. " + e.getMessage());

            ctx.config().help(true);

            ctx.exitCode(1);

            return false;
        }

        if (ctx.config().help())
            return false;

        if (ctx.config().scriptDirectory() == null) {
            System.out.println("Error: Script directory is not defined.");

            ctx.exitCode(1);

            System.exit(ctx.exitCode());
        }

        ctx.localeWorkDirectory(new File(ctx.config().scriptDirectory()).getParentFile().getAbsolutePath());

        configLog();

        LOG.debug(String.format("Locale work directory is '%s'.", ctx.localeWorkDirectory()));

        if (ctx.config().propertyFile() == null) {
            String dfltPropPath = String.format("%s/config/benchmark.properties", ctx.localeWorkDirectory());

            LOG.info(String.format("Using as a default property file '%s'.", dfltPropPath));

            if (!new File(dfltPropPath).exists()) {
                LOG.error(String.format("Failed to find default property file '%s'.", dfltPropPath));

                ctx.exitCode(1);

                System.exit(ctx.exitCode());
            }

            ctx.propertyPath(dfltPropPath);
        }
        else {
            String propFilePath = ctx.config().propertyFile();

            if (new File(propFilePath).exists())
                ctx.propertyPath(new File(propFilePath).getAbsolutePath());
            else if (Paths.get(ctx.localeWorkDirectory(), propFilePath).toFile().exists())
                ctx.propertyPath(Paths.get(ctx.localeWorkDirectory(), propFilePath).toAbsolutePath().toString());
            else {
                LOG.error(String.format("Failed to find property file '%s'", propFilePath));

                ctx.exitCode(1);

                System.exit(ctx.exitCode());
            }
        }

        LOG.debug(String.format("Property file path is '%s'.", ctx.propertyPath()));

        try {
            checkPropertyFile();

            Properties props = new Properties();

            props.load(new FileReader(ctx.propertyPath()));

            ctx.properties(props);
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        return true;
    }

    /**
     * Changes values taken from property file to values taken from command line arguments.
     */
    private void handleAdditionalArgs() {
        if (ctx.config().serverHosts() != null)
            ctx.serverHosts(hostsToList(ctx.config().serverHosts()));

        if (ctx.config().driverHosts() != null)
            ctx.driverHosts(hostsToList(ctx.config().driverHosts()));

        if (ctx.config().remoteWorkDirectory() != null)
            ctx.remoteWorkDirectory(ctx.config().remoteWorkDirectory());
    }

    /**
     * @param type Node type.
     */
    private void setRestartCtx(NodeType type) {
        String restProp = ctx.properties().getProperty(String.format("RESTART_%sS", type));

        if (restProp == null)
            return;

        if ("true".equals(restProp.toLowerCase()) || "false".equals(restProp.toLowerCase())) {
            setRestart(Boolean.valueOf(restProp), type);

            return;
        }

        parseRestartProp(restProp, type);
    }

    /**
     *
     * @param val value to set.
     * @param type Node type.
     */
    private void setRestart(boolean val, NodeType type) {
        if (type == NodeType.SERVER)
            ctx.startServersOnce(!val);
    }

    /**
     * Parse following string e.g. 172.25.1.49:0:5:5:5,172.25.1.49:1:5:5:5 to create restart schedule for node.
     *
     * @param restProp Restart property string.
     * @param type Node type.
     */
    private void parseRestartProp(String restProp, NodeType type) {
        String[] nodeList = restProp.split(",");

        RestartContext restCtx = new RestartContext();

        for (String nodeInfo : nodeList) {
            String[] values = nodeInfo.split(":");

            if (values.length != 5) {
                LOG.error(String.format("Wrong value for 'RESTART_%sS' property. String '%s' does not have 5 values.",
                    type, nodeInfo));

                ctx.exitCode(1);

                System.exit(ctx.exitCode());
            }

            String host = values[0];
            String id = values[1];

            try {
                int idInt = Integer.valueOf(id);

                List<String> hosts = ctx.hostsByType(type);

                String node = String.format("%s%s", type.toString().toLowerCase(), id);

                if(idInt >= hosts.size())
                    LOG.warn(String.format("Restart schedule '%s' was set for the node '%s' on the host '%s', but " +
                        "node '%s' will not start. (Number of defined hosts is less than %d).",
                        nodeInfo, node, host, node, idInt));

                if(idInt < hosts.size() && !hosts.get(idInt).equals(host))
                    LOG.warn(String.format("Restart schedule '%s' was set for the node '%s' on the host '%s', but " +
                        "node '%s' will be started on the host '%s'.", nodeInfo, node, host, node, hosts.get(idInt)));

            }
            catch (NumberFormatException e){
                LOG.error(String.format("Wrong value for 'RESTART_%sS' property. %s",
                    type, nodeInfo, e.getMessage()));
            }

            try {
                Long delay = convertSecToMillis(values[2]);
                Long pause = convertSecToMillis(values[3]);
                Long period = convertSecToMillis(values[4]);

                HashMap<String, RestartSchedule> hostMap = restCtx.get(host);

                if (hostMap == null)
                    hostMap = new HashMap<>();

                RestartSchedule restInfo = new RestartSchedule(delay, pause, period);

                hostMap.put(id, restInfo);

                restCtx.put(host, hostMap);
            }
            catch (NumberFormatException e) {
                LOG.error(String.format("Wrong value for 'RESTART_%sS' property. %s",
                    type, e.getMessage()));

                ctx.exitCode(1);

                System.exit(ctx.exitCode());
            }
        }

        LOG.debug(String.format("Restart context for '%s' nodes set as '%s'", type, restCtx));

        if (type == NodeType.SERVER)
            ctx.serverRestartContext(restCtx);
        else
            ctx.driverRestartContext(restCtx);
    }

    /**
     *
     * @param sec Seconds.
     * @return Milliseconds.
     * @throws NumberFormatException if failed.
     */
    private long convertSecToMillis(String sec) throws NumberFormatException {
        Double d = Double.valueOf(sec);

        Double dt = d * 1000;

        return dt.longValue();
    }

    /**
     *
     */
    private void setProps() {
//        ctx.properties(parseProps(ctx.propertiesOrig()));
//        ctx.properties(ctx.propertiesOrig());

        String remWorkDir = ctx.properties().getProperty("WORK_DIR") != null ?
            ctx.properties().getProperty("WORK_DIR") :
            ctx.localeWorkDirectory();

        ctx.remoteWorkDirectory(remWorkDir);

        LOG.debug(String.format("Remote work directory is '%s'.", ctx.remoteWorkDirectory()));
    }

    /**
     *
     */
    private void setHosts() {
        ctx.serverHosts(getHosts("SERVER_HOSTS"));

        ctx.driverHosts(getHosts("DRIVER_HOSTS"));

        Set<String> allHosts = ctx.getHostSet();

        Enumeration<NetworkInterface> e = null;

        try {
            e = NetworkInterface.getNetworkInterfaces();
        }
        catch (SocketException e1) {
            LOG.error("Failed to get network interfaces.", e1);
        }

        while (e.hasMoreElements()) {
            Enumeration<InetAddress> ee = e.nextElement().getInetAddresses();

            while (ee.hasMoreElements()) {
                String adr = ee.nextElement().getHostAddress();

                if (allHosts.contains(adr)) {
                    LOG.debug(String.format("Setting current host address as '%s'.", adr));

                    ctx.currentHost(adr);
                }
            }
        }

        if (ctx.currentHost() == null) {
            LOG.debug("Setting current host address as '127.0.0.1'.");

            ctx.currentHost("127.0.0.1");
        }
    }

    /**
     *
     */
    private void setConfigs(){
        String cfgFromProps = ctx.properties().getProperty("CONFIGS");

        if (cfgFromProps == null || cfgFromProps.isEmpty()){
            LOG.error(String.format("'CONFIGS' property is not defined in property file."));

            System.exit(1);
        }

        ctx.configs(cfgFromProps.split(","));
    }

    /**
     *
     */
    private void setServerName(){
        if(ctx.properties().getProperty("SERVER_NAME") != null)
            ctx.serverName(ctx.properties().getProperty("SERVER_NAME"));
    }

    /**
     *
     */
    private void setWarmup(){
        if(ctx.properties().getProperty("WARMUP") != null)
            ctx.warmup(ctx.properties().getProperty("WARMUP"));
    }

    /**
     *
     */
    private void setDuration(){
        if(ctx.properties().getProperty("DURATION") != null)
            ctx.duration(ctx.properties().getProperty("DURATION"));
    }

    /**
     *
     */
    private void setThreads(){
        if(ctx.properties().getProperty("THREADS") != null)
            ctx.threads(ctx.properties().getProperty("THREADS"));
    }

    /**
     *
     */
    private void setJavaHome() {
        String remJavaHome = null;

        if (ctx.properties().getProperty("JAVA_HOME") != null)
            remJavaHome = ctx.properties().getProperty("JAVA_HOME");

        String locJavaHome = System.getProperty("java.home");

        if (ctx.properties().getProperty("JAVA_HOME") != null && new File(String.format("%s/bin/java", remJavaHome)).exists())
            locJavaHome = String.format("%s/bin/java", remJavaHome);

        ctx.localeJavaHome(locJavaHome);

        ctx.remoteJavaHome(remJavaHome);
    }

    /**
     *
     */
    private void setRunModes() {
        RunMode servRunMode = ctx.properties().getProperty("RUN_SERVER_MODE") != null ?
            RunMode.valueOf(ctx.properties().getProperty("RUN_SERVER_MODE")) :
            RunMode.PLAIN;

        LOG.debug(String.format("Server run mode set as %s", servRunMode));

        ctx.serverRunMode(servRunMode);

        RunMode drvrRunMode = ctx.properties().getProperty("RUN_DRIVER_MODE") != null ?
            RunMode.valueOf(ctx.properties().getProperty("RUN_DRIVER_MODE")) :
            RunMode.PLAIN;

        LOG.debug(String.format("Driver run mode set as %s", drvrRunMode));

        ctx.driverRunMode(drvrRunMode);
    }

    /**
     *
     */
    private void setUser() {
        String remUser;

        if (ctx.properties().getProperty("REMOTE_USER") != null)
            remUser = ctx.properties().getProperty("REMOTE_USER");
        else {
            String locUser = System.getProperty("user.name");

            if (!ctx.onlyLocal()) {
                LOG.info(String.format("'REMOTE_USER' is not defined in property file. Will use '%s' " +
                    "username for remote connections.", locUser));
            }

            remUser = locUser;
        }

        ctx.remoteUser(remUser);
    }

    /**
     *
     */
    private void setCfgList() {
        List<String> cfgList = new ArrayList<>();

        for (String cfgStr : ctx.configs()) {
            if (cfgStr.length() < 5)
                continue;

            BenchmarkConfiguration cfg = new BenchmarkConfiguration();

            String[] toNewCfg = cfgStr.split(" ");

            // Check if parameters defined properly during initialization.
            try {
                BenchmarkUtils.jcommander(toNewCfg, cfg, "");
            }
            catch (Exception e){
                LOG.error(String.format("Failed to parse configuration string '%s': %s.", cfgStr, e.getMessage()));

                System.exit(1);
            }

            if(cfg.driverNames() == null){
                LOG.error(String.format("No driver names defined in configuration string %s.", cfgStr));

                System.exit(1);
            }

            cfgList.add(cfgStr);
        }

        ctx.configList(cfgList);
    }

    /**
     *
     * @return Number of nodes.
     */
    private int getNodesNum() {
        return ctx.getFullHostList().size();
    }

    /**
     * @param prop Host property.
     * @return List of host addresses.
     */
    private List<String> getHosts(String prop) {
        List<String> res = hostsToList(ctx.properties().getProperty(prop));

        if (res.isEmpty())
            LOG.info(String.format("WARNING! %s is not defined in property file.", prop));

        return res;
    }

    /**
     *
     * @param commaSepList Comma separated list of addresses.
     * @return List of host addresses.
     */
    private List<String> hostsToList(String commaSepList) {
        List<String> res = new ArrayList<>();

        if (commaSepList == null)
            return res;

        String[] ips = commaSepList.split(",");

        for (String ip : ips)
            res.add(ip);

        return res;
    }

    /**
     *
     */
    private void setDockerContext() {
        String dockerCtxPropPath;

        if (ctx.properties().getProperty("DOCKER_CONTEXT_PATH") == null) {
            dockerCtxPropPath = String.format("%s/config/docker/docker-context-default.yaml", ctx.localeWorkDirectory());

            LOG.info("'DOCKER_CONTEXT_PATH' is not defined in property file. Will " +
                "use default docker context configuration:");

            LOG.info(dockerCtxPropPath);
        }
        else {
            dockerCtxPropPath = resolvePath(ctx.properties().getProperty("DOCKER_CONTEXT_PATH"));

            if(dockerCtxPropPath == null || !new File(dockerCtxPropPath).exists()){
                LOG.error(String.format("Failed to find docker context file '%s'.", dockerCtxPropPath));

                ctx.exitCode(1);

                System.exit(ctx.exitCode());
            }
        }

        ctx.dockerContext(DockerContext.getDockerContext(dockerCtxPropPath));
    }

    /**
     *
     * @param srcPath Relative path.
     * @return Absolute path.
     */
    private String resolvePath(String srcPath) {
        if (srcPath == null || srcPath.isEmpty())
            return null;

        if (new File(srcPath).exists())
            return srcPath;

        String fullPath = null;

        if(!srcPath.startsWith(ctx.localeWorkDirectory()))
            fullPath = String.format("%s/%s", ctx.localeWorkDirectory(), srcPath);

        if (new File(fullPath).exists())
            return fullPath;

        LOG.error(String.format("Failed to find '%s' or '%s'.", srcPath, fullPath));

        ctx.exitCode(1);

        System.exit(ctx.exitCode());

        return null;
    }

    /**
     *
     * @return {@code true} if property file is OK or {@code false} otherwise.
     * @throws IOException if failed.
     */
    private boolean checkPropertyFile() throws IOException {
        String path = ctx.propertyPath();

        Set<String> propSet = new HashSet<>();

        BufferedReader reader = new BufferedReader(new FileReader(path));

        String line;

        while ((line = reader.readLine()) != null) {
            if (!line.contains("=") || line.startsWith("#"))
                continue;

            int idx0 = line.indexOf("=");

            String propName = line.substring(0, idx0);

            if (propSet.contains(propName)) {
                LOG.error(String.format("Property '%s' defined more than once in property file.", propName));

                System.exit(1);
            }
            else
                propSet.add(propName);
        }

        return false;
    }

    /**
     *
     */
    private void configLog() {
        String logPropPath = String.format("%s/config/log4j.properties", ctx.localeWorkDirectory());

        String logPath = Paths.get(ctx.localeWorkDirectory(), "output", String.format("logs-%s/%s-run.log",
            ctx.mainDateTime(), ctx.mainDateTime())).toString();

        if (new File(logPropPath).exists()){
            Properties logProps = new Properties();

            try {
                logProps.load(new FileInputStream(logPropPath));

                if (logProps.getProperty("log4j.appender.file.File") == null)
                    logProps.setProperty("log4j.appender.file.File", logPath);

                PropertyConfigurator.configure(logProps);
            } catch (IOException e) {
                configDefaultLog(logPath);

                LOG.error(String.format("Failed to load logging property file %s :", logPropPath), e);
            }
        }
        else
            configDefaultLog(logPath);
    }

    /**
     *
     * @param logPath Log file path.
     */
    private void configDefaultLog(String logPath){
        ConsoleAppender console = new ConsoleAppender();

        String ptrn = "[%d{yyyy-MM-dd HH:mm:ss}][%-5p] %m%n";

        console.setLayout(new PatternLayout(ptrn));

        console.setThreshold(Level.INFO);

        console.activateOptions();

        Logger.getRootLogger().addAppender(console);

        FileAppender fileApp = new FileAppender();

        fileApp.setName("FileLogger");

        fileApp.setFile(logPath);

        fileApp.setLayout(new PatternLayout("[%d{yyyy-MM-dd HH:mm:ss,SSS}][%-5p][%t] %m%n"));

        fileApp.setThreshold(Level.INFO);

        fileApp.setAppend(true);

        fileApp.activateOptions();

        Logger.getRootLogger().addAppender(fileApp);
    }
}
