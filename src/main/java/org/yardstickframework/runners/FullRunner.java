package org.yardstickframework.runners;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import org.yardstickframework.BenchmarkUtils;

public class FullRunner extends AbstractRunner{

    public FullRunner(Properties runProps) {
        super(runProps);
    }

    public static void main(String[] args) {
//        for(String a : args)
//            System.out.println(a);

        FullRunner runner = new FullRunner(null);

        String arg = args.length == 0 ? "/home/oostanin/yardstick/config/benchmark.properties" :
            args[0];

        try {
            runner.setRunProps(new File(arg));

//            System.out.println(String.format("setting arg = %s", arg));

            runner.setPropPath(arg);
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        if(runner.runProps.getProperty("WORK_DIR") == null) {
            runner.mainDir = new File(args[1]).getParent();

            runner.runProps.setProperty("WORK_DIR", new File(args[1]).getParent());
        }

        runner.runProps.setProperty("MAIN_DATE_TIME", BenchmarkUtils.dateTime());

        runner.run();
    }

    /**
     *
     */
    public int run(){
        Worker killWorker = new KillWorker(runProps, new CommonWorkContext(getFullUniqList()));

        killWorker.workOnHosts();

        Worker deployWorker = new DeployWorker(runProps, new CommonWorkContext(getFullUniqList()));

        deployWorker.workOnHosts();

        List<WorkResult> buildServResList = buildDockerImages(NodeType.SERVER);
//        buildDockerImages(NodeType.DRIVER);

        for(String cfgStr : runProps.getProperty("CONFIGS").split(",")) {
            String parsedCfgStr = parseCfgStr(cfgStr);

            StartMode servStartMode = runProps.getProperty("SERVER_DOCKER_IMAGE_NAME") == null ?
                StartMode.PLAIN:
                StartMode.IN_DOCKER;

            StartNodeWorkContext nodeWorkCtx = new StartNodeWorkContext(getServList(), servStartMode, parsedCfgStr,
                getPropPath());

            if(buildServResList != null && !buildServResList.isEmpty())
                nodeWorkCtx.setDockerInfo((BuildDockerResult)buildServResList.get(0));

            StartNodeWorker startServWorker = new StartServWorker(runProps, nodeWorkCtx);

            startServWorker.workOnHosts();

//            StartNodeWorker startDrvrWorker = new StartDrvrWorker(runProps, cfgStr.replace("\"", ""));
//
//            startDrvrWorker.setPropPath(getPropPath());
//
//            startDrvrWorker.workOnHosts();


        }

        return 0;
    }

    private List<WorkResult> buildDockerImages(NodeType type){
        String imageNameProp = String.format("%s_DOCKER_IMAGE_NAME", type);

        String imageName = runProps.getProperty(imageNameProp);

        String nameProp = String.format("%s_DOCKERFILE_NAME", type);
        String pathProp = String.format("%s_DOCKERFILE_PATH", type);

        if(runProps.getProperty(nameProp) == null &&
            runProps.getProperty(pathProp) == null)
            throw new IllegalArgumentException("Dockerfile name and path is not defined in property file.");

        String dockerfilePath = runProps.getProperty(pathProp) != null ?
            runProps.getProperty(pathProp) :
            String.format("%s/config/%s", getMainDir(), runProps.getProperty(nameProp));

        String imageVer = getMainDateTime();

        List<String> hostList = type == NodeType.SERVER ?
            getServList():
            getDrvrList();

        BuildDockerWorkContext docCtx = new BuildDockerWorkContext(hostList, dockerfilePath, imageName, imageVer);

        Worker buildDocWorker = new BuildDockerWorker(runProps, docCtx);

        return buildDocWorker.workOnHosts();
    }
}
