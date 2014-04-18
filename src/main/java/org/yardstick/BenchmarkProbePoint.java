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

package org.yardstick;

/**
 * Probe point. Single or multiple-value measurement with time tag.
 */
public class BenchmarkProbePoint {
    /** Time tag. */
    private final long time;

    /** Measured values. */
    private final double[] vals;

    /**
     * @param time Time tag.
     * @param vals Measured values.
     */
    public BenchmarkProbePoint(long time, double[] vals) {
        this.time = time;
        this.vals = vals;
    }

    /**
     * @return Time tag.
     */
    public long time() {
        return time;
    }

    /**
     * @return Array of measured values.
     */
    public double[] values() {
        return vals;
    }
}
