package org.yardstickframework.runners.docker;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.Map;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

public class DockerContext {
    private boolean deleteImageBeforeRun;

    private boolean buildImageIfNotExist;

    private boolean deleteImageAfterRun;

    private boolean useCommonImageName;

    private String commonImageName;

    private String serverImageName;

    private String driverImageName;

    private boolean copyYardctickIntoCont;

    private List<String> imagesToClean;
    private List<String> contToClean;

    private boolean useStandartNodeToContMapping;

    private boolean useCustomNodeToContMapping;


    private Map<String, String> customNodeToContMap;

    public DockerContext() {
    }

    public boolean isBuildImageIfNotExist() {
        return buildImageIfNotExist;
    }

    public void setBuildImageIfNotExist(boolean buildImageIfNotExist) {
        this.buildImageIfNotExist = buildImageIfNotExist;
    }

    public boolean isDeleteImageBeforeRun() {
        return deleteImageBeforeRun;
    }

    public void setDeleteImageBeforeRun(boolean deleteImageBeforeRun) {
        this.deleteImageBeforeRun = deleteImageBeforeRun;
    }

    public boolean isDeleteImageAfterRun() {
        return deleteImageAfterRun;
    }

    public void setDeleteImageAfterRun(boolean deleteImageAfterRun) {
        this.deleteImageAfterRun = deleteImageAfterRun;
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

    public boolean isCopyYardctickIntoCont() {
        return copyYardctickIntoCont;
    }

    public void setCopyYardctickIntoCont(boolean copyYardctickIntoCont) {
        this.copyYardctickIntoCont = copyYardctickIntoCont;
    }

    public List<String> getImagesToClean() {
        return imagesToClean;
    }

    public void setImagesToClean(List<String> imagesToClean) {
        this.imagesToClean = imagesToClean;
    }

    public List<String> getContToClean() {
        return contToClean;
    }

    public void setContToClean(List<String> contToClean) {
        this.contToClean = contToClean;
    }

    public boolean isUseStandartNodeToContMapping() {
        return useStandartNodeToContMapping;
    }

    public void setUseStandartNodeToContMapping(boolean useStandartNodeToContMapping) {
        this.useStandartNodeToContMapping = useStandartNodeToContMapping;
    }

    public boolean isUseCustomNodeToContMapping() {
        return useCustomNodeToContMapping;
    }

    public void setUseCustomNodeToContMapping(boolean useCustomNodeToContMapping) {
        this.useCustomNodeToContMapping = useCustomNodeToContMapping;
    }

    public Map<String, String> getCustomNodeToContMap() {
        return customNodeToContMap;
    }

    public void setCustomNodeToContMap(Map<String, String> customNodeToContMap) {
        this.customNodeToContMap = customNodeToContMap;
    }

    public static DockerContext getDockerContext(String yamlPath){
        Yaml yaml = new Yaml(new Constructor(DockerContext.class));

        DockerContext docCtx = null;

        try {
            docCtx = yaml.load(new FileInputStream("/home/oostanin/yardstick/config/docker/docker-context.yaml"));
            docCtx = yaml.load(new FileInputStream(yamlPath));
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return docCtx;
    }
}
