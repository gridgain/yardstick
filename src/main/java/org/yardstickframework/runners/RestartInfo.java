package org.yardstickframework.runners;

public class RestartInfo {
    private long delay;
    private long pause;
    private long period;

    public RestartInfo(long delay, long pause, long period) {
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
}
