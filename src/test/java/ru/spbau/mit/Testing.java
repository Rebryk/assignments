package ru.spbau.mit;

import static org.junit.Assert.*;
import org.junit.Test;
import org.omg.CORBA.PRIVATE_MEMBER;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by rebryk on 9/29/15.
 */

public class Testing {
    private static final Function1<Integer, Integer> mult3 = new Function1<Integer, Integer>() {
        @Override
        public Integer apply(Integer x) {
            return 3 * x;
        }
    };

    private static final Function2<Integer, Integer, Integer> minus = new Function2<Integer, Integer, Integer>() {
        @Override
        public Integer apply(Integer x, Integer y) {
            return x - y;
        }
    };

    @Test
    public void testFunction1() {
        Function1<Integer, Integer> plus2 = new Function1<Integer, Integer>() {
            @Override
            public Integer apply(Integer x) {
                return x + 2;
            }
        };
        assertEquals(plus2.apply(3), (Integer)5);
        assertEquals(plus2.compose(mult3).apply(1), (Integer)9);
        assertEquals(mult3.compose(plus2).apply(1), (Integer)5);
    }

    @Test
    public void testFunction2() {
        assertEquals(minus.apply(3, 2), (Integer)1);
        assertEquals(minus.bind1(3).apply(2), (Integer)1);
        assertEquals(minus.bind2(3).apply(2), (Integer)(-1));
        assertEquals(minus.curry().apply(3).apply(2), (Integer)1);
        assertEquals(minus.compose(mult3).apply(3, 2), (Integer)3);
    }

    @Test
    public void testPredicate() {
        Predicate<Integer> greaterThan10 = new Predicate<Integer>() {
            @Override
            public Boolean apply(Integer x) {
                return x > 10;
            }
        };

        Predicate<Integer> lessThan10 = new Predicate<Integer>() {
            @Override
            public Boolean apply(Integer x) {
                return x < 10;
            }
        };

        Predicate<Integer> lessThan20 = new Predicate<Integer>() {
            @Override
            public Boolean apply(Integer x) {
                return x < 20;
            }
        };

        assertTrue(lessThan20.and(greaterThan10).apply(15));
        assertFalse(lessThan20.and(greaterThan10).apply(10));
        assertFalse(lessThan20.and(greaterThan10).apply(20));

        assertTrue(lessThan10.or(greaterThan10).apply(11));
        assertFalse(lessThan10.or(greaterThan10).apply(10));

        assertFalse((boolean)Predicate.ALWAYS_FALSE.apply(null));
        assertTrue((boolean) Predicate.ALWAYS_TRUE.apply(null));

        assertFalse(lessThan20.and(greaterThan10).not().apply(15));
        assertTrue(lessThan20.and(greaterThan10).not().apply(10));
    }

    @Test
    public void testCollections() {
        Predicate<Integer> lessThan3 = new Predicate<Integer>() {
            @Override
            public Boolean apply(Integer x) {
                return x < 3;
            }
        };

        Predicate<Integer> odd = new Predicate<Integer>() {
            @Override
            public Boolean apply(Integer x) {
                return x % 2 == 1;
            }
        };

        List<Integer> list = Arrays.asList(1, 2, 3, 4);

        List<Integer> list2 = (List<Integer>) Collections.map(mult3, list);
        assertTrue(list2.equals(Arrays.asList(3, 6, 9, 12)));

        List<Integer> list3 = (List<Integer>) Collections.filter(odd, list);
        assertTrue(list3.equals(Arrays.asList(1, 3)));

        List<Integer> list4 = (List<Integer>) Collections.takeWhile(lessThan3, list);
        assertTrue(list4.equals(Arrays.asList(1, 2)));

        List<Integer> list5 = (List<Integer>) Collections.takeUnless(lessThan3.not(), list);
        assertTrue(list5.equals(Arrays.asList(1, 2)));

        Integer x = Collections.foldl(minus, 0, list);
        assertEquals(x, (Integer) (-10));

        x = Collections.foldr(minus, 0, list);
        assertEquals(x, (Integer) (-2));
    }
}
