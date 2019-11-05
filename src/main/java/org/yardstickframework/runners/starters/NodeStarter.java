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

package org.yardstickframework.runners.starters;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.yardstickframework.runners.context.NodeInfo;
import org.yardstickframework.runners.context.RunContext;

/**
 * Node starter.
 */
public abstract class NodeStarter {
    /** */
    protected RunContext runCtx;

    /**
     *
     * @param runCtx Run context.
     */
    public NodeStarter(RunContext runCtx) {
        this.runCtx = runCtx;
    }

    /**
     *
     * @param nodeInfo Node info.
     * @return Node info.
     * @throws InterruptedException if interrupted.
     */
    public abstract NodeInfo startNode(NodeInfo nodeInfo) throws InterruptedException;

    /**
     *
     * @return Logger.
     */
    protected Logger log(){
        return LogManager.getLogger(getClass().getSimpleName());
    }
}
