package org.yardstickframework.runners;

public class DockerInfo {
    private String imageName;
    private String imageVer;
    private String contName;
    private String contId;

    public DockerInfo(String imageName, String imageVer, String contName, String contId) {
        this.imageName = imageName;
        this.imageVer = imageVer;
        this.contName = contName;
        this.contId = contId;
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

    public String getContId() {
        return contId;
    }
}
