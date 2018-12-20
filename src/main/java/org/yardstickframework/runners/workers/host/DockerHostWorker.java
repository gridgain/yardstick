package org.yardstickframework.runners.workers.host;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.yardstickframework.runners.CommandExecutionResult;
import org.yardstickframework.runners.context.NodeType;
import org.yardstickframework.runners.context.DockerContext;
import org.yardstickframework.runners.context.RunContext;

/**
 * Parent for docker host workers.
 */
abstract class DockerHostWorker extends HostWorker {
    /** */
    private static final String[] IMAGES_HEADERS = new String[] {"REPOSITORY", "TAG", "IMAGE ID", "CREATED", "SIZE"};

    /** */
    private static final String[] PROCESS_HEADERS = new String[] {"CONTAINER ID", "IMAGE", "COMMAND", "CREATED", "STATUS", "PORTS", "NAMES"};

    /** */
    DockerContext dockerCtx;

    /** {@inheritDoc} */
    DockerHostWorker(RunContext runCtx, Set<String> hostSet) {
        super(runCtx, hostSet);

        dockerCtx = runCtx.dockerContext();
    }

    /**
     * @param host Host.
     */
    void removeImages(String host) {
        Collection<Map<String, String>> imageMaps = getImages(host);

        Map<String, String> toRem = new HashMap<>();

        for (Map<String, String> imageMap : imageMaps) {
            String imageName = imageMap.get("REPOSITORY");

            if (nameToDelete(imageName))
                toRem.put(imageMap.get("IMAGE ID"), imageName);
        }

        for (String id : toRem.keySet())
            removeImage(host, id, toRem.get(id));
    }

    /**
     * @param host Host.
     */
    void removeContainers(String host) {
        Collection<Map<String, String>> contMaps = getProcesses(host);

        for (Map<String, String> contMap : contMaps) {
            for (String contName : dockerCtx.getContainersToRemove()) {
                String names = contMap.get("NAMES");

                if (names.contains(contName)) {
                    log().info(String.format("Removing the container '%s' (id = %s) from the host '%s'.",
                        names, contMap.get("CONTAINER ID"), host));

                    removeSingleCont(host, contMap.get("CONTAINER ID"));
                }
            }
        }
    }

    /**
     * @param host Host.
     * @param contId Container id.
     * @return Command execution result.
     */
    private CommandExecutionResult removeSingleCont(String host, String contId) {

        CommandExecutionResult cmdRes = null;

        try {
            runCtx.handler().runDockerCmd(host, String.format("stop %s", contId));

            cmdRes = runCtx.handler().runDockerCmd(host, String.format("rm %s", contId));
        }
        catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

        Collection<Map<String, String>> processes = getProcesses(host);

        String res = "removed";

        for (Map<String, String> proc : processes)
            if (proc.get("CONTAINER ID").equals(contId))
                res = "not removed";

        log().info(String.format("The container '%s' is %s.", contId, res));

        return cmdRes;
    }

    /**
     * @param host Host.
     * @param imageId Image id.
     * @param imageName Image name.
     * @return Command execution result.
     */
    private CommandExecutionResult removeImage(String host, String imageId, String imageName) {

        log().info(String.format("Removing the image '%s' (id=%s) from the host '%s'",
            imageName, imageId, host));

        CommandExecutionResult cmdRes = null;

        try {
            cmdRes = runCtx.handler().runDockerCmd(host, String.format("rmi -f %s", imageId));
        }
        catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

        return cmdRes;
    }

    /**
     * @param host Host.
     * @param name Image name.
     * @return {@code true} if image with given name exists on the specified host or {@code false} otherwise.
     */
    boolean checkIfImageExists(String host, String name) {
        Collection<Map<String, String>> imageMaps = getImages(host);

        for (Map<String, String> imageMap : imageMaps) {
            if (imageMap.get("REPOSITORY").contains(name))
                return true;

        }

        return false;
    }

    /**
     * @param host Host.
     * @return Result of 'docker images' execution.
     */
    private Collection<Map<String, String>> getImages(String host) {
        return getMaps(host, "images", IMAGES_HEADERS);
    }

    /**
     * @param host Host.
     * @return Result of 'docker ps -a' execution.
     */
    private Collection<Map<String, String>> getProcesses(String host) {
        return getMaps(host, "ps -a", PROCESS_HEADERS);
    }

    /**
     * Maps with docker command response.
     *
     * @param host Host.
     * @param cmd Command e.g. images' or 'ps -a'.
     * @param headers Headers to create map. Each header will be the key and the value will be corresponding line
     * from the output string.
     * @return Maps with docker command response.
     */
    private Collection<Map<String, String>> getMaps(String host, String cmd, String[] headers) {

        try {
            CommandExecutionResult cmdRes = runCtx.handler().runDockerCmd(host, cmd);

            List<String> outStr = cmdRes.outputList();

            Collection<Map<String, String>> res = new ArrayList<>();

            if (outStr.size() == 1 && outStr.get(0).equals(headers[0]))
                return res;

            String hdrStr = outStr.get(0);

            for (int i = 1; i < outStr.size(); i++) {
                String toParse = outStr.get(i);

                Map<String, String> valMap = new HashMap<>(headers.length);

                for (int hdr = 0; hdr < headers.length; hdr++) {
                    int idx0 = hdrStr.indexOf(headers[hdr]);

                    int idx1 = hdr == headers.length - 1 ? toParse.length() : hdrStr.indexOf(headers[hdr + 1]);

                    String val = toParse.substring(idx0, idx1);

                    while (val.endsWith(" "))
                        val = val.substring(0, val.length() - 1);

                    valMap.put(headers[hdr], val);
                }

                res.add(valMap);
            }

            return res;
        }
        catch (IOException | InterruptedException e) {
            log().error(String.format("Failed to get response for command '%s' from the host '%s'", cmd, host), e);

            return new ArrayList<>();
        }
    }

    /**
     * @param type Node type.
     * @return Image name for the given type.
     */
    String getImageNameToUse(NodeType type) {
        return type == NodeType.SERVER ?
            dockerCtx.getServerImageName() :
            dockerCtx.getDriverImageName();
    }

    /**
     * @param name Image name.
     * @return {@code true} if image name is defined server image name or driver image name,
     * or {@code false otherwise}.
     */
    private boolean nameToDelete(String name) {
        return name.equals(dockerCtx.getServerImageName()) || name.equals(dockerCtx.getDriverImageName());
    }
}
