package org.yardstickframework.runners.context;

/**
 * Node type.
 */
public enum NodeType {
    /** */
    SERVER("SERVER"),

    /** */
    DRIVER("DRIVER");

    /** */
    private final String text;

    /**
     * Constructor.
     *
     * @param text String value.
     */
    NodeType(final String text) {
        this.text = text;
    }

    /** {@inheritDoc} */
    @Override public String toString() {
        return text;
    }
}
