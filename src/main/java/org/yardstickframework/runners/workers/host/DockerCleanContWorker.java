package org.yardstickframework.runners.workers.host;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import org.yardstickframework.runners.CommandExecutionResult;
import org.yardstickframework.runners.context.RunContext;

import org.yardstickframework.runners.workers.CheckWorkResult;
import org.yardstickframework.runners.workers.WorkResult;

/**
 * Cleans docker containers.
 */
public class DockerCleanContWorker extends DockerHostWorker {
    /** {@inheritDoc} */
    public DockerCleanContWorker(RunContext runCtx, Set<String> hostSet) {
        super(runCtx, hostSet);
    }

    /** {@inheritDoc} */
    @Override public WorkResult doWork(String host, int cnt) {
        return removeContainers(host);
    }

    /**
     * @param host Host.
     * @return Work result.
     */
    private WorkResult removeContainers(String host) {
        CheckWorkResult res = new CheckWorkResult();

        if (dockerCtx.getContainersToRemove() == null){
            log().error("Failed to remove containers. Property 'containersToRemove' is not defined.");

            res.exit(true);

            return res;
        }

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

        return res;
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

}
