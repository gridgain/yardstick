/*
 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */

package org.yardstickframework.runners.context;

import java.util.HashMap;

/**
 * Restart context.
 */
public class RestartContext extends HashMap<String, HashMap<String, RestartSchedule>> {
    /**
     *
     * @return {@code String} value of map content.
     */
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
            }

            sb.append("}");

        }

        return sb.toString();
    }

    /** {@inheritDoc} */
    @Override public String toString() {
        return mapToString();
    }
}
