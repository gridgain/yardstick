package org.yardstickframework.runners;

public enum NodeStatus {
    RUNNING("RUNNING"),
    NOT_RUNNING("NOT_RUNNING")
    ;

    private final String text;

    /**
     * @param text
     */
    NodeStatus(final String text) {
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
