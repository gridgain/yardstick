package org.yardstickframework.runners.context;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.Map;
import java.util.Properties;
import java.util.List;
import java.util.TreeSet;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.yardstickframework.runners.CommandHandler;
import org.yardstickframework.runners.checkers.InDockerNodeChecker;
import org.yardstickframework.runners.starters.InDockerNodeStarter;
import org.yardstickframework.runners.checkers.NodeChecker;
import org.yardstickframework.runners.starters.NodeStarter;
import org.yardstickframework.runners.checkers.PlainNodeChecker;
import org.yardstickframework.runners.starters.PlainNodeStarter;

/**
 * Run context.
 */
public class RunContext {
    /** */
    private static RunContext instance;

    /** */
    private int exitCode;

    /** */
    private static RunnerConfiguration cfg = new RunnerConfiguration();

    /** */
    private boolean help;

    /** */
    private Properties props;

    /** */
    private Properties propsOrig;

    /** */
    private String locWorkDir;

    /** */
    private String remWorkDir;

    /** */
    private String propPath;

    /** */
    private String locJavaHome;

    /** */
    private String remJavaHome;

    /** */
    private Map<String, String> hostJavaHomeMap = new HashMap<>();

    /** */
    private String remUser;

    /** */
    private List<String> servHosts;

    /** */
    private List<String> drvrHosts;

    /** */
    private String currHost;

    /** */
    private String mainDateTime;

    /** */
    private RunMode servRunMode;

    /** */
    private RunMode drvrRunMode;

    /** */
    private List<String> cfgList;

    /** */
    private boolean startServOnce;

    /** */
    private boolean startServEndExit;

    /** */
    private RestartContext servRestartCtx;

    /** */
    private RestartContext driverRestartCtx;

    /** */
    private DockerContext dockerCtx;

    /** */
    private CommandHandler hand;

    /**
     * Constructor.
     */
    private RunContext() {
        //No_op
    }

    /**
     * @return Exit code.
     */
    public int exitCode() {
        return exitCode;
    }

    /**
     * @param exitCode New exit code.
     */
    public void exitCode(int exitCode) {
        this.exitCode = exitCode;
    }

    /**
     * @return Help.
     */
    public boolean help() {
        return help;
    }

    /**
     * Creates and sets run context.
     *
     * @param args {@code String[]} Command line arguments.
     * @return {@code RunContext} Run context instance.
     */
    public static synchronized RunContext getRunContext(String[] args) {
        if (instance == null) {
            instance = new RunContext();

            RunContextInitializer init = new RunContextInitializer(instance);

            init.initialize(args);
        }

        return instance;
    }

    /**
     * @return Full list of server and driver IP addresses.
     */
    public List<String> getFullHostList() {
        List<String> res = new ArrayList<>(servHosts);

        res.addAll(drvrHosts);

        return res;
    }

    /**
     * @return Full unique list of server and driver IP addresses.
     */
    public Set<String> getHostSet() {
        Set<String> res = new TreeSet<>(servHosts);

        if(!startServEndExit)
            res.addAll(drvrHosts);

        return res;
    }

    /**
     * @return Unique list of driver IP addresses.
     */
    public Set<String> driverSet() {
        return new TreeSet<>(drvrHosts);
    }

    /**
     * @param nodeInfo Node info.
     * @return Node starter depending on node run mode.
     */
    public NodeStarter nodeStarter(NodeInfo nodeInfo) {
        RunMode mode = nodeInfo.runMode();

        switch (mode) {
            case PLAIN:
                return new PlainNodeStarter(this);
            case DOCKER:
                return new InDockerNodeStarter(this);
            default:
                throw new IllegalArgumentException(String.format("Unknown start mode: %s", mode.toString()));
        }
    }

    /**
     * @param nodeInfo Node info.
     * @return Node checker depending on node run mode.
     */
    public NodeChecker nodeChecker(NodeInfo nodeInfo) {
        RunMode mode = nodeInfo.runMode();

        switch (mode) {
            case PLAIN:
                return new PlainNodeChecker(this);
            case DOCKER:
                return new InDockerNodeChecker(this);
            default:
                throw new IllegalArgumentException(String.format("Unknown start mode: %s", mode.toString()));
        }
    }

