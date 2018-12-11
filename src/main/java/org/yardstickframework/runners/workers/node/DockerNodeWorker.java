package org.yardstickframework.runners.workers.node;

import java.util.List;
import org.yardstickframework.runners.context.NodeInfo;
import org.yardstickframework.runners.context.RunContext;

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
