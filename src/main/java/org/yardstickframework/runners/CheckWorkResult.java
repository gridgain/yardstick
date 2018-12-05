package org.yardstickframework.runners;

import java.util.ArrayList;
import java.util.List;

public class CheckWorkResult implements WorkResult {
    private boolean exit;

    private List<String> errMsgs = new ArrayList<>();

    public List<String> getErrMsgs() {
        return errMsgs;
    }

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
