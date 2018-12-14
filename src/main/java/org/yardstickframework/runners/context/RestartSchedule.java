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
