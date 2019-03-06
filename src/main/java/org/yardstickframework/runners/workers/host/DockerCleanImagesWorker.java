/*
 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */

package org.yardstickframework.runners.workers.host;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.yardstickframework.runners.CommandExecutionResult;
import org.yardstickframework.runners.context.RunContext;
import org.yardstickframework.runners.workers.WorkResult;

/**
 * Cleans docker containers.
 */
public class DockerCleanImagesWorker extends DockerHostWorker {
    /** {@inheritDoc} */
    public DockerCleanImagesWorker(RunContext runCtx, Set<String> hostSet) {
        super(runCtx, hostSet);
    }

    /** {@inheritDoc} */
    @Override public WorkResult doWork(String host, int cnt) {
        removeImages(host);

        return null;
    }

    /**
     * @param host Host.
     */
    private void removeImages(String host) {
        Collection<Map<String, String>> imageMaps = getImages(host);

        Map<String, String> toRem = new HashMap<>();

        for (Map<String, String> imageMap : imageMaps) {
            String imageName = imageMap.get("REPOSITORY");

            if (nameToDelete(imageName))
                toRem.put(imageMap.get("IMAGE ID"), imageName);
        }

        int tryes = 2;

        // Removing images twice because some of the server-node images can have child driver-node images and therefore
        // cannot be removed right away.
        while (tryes-- > 0) {
            for (String id : toRem.keySet()) {
                if (checkIfImageIdExists(host, id))
                    removeImage(host, id, toRem.get(id));
            }
        }
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

        CommandExecutionResult cmdRes = CommandExecutionResult.emptyFailedResult();

        try {
            cmdRes = runCtx.handler().runDockerCmd(host, String.format("rmi -f %s", imageName));
        }
        catch (IOException | InterruptedException e) {
            log().error(String.format("Failed to remove image '%s' from the host '%s'.", imageName, host));
        }

        if(!checkIfImageIdExists(host, imageId))
            log().info(String.format("Image '%s' on the host '%s' is successfully removed.", imageName, host));
        else
            log().info(String.format("Image '%s' on the host '%s' is not removed.", imageName, host));

        return cmdRes;
    }
}
