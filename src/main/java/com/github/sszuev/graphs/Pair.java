package com.github.sszuev.graphs;

class Pair<X, Y> {
    final X first;
    final Y second;

    Pair(X first, Y second) {
        this.first = first;
        this.second = second;
    }

    public X getFirst() {
        return first;
    }

    public Y getSecond() {
        return second;
    }
}
