package org.yardstickframework.runners;

import java.util.ArrayList;
import java.util.List;
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
        for(NodeType type : nodeTypeList)
            prepareForNodeType(type);
    }

    public void clean(List<NodeType> nodeTypeList){
        for(NodeType type : nodeTypeList)
            cleanForNodeType(type);
    }

    public void prepareForNodeType(NodeType type){
        BenchmarkUtils.println(String.format("Preparing docker for %s nodes.", type.toString().toLowerCase()));

        DockerWorkContext uniqListWorkCtx = new DockerWorkContext(
            getUniqHosts(type), type);

        List<DockerWorker> workerList = new ArrayList<>();

        if (runCtx.getDockerContext().isRemoveContainersBeforeRun())
            workerList.add(new DockerCleanContWorker(runCtx, uniqListWorkCtx));

        if (runCtx.getDockerContext().isRemoveImagesBeforeRun())
            workerList.add(new DockerCleanImagesWorker(runCtx, uniqListWorkCtx));


        workerList.add(new DockerBuildImagesWorker(runCtx, uniqListWorkCtx));

        if (runCtx.getDockerContext().isStartContainersBeforeRun()){
            DockerWorkContext startCtx =new DockerWorkContext(
                getHosts(type),
                type);

            workerList.add(new DockerStartContWorker(runCtx, startCtx));
        }

        for(DockerWorker worker : workerList)
            worker.workOnHosts();

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
            getHosts(type), type);

        new DockerCleanContWorker(runCtx, workCtx).workOnHosts();
    }
}
