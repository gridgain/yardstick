package org.yardstickframework.runners.workers;

/**
 * Check work result.
 */
public class CheckWorkResult implements WorkResult {
    /** Flag indicating whether or not process should be terminated.*/
    private boolean exit;

    /**
     * @return Exit.
     */
    public boolean exit() {
        return exit;
    }

    /**
     * @param exit New exit.
     */
    public void exit(boolean exit) {
        this.exit = exit;
    }
}
