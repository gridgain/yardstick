package org.yardstickframework.runners;

public class BuildDockerResult implements WorkResult {
    private String imageName;
    private String imageVer;

    private String host;

    private int cnt;

    public BuildDockerResult(String imageName, String imageVer, String host, int cnt) {
        this.imageName = imageName;
        this.imageVer = imageVer;
        this.host = host;
        this.cnt = cnt;
    }

    public String getImageName() {
        return imageName;
    }

    public String getImageVer() {
        return imageVer;
    }

    public String getHost() {
        return host;
    }

    public int getCnt() {
        return cnt;
    }
}
