package org.yardstickframework.runners;

import java.awt.List;
import java.util.Collection;

/**
 *
 */
public class CommandExecutionResult {

    private int exitCode;

    private Collection<String> outStream;

    private Collection<String> errStream;

    public CommandExecutionResult(int exitCode, Collection<String> outStream,
        Collection<String> errStream) {
        this.exitCode = exitCode;
        this.outStream = outStream;
        this.errStream = errStream;
    }

    public int getExitCode() {
        return exitCode;
    }

    public Collection<String> getOutStream() {
        return outStream;
    }

    public Collection<String> getErrStream() {
        return errStream;
    }
}
