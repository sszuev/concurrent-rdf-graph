package com.github.sszuev.graphs;

import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.util.iterator.NullIterator;
import org.apache.jena.util.iterator.WrappedIterator;

import java.util.Iterator;
import java.util.Queue;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class Iterators {
    public static <X> ExtendedIterator<X> asExtendedIterator(Stream<X> stream) {
        return new WrappedIterator<>(stream.iterator()) {
            @Override
            public void close() {
                stream.close();
            }
        };
    }

    public static <X> Stream<X> asStream(ExtendedIterator<X> iterator) {
        if (iterator instanceof NullIterator) {
            return Stream.empty();
        }
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(iterator, Spliterator.NONNULL), false)
                .onClose(iterator::close);
    }

    public static <X> Iterator<X> erasingIterator(Queue<X> queue) {
        return new Iterator<>() {
            @Override
            public boolean hasNext() {
                return !queue.isEmpty();
            }

            @Override
            public X next() {
                return queue.remove();
            }
        };
    }
}
