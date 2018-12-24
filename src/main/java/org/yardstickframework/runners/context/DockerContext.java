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
    private boolean rebuildImagesIfExist;

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
     * @return {@code boolean} Flag indicating whether or not images should be rebuild if exist.
     */
    public boolean isRebuildImagesIfExist() {
        return rebuildImagesIfExist;
    }

    /**
     *
     * @param rebuildImagesIfExist Flag indicating whether or not images should be rebuild if exist.
     */
    public void setRebuildImagesIfExist(boolean rebuildImagesIfExist) {
        this.rebuildImagesIfExist = rebuildImagesIfExist;
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
}
