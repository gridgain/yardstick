package org.yardstickframework.runners;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.yardstickframework.runners.context.NodeType;
import org.yardstickframework.runners.context.RunContext;
import org.yardstickframework.runners.context.RunMode;
import org.yardstickframework.runners.workers.CheckWorkResult;
import org.yardstickframework.runners.workers.WorkResult;
import org.yardstickframework.runners.workers.host.DockerBuildImagesWorker;
import org.yardstickframework.runners.workers.host.DockerCheckWorker;
import org.yardstickframework.runners.workers.host.DockerCleanContWorker;
import org.yardstickframework.runners.workers.host.DockerCleanImagesWorker;
import org.yardstickframework.runners.workers.node.DockerCollectWorker;
import org.yardstickframework.runners.workers.node.DockerStartContWorker;

/**
 * Helper class. Executes docker related tasks.
 */
public class DockerRunner extends FullRunner {
    /** */
    private List<NodeType> dockerList;

    /**
     * Constructor.
     *
     * @param runCtx Run context.
     */
    public DockerRunner(RunContext runCtx) {
        super(runCtx);
    }

    /**
     * @return Exit code. TODO implement exit code return.
     */
    @Override protected int run0() {
        super.run0();

        generalPrepare();

        dockerPrepare();

        execute();

        dockerAfterExecution();

        afterExecution();

        return 0;
    }

    /**
     *
     */
    void dockerPrepare() {
        dockerList = runCtx.nodeTypes(RunMode.DOCKER);

        check(dockerList);

        cleanUp(dockerList, "before");

        prepare(dockerList);

        start(dockerList);
    }

    /**
     *
     */
    void dockerAfterExecution() {
        collect(dockerList);

        cleanUp(dockerList, "after");
    }

    /**
     * @param nodeTypeList Node type list.
     */
    public void check(Iterable<NodeType> nodeTypeList) {
        for (NodeType type : nodeTypeList) {
            log().info(String.format("Run mode DOCKER enabled for %s nodes.", type.toString().toLowerCase()));

            checkForNodeType(type);
        }
    }

    /**
     * @param type Node type.
     */
    private void checkForNodeType(NodeType type) {
        new DockerCheckWorker(runCtx, runCtx.uniqueHostsByType(type)).workOnHosts();
    }

    /**
     * @param nodeTypeList Node type list.
     */
    private void prepare(Iterable<NodeType> nodeTypeList) {
        int poolSize = runCtx.checkIfDifferentHosts() ? 2 : 1;

        ExecutorService prepareServ = Executors.newFixedThreadPool(poolSize);

        Collection<Future<?>> futList = new ArrayList<>(2);

        for (final NodeType type : nodeTypeList) {
            futList.add(prepareServ.submit(new Callable<Object>() {
                @Override public Object call() throws Exception {
                    Thread.currentThread().setName(String.format("Prepare-%ss",
                        type.toString().toLowerCase()));

                    prepareForNodeType(type);

                    return null;
                }
            }));
        }

        for (Future<?> fut : futList) {
            try {
                fut.get();
            }
            catch (ExecutionException | InterruptedException e) {
                log().error("Failed to prepare docker.", e);
            }
        }

        prepareServ.shutdown();
    }

    /**
     * @param nodeTypeList Node type list.
     * @param flag Flag.
     */
    public void cleanUp(Iterable<NodeType> nodeTypeList, String flag) {
        for (final NodeType type : nodeTypeList)
            cleanForNodeType(type, flag);
    }

    /**
     * @param type Node type.
     * @param flag Flag.
     */
    private void cleanForNodeType(NodeType type, String flag) {
        if (runCtx.dockerContext().getRemoveContainersFlags().get(flag)) {
            List<WorkResult> resList = new DockerCleanContWorker(runCtx, runCtx.uniqueHostsByType(type))
                .workOnHosts();

            checkRes(resList);
        }

        if (runCtx.dockerContext().getRemoveImagesFlags().get(flag)) {
            if (!runCtx.dockerContext().getRemoveContainersFlags().get(flag)) {
                log().error(String.format("Cannot remove docker images because removeContainersFlag in docker " +
                    "context file '%s' is set to '%s'", flag, "false"));
            }
            else
                new DockerCleanImagesWorker(runCtx, runCtx.uniqueHostsByType(type)).workOnHosts();
        }
    }

    /**
     * @param type Node type.
     */
    private void prepareForNodeType(NodeType type) {
        List<WorkResult> resList = new DockerBuildImagesWorker(runCtx, runCtx.uniqueHostsByType(type), type)
            .workOnHosts();

        checkRes(resList);
    }

    /**
     * @param nodeTypeList Node type list.
     */
    public void start(Iterable<NodeType> nodeTypeList) {
        for (NodeType type : nodeTypeList)
            startForNodeType(type);

    }

    /**
     * @param type Node type.
     */
    private void startForNodeType(NodeType type) {
        new DockerStartContWorker(runCtx, runCtx.getNodeInfos(type)).workForNodes();
    }

    /**
     * @param nodeTypeList Node type list.
     */
    public void collect(Iterable<NodeType> nodeTypeList) {
        for (NodeType type : nodeTypeList)
            collectForNodeType(type);
    }

    /**
     * @param type Node type.
     */
    private void collectForNodeType(NodeType type) {
        new DockerCollectWorker(runCtx, runCtx.getNodeInfos(type)).workForNodes();
    }
}
