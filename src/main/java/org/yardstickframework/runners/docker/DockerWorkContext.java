package org.yardstickframework.runners.docker;

import java.util.List;
import org.yardstickframework.runners.CommonWorkContext;
import org.yardstickframework.runners.NodeType;
import org.yardstickframework.runners.docker.DockerContext;

public class DockerWorkContext extends CommonWorkContext {

    private DockerContext dockerCtx;

    private NodeType nodeType;

    private String dockerFilePath;

    private String imageName;

    private String imageVer;

    public DockerWorkContext(List<String> hostList, DockerContext dockerCtx,
        NodeType nodeType, String dockerFilePath, String imageName, String imageVer) {
        super(hostList);
        this.dockerCtx = dockerCtx;
        this.nodeType = nodeType;
        this.dockerFilePath = dockerFilePath;
        this.imageName = imageName;
        this.imageVer = imageVer;
    }

    public DockerContext getDockerCtx() {
        return dockerCtx;
    }

    public NodeType getNodeType() {
        return nodeType;
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
