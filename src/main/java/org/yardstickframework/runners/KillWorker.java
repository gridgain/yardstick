package org.yardstickframework.runners;

import java.io.IOException;
import java.util.Properties;
import org.yardstickframework.BenchmarkUtils;

public class KillWorker extends Worker{

    public KillWorker(RunContext runCtx, WorkContext workCtx) {
        super(runCtx, workCtx);
    }

    @Override public void beforeWork() {
        //NO_OP
    }

    @Override public void afterWork() {
        //NO_OP
    }

    @Override public WorkResult doWork(String host, int cnt) {
        CommandHandler hndl = new CommandHandler(runCtx);

        String killServCmd = "pkill -9 -f \"Dyardstick.server\"";
        String killDrvrCmd = "pkill -9 -f \"Dyardstick.driver\"";

        try {
            hndl.runCmd(host, killServCmd);

            hndl.runCmd(host, killDrvrCmd);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }


        return null;
    }

    public WorkResult killNode(NodeInfo nodeInfo){
        CommandHandler hndl = new CommandHandler(runCtx);

        NodeInfo res = nodeInfo;

        try {
            res = (NodeInfo) hndl.killNode(nodeInfo);


        }
        catch (InterruptedException | IOException e) {
            e.printStackTrace();
        }

        return res;
    }

    @Override public String getWorkerName() {
        return getClass().getSimpleName();
    }
}
