package org.yardstickframework.runners;

public enum StartMode {
    PLAIN("PLAIN"),
    IN_DOCKER("IN_DOCKER")
    ;

    private final String text;

    /**
     * @param text
     */
    StartMode(final String text) {
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
