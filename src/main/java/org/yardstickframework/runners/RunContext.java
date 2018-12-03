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
import org.apache.log4j.RollingFileAppender;
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

    private Properties props;

    private Properties propsOrig;

    private static String locWorkDir;

    private static String remWorkDir;

    private String propPath;

    private String locJavaHome;

    private String remJavaHome;

    private String locUser;

    private String remUser;

    private List<String> servHosts;

    private List<String> drvrHosts;

    private String currentHost = "127.0.0.1";

    private String mainDateTime;

    protected RunMode servRunMode;

    protected RunMode drvrRunMode;

    private List<String> cfgList;

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

        if (args.length == 0) {
            BenchmarkUtils.println("Work directory is not defined.");

            System.exit(1);
        }

        locWorkDir = new File(args[0]).getParentFile().getAbsolutePath();

        configLog();

        LOG.info(String.format("Local work directory is %s", locWorkDir));

        if (args.length == 1) {
            String dfltPropPath = String.format("%s/config/benchmark.properties", locWorkDir);

            BenchmarkUtils.println(String.format("Using as a default property file %s", dfltPropPath));

            propPath = dfltPropPath;
        }
        else {
            if (new File(args[1]).exists())
                propPath = new File(args[1]).getAbsolutePath();
            else if (Paths.get(locWorkDir, args[1]).toFile().exists())
                propPath = Paths.get(locWorkDir, args[1]).toAbsolutePath().toString();
            else {
                BenchmarkUtils.println(String.format("Error. Failed to find property %s", args[1]));

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

        LOG.info(String.format("Server run mode set as %s", servRunMode));

        drvrRunMode = props.getProperty("RUN_DRIVER_MODE") != null ?
            RunMode.valueOf(props.getProperty("RUN_DRIVER_MODE")) :
            RunMode.PLAIN;

        LOG.info(String.format("Driver run mode set as %s", drvrRunMode));
    }

    /**
     * @return Value
     */
    protected void setUser() {
        locUser = System.getProperty("user.name");

        if (props.getProperty("REMOTE_USER") != null)
            remUser = props.getProperty("REMOTE_USER");
        else {
            BenchmarkUtils.println(String.format("REMOTE_USER is not defined in property file. Will use '%s' " +
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
            BenchmarkUtils.println(String.format("WARNING! %s is not defined in property file.", prop));

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
                return new InDockerNodeStarter(this, nodeInfo.getStartCtx());
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
                return new InDockerNodeChecker(this, nodeInfo.getStartCtx());
            default:
                throw new IllegalArgumentException(String.format("Unknown start mode: %s", mode.toString()));
        }
    }

    public DockerContext getDockerContext() {
        if (dockerCtx != null)
            return dockerCtx;

        String dockerCtxPropPath = null;

        if (props.getProperty("DOCKER_CONTEXT_PATH") == null) {
            dockerCtxPropPath = String.format("%s/config/docker/docker-context.yaml", locWorkDir);

            BenchmarkUtils.println(String.format("DOCKER_CONTEXT_PATH is not defined in property file. Will try " +
                "to use default docker context configuration %s", dockerCtxPropPath));
        }
        else
            dockerCtxPropPath = resolvePath(props.getProperty("DOCKER_CONTEXT_PATH"));

        dockerCtx = DockerContext.getDockerContext(dockerCtxPropPath);

        return dockerCtx;
    }

    private RunMode getStartMode(NodeInfo nodeInfo) {
        return nodeInfo.getStartCtx().getRunMode();
    }

    protected String getDescription(String src) {
        String[] argArr = src.split(" ");

        String res = "Unknown";

        for (int i = 0; i < argArr.length; i++) {
            if (argArr[i].equals("-ds") || argArr[i].equals("--descriptions")) {
                if (argArr.length < i + 2) {
                    BenchmarkUtils.println("Failed to get description");

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

        BenchmarkUtils.println(String.format("Failed to find %s or %s.", srcPath, fullPath));

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

    public List<NodeType> getRunModeTypes(RunMode mode) {
        List<NodeType> res = new ArrayList<>();

        if (getServRunMode() == mode)
            res.add(NodeType.SERVER);

        if (getDrvrRunMode() == mode)
            res.add(NodeType.DRIVER);

        return res;
    }

    private void configLog() {
        ConsoleAppender console = new ConsoleAppender(); //create appender
        //configure the appender
        String PATTERN = "%d [%p|%c|%C{1}] %m%n";
        console.setLayout(new PatternLayout(PATTERN));
        console.setThreshold(Level.INFO);
        console.activateOptions();
        //add appender to any Logger (here is root)
//        Logger.getRootLogger().addAppender(console);

        FileAppender fa = new FileAppender();
        fa.setName("FileLogger");
        fa.setFile(Paths.get(locWorkDir, "output", String.format("%s-run.log", mainDateTime)).toString());
        fa.setLayout(new PatternLayout("[%d{yyyy-MM-dd HH:mm:ss,SSS}][%-5p][%t] %m%n"));
        fa.setThreshold(Level.INFO);
        fa.setAppend(true);
        fa.activateOptions();

        //add appender to any Logger (here is root)
        Logger.getRootLogger().addAppender(fa);
    }

}
