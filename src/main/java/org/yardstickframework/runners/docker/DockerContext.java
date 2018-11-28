package org.yardstickframework.runners.docker;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.Map;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

public class DockerContext {
    private boolean removeImagesBeforeRun;

    private boolean removeContainersBeforeRun;

    private boolean buildImagesIfNotExist;

    private boolean tagImageWithTime;

    private boolean removeImagesAfterRun;

    private boolean startContainersBeforeRun;
    private boolean removeContainersAfterRun;

    private boolean useCommonImageName;

    private String commonImageName;

    private String serverImageName;

    private String driverImageName;

    private String serverDockerfilePath;
    private String driverDockerfilePath;

    private boolean copyYardctickIntoContainer;

    private List<String> imagesToClean;

    private List<String> containersToClean;

    private boolean useStandartNodeToContainerMapping;

    private boolean useCustomNodeToContainerMapping;


    private Map<String, String> customNodeToContainerMap;

    public DockerContext() {
    }

    public boolean isBuildImagesIfNotExist() {
        return buildImagesIfNotExist;
    }

    public void setBuildImagesIfNotExist(boolean buildImagesIfNotExist) {
        this.buildImagesIfNotExist = buildImagesIfNotExist;
    }

    public boolean isTagImageWithTime() {
        return tagImageWithTime;
    }

    public void setTagImageWithTime(boolean tagImageWithTime) {
        this.tagImageWithTime = tagImageWithTime;
    }

    public boolean isRemoveImagesBeforeRun() {
        return removeImagesBeforeRun;
    }

    public void setRemoveImagesBeforeRun(boolean removeImagesBeforeRun) {
        this.removeImagesBeforeRun = removeImagesBeforeRun;
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

    public boolean isRemoveImagesAfterRun() {
        return removeImagesAfterRun;
    }

    public void setRemoveImagesAfterRun(boolean removeImagesAfterRun) {
        this.removeImagesAfterRun = removeImagesAfterRun;
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

    public boolean isCopyYardctickIntoContainer() {
        return copyYardctickIntoContainer;
    }

    public void setCopyYardctickIntoContainer(boolean copyYardctickIntoContainer) {
        this.copyYardctickIntoContainer = copyYardctickIntoContainer;
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
            docCtx = yaml.load(new FileInputStream("/home/oostanin/yardstick/config/docker/docker-context.yaml"));
//            docCtx = yaml.load(new FileInputStream(yamlPath));
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return docCtx;
    }
}
