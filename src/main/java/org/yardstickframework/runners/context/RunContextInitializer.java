package org.yardstickframework.runners.context;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
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

        setJavaHome();

        setRunModes();

        setUser();

        setCfgList();

        setRestartCtx(NodeType.SERVER);

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

            e.printStackTrace();

            System.exit(1);
        }

        if (ctx.config().help())
            return false;

        if (ctx.config().scriptDirectory() == null) {
            System.out.println("Error: Script directory is not defined.");

            System.exit(1);
        }

        ctx.localeWorkDirectory(new File(ctx.config().scriptDirectory()).getParentFile().getAbsolutePath());

        configLog();

        LOG.info(String.format("Local work directory is %s", ctx.localeWorkDirectory()));

        if (ctx.config().propertyFile() == null) {
            String dfltPropPath = String.format("%s/config/benchmark.properties", ctx.localeWorkDirectory());

            LOG.info(String.format("Using as a default property file %s", dfltPropPath));

            if (!new File(dfltPropPath).exists()) {
                LOG.info(String.format("Failed to find default property file %s", dfltPropPath));

                System.exit(1);
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
                LOG.info(String.format("Error. Failed to find property %s", propFilePath));

                System.exit(1);
            }
        }

        LOG.info(String.format("Property file path is %s", ctx.propertyPath()));

        try {
            Properties propsOrig = new Properties();

            propsOrig.load(new FileReader(ctx.propertyPath()));

            ctx.propertiesOrig(propsOrig);
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

        if (restProp == null) {
            setRestart(false, type);

            return;
        }

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
                LOG.error(String.format("Wrong value for RESTART_%sS property. String %s does not have 5 values.",
                    type, nodeInfo));

                System.exit(1);
            }

            String host = values[0];
            String id = values[1];

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
                LOG.error(String.format("Wrong value for RESTART_%sS property. %s",
                    type, e.getMessage()));

                System.exit(1);
            }
        }

        LOG.debug(String.format("Restart context for %s nodes set as %s", type, restCtx));

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
        ctx.properties(parseProps(ctx.propertiesOrig()));

        String remWorkDir = ctx.properties().getProperty("WORK_DIR") != null ?
            ctx.properties().getProperty("WORK_DIR") :
            ctx.localeWorkDirectory();

        ctx.remoteWorkDirectory(remWorkDir);

        LOG.info(String.format("Remote work directory is %s", ctx.remoteWorkDirectory()));
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
                    LOG.info(String.format("Setting current host address as %s.", adr));

                    ctx.currentHost(adr);
                }
            }
        }

        if (ctx.currentHost() == null) {
            LOG.info("Setting current host address as 127.0.0.1");

            ctx.currentHost("127.0.0.1");
        }
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

            LOG.info(String.format("REMOTE_USER is not defined in property file. Will use '%s' " +
                "username for remote connections.", locUser));

            remUser = locUser;
        }

        ctx.remoteUser(remUser);
    }

    /**
     *
     */
    private void setCfgList() {
        List<String> cfgList = new ArrayList<>();

        for (String cfgStr : ctx.properties().getProperty("CONFIGS").split(",")) {
            if (cfgStr.length() < 10)
                continue;

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
        List<String> res = hostsToList(ctx.propertiesOrig().getProperty(prop));

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

        for (String ip : ips) {
            check(ip);

            res.add(ip);
        }

        return res;
    }

    /**
     *
     * @param host Host.
     */
    private void check(String host) {
        //TODO
    }

    /**
     *
     * @param src Source properties.
     * @return Parsed properties.
     */
    private Properties parseProps(Properties src) {
        Properties res = new Properties();

        for (String propName : src.stringPropertyNames()) {
            String newVal = parsePropVal(src.getProperty(propName));

            res.setProperty(propName, newVal.replace("\"", ""));
        }

        return res;
    }

    /**
     * @param src Source string
     * @return Parsed string.
     */
    private String parsePropVal(String src) {
        String res = src.replace("\"", "");

        if (src.contains("${SCRIPT_DIR}/.."))
            res = res.replace("${SCRIPT_DIR}/..", ctx.localeWorkDirectory());
        if (src.contains("${nodesNum}"))
            res = res.replace("${nodesNum}", String.valueOf(getNodesNum()));

        for (String propName : ctx.propertiesOrig().stringPropertyNames()) {
            if (src.contains(String.format("${%s}", propName)))
                res = res.replace(String.format("${%s}", propName), ctx.propertiesOrig().get(propName).toString());

            if (src.contains(String.format("$%s", propName)))
                res = res.replace(String.format("$%s", propName), ctx.propertiesOrig().get(propName).toString());
        }

        return res;
    }

    /**
     *
     */
    private void setDockerContext() {
        String dockerCtxPropPath;

        if (ctx.properties().getProperty("DOCKER_CONTEXT_PATH") == null) {
            dockerCtxPropPath = String.format("%s/config/docker/docker-context-default.yaml", ctx.localeWorkDirectory());

            LOG.info(String.format("DOCKER_CONTEXT_PATH is not defined in property file. Will try " +
                "to use default docker context configuration %s", dockerCtxPropPath));
        }
        else
            dockerCtxPropPath = resolvePath(ctx.properties().getProperty("DOCKER_CONTEXT_PATH"));

        ctx.dockerContext(DockerContext.getDockerContext(dockerCtxPropPath));
    }

    /**
     *
     * @param srcPath Relative path.
     * @return Absolute path.
     */
    private String resolvePath(String srcPath) {
        if (new File(srcPath).exists())
            return srcPath;

        String fullPath = null;

        if(!srcPath.startsWith(ctx.localeWorkDirectory()))
            fullPath = String.format("%s/%s", ctx.localeWorkDirectory(), srcPath);

        if (new File(fullPath).exists())
            return fullPath;

        LOG.info(String.format("Failed to find %s or %s.", srcPath, fullPath));

        System.exit(1);

        return null;
    }

    /**
     *
     */
    private void configLog() {
        ConsoleAppender console = new ConsoleAppender(); //create appender
        //configure the appender
        String ptrn = "[%d{yyyy-MM-dd HH:mm:ss}][%-5p][%t] %m%n";
        console.setLayout(new PatternLayout(ptrn));
        console.setThreshold(Level.INFO);
        console.activateOptions();
        //add appender to any Logger (here is root)
        Logger.getRootLogger().addAppender(console);

        FileAppender fa = new FileAppender();
        fa.setName("FileLogger");
        fa.setFile(Paths.get(ctx.localeWorkDirectory(), "output", String.format("logs-%s/%s-run.log",
            ctx.mainDateTime(), ctx.mainDateTime())).toString());
        fa.setLayout(new PatternLayout("[%d{yyyy-MM-dd HH:mm:ss,SSS}][%-5p][%t] %m%n"));
        fa.setThreshold(Level.INFO);
        fa.setAppend(true);
        fa.activateOptions();

        //add appender to any Logger (here is root)
        Logger.getRootLogger().addAppender(fa);

        //TODO remove
        if (new File("/home/oostanin/yardstick").exists()) {
            FileAppender fa1 = new FileAppender();
            fa1.setName("FileLogger1");
            fa1.setFile(String.format("/home/oostanin/yardstick/log-%s.log", BenchmarkUtils.hms()));
            fa1.setLayout(new PatternLayout("[%d{yyyy-MM-dd HH:mm:ss,SSS}][%-5p][%t] %m%n"));
            fa1.setThreshold(Level.DEBUG);
            fa1.setAppend(false);
            fa1.activateOptions();

            //add appender to any Logger (here is root)
            Logger.getRootLogger().addAppender(fa1);
        }
    }
}
