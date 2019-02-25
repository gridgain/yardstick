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

import java.util.ArrayList;
import java.util.List;

/**
 * Command execution result.
 */
public class CommandExecutionResult {
    /** */
    private int exitCode;

    /** */
    private List<String> outList;

    /** */
    private List<String> errList;

    /** */
    private Process proc;

    /**
     * Constructor.
     *
     * @param exitCode Exit code.
     * @param outList Output stream.
     * @param errList Error stream.
     * @param proc Process.
     */
    CommandExecutionResult(int exitCode, List<String> outList, List<String> errList, Process proc) {
        this.exitCode = exitCode;
        this.outList = new ArrayList<>(outList);
        this.errList = new ArrayList<>(errList);
        this.proc = proc;
    }

    /**
     *
     * @return Command execution result with exit code 1 and empty output and error stream.
     */
    public static CommandExecutionResult emptyFailedResult(){
        return new CommandExecutionResult(1, new ArrayList<>(), new ArrayList<>(), null);
    }

    /**
     *
     * @return Exit code.
     */
    public int exitCode() {
        return exitCode;
    }

    /**
     *
     * @param exitCode New exit code.
     */
    public void exitCode(int exitCode){
        this.exitCode = exitCode;
    }

    /**
     *
     * @return Output stream.
     */
    public List<String> outputList() {
        return new ArrayList<>(outList);
    }

    /**
     *
     * @return Error stream.
     */
    public List<String> errorList() {
        return new ArrayList<>(errList);
    }

    /**
     *
     * @return Process.
     */
    public Process process() {
        return proc;
    }

    /** {@inheritDoc} */
    @Override public String toString() {
        return "CommandExecutionResult{" +
            "exitCode=" + exitCode +
            ", outputList=" + outList +
            ", errorList=" + errList +
            '}';
    }
}
