package org.yardstickframework.runners.docker;

import java.util.List;
import org.yardstickframework.runners.NodeInfo;
import org.yardstickframework.runners.NodeWorker;
import org.yardstickframework.runners.RunContext;

public abstract class DockerNodeWorker extends NodeWorker {

    private static final String[] imagesHdrs = new String[] {"REPOSITORY", "TAG", "IMAGE ID", "CREATED", "SIZE"};
    private static final String[] psHdrs = new String[] {"CONTAINER ID", "IMAGE", "COMMAND", "CREATED", "STATUS", "PORTS", "NAMES"};


    /**
     * @param runCtx
     */
    public DockerNodeWorker(RunContext runCtx, List<NodeInfo> nodeList) {
        super(runCtx, nodeList);
    }
}
