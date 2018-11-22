package org.yardstickframework.runners;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
//import org.apache.logging.log4j.LogManager;
//import org.apache.logging.log4j.Logger;
import org.yardstickframework.BenchmarkUtils;

/**
 *
 */
public class RunContext {
    /** */
//    private static final Logger LOG = LogManager.getLogger(RunContext.class.getName());

    private Properties props;

    private Properties propsOrig;

    private String locWorkDir;

    private String remWorkDir;

    private String propPath;

    private String locJavaHome;

    private String remJavaHome;

    private String locUser;

    private String remUser;

    private List<String> servHosts;

    private List<String> drvrHosts;

    private String mainDateTime;

    protected RunMode servRunMode;

    protected RunMode drvrRunMode;

    private List<String> cfgList;

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
        RunContext runCtx = new RunContext();

        runCtx.setContext(args);

        return runCtx;
    }

    private void setContext(String[] args) {
        mainDateTime = BenchmarkUtils.dateTime();

        handleArgs(args);

        handleHosts();

        handleProps();

        handleJavaHome();

        handleRunModes();

        handleUser();

        handleCfgList();
    }

    /**
     * @param args
     */
    private void handleArgs(String[] args) {
        if (args.length == 0) {
            args = new String[] {
                "/home/oostanin/gg/incubator-ignite/modules/yardstick/target/assembly/bin",
                "/home/oostanin/gg/incubator-ignite/modules/yardstick/target/assembly/config/benchmark.properties"};
        }

        if (args.length == 0) {
            BenchmarkUtils.println("Work directory is not defined.");

            System.exit(1);
        }

        locWorkDir = new File(args[0]).getParentFile().getAbsolutePath();

        if (args.length == 1) {
            String dfltPropPath = String.format("%s/config/benchmark.properties", locWorkDir);

            BenchmarkUtils.println(String.format("Using as a default property file %s", dfltPropPath));

            propPath = dfltPropPath;
        }
        else {
            if (new File(args[1]).exists())
                propPath = args[1];
            else {
                BenchmarkUtils.println(String.format("Error. Failed to find property %s", args[1]));

                System.exit(1);
            }
        }

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
    private void handleProps() {
        props = parseProps(propsOrig);

        remWorkDir = props.getProperty("WORK_DIR") != null ?
            props.getProperty("WORK_DIR"):
            locWorkDir;
    }

    /**
     *
     */
    private void handleHosts() {
        servHosts = getHosts("SERVER_HOSTS");

        drvrHosts = getHosts("DRIVER_HOSTS");
    }

    /**
     *
     */
    private void handleJavaHome() {
        locJavaHome = System.getProperty("java.home");

        if (props.getProperty("JAVA_HOME") != null)
            remJavaHome = props.getProperty("JAVA_HOME");
        else {
            BenchmarkUtils.println(String.format("JAVA_HOME is not defined in property file. Will try to use %s",
                locJavaHome));
        }

        if (new File(String.format("%s/bin/java", remJavaHome)).exists())
            locJavaHome = String.format("%s/bin/java", remJavaHome);
    }

    /**
     *
     */
    private void handleRunModes() {
        servRunMode = props.getProperty("RUN_SERVER_MODE") != null ?
            RunMode.valueOf(props.getProperty("RUN_SERVER_MODE")) :
            RunMode.PLAIN;

        drvrRunMode = props.getProperty("RUN_DRIVER_MODE") != null ?
            RunMode.valueOf(props.getProperty("RUN_DRIVER_MODE")) :
            RunMode.PLAIN;
    }

    /**
     *
     * @return Value
     */
    protected void handleUser(){
        locUser = System.getProperty("user.name");

        if(props.getProperty("REMOTE_USER") != null)
            remUser = props.getProperty("REMOTE_USER");
        else {
            BenchmarkUtils.println(String.format("REMOTE_USER is not defined in property file. Will use '%s' " +
                "username for remote connections.", locUser));

            remUser = locUser;
        }
    }

    protected void handleCfgList(){
        cfgList = new ArrayList<>();

        for (String cfgStr : props.getProperty("CONFIGS").split(",")) {
            if (cfgStr.length() < 10)
                continue;

            cfgList.add(cfgStr);
        }
    }

    /** */
    public List<String> getFullHostList() {
        List<String> res = getServList();

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
     *
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
     *
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
            case IN_DOCKER:
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
            case IN_DOCKER:
                return new InDockerNodeChecker(this, nodeInfo.getStartCtx());
            default:
                throw new IllegalArgumentException(String.format("Unknown start mode: %s", mode.toString()));
        }
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
}
