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
package org.algo.function.multiary;

import java.math.BigDecimal;

import org.algo.access.Access1D;
import org.algo.matrix.store.BigDenseStore;
import org.algo.matrix.store.ComplexDenseStore;
import org.algo.matrix.store.MatrixStore;
import org.algo.matrix.store.PhysicalStore;
import org.algo.matrix.store.PrimitiveDenseStore;
import org.algo.matrix.store.PhysicalStore.Factory;
import org.algo.scalar.ComplexNumber;

/**
 * Constant valued function - always returns the same value.
 *
 * @author apete
 */
public final class ConstantFunction<N extends Number> extends AbstractMultiary<N, ConstantFunction<N>> {

    public static ConstantFunction<BigDecimal> makeBig(final int arity) {
        return new ConstantFunction<>(arity, BigDenseStore.FACTORY, null);
    }

    public static ConstantFunction<BigDecimal> makeBig(final int arity, final Number constant) {
        return new ConstantFunction<>(arity, BigDenseStore.FACTORY, constant);
    }

    public static ConstantFunction<ComplexNumber> makeComplex(final int arity) {
        return new ConstantFunction<>(arity, ComplexDenseStore.FACTORY, null);
    }

    public static ConstantFunction<ComplexNumber> makeComplex(final int arity, final Number constant) {
        return new ConstantFunction<>(arity, ComplexDenseStore.FACTORY, constant);
    }

    public static ConstantFunction<Double> makePrimitive(final int arity) {
        return new ConstantFunction<>(arity, PrimitiveDenseStore.FACTORY, null);
    }

    public static ConstantFunction<Double> makePrimitive(final int arity, final Number constant) {
        return new ConstantFunction<>(arity, PrimitiveDenseStore.FACTORY, constant);
    }

    private final int myArity;

    private final PhysicalStore.Factory<N, ?> myFactory;

    @SuppressWarnings("unused")
    private ConstantFunction() {
        this(0, null, null);
    }

    ConstantFunction(final int arity, final PhysicalStore.Factory<N, ?> factory, final Number constant) {

        super();

        myArity = arity;
        myFactory = factory;

        this.setConstant(constant);
    }

    public int arity() {
        return myArity;
    }

    public MatrixStore<N> getGradient(final Access1D<N> point) {
        //return new ZeroStore<>(this.factory(), this.arity(), 1);
        return this.factory().builder().makeZero(this.arity(), 1).get();
    }

    public MatrixStore<N> getHessian(final Access1D<N> point) {
        //return new ZeroStore<>(this.factory(), this.arity(), this.arity());
        return this.factory().builder().makeZero(this.arity(), this.arity()).get();
    }

    public N invoke(final Access1D<N> arg) {
        return this.getConstant();
    }

    @Override
    protected Factory<N, ?> factory() {
        return myFactory;
    }

}
