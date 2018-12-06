package org.yardstickframework.runners;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.yardstickframework.BenchmarkUtils;
import org.yardstickframework.runners.docker.DockerBuildImagesWorker;
import org.yardstickframework.runners.docker.DockerCheckWorkResult;
import org.yardstickframework.runners.docker.DockerCheckWorker;
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

    public void check(List<NodeType> nodeTypeList){
        for(NodeType type : nodeTypeList) {
            BenchmarkUtils.println(String.format("Run mode DOCKER enabled for %s nodes.", type.toString().toLowerCase()));

            checkForNodeType(type);
        }
    }

    public void checkForNodeType(NodeType type){
        DockerWorkContext workCtx = new DockerWorkContext(
            getUniqHosts(type), type);

        Collection<WorkResult> checks = new DockerCheckWorker(runCtx, workCtx).workOnHosts();

        for(WorkResult res : checks){
            DockerCheckWorkResult checkRes = (DockerCheckWorkResult) res;

            if(!checkRes.getErrMsgs().isEmpty()){
                for(String errMsg : checkRes.getErrMsgs())
                    BenchmarkUtils.println(errMsg);

                System.exit(1);
            }
        }
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
//        BenchmarkUtils.println(String.format("Cleaning up docker for %s nodes.", type.toString().toLowerCase()));

        DockerWorkContext workCtx = new DockerWorkContext(
            getUniqHosts(type), type);

        if(runCtx.getDockerContext().getRemoveContainersFlags().get(flag))
            new DockerCleanContWorker(runCtx, workCtx).workOnHosts();

        if(runCtx.getDockerContext().getRemoveImagesFlags().get(flag))
            new DockerCleanImagesWorker(
                runCtx,
                workCtx).workOnHosts();
    }


    public void prepareForNodeType(NodeType type){
//        BenchmarkUtils.println(String.format("Preparing docker for %s nodes.", type.toString().toLowerCase()));

        DockerWorkContext uniqListWorkCtx = new DockerWorkContext(
            getUniqHosts(type), type);

        List<DockerWorker> workerList = new ArrayList<>();

        workerList.add(new DockerBuildImagesWorker(runCtx, uniqListWorkCtx));

        for(DockerWorker worker : workerList)
            worker.workOnHosts();
    }

    public void start(List<NodeType> nodeTypeList){
        for (NodeType type : nodeTypeList)
            startForNodeType(type);

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
}
