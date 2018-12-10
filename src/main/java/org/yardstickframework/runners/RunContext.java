package org.yardstickframework.runners;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
import org.yardstickframework.BenchmarkUtils;
import org.yardstickframework.runners.docker.DockerContext;

import static org.yardstickframework.BenchmarkUtils.dateTime;

/**
 *
 */
public class RunContext {
    /** */
    private static final Logger LOG = LogManager.getLogger(RunContext.class);

    private static RunContext instance;

    private static RunnerConfiguration cfg = new RunnerConfiguration();

    private Properties props;

    private Properties propsOrig;

    private static String locWorkDir;

    private static String remWorkDir;

    private String propPath;

    private String locJavaHome;

    private String remJavaHome;

    private Map<String, String> hostJavaHomeMap = new HashMap<>();

    private String locUser;

    private String remUser;

    private List<String> servHosts;

    private List<String> drvrHosts;

    private String currentHost;

    private String mainDateTime;

    protected RunMode servRunMode;

    protected RunMode drvrRunMode;

    private List<String> cfgList;

    private boolean restartServers;

    private boolean startServersOnce;

    private RestartContext serverRestartCtx;

    private boolean restartDrivers;

    private RestartContext driverRestartCtx;

    private DockerContext dockerCtx;

    private RunContext() {
        //No_op
    }

    public Properties getProps() {
        return props;
    }

    public String getLocWorkDir() {
        return locWorkDir;
    }

    public String getRemWorkDir() {
        return remWorkDir;
    }

    public String getPropPath() {
        return propPath;
    }

    public String getLocJavaHome() {
        return locJavaHome;
    }

    public String getRemJavaHome() {
        return remJavaHome;
    }

    public Map<String, String> getHostJavaHomeMap() {
        return hostJavaHomeMap;
    }

    public String getLocUser() {
        return locUser;
    }

    public String getRemUser() {
        return remUser;
    }

    public List<String> getServHosts() {
        return servHosts;
    }

    public List<String> getDrvrHosts() {
        return drvrHosts;
    }

    public String getCurrentHost() {
        return currentHost;
    }

    public String getMainDateTime() {
        return mainDateTime;
    }

    public RunMode getServRunMode() {
        return servRunMode;
    }

    public RunMode getDrvrRunMode() {
        return drvrRunMode;
    }

    public List<String> getCfgList() {
        return cfgList;
    }

    public static Logger getLOG() {
        return LOG;
    }

    public boolean isRestartServers() {
        return restartServers;
    }

    public void setRestartServers(boolean restartServers) {
        this.restartServers = restartServers;
    }

    public RestartContext getServerRestartCtx() {
        return serverRestartCtx;
    }

    public void setRestartCtx(RestartContext serverRestartCtx) {
        this.serverRestartCtx = serverRestartCtx;
    }

    public boolean isRestartDrivers() {
        return restartDrivers;
    }

    public void setRestartDrivers(boolean restartDrivers) {
        this.restartDrivers = restartDrivers;
    }

    public RestartContext getDriverRestartCtx() {
        return driverRestartCtx;
    }

    public void setDriverRestartCtx(RestartContext driverRestartCtx) {
        this.driverRestartCtx = driverRestartCtx;
    }

    /**
     *
     */
    public static RunContext getRunContext(String[] args) {
        if (instance == null) {
            instance = new RunContext();

            instance.setContext(args);
        }

        return instance;
    }

    private void setContext(String[] args) {
        mainDateTime = dateTime();

        handleArgs(args);

        setHosts();

        setProps();

        setJavaHome();

        setRunModes();

        setUser();

        setCfgList();

        setRestartCtx(NodeType.SERVER);

//        if(servRunMode == RunMode.DOCKER || drvrRunMode == RunMode.DOCKER)
//            setDockerCtx();
    }

