package net.blay09.mods.shogi.coercion;

/**
 * Exception raised when a value cannot be coerced to a requested type.
 */
public class CoercionException extends RuntimeException {
    private final Object input;

    /**
     * Creates a coercion exception.
     *
     * @param input the original value that failed coercion
     * @param message the failure message
     */
    public CoercionException(Object input, String message) {
        super(message);
        this.input = input;
    }

    /**
     * Returns the original input value that failed coercion.
     *
     * @return the failing input
     */
    public Object getInput() {
        return input;
    }
}
