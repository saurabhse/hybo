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
package org.algo.matrix.task;

import java.math.BigDecimal;

import org.algo.RecoverableCondition;
import org.algo.access.Access2D;
import org.algo.access.Structure2D;
import org.algo.matrix.BasicMatrix;
import org.algo.matrix.MatrixUtils;
import org.algo.matrix.decomposition.Cholesky;
import org.algo.matrix.decomposition.LU;
import org.algo.matrix.decomposition.QR;
import org.algo.matrix.decomposition.SingularValue;
import org.algo.matrix.store.MatrixStore;
import org.algo.matrix.store.PhysicalStore;
import org.algo.scalar.ComplexNumber;

public interface InverterTask<N extends Number> extends MatrixTask<N> {

    public static abstract class Factory<N extends Number> {

        public final InverterTask<N> make(final MatrixStore<N> template) {
            return this.make(template, MatrixUtils.isHermitian(template), false);
        }

        public abstract InverterTask<N> make(MatrixStore<N> template, boolean symmetric, boolean positiveDefinite);

    }

    public static final Factory<BigDecimal> BIG = new Factory<BigDecimal>() {

        @Override
        public InverterTask<BigDecimal> make(final MatrixStore<BigDecimal> template, final boolean symmetric, final boolean positiveDefinite) {
            if (symmetric && positiveDefinite) {
                return Cholesky.BIG.make(template);
            } else if (template.isSquare()) {
                return LU.BIG.make(template);
            } else if (template.isTall()) {
                return QR.BIG.make(template);
            } else {
                return SingularValue.BIG.make(template);
            }
        }

    };

    public static final Factory<ComplexNumber> COMPLEX = new Factory<ComplexNumber>() {

        @Override
        public InverterTask<ComplexNumber> make(final MatrixStore<ComplexNumber> template, final boolean symmetric, final boolean positiveDefinite) {
            if (symmetric && positiveDefinite) {
                return Cholesky.COMPLEX.make(template);
            } else if (template.isSquare()) {
                return LU.COMPLEX.make(template);
            } else if (template.isTall()) {
                return QR.COMPLEX.make(template);
            } else {
                return SingularValue.COMPLEX.make(template);
            }
        }

    };

    public static final Factory<Double> PRIMITIVE = new Factory<Double>() {

        @Override
        public InverterTask<Double> make(final MatrixStore<Double> template, final boolean symmetric, final boolean positiveDefinite) {

            final long tmpDim = template.countRows();

            if (symmetric) {
                if (tmpDim == 1L) {
                    return AbstractInverter.FULL_1X1;
                } else if (tmpDim == 2L) {
                    return AbstractInverter.SYMMETRIC_2X2;
                } else if (tmpDim == 3L) {
                    return AbstractInverter.SYMMETRIC_3X3;
                } else if (tmpDim == 4L) {
                    return AbstractInverter.SYMMETRIC_4X4;
                } else if (tmpDim == 5L) {
                    return AbstractInverter.SYMMETRIC_5X5;
                } else {
                    return positiveDefinite ? Cholesky.PRIMITIVE.make(template) : LU.PRIMITIVE.make(template);
                }
            } else if (template.isSquare()) {
                if (tmpDim == 1L) {
                    return AbstractInverter.FULL_1X1;
                } else if (tmpDim == 2L) {
                    return AbstractInverter.FULL_2X2;
                } else if (tmpDim == 3L) {
                    return AbstractInverter.FULL_3X3;
                } else if (tmpDim == 4L) {
                    return AbstractInverter.FULL_4X4;
                } else if (tmpDim == 5L) {
                    return AbstractInverter.FULL_5X5;
                } else {
                    return LU.PRIMITIVE.make(template);
                }
            } else if (template.isTall()) {
                return QR.PRIMITIVE.make(template);
            } else {
                return SingularValue.PRIMITIVE.make(template);
            }
        }

    };

    /**
     * The output must be a "right inverse" and a "generalised inverse".
     * @throws RecoverableCondition TODO
     *
     * @see BasicMatrix#invert()
     */
    default MatrixStore<N> invert(final Access2D<?> original) throws RecoverableCondition {
        return this.invert(original, this.preallocate(original));
    }

    /**
     * <p>
     * Exactly how (if at all) a specific implementation makes use of <code>preallocated</code> is not
     * specified by this interface. It must be documented for each implementation.
     * </p>
     * <p>
     * Should produce the same results as calling {@link #invert(Access2D)}.
     * </p>
     * <p>
     * Use {@link #preallocate(Structure2D)} to obtain a suitbale <code>preallocated</code>.
     * </p>
     *
     * @param preallocated Preallocated memory for the results, possibly some intermediate results. You must
     *        assume this is modified, but you cannot assume it will contain the full/final/correct solution.
     * @return The inverse
     * @throws RecoverableCondition TODO
     */
    MatrixStore<N> invert(Access2D<?> original, PhysicalStore<N> preallocated) throws RecoverableCondition;

    /**
     * <p>
     * Will create a {@linkplain PhysicalStore} instance suitable for use with
     * {@link #invert(Access2D, PhysicalStore)}.
     * </p>
     * <p>
     * When inverting a matrix (mxn) the preallocated memory/matrix will typically be nxm (and of course most
     * of the time A is square).
     * </p>
     */
    PhysicalStore<N> preallocate(Structure2D template);

}
