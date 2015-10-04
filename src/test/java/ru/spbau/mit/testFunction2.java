package ru.spbau.mit;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by rebryk on 10/4/15.
 */
public class testFunction2 {
    private static final Function1<Integer, Integer> MULT3 = new Function1<Integer, Integer>() {
        @Override
        public Integer apply(Integer x) {
            return 3 * x;
        }
    };

    private static final Function2<Integer, Integer, Integer> MINUS = new Function2<Integer, Integer, Integer>() {
        @Override
        public Integer apply(Integer x, Integer y) {
            return x - y;
        }
    };

    @Test
    public void testApply() {
        assertEquals(MINUS.apply(3, 2), (Integer)1);
    }

    @Test
    public void testBind() {
        assertEquals(MINUS.bind1(3).apply(2), (Integer)1);
        assertEquals(MINUS.bind2(3).apply(2), (Integer)(-1));
    }

    @Test
    public void testCurry() {
        assertEquals(MINUS.curry().apply(3).apply(2), (Integer)1);
    }

    @Test
    public void testCompose() {
        assertEquals(MINUS.compose(MULT3).apply(3, 2), (Integer)3);
    }
}
