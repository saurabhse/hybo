/*
 * Copyright 1997-2017 Optimatika (www.optimatika.se)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package org.algo.access;

import java.util.Iterator;
import java.util.ListIterator;

import org.algo.ProgrammingError;

public interface ElementView1D<N extends Number, V extends ElementView1D<N, V>> extends AccessScalar<N>, ListIterator<V>, Iterable<V> {

    default void add(final V e) {
        ProgrammingError.throwForUnsupportedOptionalOperation();
    }

    long index();

    default Iterator<V> iterator() {
        return this;
    }

    default int nextIndex() {
        return (int) (this.index() + 1);
    }

    default int previousIndex() {
        return (int) (this.index() - 1);
    }

    default void remove() {
        ProgrammingError.throwForUnsupportedOptionalOperation();
    }

    default void set(final V e) {
        ProgrammingError.throwForUnsupportedOptionalOperation();
    }

}
