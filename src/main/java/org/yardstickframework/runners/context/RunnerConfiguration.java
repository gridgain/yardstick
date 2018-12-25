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

import com.beust.jcommander.Parameter;
import java.io.Serializable;

/**
 * Input arguments for benchmarks.
 */
@SuppressWarnings({"UnusedDeclaration", "FieldCanBeLocal"})
public class RunnerConfiguration implements Serializable {
    /** */
    private static final long serialVersionUID = 0L;

    /** */
    @Parameter(names = {"-h", "--help"}, description = "Print help message", help = true, hidden = true)
    private boolean help;

    /** */
    @Parameter(names = {"-sd", "--scriptDir"}, description = "Local script directory path")
    private String scriptDir;

    /** */
    @Parameter(names = {"-rwd", "--remoteWorkDir"}, description = "Remote work directory path")
    private String remoteWorkDir;

    /** */
    @Parameter(names = {"-pf", "--propertyFile"}, description = "Framework configuration file path")
    private String propertyFile;

    /** */
    @Parameter(names = {"-s", "--serverHosts"}, description = "Comma separated list of server nodes addresses.")
    private String serverHosts;

    /** */
    @Parameter(names = {"-d", "--driverHosts"}, description = "Comma separated list of driver nodes addresses.")
    private String driverHosts;

    /**
     * @return Help.
     */
    public boolean help() {
        return help;
    }

    /**
     * @return Local work directory.
     */
    public String scriptDirectory() {
        return scriptDir;
    }

    /**
     * @param scriptDir New local work directory.
     */
    public void scriptDirectory(String scriptDir) {
        this.scriptDir = scriptDir;
    }

    /**
     * @return Remote work directory.
     */
    public String remoteWorkDirectory() {
        return remoteWorkDir;
    }

    /**
     * @param remoteWorkDir New remote work directory.
     */
    public void remoteWorkDirectory(String remoteWorkDir) {
        this.remoteWorkDir = remoteWorkDir;
    }

    /**
     * @return Property file.
     */
    public String propertyFile() {
        return propertyFile;
    }

    /**
     * @param propFile New property file.
     */
    public void propertyFile(String propFile) {
        propertyFile = propFile;
    }

    /**
     * @return Server hosts.
     */
    public String serverHosts() {
        return serverHosts;
    }

    /**
     * @param srvHosts New server hosts.
     */
    public void serverHosts(String srvHosts) {
        serverHosts = srvHosts;
    }

    /**
     * @return Driver hosts.
     */
    public String driverHosts() {
        return driverHosts;
    }

    /**
     * @param driverHosts New driver hosts.
     */
    public void driverHosts(String driverHosts) {
        this.driverHosts = driverHosts;
    }
}
