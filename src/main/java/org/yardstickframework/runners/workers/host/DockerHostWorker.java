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
    public DockerHostWorker(RunContext runCtx, Set<String> hostSet) {
        super(runCtx, hostSet);

        dockerCtx = runCtx.dockerContext();
    }


    /**
     * @param host Host.
     * @param name Image name.
     * @return {@code true} if image with given name exists on the specified host or {@code false} otherwise.
     */
    public boolean checkIfImageExists(String host, String name) {
        Collection<Map<String, String>> imageMaps = getImages(host);

        for (Map<String, String> imageMap : imageMaps) {
            if (imageMap.get("REPOSITORY").equals(name))
                return true;

        }

        return false;
    }

    /**
     * @param host Host.
     * @param id Image id.
     * @return {@code true} if image with given name exists on the specified host or {@code false} otherwise.
     */
    boolean checkIfImageIdExists(String host, String id) {
        Collection<Map<String, String>> imageMaps = getImages(host);

        for (Map<String, String> imageMap : imageMaps) {
            if (imageMap.get("IMAGE ID").equals(id))
                return true;

        }

        return false;
    }

    /**
     * @param host Host.
     * @return Result of 'docker images' execution.
     */
    Collection<Map<String, String>> getImages(String host) {
        return getMaps(host, "images", IMAGES_HEADERS);
    }

    /**
     * @param host Host.
     * @return Result of 'docker ps -a' execution.
     */
    Collection<Map<String, String>> getProcesses(String host) {
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
        return dockerCtx.getNodeContext(type).getImageName();
    }

    /**
     * @param name Image name.
     * @return {@code true} if image name is defined server image name or driver image name,
     * or {@code false otherwise}.
     */
    boolean nameToDelete(String name) {
        return name.equals(dockerCtx.getNodeContext(NodeType.SERVER).getImageName())
            || name.equals(dockerCtx.getNodeContext(NodeType.DRIVER).getImageName());
    }
}
