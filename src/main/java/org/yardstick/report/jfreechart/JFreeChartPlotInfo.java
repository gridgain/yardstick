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
 * Additional info for plot.
 */
public class JFreeChartPlotInfo {
    /** */
    private final String name;

    /** */
    private final double avg;

    /** */
    private final double min;

    /** */
    private final double max;

    /** */
    private final double stdDiv;

    /**
     * @param name Plot name.
     * @param avg Average.
     * @param min Minimum.
     * @param max Maximum.
     * @param stdDiv Standard deviation.
     */
    public JFreeChartPlotInfo(String name, double avg, double min, double max, double stdDiv) {
        this.name = name;
        this.avg = avg;
        this.min = min;
        this.max = max;
        this.stdDiv = stdDiv;
    }

    /**
     * @return Plot name.
     */
    public String name() {
        return name;
    }

    /**
     * @return Average.
     */
    public double average() {
        return avg;
    }

    /**
     * @return Minimum.
     */
    public double minimum() {
        return min;
    }

    /**
     * @return Maximum.
     */
    public double maximum() {
        return max;
    }

    /**
     * @return Standard deviation.
     */
    public double standardDeviation() {
        return stdDiv;
    }
}
