package org.yardstickframework.runners;

import java.io.IOException;
import org.yardstickframework.BenchmarkUtils;

public class CheckLogWorker extends NodeServiceWorker{

    public CheckLogWorker(RunContext runCtx, WorkContext workCtx) {
        super(runCtx, workCtx);
    }

    @Override public void beforeWork() {
        //NO_OP
    }

    @Override public WorkResult doWork(NodeInfo nodeInfo) {
//        log().info(String.format("Checking node %s%s on the host %s.",
//            nodeInfo.typeLow(),
//            nodeInfo.getId(),
//            nodeInfo.getHost()));

        String host = nodeInfo.getHost();

        String logPath = nodeInfo.getLogPath();

        CheckWorkResult checkLogRes = new CheckWorkResult();

        CommandHandler hndl = new CommandHandler(runCtx);

        boolean fileExists = false;

        int cnt = 10;

        while (!fileExists && cnt-- > 0) {
            fileExists = hndl.checkRemFile(host, logPath);

            try {
                Thread.sleep(1000L);
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }

        }

        NodeChecker checker = runCtx.getNodeChecker(nodeInfo);

        NodeCheckResult checkRes = (NodeCheckResult)checker.checkNode(nodeInfo);

        if(checkRes.getNodeStatus() == NodeStatus.NOT_RUNNING) {
            log().info(String.format("Node %s%s on the host %s in not running. Will check log file and exit.",
                nodeInfo.typeLow(), nodeInfo.getId(), host));

            checkLogRes.exit(true);
        }

        if(!fileExists){
            log().info(String.format("No log file %s on the host %s.", logPath, host));

            return checkLogRes;
        }

        String cmd = String.format("head -20 %s | grep 'Exception'", logPath);

        try {
            CommandExecutionResult res = hndl.runCmd(host, cmd);

            if(!res.getOutStream().isEmpty()){
                checkLogRes.getErrMsgs().addAll(res.getOutStream());

                log().info(String.format("WARNING! Log file '%s' contains following error messages:",
                    logPath));

                for(String msg : res.getOutStream())
                    log().info(msg);

                return checkLogRes;
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }

        return checkLogRes;
    }

    @Override public void afterWork() {
        //NO_OP
    }

    @Override public String getWorkerName() {
        return getClass().getSimpleName();
    }
}
