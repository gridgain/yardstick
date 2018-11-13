package org.yardstickframework.runners;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AbstractRunner {

    protected Properties runProps;

    protected String[] toDeploy = new String[]{"bin", "config", "libs"};

    protected String mainDir;

    protected void setRunProps(File propPath) throws FileNotFoundException, IOException {
        runProps = new Properties();

        runProps.load(new FileReader(propPath));
    }

    protected List<String> runCmd(String cmd){

        List<String> res = new ArrayList<>();

        final Process p;

        ExecutorService errStreamPrinter = Executors.newSingleThreadExecutor();


        try {
            p = Runtime.getRuntime().exec(cmd);
            p.waitFor();
            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));


            errStreamPrinter.submit(new Callable<Object>() {
                @Override public Object call() throws IOException {
                    String line = "";


                    BufferedReader errReader = new BufferedReader(new InputStreamReader(p.getErrorStream()));

                    while ((line = errReader.readLine())!= null)
                        System.out.println(line);

                    return null;
                }
            });

            String line = "";

            while ((line = reader.readLine())!= null)
                res.add(line);
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        errStreamPrinter.shutdown();

        return res;
    }
}
