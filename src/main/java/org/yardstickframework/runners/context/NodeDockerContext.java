package org.yardstickframework.runners.context;

/**
 * Node docker context.
 */
public class NodeDockerContext {
    /** */
    private String dockerfilePath;

    /** */
    private String imageName;

    /** Container name prefix. */
    private String contPref;

    /**
     *
     * @return Dockerfile path.
     */
    public String getDockerfilePath() {
        return dockerfilePath;
    }

    /**
     *
     * @param dockerfilePath Dockerfile path.
     */
    public void setDockerfilePath(String dockerfilePath) {
        this.dockerfilePath = dockerfilePath;
    }

    /**
     *
     * @return Image name.
     */
    public String getImageName() {
        return imageName;
    }

    /**
     *
     * @return Container name prefix.
     */
    public String getContPref() {
        return contPref;
    }

    /**
     *
     * @param contPref Container name prefix.
     */
    public void setContPref(String contPref) {
        this.contPref = contPref;
    }

    /**
     *
     * @param imageName Image name.
     */
    public void setImageName(String imageName) {
        this.imageName = imageName;
    }
}
