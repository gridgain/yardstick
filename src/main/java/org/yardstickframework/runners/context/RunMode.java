package org.yardstickframework.runners.context;

public enum RunMode {
    PLAIN("PLAIN"),
    DOCKER("DOCKER")
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
