package org.yardstickframework.runners.context;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
import org.yardstickframework.runners.checkers.InDockerNodeChecker;
import org.yardstickframework.runners.starters.InDockerNodeStarter;
import org.yardstickframework.runners.checkers.NodeChecker;
import org.yardstickframework.runners.starters.NodeStarter;
import org.yardstickframework.runners.checkers.PlainNodeChecker;
import org.yardstickframework.runners.starters.PlainNodeStarter;

/**
 *
 */
public class RunContext {
    /** */
    private static final Logger LOG = LogManager.getLogger(RunContext.class);

    /** */
    private static RunContext instance;

    /** */
    private static RunnerConfiguration cfg = new RunnerConfiguration();

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
    private String locUser;

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
    private boolean restartServs;

    /** */
    private boolean startServOnce;

    /** */
    private RestartContext servRestartCtx;

    /** */
    private boolean restartDrivers;

    /** */
    private RestartContext driverRestartCtx;

    /** */
    private DockerContext dockerCtx;

    /** */
    private RunContext() {
        //No_op
    }



    public boolean isRestartServs() {
        return restartServs;
    }

    public void setRestartServs(boolean restartServs) {
        this.restartServs = restartServs;
    }

    public RestartContext getServRestartCtx() {
        return servRestartCtx;
    }

    public void setRestartCtx(RestartContext serverRestartCtx) {
        this.servRestartCtx = serverRestartCtx;
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

            RunContextInitializer init = new RunContextInitializer(instance);

            init.initialize(args);
        }

        return instance;
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
     * @param src
     * @return Value
     */
    private List<String> makeUniq(List<String> src) {
        Set<String> set = new HashSet<>(src);

        List<String> res = new ArrayList<>(set);

        Collections.sort(res);

        return res;
    }

    public NodeStarter getNodeStarter(NodeInfo nodeInfo) {
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

    public NodeChecker getNodeChecker(NodeInfo nodeInfo) {
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

    private RunMode getStartMode(NodeInfo nodeInfo) {
        return nodeInfo.runMode();
    }

    public String getDescription(String src) {
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
    public String resolveRemotePath(String srcPath) {
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

        if (serverRunMode() == mode)
            res.add(NodeType.SERVER);

        if (driverRunMode() == mode)
            res.add(NodeType.DRIVER);

        return res;
    }

    public List<String> getHostsByType(NodeType type) {
        return type == NodeType.SERVER ? servHosts : drvrHosts;
    }

    public List<String> getHostsByMode(RunMode mode) {
        List<String> res = new ArrayList<>();

        if (servRunMode == mode)
            res.addAll(servHosts);

        if (drvrRunMode == mode)
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
                return servRestartCtx;
            case DRIVER:
                return driverRestartCtx;
            default:
                throw new IllegalArgumentException(String.format("Unknown node type: %s", type));
        }

    }

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
     * @return Server restart context.
     */
    public RestartContext serverRestartContext() {
        return servRestartCtx;
    }

    /**
     * @param srvRestartCtx New server restart context.
     */
    public void serverRestartContext(RestartContext srvRestartCtx) {
        servRestartCtx = srvRestartCtx;
    }

    /**
     * @return Driver restart context.
     */
    public RestartContext driverRestartContext() {
        return driverRestartCtx;
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
        return cfgList;
    }

    /**
     * @param cfgList New config list.
     */
    public void configList(List<String> cfgList) {
        this.cfgList = cfgList;
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
     * @return Server run mode.
     */
    public RunMode serverRunMode() {
        return servRunMode;
    }

    /**
     * @param servRunMode New server run mode.
     */
    public void serverRunMode(RunMode servRunMode) {
        this.servRunMode = servRunMode;
    }

    /**
     * @return Driver run mode.
     */
    public RunMode driverRunMode() {
        return drvrRunMode;
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
     * @return Serv hosts.
     */
    public List<String> serverHosts() {
        return servHosts;
    }

    /**
     * @param servHosts New serv hosts.
     */
    public void serverHosts(List<String> servHosts) {
        this.servHosts = servHosts;
    }

    /**
     * @return Drvr hosts.
     */
    public List<String> driverHosts() {
        return drvrHosts;
    }

    /**
     * @param drvrHosts New drvr hosts.
     */
    public void driverHosts(List<String> drvrHosts) {
        this.drvrHosts = drvrHosts;
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
        return hostJavaHomeMap;
    }

    /**
     * @param hostJavaHomeMap New host java home map.
     */
    public void hostJavaHomeMap(Map<String, String> hostJavaHomeMap) {
        this.hostJavaHomeMap = hostJavaHomeMap;
    }
}
