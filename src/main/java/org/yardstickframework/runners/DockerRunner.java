package org.yardstickframework.runners;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.yardstickframework.runners.context.NodeType;
import org.yardstickframework.runners.context.RunContext;
import org.yardstickframework.runners.workers.host.DockerBuildImagesWorker;
import org.yardstickframework.runners.workers.host.DockerCheckWorker;
import org.yardstickframework.runners.workers.host.DockerCleanContWorker;
import org.yardstickframework.runners.workers.host.DockerCleanImagesWorker;
import org.yardstickframework.runners.workers.node.DockerCollectWorker;
import org.yardstickframework.runners.workers.node.DockerStartContWorker;

public class DockerRunner extends AbstractRunner {
    public DockerRunner(RunContext runCtx) {
        super(runCtx);
    }

    public void check(List<NodeType> nodeTypeList){
        for(NodeType type : nodeTypeList) {
            log().info(String.format("Run mode DOCKER enabled for %s nodes.", type.toString().toLowerCase()));

            checkForNodeType(type);
        }
    }

    public void checkForNodeType(NodeType type){
        new DockerCheckWorker(runCtx, runCtx.getUniqHostsByType(type)).workOnHosts();
    }

    public void prepare(List<NodeType> nodeTypeList){
        int poolSize = runCtx.checkIfDifferentHosts() ? 2 : 1;

        ExecutorService prepareServ = Executors.newFixedThreadPool(poolSize);

        List<Future<?>> futList = new ArrayList<>(2);

        for(final NodeType type : nodeTypeList) {
            futList.add(prepareServ.submit(new Callable<Object>() {
                @Override public Object call() throws Exception {
                    Thread.currentThread().setName(String.format("Prepare-%ss",
                        type.toString().toLowerCase()));

                    prepareForNodeType(type);

                    return null;
                }
            }));
        }

        for(Future<?> fut : futList) {
            try {
                fut.get();
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }
            catch (ExecutionException e) {
                e.printStackTrace();
            }
        }

        prepareServ.shutdown();
    }

    public void cleanUp(List<NodeType> nodeTypeList, String flag){
        for(final NodeType type : nodeTypeList)
            cleanForNodeType(type, flag);
    }

    public void cleanForNodeType(NodeType type, String flag){
        if(runCtx.dockerContext().getRemoveContainersFlags().get(flag))
            new DockerCleanContWorker(runCtx, runCtx.getUniqHostsByType(type)).workOnHosts();

        if(runCtx.dockerContext().getRemoveImagesFlags().get(flag))
            new DockerCleanImagesWorker(runCtx, runCtx.getUniqHostsByType(type)).workOnHosts();
    }


    public void prepareForNodeType(NodeType type){
        new DockerBuildImagesWorker(runCtx, runCtx.getUniqHostsByType(type), type).workOnHosts();
    }

    public void start(List<NodeType> nodeTypeList){
        for (NodeType type : nodeTypeList)
            startForNodeType(type);

    }

    public void startForNodeType(NodeType type){
        new DockerStartContWorker(runCtx, runCtx.getNodes(type)).workForNodes();
    }


    public void collect(List<NodeType> nodeTypeList){
        for(NodeType type : nodeTypeList)
            collectForNodeType(type);
    }

    public void collectForNodeType(NodeType type){
        new DockerCollectWorker(runCtx, runCtx.getNodes(type)).workForNodes();
    }
}
