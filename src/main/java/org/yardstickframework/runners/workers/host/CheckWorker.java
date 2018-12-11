package org.yardstickframework.runners.workers.host;

import java.util.List;
import org.yardstickframework.runners.workers.CheckWorkResult;
import org.yardstickframework.runners.workers.WorkResult;
import org.yardstickframework.runners.context.RunContext;

public abstract class CheckWorker extends HostWorker {
    public CheckWorker(RunContext runCtx, List<String> hostList) {
        super(runCtx, hostList);
    }

    /** {@inheritDoc} */
    @Override public void afterWork() {
        for(WorkResult res : getResList()){
            CheckWorkResult checkRes = (CheckWorkResult) res;

            if(checkRes.exit())
                System.exit(1);
        }
    }
}