    /**
     * @param src {@code String} Source string.
     * @return Description.
     */
    public String description(String src) {
        String[] argArr = src.split(" ");

        String res = "Unknown";

        for (int i = 0; i < argArr.length; i++) {
            if ("-ds".equals(argArr[i]) || "--descriptions".equals(argArr[i])) {
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

    /**
     * Resolves remote path.
     *
     * @param srcPath Relative path.
     * @return Remote path.
     */
    public String resolveRemotePath(String srcPath) {
        if (!srcPath.startsWith(remWorkDir))
            return String.format("%s/%s", remWorkDir, srcPath);

        return srcPath;
    }

    /**
     * Checks if server host list contains any of driver hosts and vice versa.
     *
     * @return {@code true} if all hosts are different or {@code false} otherwise.
     */
    public boolean checkIfDifferentHosts() {
        for (String host : servHosts)
            if (drvrHosts.contains(host))
                return false;

        return true;
    }

    /**
     * @param mode Run mode.
     * @return List of related node types depending on run mode.
     */
    public List<NodeType> nodeTypes(RunMode mode) {
        List<NodeType> res = new ArrayList<>();

        if (servRunMode == mode)
            res.add(NodeType.SERVER);

        if (drvrRunMode == mode && !startServEndExit)
            res.add(NodeType.DRIVER);

        return res;
    }

    /**
     * @param type Node type.
     * @return List of related hosts depending on node type.
     */
    List<String> hostsByType(NodeType type) {
        return type == NodeType.SERVER ? servHosts : drvrHosts;
    }

    /**
     * @param mode Run mode.
     * @return List of related hosts depending on run mode.
     */
    private List<String> hostsByMode(RunMode mode) {
        List<String> res = new ArrayList<>();

        if (servRunMode == mode)
            res.addAll(servHosts);

        if (drvrRunMode == mode && !startServEndExit)
            res.addAll(drvrHosts);

        return res;
    }

    /**
     * @param type Node type.
     * @return List of unique related hosts depending on node type.
     */
    public Set<String> uniqueHostsByType(NodeType type) {
        return new TreeSet<>(hostsByType(type));
    }

    /**
     * @param mode Run mode.
     * @return List of unique related hosts depending on run mode.
     */
    public Set<String> uniqueHostsByMode(RunMode mode) {
        return new TreeSet<>(hostsByMode(mode));
    }

    /**
     * @param host Host.
     * @return {@code String} Host Java home.
     */
    public String getHostJava(String host) {
        return hostJavaHomeMap.get(host);
    }

    /**
     * @param type Node type.
     * @return Node run mode.
     */
    private RunMode getRunMode(NodeType type) {
        switch (type) {
            case SERVER:
                return servRunMode;
            case DRIVER:
                return drvrRunMode;
            default:
                throw new IllegalArgumentException(String.format("Unknown node type: %s", type));
        }
    }

    /**
     * @param type Node type.
     * @return List of {@code NodeInfo} objects created with related run modes.
     */
    public List<NodeInfo> getNodeInfos(NodeType type) {
        List<String> hosts = hostsByType(type);

        RunMode mode = getRunMode(type);

        List<NodeInfo> res = new ArrayList<>(hosts.size());

        for (int i = 0; i < hosts.size(); i++)
            res.add(new NodeInfo(type, hosts.get(i), null, String.valueOf(i), mode));

        return res;
    }

    /**
     * @param type Node type.
     * @return Restart context.
     */
    public RestartContext restartContext(NodeType type) {
        switch (type) {
            case SERVER:
                return servRestartCtx;
            case DRIVER:
                return driverRestartCtx;
            default:
                throw new IllegalArgumentException(String.format("Unknown node type: %s", type));
        }
    }

    /**
     * @return Logger.
     */
    protected static Logger log() {
        return LogManager.getLogger(RunContext.class.getSimpleName());
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
        return startServOnce;
    }

    /**
     * @param startSrvsOnce New start servers once.
     */
    public void startServersOnce(boolean startSrvsOnce) {
        startServOnce = startSrvsOnce;
    }

    /**
     * @return Start serv end exit.
     */
    public boolean startServersEndExit() {
        return startServEndExit;
    }

    /**
     * @param startServEndExit New start serv end exit.
     */
    public void startServersEndExit(boolean startServEndExit) {
        this.startServEndExit = startServEndExit;
    }

    /**
     * @param srvRestartCtx New server restart context.
     */
    public void serverRestartContext(RestartContext srvRestartCtx) {
        servRestartCtx = srvRestartCtx;
    }

    /**
     * @param driverRestartCtx New driver restart context.
     */
    public void driverRestartContext(RestartContext driverRestartCtx) {
        this.driverRestartCtx = driverRestartCtx;
    }

    /**
     * @return Locale work directory.
     */
    public String localeWorkDirectory() {
        return locWorkDir;
    }

    /**
     * @param locWorkDir New locale work directory.
     */
    public void localeWorkDirectory(String locWorkDir) {
        this.locWorkDir = locWorkDir;
    }

    /**
     * @return Main date time.
     */
    public String mainDateTime() {
        return mainDateTime;
    }

    /**
     * @param mainDateTime New main date time.
     */
    public void mainDateTime(String mainDateTime) {
        this.mainDateTime = mainDateTime;
    }

    /**
     * @return Remote work directory.
     */
    public String remoteWorkDirectory() {
        return remWorkDir;
    }

    /**
     * @param remWorkDir New remote work directory.
     */
    public void remoteWorkDirectory(String remWorkDir) {
        this.remWorkDir = remWorkDir;
    }

    /**
     * @return Properties.
     */
    public Properties properties() {
        return props;
    }

    /**
     * @param props New properties.
     */
    public void properties(Properties props) {
        this.props = props;
    }

    /**
     * @return Properties orig.
     */
    public Properties propertiesOrig() {
        return propsOrig;
    }

    /**
     * @param propsOrig New properties orig.
     */
    public void propertiesOrig(Properties propsOrig) {
        this.propsOrig = propsOrig;
    }

    /**
     * @return Docker context.
     */
    public DockerContext dockerContext() {
        return dockerCtx;
    }

    /**
     * @param dockerCtx New docker context.
     */
    public void dockerContext(DockerContext dockerCtx) {
        this.dockerCtx = dockerCtx;
    }

    /**
     * @return Config list.
     */
    public List<String> configList() {
        return new ArrayList<>(cfgList);
    }

    /**
     * @param cfgList New config list.
     */
    public void configList(List<String> cfgList) {
        this.cfgList = new ArrayList<>(cfgList);
    }

    /**
     * @return Rem user.
     */
    public String remoteUser() {
        return remUser;
    }

    /**
     * @param remUser New rem user.
     */
    public void remoteUser(String remUser) {
        this.remUser = remUser;
    }

    /**
     * @param servRunMode New server run mode.
     */
    public void serverRunMode(RunMode servRunMode) {
        this.servRunMode = servRunMode;
    }

    /**
     * @param drvrRunMode New driver run mode.
     */
    public void driverRunMode(RunMode drvrRunMode) {
        this.drvrRunMode = drvrRunMode;
    }

    /**
     * @return Locale java home.
     */
    public String localeJavaHome() {
        return locJavaHome;
    }

    /**
     * @param locJavaHome New locale java home.
     */
    public void localeJavaHome(String locJavaHome) {
        this.locJavaHome = locJavaHome;
    }

    /**
     * @return Rem java home.
     */
    public String remoteJavaHome() {
        return remJavaHome;
    }

    /**
     * @param remJavaHome New rem java home.
     */
    public void remoteJavaHome(String remJavaHome) {
        this.remJavaHome = remJavaHome;
    }

    /**
     * @return Server hosts.
     */
    public List<String> serverHosts() {
        return new ArrayList<>(servHosts);
    }

    /**
     * @param servHosts New server hosts.
     */
    void serverHosts(List<String> servHosts) {
        this.servHosts = new ArrayList<>(servHosts);
    }

    /**
     * @return Driver hosts.
     */
    public List<String> driverHosts() {
        return new ArrayList<>(drvrHosts);
    }

    /**
     * @param drvrHosts New drvr hosts.
     */
    void driverHosts(List<String> drvrHosts) {
        this.drvrHosts = new ArrayList<>(drvrHosts);
    }

    /**
     * @return Current host.
     */
    public String currentHost() {
        return currHost;
    }

    /**
     * @param currHost New current host.
     */
    public void currentHost(String currHost) {
        this.currHost = currHost;
    }

    /**
     * @return Property path.
     */
    public String propertyPath() {
        return propPath;
    }

    /**
     * @param propPath New property path.
     */
    public void propertyPath(String propPath) {
        this.propPath = propPath;
    }

    /**
     * @return Host java home map.
     */
    public Map<String, String> hostJavaHomeMap() {
        return new HashMap<>(hostJavaHomeMap);
    }

    /**
     * @param host Host.
     * @param javaHome Java home.
     */
    public void putInJavaHostMap(String host, String javaHome) {
        hostJavaHomeMap.put(host, javaHome);
    }

    /**
     * @return Command Handler.
     */
    public CommandHandler handler() {
        if (hand == null)
            hand = CommandHandler.getCommandHandler(this);

        return hand;
    }

    /**
     * @return {@code true} if docker is enabled for server or driver nodes or {@code false} otherwise.
     */
    public boolean dockerEnabled() {
        return servRunMode == RunMode.DOCKER || drvrRunMode == RunMode.DOCKER;
    }
}