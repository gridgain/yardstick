package org.yardstickframework.runners.docker;

import java.util.ArrayList;
import java.util.List;
import org.yardstickframework.runners.WorkResult;

public class DockerCheckWorkResult implements WorkResult{
    private List<String> errMsgs = new ArrayList<>();

    public List<String> getErrMsgs() {
        return errMsgs;
    }

    public void setErrMsgs(List<String> errMsgs) {
        this.errMsgs = errMsgs;
    }
}
