package nightgames.gui;

import java.util.concurrent.CompletableFuture;

/**
 * A button that completes a future on click.
 */
public class ValueButton<T> extends RunnableButton {
    private static final long serialVersionUID = -2698381865901846194L;
    private final T value;
    protected final CompletableFuture<T> future;


    public ValueButton(LabeledValue<T> value, CompletableFuture<T> future) {
        this(value.getValue(), value.getLabel(), future);
    }

    public ValueButton(T value, String label, CompletableFuture<T> future) {
        super(label);
        this.value = value;
        this.future = future;
    }

    public T getValue() {
        return value;
    }

    @Override protected void run() {
        future.complete(value);
    }
}
