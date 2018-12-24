package org.yardstickframework.runners.context;

/**
 * Node docker context.
 */
public class NodeDockerContext {
    /** */
    private String dockerfilePath;

    /** */
    private String imageName;

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
     * @param imageName Image name.
     */
    public void setImageName(String imageName) {
        this.imageName = imageName;
    }
}
