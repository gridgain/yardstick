package org.yardstickframework.runners.context;

/**
 * Docker info.
 */
public class DockerInfo {
    /** */
    private String imageName;

    /** */
    private String imageVer;

    /** */
    private String contName;

    /** */
    private String contId;

    /**
     * Constructor.
     *
     * @param imageName Image name.
     * @param contName Container name.
     */
    public DockerInfo(String imageName, String contName) {
        this.imageName = imageName;
        this.contName = contName;
    }

    /**
     * @return Image name.
     */
    public String imageName() {
        return imageName;
    }

    /**
     * @param imageName New image name.
     */
    public void imageName(String imageName) {
        this.imageName = imageName;
    }

    /**
     * @return Image version.
     */
    public String imageVersion() {
        return imageVer;
    }

    /**
     * @param imageVer New image version.
     */
    public void imageVersion(String imageVer) {
        this.imageVer = imageVer;
    }

    /**
     * @return Cont name.
     */
    public String contName() {
        return contName;
    }

    /**
     * @param contName New cont name.
     */
    public void contName(String contName) {
        this.contName = contName;
    }

    /**
     * @return Cont id.
     */
    public String contId() {
        return contId;
    }

    /**
     * @param contId New cont id.
     */
    public void contId(String contId) {
        this.contId = contId;
    }
}