    /**
     * @param args
     */
    private void handleArgs(String[] args) {
//        if (args.length == 0) {
//            args = new String[] {
//                "/home/oostanin/gg/incubator-ignite/modules/yardstick/target/assembly/bin",
//                "/home/oostanin/gg/incubator-ignite/modules/yardstick/target/assembly/config/benchmark.properties"};
//        }
        try {

            BenchmarkUtils.jcommander(args, cfg, "<benchmark-runner>");
        }
        catch (Exception e) {
            BenchmarkUtils.println("Failed to parse command line arguments. " + e.getMessage());

            e.printStackTrace();

            System.exit(1);
        }

        if (cfg.scriptDirectory() == null) {
            log().error("Script directory is not defined.");

            System.exit(1);
        }

        locWorkDir = new File(cfg.scriptDirectory()).getParentFile().getAbsolutePath();

        configLog();

        LOG.info(String.format("Local work directory is %s", locWorkDir));

        if (cfg.propertyFile() == null) {
            String dfltPropPath = String.format("%s/config/benchmark.properties", locWorkDir);

            log().info(String.format("Using as a default property file %s", dfltPropPath));

            if (!new File(dfltPropPath).exists()) {
                log().info(String.format("Failed to find default property file %s", dfltPropPath));

                System.exit(1);
            }

            propPath = dfltPropPath;
        }
        else {
            String propFilePath = cfg.propertyFile();

            if (new File(propFilePath).exists())
                propPath = new File(propFilePath).getAbsolutePath();
            else if (Paths.get(locWorkDir, propFilePath).toFile().exists())
                propPath = Paths.get(locWorkDir, propFilePath).toAbsolutePath().toString();
            else {
                log().info(String.format("Error. Failed to find property %s", propFilePath));

                System.exit(1);
            }
        }

        LOG.info(String.format("Property file path is %s", propPath));

        try {
            propsOrig = new Properties();

            propsOrig.load(new FileReader(propPath));
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setRestartCtx(NodeType type) {
        String restProp = props.getProperty(String.format("RESTART_%sS", type));

        boolean val = false;

        if (restProp == null) {
            setRestart(val, type);

            return;
        }

        if (restProp.toLowerCase().equals("true") || restProp.toLowerCase().equals("false")) {
            val = Boolean.valueOf(restProp);

            setRestart(val, type);

            return;
        }

        parseRestartProp(restProp, type);
    }

    private void setRestart(boolean val, NodeType type) {
        if (type == NodeType.SERVER) {
            restartServers = val;

            startServersOnce = !val;
        }
        else
            restartDrivers = val;
    }

    private void parseRestartProp(String restProp, NodeType type) {
        String[] nodeList = restProp.split(",");

        RestartContext ctx = new RestartContext();

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

                HashMap<String, RestartSchedule> hostMap = ctx.get(host);

                if (hostMap == null)
                    hostMap = new HashMap<>();

                RestartSchedule restInfo = new RestartSchedule(delay, pause, period);

                hostMap.put(id, restInfo);

                ctx.put(host, hostMap);

//                System.out.println(hostMap);
            }
            catch (NumberFormatException e) {
                LOG.error(String.format("Wrong value for RESTART_%sS property. %s",
                    type, e.getMessage()));

                System.exit(1);
            }
        }

        log().debug(String.format("Restart context for %s nodes set as %s", type, ctx));

        if (type == NodeType.SERVER)
            serverRestartCtx = ctx;
        else
            driverRestartCtx = ctx;
    }

    private long convertSecToMillis(String sec) throws NumberFormatException {
        Double d = Double.valueOf(sec);

        Double dMult = d * 1000;

        return dMult.longValue();
    }

    /**
     *
     */
    private void setProps() {
        props = parseProps(propsOrig);

        remWorkDir = props.getProperty("WORK_DIR") != null ?
            props.getProperty("WORK_DIR") :
            locWorkDir;

        LOG.info(String.format("Remote work directory is %s", remWorkDir));
    }

