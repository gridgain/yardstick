package org.yardstickframework.runners.docker;

import java.util.Collection;
import java.util.Map;
import org.yardstickframework.runners.CommandHandler;
import org.yardstickframework.runners.RunContext;
import org.yardstickframework.runners.WorkContext;
import org.yardstickframework.runners.Worker;

public abstract class DockerWorker extends Worker {

    private static final String[] imagesHdrs = new String[]{"REPOSITORY", "TAG", "IMAGE ID", "CREATED", "SIZE"};
    private static final String[] psHdrs = new String[]{"CONTAINER ID", "IMAGE", "COMMAND", "CREATED", "STATUS", "PORTS", "NAMES"};

    protected DockerWorkContext dockerWorkCtx;
    /**
     * @param runCtx
     * @param workCtx
     */
    public DockerWorker(RunContext runCtx, WorkContext workCtx) {
        super(runCtx, workCtx);

        dockerWorkCtx = (DockerWorkContext) workCtx;
    }

    protected void deleteImage(String host){

    }

    public String getImageIdByName(String host, String imageName){

        return null;
    }

    protected Collection<Map<String, String>> getImages(String host){

        return null;
    }

    protected Collection<Map<String, String>> getProcesses(String host){

        return null;
    }

    private Collection<Map<String, String>> getMaps(String host, String cmd, String[] hdrs) {
        CommandHandler hndl = new CommandHandler(runCtx);




        return null;
    }
}
