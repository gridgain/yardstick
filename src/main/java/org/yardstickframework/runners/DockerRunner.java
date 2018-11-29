package org.yardstickframework.runners;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.yardstickframework.BenchmarkUtils;
import org.yardstickframework.runners.docker.DockerBuildImagesWorker;
import org.yardstickframework.runners.docker.DockerCleanContWorker;
import org.yardstickframework.runners.docker.DockerCleanImagesWorker;
import org.yardstickframework.runners.docker.DockerCollectWorker;
import org.yardstickframework.runners.docker.DockerStartContWorker;
import org.yardstickframework.runners.docker.DockerWorkContext;
import org.yardstickframework.runners.docker.DockerWorker;

public class DockerRunner extends AbstractRunner {
    public DockerRunner(RunContext runCtx) {
        super(runCtx);
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

    public void cleanBefore(List<NodeType> nodeTypeList){
        for(final NodeType type : nodeTypeList)
            cleanBeforeForNodeType(type);
    }

    public void clean(List<NodeType> nodeTypeList){
        for(NodeType type : nodeTypeList)
            cleanForNodeType(type);
    }

    public void cleanBeforeForNodeType(NodeType type){
        BenchmarkUtils.println(String.format("Cleaning up docker for %s nodes.", type.toString().toLowerCase()));

        DockerWorkContext uniqListWorkCtx = new DockerWorkContext(
            getUniqHosts(type), type);

        List<DockerWorker> workerList = new ArrayList<>();

        if (runCtx.getDockerContext().isRemoveContainersBeforeRun())
            workerList.add(new DockerCleanContWorker(runCtx, uniqListWorkCtx));

        if (runCtx.getDockerContext().isRemoveImagesBeforeRun())
            workerList.add(new DockerCleanImagesWorker(runCtx, uniqListWorkCtx));


        for(DockerWorker worker : workerList)
            worker.workOnHosts();

    }


    public void prepareForNodeType(NodeType type){
        BenchmarkUtils.println(String.format("Preparing docker for %s nodes.", type.toString().toLowerCase()));

        DockerWorkContext uniqListWorkCtx = new DockerWorkContext(
            getUniqHosts(type), type);

        List<DockerWorker> workerList = new ArrayList<>();

        workerList.add(new DockerBuildImagesWorker(runCtx, uniqListWorkCtx));

        for(DockerWorker worker : workerList)
            worker.workOnHosts();
    }

    public void start(List<NodeType> nodeTypeList){
        if (runCtx.getDockerContext().isStartContainersBeforeRun()) {

            for (NodeType type : nodeTypeList)
                startForNodeType(type);
        }
    }

    public void startForNodeType(NodeType type){
        DockerWorkContext workCtx = new DockerWorkContext(
            getHosts(type), type);

        new DockerStartContWorker(runCtx, workCtx).workOnHosts();
    }


    public void collect(List<NodeType> nodeTypeList){
        for(NodeType type : nodeTypeList)
            collectForNodeType(type);
    }

    public void collectForNodeType(NodeType type){
        DockerWorkContext workCtx = new DockerWorkContext(
            getHosts(type), type);

        new DockerCollectWorker(runCtx, workCtx).workOnHosts();
    }

    public void cleanForNodeType(NodeType type){
        DockerWorkContext workCtx = new DockerWorkContext(
            getUniqHosts(type), type);

        new DockerCleanContWorker(runCtx, workCtx).workOnHosts();
    }
}
