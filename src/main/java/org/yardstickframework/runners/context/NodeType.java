package org.yardstickframework.runners.context;

public enum NodeType {
    SERVER("SERVER"),
    DRIVER("DRIVER")
    ;

    private final String text;

    /**
     * @param text
     */
    NodeType(final String text) {
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