    /**
     *
     */
    private void setHosts() {
        servHosts = getHosts("SERVER_HOSTS");

        drvrHosts = getHosts("DRIVER_HOSTS");

        List<String> allHosts = getFullUniqList();

        Enumeration e = null;

        try {
            e = NetworkInterface.getNetworkInterfaces();
        }
        catch (SocketException e1) {
            e1.printStackTrace();
        }

        while (e.hasMoreElements()) {
            NetworkInterface n = (NetworkInterface)e.nextElement();

            Enumeration ee = n.getInetAddresses();

            while (ee.hasMoreElements()) {
                InetAddress i = (InetAddress)ee.nextElement();

                String adr = i.getHostAddress();

                if (allHosts.contains(adr)) {
                    LOG.info(String.format("Setting current host address as %s.", adr));

                    currentHost = adr;
                }
            }
        }

        if (currentHost == null) {
            LOG.info("Setting current host address as 127.0.0.1");

            currentHost = "127.0.0.1";
        }
    }

    /**
     *
     */
    private void setJavaHome() {
        locJavaHome = System.getProperty("java.home");

        if (props.getProperty("JAVA_HOME") != null)
            remJavaHome = props.getProperty("JAVA_HOME");

        if (props.getProperty("JAVA_HOME") != null && new File(String.format("%s/bin/java", remJavaHome)).exists())
            locJavaHome = String.format("%s/bin/java", remJavaHome);
    }

    /**
     *
     */
    private void setRunModes() {
        servRunMode = props.getProperty("RUN_SERVER_MODE") != null ?
            RunMode.valueOf(props.getProperty("RUN_SERVER_MODE")) :
            RunMode.PLAIN;

        LOG.debug(String.format("Server run mode set as %s", servRunMode));

        drvrRunMode = props.getProperty("RUN_DRIVER_MODE") != null ?
            RunMode.valueOf(props.getProperty("RUN_DRIVER_MODE")) :
            RunMode.PLAIN;

        LOG.debug(String.format("Driver run mode set as %s", drvrRunMode));
    }

    /**
     * @return Value
     */
    protected void setUser() {
        locUser = System.getProperty("user.name");

        if (props.getProperty("REMOTE_USER") != null)
            remUser = props.getProperty("REMOTE_USER");
        else {
            log().info(String.format("REMOTE_USER is not defined in property file. Will use '%s' " +
                "username for remote connections.", locUser));

            remUser = locUser;
        }
    }

    protected void setCfgList() {
        cfgList = new ArrayList<>();

        for (String cfgStr : props.getProperty("CONFIGS").split(",")) {
            if (cfgStr.length() < 10)
                continue;

            cfgList.add(cfgStr);
        }
    }

    private void setDockerCtx() {
        if (getServRunMode() == RunMode.DOCKER || getDrvrRunMode() == RunMode.DOCKER)
            dockerCtx = DockerContext.getDockerContext(String.format("%s/config/docker/docker-context.yaml", locWorkDir));

    }

    /** */
    public List<String> getFullHostList() {
        List<String> res = new ArrayList<>(getServList());

        res.addAll(getDrvrList());

        return res;
    }

    /** */
    public List<String> getFullUniqList() {
        List<String> res = new ArrayList<>();

        res.addAll(getServList());

        res.addAll(getDrvrList());

        return makeUniq(res);
    }

    private int getNodesNum() {
        return getFullHostList().size();
    }

    public List<String> getServUniqList() {
        return makeUniq(getServList());
    }

    public List<String> getDrvrUniqList() {
        return makeUniq(getDrvrList());
    }

    public List<String> getServList() {
        return servHosts;
    }

    public List<String> getDrvrList() {
        return drvrHosts;
    }

    /**
     * @param prop
     * @return Value
     */
    private List<String> getHosts(String prop) {
        List<String> res = new ArrayList<>();

        String commaSepList = propsOrig.getProperty(prop);

        if (commaSepList == null)
            return res;

        String[] ips = commaSepList.split(",");

        for (String ip : ips) {
            check(ip);

            res.add(ip);
        }

        if (res.isEmpty())
            log().info(String.format("WARNING! %s is not defined in property file.", prop));

        return res;
    }

