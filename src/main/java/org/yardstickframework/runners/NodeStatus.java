package org.yardstickframework.runners;

public enum NodeStatus {
    ACTIVE("ACTIVE"),
    NOT_EXIST("NOT_EXIST")
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
