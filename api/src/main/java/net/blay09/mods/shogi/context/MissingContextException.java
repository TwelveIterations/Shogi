package net.blay09.mods.shogi.context;

/**
 * Thrown when a required context value is missing.
 */
public class MissingContextException extends RuntimeException {
    private final ShogiContext context;

    /**
     * Creates the exception for the given context.
     *
     * @param context the context that was missing a required value
     */
    public MissingContextException(ShogiContext context) {
        this.context = context;
    }

    /**
     * Returns the context that failed the requirement check.
     *
     * @return the context missing required data
     */
    public ShogiContext getContext() {
        return context;
    }
}
