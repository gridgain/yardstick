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

package org.yardstickframework.report.jfreechart;

/**
 * Mode that defines whether a summary plot is added to a graph or not.
 */
public enum JFreeChartSummaryMode {
    /**
     * Summary plot is added to a graph.
     */
    SUM_ONLY,

    /**
     * Individual plots are displayed.
     */
    INDIVIDUAL_ONLY,

    /**
     * Individual and summary plots are displayed.
     */
    INDIVIDUAL_AND_SUM
}
