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

package org.yardstickframework.runners.context;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.Map;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

/**
 * Docker context.
 */
public class DockerContext {
    /** */
    private static final Logger LOG = LogManager.getLogger(DockerContext.class);

    /** */
    private NodeDockerContext serverCtx;

    /** */
    private NodeDockerContext driverCtx;

    /** */
    private Map<String, Boolean> removeImagesFlags;

    /** */
    private String dockerBuildCmd;

    /** */
    private String dockerRunCmd;

    /** */
    private Map<String, Boolean> removeContainersFlags;

    /** */
    private List<String> containersToRemove;

    /** */
    private String serverDockerJavaHome;

    /** */
    private String driverDockerJavaHome;

    public NodeDockerContext getServerCtx() {
        return serverCtx;
    }

    public void setServerCtx(NodeDockerContext serverCtx) {
        this.serverCtx = serverCtx;
    }

    public NodeDockerContext getDriverCtx() {
        return driverCtx;
    }

    public void setDriverCtx(NodeDockerContext driverCtx) {
        this.driverCtx = driverCtx;
    }

    /**
     *
     * @return {@code String} Docker build command.
     */
    public String getDockerBuildCmd() {
        return dockerBuildCmd;
    }

    /**
     *
     * @param dockerBuildCmd Docker build command.
     */
    public void setDockerBuildCmd(String dockerBuildCmd) {
        this.dockerBuildCmd = dockerBuildCmd;
    }

    /**
     *
     * @return {@code Map} Contains boolean flags indicating whether or not images should be removed.
     */
    public Map<String, Boolean> getRemoveImagesFlags() {
        return removeImagesFlags;
    }

    /**
     *
     * @param removeImagesFlags {@code Map} Contains boolean flags indicating whether or not images should be removed.
     */
    public void setRemoveImagesFlags(Map<String, Boolean> removeImagesFlags) {
        this.removeImagesFlags = removeImagesFlags;
    }

    /**
     *
     * @return {@code String} Docker run command.
     */
    public String getDockerRunCmd() {
        return dockerRunCmd;
    }

    /**
     *
     * @param dockerRunCmd Docker run command.
     */
    public void setDockerRunCmd(String dockerRunCmd) {
        this.dockerRunCmd = dockerRunCmd;
    }

    /**
     *
     * @return {@code Map} Contains boolean flags indicating whether or not containers should be removed.
     */
    public Map<String, Boolean> getRemoveContainersFlags() {
        return removeContainersFlags;
    }

    /**
     *
     * @param removeContainersFlags {@code Map} Contains boolean flags indicating whether or not containers should
     * be removed.
     */
    public void setRemoveContainersFlags(Map<String, Boolean> removeContainersFlags) {
        this.removeContainersFlags = removeContainersFlags;
    }

    /**
     *
     * @return {@code List} List of containers names to remove.
     */
    public List<String> getContainersToRemove() {
        return containersToRemove;
    }

    /**
     *
     * @param containersToRemove {@code List} List of containers names to remove.
     */
    public void setContainersToRemove(List<String> containersToRemove) {
        this.containersToRemove = containersToRemove;
    }

    /**
     *
     * @return {@code String} Server Java home path.
     */
    public String serverDockerJavaHome() {
        return serverDockerJavaHome;
    }

    /**
     *
     * @param serverDockerJavaHome {@code String} Server Java home path.
     */
    public void serverDockerJavaHome(String serverDockerJavaHome) {
        this.serverDockerJavaHome = serverDockerJavaHome;
    }

    /**
     *
     * @return {@code String} Driver Java home path.
     */
    public String driverDockerJavaHome() {
        return driverDockerJavaHome;
    }

    /**
     *
     * @param driverDockerJavaHome {@code String} Server Java home path.
     */
    public void driverDockerJavaHome(String driverDockerJavaHome) {
        this.driverDockerJavaHome = driverDockerJavaHome;
    }

    /**
     *
     * @param yamlPath {@code String} Path to docker context file.
     * @return Instance of {@code DockerContext}.
     */
    public static DockerContext getDockerContext(String yamlPath){
        Yaml yaml = new Yaml(new Constructor(DockerContext.class));

        try {
            return yaml.load(new FileInputStream(yamlPath));
        }
        catch (FileNotFoundException ignored) {
            LOG.info(String.format("Failed to find file %s. Will use default docker context.", yamlPath));

            return new DockerContext();
        }
    }

    /**
     *
     * @param type {@code NodeType} Node type.
     * @return {@code String} Image name depending on node type.
     */
    public String getImageName(NodeType type){
        return getNodeContext(type).getImageName();
    }

    /**
     *
     * @param type Node type.
     * @return Java home path.
     */
    public String javaHome(NodeType type){
        return type == NodeType.SERVER ?
            serverDockerJavaHome:
            driverDockerJavaHome;
    }

    /**
     *
     * @param type Node type.
     * @return Node docker context.
     */
    public NodeDockerContext getNodeContext(NodeType type){
        switch (type) {
            case SERVER:
                return serverCtx;
            case DRIVER:
                return driverCtx;
            default:
                throw new IllegalArgumentException(String.format("Unknown node type: %s", type));
        }
    }

    /**
     *
     * @param type Node type.
     * @return Container name prefix.
     */
    public String contNamePrefix(NodeType type){
        return getNodeContext(type).getContPref();
    }
}
