package org.yardstickframework.runners;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.yardstickframework.BenchmarkConfiguration;
import org.yardstickframework.BenchmarkUtils;

import static org.yardstickframework.BenchmarkUtils.jcommander;

public class AbstractRunner {
    protected static final long DFLT_TIMEOUT = 300_000L;

    protected Properties runProps;

    /** */
    private String propPath;

    protected String[] toDeploy = new String[]{"bin", "config", "libs"};

    protected String[] toClean = new String[]{"bin", "config", "libs", "output", "work"};

    protected String mainDir;

    public AbstractRunner(Properties runProps) {
        this.runProps = runProps;
    }

    public String getPropPath() {
        return propPath;
    }

    public void setPropPath(String propPath) {
        this.propPath = propPath;
    }

    protected void setRunProps(File propPath) throws FileNotFoundException, IOException {
        runProps = new Properties();

        runProps.load(new FileReader(propPath));
    }

    protected String getMainDir(){
        return runProps.getProperty("WORK_DIR");
    }

    protected String getRemUser(){
        if(runProps.getProperty("REMOTE_USER") == null)
            runProps.setProperty("REMOTE_USER", System.getProperty("user.name"));

        return runProps.getProperty("REMOTE_USER");
    }

    protected String getRemJava(){
        if(runProps.getProperty("JAVA_HOME") == null) {
            BenchmarkUtils.println("JAVA_HOME is not defined in property file.");
            BenchmarkUtils.println(String.format("Will use %s/bin/java to run nodes.", System.getProperty("java.home")));

            runProps.setProperty("JAVA_HOME", System.getProperty("java.home"));
        }

        return runProps.getProperty("JAVA_HOME");
    }

    protected String getDockerJava(){
        return runProps.getProperty("DOCKER_JAVA_HOME");
    }

    protected String getMainDateTime(){
        return runProps.getProperty("MAIN_DATE_TIME");
    }

    protected List<String> runCmd(final String cmd){

        List<String> res = new ArrayList<>();

        final Process p;

        ExecutorService errStreamPrinter = Executors.newSingleThreadExecutor();


        try {
            p = Runtime.getRuntime().exec(cmd);
            p.waitFor();
            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));


            errStreamPrinter.submit(new Callable<Object>() {
                @Override public Object call() throws IOException {
                    String line = "";


                    BufferedReader errReader = new BufferedReader(new InputStreamReader(p.getErrorStream()));

                    while ((line = errReader.readLine())!= null)
                        System.out.println(String.format("Command '%s' returned error line: %s:", cmd, line));

                    return null;
                }
            });

            String line = "";

            while ((line = reader.readLine())!= null) {
                res.add(line);

                if(line.contains("Successfully built "))
                     BenchmarkUtils.println(line);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        errStreamPrinter.shutdown();

        return res;
    }

    protected List<String> getFullUniqList(){
        List<String> res = getServList();

        res.addAll(getDrvrList());

        return makeUniq(res);
    }

    protected List<String> getFullHostList(){
        List<String> res = getServList();

        res.addAll(getDrvrList());

        return res;
    }

    protected int getNodesNum(){
        return getFullHostList().size();
    }

    protected List<String> getServUniqList(){
        return makeUniq(getServList());
    }

    protected List<String> getDrvrUniqList(){
        return makeUniq(getDrvrList());
    }

    protected List<String> getServList(){
        return getHosts("SERVER_HOSTS");
    }

    protected List<String> getDrvrList(){
        return getHosts("DRIVER_HOSTS");
    }

    protected List<String> getHosts(String prop){
        List<String> res = new ArrayList<>();

        String commaSepList = runProps.getProperty(prop);

        if(commaSepList == null)
            return res;

        String[] ips = commaSepList.split(",");

        for(String ip : ips){
            check(ip);

            res.add(ip);
        }

        return res;
    }

    private List<String> makeUniq(List<String> src){
        Set<String> set = new HashSet<>(src);

        List<String> res = new ArrayList<>(set);

        Collections.sort(res);

        return res;
    }

    private void check(String ip){
        //TODO
    }

    protected String parseCfgStr(String src){
        String res = src.replace("\"", "");

        if(src.contains("${SCRIPT_DIR}/.."))
            res = res.replace("${SCRIPT_DIR}/..", getMainDir());
        if(src.contains("${nodesNum}"))
            res = res.replace("${nodesNum}", String.valueOf(getNodesNum()));

        for(String propName : runProps.stringPropertyNames()){
            if(src.contains(String.format("${%s}", propName)))
                res = res.replace(String.format("${%s}", propName), runProps.get(propName).toString());

            if(src.contains(String.format("$%s", propName)))
                res = res.replace(String.format("$%s", propName), runProps.get(propName).toString());
        }

        return res;
    }

    protected NodeStarter getNodeStarter(NodeInfo nodeInfo){
        StartMode mode = getStartMode(nodeInfo);

        switch(mode) {
            case PLAIN:
                return new PlainNodeStarter(runProps);
            case IN_DOCKER:
                return new InDockerNodeStarter(runProps, nodeInfo.getStartCtx());
            default:
                throw new IllegalArgumentException(String.format("Unknown start mode: %s", mode.toString()));
        }
    }

    protected NodeChecker getNodeChecker(NodeInfo nodeInfo){
        StartMode mode = getStartMode(nodeInfo);

        switch(mode) {
            case PLAIN:
                return new PlainNodeChecker(runProps);
            case IN_DOCKER:
                return new InDockerNodeChecker(runProps, nodeInfo.getStartCtx());
            default:
                throw new IllegalArgumentException(String.format("Unknown start mode: %s", mode.toString()));
        }

    }

    private StartMode getStartMode(NodeInfo nodeInfo){
        return nodeInfo.getStartCtx().getStartMode();
    }

    protected String getDescription(String src){
        String[] argArr = src.split(" ");

        String res = "Unknown";

        for(int i = 0; i < argArr.length; i++){
            if(argArr[i].equals("-ds") || argArr[i].equals("--descriptions")){
                if(argArr.length < i + 2){
                    BenchmarkUtils.println("Failed to get description");

                    return res;
                }
                else
                    res = argArr[i+1];
            }
        }

        return res;
    }
}
