package org.yardstickframework.runners.docker;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.Map;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yardstickframework.runners.NodeType;
import org.yardstickframework.runners.RestartContext;

public class DockerContext {
    private String serverDockerfilePath;

    private String driverDockerfilePath;

    private String serverImageName;

    private String driverImageName;

    private boolean rebuildImagesIfExist;

    private Map<String, Boolean> removeImagesFlags;

    private List<String> imagesToRemove;

    private String dockerRunCmdArgs;

    private String startContCmd;

    private List<String> startContCmdArgs;

    private Map<String, Boolean> removeContainersFlags;

    private List<String> containersToRemove;

    private String serverDockerJavaHome;
    private String driverDockerJavaHome;


    public DockerContext() {
    }

    public String getServerDockerfilePath() {
        return serverDockerfilePath == null ? serverDockerfilePath : "config/docker/Dockerfile-server";
    }

    public void setServerDockerfilePath(String serverDockerfilePath) {
        this.serverDockerfilePath = serverDockerfilePath;
    }

    public String getDriverDockerfilePath() {
        return driverDockerfilePath == null ? driverDockerfilePath : "config/docker/Dockerfile-driver";
    }

    public void setDriverDockerfilePath(String driverDockerfilePath) {
        this.driverDockerfilePath = driverDockerfilePath;
    }

    public String getServerImageName() {
        return serverImageName;
    }

    public void setServerImageName(String serverImageName) {
        this.serverImageName = serverImageName;
    }

    public String getDriverImageName() {
        return driverImageName;
    }

    public void setDriverImageName(String driverImageName) {
        this.driverImageName = driverImageName;
    }

    public boolean isRebuildImagesIfExist() {
        return rebuildImagesIfExist;
    }

    public void setRebuildImagesIfExist(boolean rebuildImagesIfExist) {
        this.rebuildImagesIfExist = rebuildImagesIfExist;
    }

    public Map<String, Boolean> getRemoveImagesFlags() {
        return removeImagesFlags;
    }

    public void setRemoveImagesFlags(Map<String, Boolean> removeImagesFlags) {
        this.removeImagesFlags = removeImagesFlags;
    }

    public String getDockerRunCmdArgs() {
        return dockerRunCmdArgs;
    }

    public void setDockerRunCmdArgs(String dockerRunCmdArgs) {
        this.dockerRunCmdArgs = dockerRunCmdArgs;
    }

    public String getStartContCmd() {
        return startContCmd;
    }

    public void setStartContCmd(String startContCmd) {
        this.startContCmd = startContCmd;
    }

    public List<String> getStartContCmdArgs() {
        return startContCmdArgs;
    }

    public void setStartContCmdArgs(List<String> startContCmdArgs) {
        this.startContCmdArgs = startContCmdArgs;
    }

    public Map<String, Boolean> getRemoveContainersFlags() {
        return removeContainersFlags;
    }

    public void setRemoveContainersFlags(Map<String, Boolean> removeContainersFlags) {
        this.removeContainersFlags = removeContainersFlags;
    }

    public List<String> getContainersToRemove() {
        return containersToRemove;
    }

    public void setContainersToRemove(List<String> containersToRemove) {
        this.containersToRemove = containersToRemove;
    }

    public String getServerDockerJavaHome() {
        return serverDockerJavaHome;
    }

    public void setServerDockerJavaHome(String serverDockerJavaHome) {
        this.serverDockerJavaHome = serverDockerJavaHome;
    }

    public String getDriverDockerJavaHome() {
        return driverDockerJavaHome;
    }

    public void setDriverDockerJavaHome(String driverDockerJavaHome) {
        this.driverDockerJavaHome = driverDockerJavaHome;
    }

    public static DockerContext getDockerContext(String yamlPath){
        Yaml yaml = new Yaml(new Constructor(DockerContext.class));

        DockerContext docCtx = null;

        try {
//            docCtx = yaml.load(new FileInputStream("/home/oostanin/yardstick/config/docker/docker-context.yaml"));
            docCtx = yaml.load(new FileInputStream(yamlPath));
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return docCtx;
    }

    public String getImageName(NodeType type){
        switch (type) {
            case SERVER:
                return serverImageName;
            case DRIVER:
                return driverImageName;
            default:
                throw new IllegalArgumentException(String.format("Unknown node type: %s", type));
        }
    }
}
