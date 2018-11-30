package org.yardstickframework.runners;

import java.util.ArrayList;
import java.util.List;

public class CheckWorkResult implements WorkResult{
    private List<String> errMsgs = new ArrayList<>();

    public List<String> getErrMsgs() {
        return errMsgs;
    }
}
