package org.yardstickframework.runners.context;

/**
 * Node Status.
 */
public enum NodeStatus {
    /** */
    RUNNING("RUNNING"),

    /** */
    NOT_RUNNING("NOT_RUNNING");

    /** */
    private final String text;

    /**
     * Constructor.
     *
     * @param text String value.
     */
    NodeStatus(final String text) {
        this.text = text;
    }

    /** {@inheritDoc} */
    @Override public String toString() {
        return text;
    }
}
