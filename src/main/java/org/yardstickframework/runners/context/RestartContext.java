package org.yardstickframework.runners.context;

import java.util.HashMap;

public class RestartContext extends HashMap<String, HashMap<String, RestartSchedule>> {
    private String mapToString(){
        StringBuilder sb = new StringBuilder("RestartContext");

        for(String host : keySet()){
            sb.append("{");
            sb.append(host);

            for(String id : get(host).keySet()){
                sb.append("{");
                sb.append(id);
                sb.append(":");
                sb.append(get(host).get(id));
                sb.append(";");
//                sb.append("}");
            }

            sb.append("}");

        }

        return sb.toString();
    }

    @Override public String toString() {
        return mapToString();
    }
}
