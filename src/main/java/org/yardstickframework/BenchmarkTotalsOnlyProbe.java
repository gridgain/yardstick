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

package org.yardstickframework;

import java.util.Collection;

/**
 * Probe marked with this interface should be asked for collected points
 * only when warm-up is finished and right before benchmark stops.
 */
public interface BenchmarkTotalsOnlyProbe extends BenchmarkProbe {
    /**
     * Gets collection of the points gathered by the probe.
     * This method should be called only when warm-up is finished
     * and right before benchmark stops.
     *
     * @return Collection of points.
     * @throws IllegalStateException If called more than once.
     */
    @Override public Collection<BenchmarkProbePoint> points();
}
