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

    public CommandExecutionResult(int exitCode, List<String> outStream,
        List<String> errStream) {
        this.exitCode = exitCode;
        this.outStream = outStream;
        this.errStream = errStream;
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
}
