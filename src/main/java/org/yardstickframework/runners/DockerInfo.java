package org.yardstickframework.runners;

public class DockerInfo {
    private String imageName;
    private String imageVer;
    private String contName;

    public DockerInfo(String imageName, String imageVer, String contName) {
        this.imageName = imageName;
        this.imageVer = imageVer;
        this.contName = contName;
    }

    public String getImageName() {
        return imageName;
    }

    public String getImageVer() {
        return imageVer;
    }

    public String getContName() {
        return contName;
    }
}
