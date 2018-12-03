package org.yardstickframework.runners.docker;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.Map;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

public class DockerContext {
    private List<String> removeImagesBeforeRunList;

    private List<String> removeImagesAfterRunList;


    private boolean removeContainersBeforeRun;

    private boolean rebuildImagesIfExist;

    private boolean tagImageWithTime;


    private boolean startContainersBeforeRun;
    private boolean removeContainersAfterRun;

    private boolean useCommonImageName;

    private String commonImageName;

    private String serverImageName;

    private String driverImageName;

    private String serverDockerfilePath;
    private String driverDockerfilePath;

    private String dockerRunCmdArgs;

    private String startContCmd;

    private List<String> startContCmdArgs;

    private boolean copyYardctickIntoContainer;

    private  String serverDockerJavaHome;

    private  String driverDockerJavaHome;

    private List<String> imagesToClean;

    private List<String> containersToClean;

    private boolean useStandartNodeToContainerMapping;

    private boolean useCustomNodeToContainerMapping;


    private Map<String, String> customNodeToContainerMap;

    public DockerContext() {
    }

    public List<String> getRemoveImagesBeforeRunList() {
        return removeImagesBeforeRunList;
    }

    public void setRemoveImagesBeforeRunList(List<String> removeImagesBeforeRunList) {
        this.removeImagesBeforeRunList = removeImagesBeforeRunList;
    }

    public List<String> getRemoveImagesAfterRunList() {
        return removeImagesAfterRunList;
    }

    public void setRemoveImagesAfterRunList(List<String> removeImagesAfterRunList) {
        this.removeImagesAfterRunList = removeImagesAfterRunList;
    }

    public boolean isRebuildImagesIfExist() {
        return rebuildImagesIfExist;
    }

    public void setRebuildImagesIfExist(boolean rebuildImagesIfExist) {
        this.rebuildImagesIfExist = rebuildImagesIfExist;
    }

    public boolean isTagImageWithTime() {
        return tagImageWithTime;
    }

    public void setTagImageWithTime(boolean tagImageWithTime) {
        this.tagImageWithTime = tagImageWithTime;
    }



    public boolean isRemoveContainersBeforeRun() {
        return removeContainersBeforeRun;
    }

    public void setRemoveContainersBeforeRun(boolean removeContainersBeforeRun) {
        this.removeContainersBeforeRun = removeContainersBeforeRun;
    }

    public boolean isRemoveContainersAfterRun() {
        return removeContainersAfterRun;
    }

    public void setRemoveContainersAfterRun(boolean removeContainersAfterRun) {
        this.removeContainersAfterRun = removeContainersAfterRun;
    }



    public boolean isStartContainersBeforeRun() {
        return startContainersBeforeRun;
    }

    public void setStartContainersBeforeRun(boolean startContainersBeforeRun) {
        this.startContainersBeforeRun = startContainersBeforeRun;
    }

    public boolean isUseCommonImageName() {
        return useCommonImageName;
    }

    public void setUseCommonImageName(boolean useCommonImageName) {
        this.useCommonImageName = useCommonImageName;
    }

    public String getCommonImageName() {
        return commonImageName;
    }

    public void setCommonImageName(String commonImageName) {
        this.commonImageName = commonImageName;
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

    public boolean isCopyYardctickIntoContainer() {
        return copyYardctickIntoContainer;
    }

    public void setCopyYardctickIntoContainer(boolean copyYardctickIntoContainer) {
        this.copyYardctickIntoContainer = copyYardctickIntoContainer;
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

    public List<String> getImagesToClean() {
        return imagesToClean;
    }

    public void setImagesToClean(List<String> imagesToClean) {
        this.imagesToClean = imagesToClean;
    }

    public List<String> getContainersToClean() {
        return containersToClean;
    }

    public void setContainersToClean(List<String> containersToClean) {
        this.containersToClean = containersToClean;
    }

    public boolean isUseStandartNodeToContainerMapping() {
        return useStandartNodeToContainerMapping;
    }

    public void setUseStandartNodeToContainerMapping(boolean useStandartNodeToContainerMapping) {
        this.useStandartNodeToContainerMapping = useStandartNodeToContainerMapping;
    }

    public boolean isUseCustomNodeToContainerMapping() {
        return useCustomNodeToContainerMapping;
    }

    public void setUseCustomNodeToContainerMapping(boolean useCustomNodeToContainerMapping) {
        this.useCustomNodeToContainerMapping = useCustomNodeToContainerMapping;
    }

    public Map<String, String> getCustomNodeToContainerMap() {
        return customNodeToContainerMap;
    }

    public void setCustomNodeToContainerMap(Map<String, String> customNodeToContainerMap) {
        this.customNodeToContainerMap = customNodeToContainerMap;
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
}