    /**
     * @param src
     * @return Value
     */
    private List<String> makeUniq(List<String> src) {
        Set<String> set = new HashSet<>(src);

        List<String> res = new ArrayList<>(set);

        Collections.sort(res);

        return res;
    }

    private void check(String ip) {
        //TODO
    }

    private Properties parseProps(Properties src) {
        Properties res = new Properties();

        for (String propName : src.stringPropertyNames()) {
            String newVal = parsePropVal(src.getProperty(propName));

            res.setProperty(propName, newVal.replace("\"", ""));
        }

        return res;
    }

    /**
     * @param src
     * @return Value
     */
    private String parsePropVal(String src) {
        String res = src.replace("\"", "");

        if (src.contains("${SCRIPT_DIR}/.."))
            res = res.replace("${SCRIPT_DIR}/..", locWorkDir);
        if (src.contains("${nodesNum}"))
            res = res.replace("${nodesNum}", String.valueOf(getNodesNum()));

        for (String propName : propsOrig.stringPropertyNames()) {
            if (src.contains(String.format("${%s}", propName)))
                res = res.replace(String.format("${%s}", propName), propsOrig.get(propName).toString());

            if (src.contains(String.format("$%s", propName)))
                res = res.replace(String.format("$%s", propName), propsOrig.get(propName).toString());
        }

        return res;
    }

    protected NodeStarter getNodeStarter(NodeInfo nodeInfo) {
        RunMode mode = getStartMode(nodeInfo);

        switch (mode) {
            case PLAIN:
                return new PlainNodeStarter(this);
            case DOCKER:
                return new InDockerNodeStarter(this);
            default:
                throw new IllegalArgumentException(String.format("Unknown start mode: %s", mode.toString()));
        }
    }

    protected NodeChecker getNodeChecker(NodeInfo nodeInfo) {
        RunMode mode = getStartMode(nodeInfo);

        switch (mode) {
            case PLAIN:
                return new PlainNodeChecker(this);
            case DOCKER:
                return new InDockerNodeChecker(this);
            default:
                throw new IllegalArgumentException(String.format("Unknown start mode: %s", mode.toString()));
        }
    }

    public DockerContext getDockerContext() {
        if (dockerCtx != null)
            return dockerCtx;

        String dockerCtxPropPath = null;

        if (props.getProperty("DOCKER_CONTEXT_PATH") == null) {
            dockerCtxPropPath = String.format("%s/config/docker/docker-context-default.yaml", locWorkDir);

            log().info(String.format("DOCKER_CONTEXT_PATH is not defined in property file. Will try " +
                "to use default docker context configuration %s", dockerCtxPropPath));
        }
        else
            dockerCtxPropPath = resolvePath(props.getProperty("DOCKER_CONTEXT_PATH"));

        dockerCtx = DockerContext.getDockerContext(dockerCtxPropPath);

        return dockerCtx;
    }

    private RunMode getStartMode(NodeInfo nodeInfo) {
        return nodeInfo.runMode();
    }

    protected String getDescription(String src) {
        String[] argArr = src.split(" ");

        String res = "Unknown";

        for (int i = 0; i < argArr.length; i++) {
            if (argArr[i].equals("-ds") || argArr[i].equals("--descriptions")) {
                if (argArr.length < i + 2) {
                    log().info("Failed to get description");

                    return res;
                }
                else
                    res = argArr[i + 1];
            }
        }

        return res;
    }

    //TODO
    public static String resolvePath(String srcPath) {
        if (new File(srcPath).exists())
            return srcPath;

        String fullPath = String.format("%s/%s", locWorkDir, srcPath);

        if (new File(fullPath).exists())
            return fullPath;

        log().info(String.format("Failed to find %s or %s.", srcPath, fullPath));

        System.exit(1);

        return null;
    }

