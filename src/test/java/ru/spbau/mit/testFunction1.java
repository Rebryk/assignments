package ru.spbau.mit;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by rebryk on 10/4/15.
 */
public class testFunction1 {
    private static final Function1<Integer, Integer> MULT3 = new Function1<Integer, Integer>() {
        @Override
        public Integer apply(Integer x) {
            return 3 * x;
        }
    };

    private static final Function1<Integer, Integer> PLUS2 = new Function1<Integer, Integer>() {
        @Override
        public Integer apply(Integer x) {
            return x + 2;
        }
    };

    @Test
    public void testApply() {
        assertEquals(PLUS2.apply(3), (Integer)5);
    }

    @Test
    public void testCompose() {
        assertEquals(PLUS2.compose(MULT3).apply(1), (Integer)9);
        assertEquals(MULT3.compose(PLUS2).apply(1), (Integer)5);
    }
}
