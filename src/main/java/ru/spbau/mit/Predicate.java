package ru.spbau.mit;

/**
 * Created by rebryk on 9/29/15.
 */
public abstract class Predicate<A> extends Function1<A, Boolean> {
    public static final Predicate ALWAYS_TRUE = new Predicate<Object>() {
        @Override
        public Boolean apply(Object x) {
            return true;
        }
    };

    public static final Predicate ALWAYS_FALSE = new Predicate<Object>() {
        @Override
        public Boolean apply(Object x) {
            return false;
        }
    };

    public <B extends A> Predicate<B> or(final Predicate<? super B> predicate) {
        return new Predicate<B>() {
            @Override
            public Boolean apply(B x) {
                return Predicate.this.apply(x) || predicate.apply(x);
            }
        };
    }

    public <B extends A> Predicate<B> and(final Predicate<? super B> predicate) {
        return new Predicate<B>() {
            @Override
            public Boolean apply(B x) {
                return Predicate.this.apply(x) && predicate.apply(x);
            }
        };
    }

    public Predicate<A> not() {
        return new Predicate<A>() {
            @Override
            public Boolean apply(A x) {
                return !Predicate.this.apply(x);
            }
        };
    }
}
