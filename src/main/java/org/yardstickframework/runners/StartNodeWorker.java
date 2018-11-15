package org.yardstickframework.runners;

import java.util.Properties;
import org.yardstickframework.BenchmarkConfiguration;

import static org.yardstickframework.BenchmarkUtils.jcommander;

public abstract class StartNodeWorker extends Worker {
    /** */
    protected String dateTime;

    /** */
    protected String resDirName;

    /** */
    protected String logDirName;

    /** */
    protected String baseLogDirFullName;

    /** */
    protected String cfgFullStr;

    /** */
    private BenchmarkConfiguration iterCfg;


    public StartNodeWorker(Properties runProps) {
        super(runProps);
    }

    public StartNodeWorker(Properties runProps, String cfgFullStr) {
        super(runProps);

        this.cfgFullStr = parseCfgStr(cfgFullStr);
    }

    public String getCfgFullStr() {
        return cfgFullStr;
    }

    @Override public void beforeWork() {
        super.beforeWork();

        dateTime = runProps.getProperty("MAIN_DATE_TIME");

        resDirName = String.format("results-%s", dateTime);

        logDirName = String.format("logs-%s", dateTime);

        baseLogDirFullName = String.format("%s/output/%s", getMainDir(), logDirName);
    }

    private String parseCfgStr(String src){
        String res = src;

        if(src.contains("${SCRIPT_DIR}/.."))
            res = res.replace("${SCRIPT_DIR}/..", getMainDir());
        if(src.contains("${nodesNum}"))
            res = res.replace("${nodesNum}", String.valueOf(getNodesNum()));

        for(String propName : runProps.stringPropertyNames()){
            if(src.contains(String.format("${%s}", propName)))
                res = res.replace(String.format("${%s}", propName), runProps.get(propName).toString());

            if(src.contains(String.format("$%s", propName)))
                res = res.replace(String.format("$%s", propName), runProps.get(propName).toString());
        }

        iterCfg = new BenchmarkConfiguration();

        String[] argArr = res.split(" ");

        for(String s : argArr)
            System.out.println(s);

        jcommander(res.split(" "), iterCfg, "<runner>");

        return res;
    }
}
