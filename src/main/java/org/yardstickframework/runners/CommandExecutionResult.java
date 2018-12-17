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
     * @return Exit code.
     */
    public int exitCode() {
        return exitCode;
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
