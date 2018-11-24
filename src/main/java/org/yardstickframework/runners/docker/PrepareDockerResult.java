package org.yardstickframework.runners.docker;

import org.yardstickframework.runners.WorkResult;

public class PrepareDockerResult implements WorkResult {
    private String imageName;
    private String imageVer;
    private String javaHome;

    private String host;

    private int cnt;

    public PrepareDockerResult(String imageName, String imageVer, String javaHome, String host, int cnt) {
        this.imageName = imageName;
        this.imageVer = imageVer;
        this.javaHome = javaHome;
        this.host = host;
        this.cnt = cnt;
    }

    public String getImageName() {
        return imageName;
    }

    public String getImageVer() {
        return imageVer;
    }

    public String getJavaHome() {
        return javaHome;
    }

    public void setJavaHome(String javaHome) {
        this.javaHome = javaHome;
    }

    public String getHost() {
        return host;
    }

    public int getCnt() {
        return cnt;
    }
}
