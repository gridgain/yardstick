package org.yardstickframework.runners.context;

/**
 * Run mode.
 */
public enum RunMode {
    /** */
    PLAIN("PLAIN"),

    /** */
    DOCKER("DOCKER");

    /** */
    private final String text;

    /**
     * @param text String value.
     */
    RunMode(final String text) {
        this.text = text;
    }

    /** {@inheritDoc} */
    @Override public String toString() {
        return text;
    }
}
