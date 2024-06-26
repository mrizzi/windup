package org.jboss.windup.graph.frames;

import java.util.Iterator;

import com.syncleus.ferma.VertexFrame;
import org.apache.tinkerpop.gremlin.structure.Vertex;

/**
 * Converts an {@link Iterable<? extends VertexFrame>} to {@link Iterable<Vertex>}.
 */
public class VertexFromFramedIterable implements Iterable<Vertex> {
    private final Iterable<? extends VertexFrame> iterable;

    /**
     * Converts the provided {@link Iterable<? extends VertexFrame>} to {@link Iterable<Vertex>}.
     */
    public VertexFromFramedIterable(final Iterable<? extends VertexFrame> iterable) {
        this.iterable = iterable;
    }

    @Override
    public Iterator<Vertex> iterator() {
        return new Iterator<Vertex>() {
            private final Iterator<? extends VertexFrame> iterator = iterable.iterator();

            public void remove() {
                throw new UnsupportedOperationException();
            }

            public boolean hasNext() {
                return this.iterator.hasNext();
            }

            public Vertex next() {
                return iterator.next().getElement();
            }
        };
    }
}
