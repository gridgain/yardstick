package org.yardstickframework.runners;

public enum RunMode {
    PLAIN("PLAIN"),
    IN_DOCKER("IN_DOCKER")
    ;

    private final String text;

    /**
     * @param text
     */
    RunMode(final String text) {
        this.text = text;
    }

    /* (non-Javadoc)
     * @see java.lang.Enum#toString()
     */
    @Override
    public String toString() {
        return text;
    }
}
