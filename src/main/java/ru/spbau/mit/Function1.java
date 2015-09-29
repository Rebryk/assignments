/**
 * Created by rebryk on 9/29/15.
 */
package ru.spbau.mit;

public abstract  class Function1<A, B> {
    public abstract B apply(A x);

    public <C> Function1<A, C> compose(final Function1<? super B, C> g) {
        return new Function1<A, C>() {
            @Override
            public C apply(A x) {
                 return g.apply(Function1.this.apply(x));
            }
        };
    }
};
