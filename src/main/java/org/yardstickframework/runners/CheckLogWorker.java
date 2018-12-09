package org.yardstickframework.runners;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class CheckLogWorker extends NodeWorker {
    /**
     *
     * @param runCtx
     * @param nodeList
     */
    CheckLogWorker(RunContext runCtx, List<NodeInfo> nodeList) {
        super(runCtx, nodeList);
    }

    @Override public NodeInfo doWork(NodeInfo nodeInfo) throws InterruptedException {
        //TODO refactor all method
        String host = nodeInfo.getHost();

        String logPath = nodeInfo.getLogPath();

        CommandHandler hndl = new CommandHandler(runCtx);

        boolean fileExists = false;

        int cnt = 10;

        while (!fileExists && cnt-- > 0) {
            fileExists = hndl.checkRemFile(host, logPath);

            try {
                new CountDownLatch(1).await(1000L, TimeUnit.MILLISECONDS);
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }

        }

        NodeChecker checker = runCtx.getNodeChecker(nodeInfo);

        checker.checkNode(nodeInfo);

        if(nodeInfo.nodeStatus() == NodeStatus.NOT_RUNNING)
            log().info(String.format("Node %s on the host %s in not running. Will check log file and exit.",
                nodeInfo.toShortStr(), host));


        if(!fileExists){
            log().info(String.format("No log file %s on the host %s.", logPath, host));

            return nodeInfo;
        }

        try {
            String cmd = String.format("head -20 %s | grep 'Exception'", logPath);

            CommandExecutionResult res = hndl.runCmd(host, cmd);

            if(!res.getOutStream().isEmpty()){
                nodeInfo.getErrMsgs().addAll(res.getOutStream());

                log().info(String.format("WARNING! Log file '%s' contains following error messages:",
                    logPath));

                for(String msg : res.getOutStream())
                    log().info(msg);

                return nodeInfo;
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        return nodeInfo;
    }
}
