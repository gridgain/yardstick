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

/**
 * Restart schedule.
 */
public class RestartSchedule {
    /** */
    private long delay;

    /** */
    private long pause;

    /** */
    private long period;

    /**
     * Constructor.
     *
     * @param delay Delay.
     * @param pause Pause.
     * @param period Period.
     */
    RestartSchedule(long delay, long pause, long period) {
        this.delay = delay;
        this.pause = pause;
        this.period = period;
    }

    /**
     * @return Delay.
     */
    public long delay() {
        return delay;
    }

    /**
     * @param delay New delay.
     */
    public void delay(long delay) {
        this.delay = delay;
    }

    /**
     * @return Pause.
     */
    public long pause() {
        return pause;
    }

    /**
     * @param pause New pause.
     */
    public void pause(long pause) {
        this.pause = pause;
    }

    /**
     * @return Period.
     */
    public long period() {
        return period;
    }

    /**
     * @param period New period.
     */
    public void period(long period) {
        this.period = period;
    }

    /**
     * Converts milliseconds to seconds.
     *
     * @param millis {@code Long} Milliseconds to convert.
     * @return {@code String} String value in seconds e.g. 10.5.
     */
    private String millisToSec(Long millis){
        return String.format("%(.1f", millis.doubleValue() / 1000);
    }

    /** {@inheritDoc} */
    @Override public String toString() {
        return "RestartSchedule{" +
            "delay=" + millisToSec(delay) +
            ", pause=" + millisToSec(pause) +
            ", period=" + millisToSec(period) +
            '}';
    }
}
