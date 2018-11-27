package org.yardstickframework.runners.docker;

import java.util.List;
import org.yardstickframework.runners.CommonWorkContext;
import org.yardstickframework.runners.NodeType;
import org.yardstickframework.runners.docker.DockerContext;

public class DockerWorkContext extends CommonWorkContext {


    private NodeType nodeType;



    public DockerWorkContext(List<String> hostList,
        NodeType nodeType) {
        super(hostList);
        this.nodeType = nodeType;

    }


    public NodeType getNodeType() {
        return nodeType;
    }
}
