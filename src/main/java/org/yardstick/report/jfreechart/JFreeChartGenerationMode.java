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

package org.yardstick.report.jfreechart;

/**
 * Mode that defines the way how different benchmark runs are compared with each other.
 */
public enum JFreeChartGenerationMode {
    /**
     * All benchmark runs are separate. Every chart contains one graph. It's default mode.
     */
    STANDARD,

    /**
     * In this mode benchmark runs from one folder are compared with benchmark runs from another folder,
     * first with first, second with second etc. Many graphs are displayed on one chart.
     */
    COMPARISON,

    /**
     * In this mode all benchmark runs are compared with each other. Many graphs are displayed on one chart.
     */
    COMPOUND
}
