package org.yardstickframework.runners;

import java.util.List;
import java.util.Collection;

/**
 *
 */
public class CommandExecutionResult {

    private int exitCode;

    private List<String> outStream;

    private List<String> errStream;

    private Process proc;

    public CommandExecutionResult(int exitCode, List<String> outStream, List<String> errStream, Process proc) {
        this.exitCode = exitCode;
        this.outStream = outStream;
        this.errStream = errStream;
        this.proc = proc;
    }

    public int getExitCode() {
        return exitCode;
    }

    public List<String> getOutStream() {
        return outStream;
    }

    public List<String> getErrStream() {
        return errStream;
    }

    public Process getProc() {
        return proc;
    }

    @Override public String toString() {
        return "CommandExecutionResult{" +
            "exitCode=" + exitCode +
            ", outStream=" + outStream +
            ", errStream=" + errStream +
            '}';
    }
}
