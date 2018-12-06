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

package org.yardstickframework.runners;

import com.beust.jcommander.Parameter;
import java.io.PrintStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Input arguments for benchmarks.
 */
@SuppressWarnings({"UnusedDeclaration", "FieldCanBeLocal"})
public class RunnerConfiguration implements Serializable {
    /** */
    private static final long serialVersionUID = 0L;

    /** */
    @Parameter(names = {"-lwd", "--localWorkDir"}, description = "Local work directory path")
    private String localWorkDir;

    /** */
    @Parameter(names = {"-rwd", "--remoteWorkDir"}, description = "Local work directory path")
    private String remoteWorkDir;

    /** */
    @Parameter(names = {"-pf", "--propertyFile"}, description = "Framework configuration file path")
    private String propertyFile = "config/benchmark.properties";

    /** */
    @Parameter(names = {"-s", "--serverHosts"}, description = "Comma separated list of server nodes addresses.")
    private String serverHosts;

    /** */
    @Parameter(names = {"-s", "--driverHosts"}, description = "Comma separated list of driver nodes addresses.")
    private String driverHosts;


}
