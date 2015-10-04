package ru.spbau.mit;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by rebryk on 10/4/15.
 */
public class testPredicate {
    private static final Predicate<Integer> GREATER_THAN_10 = new Predicate<Integer>() {
        @Override
        public Boolean apply(Integer x) {
            return x > 10;
        }
    };

    private static final Predicate<Integer> LESS_THAN_10 = new Predicate<Integer>() {
        @Override
        public Boolean apply(Integer x) {
            return x < 10;
        }
    };

    private static final Predicate<Integer> LESS_THAN_20 = new Predicate<Integer>() {
        @Override
        public Boolean apply(Integer x) {
            return x < 20;
        }
    };

    @Test
    public void testNot() {
        assertFalse(LESS_THAN_20.and(GREATER_THAN_10).not().apply(15));
        assertTrue(LESS_THAN_20.and(GREATER_THAN_10).not().apply(10));
    }

    @Test
    public void testOr() throws Exception {
        assertTrue(LESS_THAN_10.or(GREATER_THAN_10).apply(11));
        assertFalse(LESS_THAN_10.or(GREATER_THAN_10).apply(10));

        Predicate<Boolean> or = Predicate.ALWAYS_TRUE.or(new Predicate<Boolean>() {
            @Override
            public Boolean apply(Boolean x) {
                throw new IllegalArgumentException();
            }
        });

        assertTrue(or.apply(false));
    }

    @Test
    public void testConst() {
        assertFalse((boolean)Predicate.ALWAYS_FALSE.apply(null));
        assertTrue((boolean)Predicate.ALWAYS_TRUE.apply(null));
    }

    @Test
    public void testAnd() throws Exception {
        assertTrue(LESS_THAN_20.and(GREATER_THAN_10).apply(15));
        assertFalse(LESS_THAN_20.and(GREATER_THAN_10).apply(10));
        assertFalse(LESS_THAN_20.and(GREATER_THAN_10).apply(20));

        Predicate<Boolean> and = Predicate.ALWAYS_FALSE.and(new Predicate<Boolean>() {
            @Override
            public Boolean apply(Boolean x) {
                throw new IllegalArgumentException();
            }
        });

        assertFalse(and.apply(false));
    }
}
