package org.yardstickframework.runners.docker;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.yardstickframework.BenchmarkUtils;
import org.yardstickframework.runners.CommandExecutionResult;
import org.yardstickframework.runners.CommandHandler;
import org.yardstickframework.runners.NodeType;
import org.yardstickframework.runners.RunContext;
import org.yardstickframework.runners.WorkContext;
import org.yardstickframework.runners.Worker;

import static org.yardstickframework.BenchmarkUtils.dateTime;

public abstract class DockerWorker extends HostWorker {

    private static final String[] imagesHdrs = new String[] {"REPOSITORY", "TAG", "IMAGE ID", "CREATED", "SIZE"};
    private static final String[] psHdrs = new String[] {"CONTAINER ID", "IMAGE", "COMMAND", "CREATED", "STATUS", "PORTS", "NAMES"};

    protected DockerWorkContext dockerWorkCtx;
    protected DockerContext dockerCtx;

    /**
     * @param runCtx
     * @param workCtx
     */
    public DockerWorker(RunContext runCtx, WorkContext workCtx) {
        super(runCtx, workCtx);

        dockerWorkCtx = (DockerWorkContext)workCtx;
        dockerCtx = runCtx.getDockerContext();
    }

    protected void removeImages(String host) {
        Collection<Map<String, String>> imageMaps = getImages(host);

        Map<String, String> toRemove = new HashMap<>();

        for (Map<String, String> imageMap : imageMaps) {
            String imageName = imageMap.get("REPOSITORY");

            if (nameToDelete(imageName))
                toRemove.put(imageMap.get("IMAGE ID"), imageName);

        }

        for(String id : toRemove.keySet())
            removeImage(host, id, toRemove.get(id));
    }

    protected void removeContainers(String host) {
        Collection<Map<String, String>> contMaps = getProcesses(host);

        for (Map<String, String> contMap : contMaps) {
            for (String contName : dockerCtx.getContainersToRemove()) {
                String names = contMap.get("NAMES");

                if (names.contains(contName)) {
                    log().info(String.format("Removing the container '%s' (id = %s) from the host %s.",
                        names, contMap.get("CONTAINER ID"), host));

                    removeSingleCont(host, contMap.get("CONTAINER ID"));
                }
            }
        }
    }

    private CommandExecutionResult removeSingleCont(String host, String contId) {
        CommandHandler hndl = new CommandHandler(runCtx);

        CommandExecutionResult cmdRes = null;

        try {
            hndl.runDockerCmd(host, String.format("stop %s", contId));

            cmdRes = hndl.runDockerCmd(host, String.format("rm %s", contId));
        }
        catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

        Collection<Map<String, String>> processes = getProcesses(host);

        String res = "removed";

        for(Map<String, String> proc : processes)
            if(proc.get("CONTAINER ID").equals(contId))
                res = "not removed";

        log().info(String.format("The container %s is %s.", contId, res));

        return cmdRes;
    }

    private CommandExecutionResult removeImage(String host, String imageId, String imageName) {
        CommandHandler hndl = new CommandHandler(runCtx);

        log().info(String.format("Removing the image '%s' (id=%s) from the host %s",
            imageName, imageId, host));

        CommandExecutionResult cmdRes = null;

        try {
            cmdRes = hndl.runDockerCmd(host, String.format("rmi -f %s", imageId));
        }
        catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

        return cmdRes;
    }

    protected boolean checkIfImageExists(String host, String name) {
        Collection<Map<String, String>> imageMaps = getImages(host);

        for (Map<String, String> imageMap : imageMaps) {
            if (imageMap.get("REPOSITORY").contains(name))
                return true;

        }

        return false;
    }

    public String getImageIdByName(String host, String imageName) {

        return null;
    }

    protected String getContId(String host, String contName) {
        Collection<Map<String, String>> contMaps = getProcesses(host);

        for (Map<String, String> contMap : contMaps) {
            if (contMap.get("NAMES").contains(contName))
                return contMap.get("CONTAINER ID");
        }

        return "Unknown";
    }



    protected Collection<Map<String, String>> getImages(String host) {

        return getMaps(host, "images", imagesHdrs);
    }

    protected Collection<Map<String, String>> getProcesses(String host) {

        return getMaps(host, "ps -a", psHdrs);
    }

    private Collection<Map<String, String>> getMaps(String host, String cmd, String[] hdrs) {
        List<Map<String, String>> res = new ArrayList<>();

        CommandHandler hndl = new CommandHandler(runCtx);

        CommandExecutionResult cmdRes = null;

        try {
            cmdRes = hndl.runDockerCmd(host, cmd);
        }
        catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

        List<String> outStr = cmdRes.getOutStream();

        if (outStr.size() == 1 && outStr.get(0).equals(hdrs[0]))
            return res;

        String hdrStr = outStr.get(0);

        for (int i = 1; i < outStr.size(); i++) {
            String toParse = outStr.get(i);

            Map<String, String> valMap = new HashMap<>(hdrs.length);

            for (int hdr = 0; hdr < hdrs.length; hdr++) {
                int idx0 = hdrStr.indexOf(hdrs[hdr]);

                int idx1 = hdr == hdrs.length - 1 ? toParse.length() : hdrStr.indexOf(hdrs[hdr + 1]);

                String val = toParse.substring(idx0, idx1);

                while (val.endsWith(" "))
                    val = val.substring(0, val.length() - 1);

                valMap.put(hdrs[hdr], val);
            }

            res.add(valMap);

        }

        return res;
    }

    protected String getImageNameToUse(NodeType type){
        return type == NodeType.SERVER ?
            dockerCtx.getServerImageName():
            dockerCtx.getDriverImageName();
    }

    private boolean nameToDelete(String name){
        return name.equals(dockerCtx.getServerImageName()) || name.equals(dockerCtx.getDriverImageName());
    }
}
