package org.yardstickframework.runners;

import java.util.List;

public class BuildDockerWorkContext extends CommonWorkContext {
    private List<String> hostList;

    private String dockerFilePath;

    private String imageName;

    private String imageVer;

    public BuildDockerWorkContext(List<String> hostList, String dockerFilePath, String imageName, String imageVer) {
        super(hostList);
        this.dockerFilePath = dockerFilePath;
        this.imageName = imageName;
        this.imageVer = imageVer;
    }

    public String getDockerFilePath() {
        return dockerFilePath;
    }

    public String getImageName() {
        return imageName;
    }

    public String getImageVer() {
        return imageVer;
    }
}
