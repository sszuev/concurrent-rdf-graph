package com.github.sszuev.graphs;

public interface Action<X> {
    X execute();

    static Action<Void> from(Runnable runnable) {
        return () -> {
            runnable.run();
            return null;
        };
    }
}