    //TODO
    public static String resolveRemotePath(String srcPath) {
        String fullPath = String.format("%s/%s", remWorkDir, srcPath);

        return fullPath;
    }

    public boolean checkIfDifferentHosts() {
        for (String host : getServUniqList())
            if (getDrvrUniqList().contains(host))
                return false;

        return true;
    }

    public List<NodeType> getNodeTypes(RunMode mode) {
        List<NodeType> res = new ArrayList<>();

        if (getServRunMode() == mode)
            res.add(NodeType.SERVER);

        if (getDrvrRunMode() == mode)
            res.add(NodeType.DRIVER);

        return res;
    }

    public List<String> getHostsByType(NodeType type) {
        return type == NodeType.SERVER ? servHosts : drvrHosts;
    }

    public List<String> getHostsByMode(RunMode mode) {
        List<String> res = new ArrayList<>();

        if(servRunMode == mode)
            res.addAll(servHosts);

        if(drvrRunMode == mode)
            res.addAll(drvrHosts);

        return res;
    }

    public List<String> getUniqHostsByType(NodeType type) {
        return makeUniq(getHostsByType(type));
    }

    public List<String> getUniqHostsByMode(RunMode mode) {
        return makeUniq(getHostsByMode(mode));
    }

    public String getHostJava(String host) {
        return hostJavaHomeMap.get(host);
    }

    private void configLog() {
        ConsoleAppender console = new ConsoleAppender(); //create appender
        //configure the appender
        String PATTERN = "[%d{yyyy-MM-dd HH:mm:ss}][%-5p][%t] %m%n";
        console.setLayout(new PatternLayout(PATTERN));
        console.setThreshold(Level.INFO);
        console.activateOptions();
        //add appender to any Logger (here is root)
        Logger.getRootLogger().addAppender(console);

        FileAppender fa = new FileAppender();
        fa.setName("FileLogger");
        fa.setFile(Paths.get(locWorkDir, "output", String.format("logs-%s/%s-run.log",
            mainDateTime, mainDateTime)).toString());
        fa.setLayout(new PatternLayout("[%d{yyyy-MM-dd HH:mm:ss,SSS}][%-5p][%t] %m%n"));
        fa.setThreshold(Level.INFO);
        fa.setAppend(true);
        fa.activateOptions();

        //add appender to any Logger (here is root)
        Logger.getRootLogger().addAppender(fa);

        if(new File("/home/oostanin/yardstick").exists()) {
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

    public RunMode getRunMode(NodeType type) {
        switch (type) {
            case SERVER:
                return servRunMode;
            case DRIVER:
                return drvrRunMode;
            default:
                throw new IllegalArgumentException(String.format("Unknown node type: %s", type));
        }
    }

    public List<NodeInfo> getNodes(NodeType type) {
        List<String> hosts = getHostsByType(type);

        RunMode mode = getRunMode(type);

        List<NodeInfo> res = new ArrayList<>(hosts.size());

        for (int i = 0; i < hosts.size(); i++)
            res.add(new NodeInfo(type, hosts.get(i), null, String.valueOf(i), mode));

        return res;
    }

    public RestartContext getRestartContext(NodeType type) {
        switch (type) {
            case SERVER:
                return serverRestartCtx;
            case DRIVER:
                return driverRestartCtx;
            default:
                throw new IllegalArgumentException(String.format("Unknown node type: %s", type));
        }

    }

    protected static Logger log() {
        Logger log = LogManager.getLogger(RunContext.class.getSimpleName());

        return log;
    }

    /**
     * @return Config.
     */
    public RunnerConfiguration config() {
        return cfg;
    }

    /**
     * @return Start servers once.
     */
    public boolean startServersOnce() {
        return startServersOnce;
    }

    /**
     * @param startSrvsOnce New start servers once.
     */
    public void startServersOnce(boolean startSrvsOnce) {
        startServersOnce = startSrvsOnce;
    }
}
