package ru.spbau.mit;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by rebryk on 10/4/15.
 */

public class testCollections {
    private static final Predicate<Integer> LESS_THAN_3 = new Predicate<Integer>() {
        @Override
        public Boolean apply(Integer x) {
            return x < 3;
        }
    };

    private static final Predicate<Integer> ODD = new Predicate<Integer>() {
        @Override
        public Boolean apply(Integer x) {
            return x % 2 == 1;
        }
    };

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
    public void testMap() {
        List<Integer> list = Arrays.asList(1, 2, 3, 4);
        List<Integer> list2 = (List<Integer>) Collections.map(MULT3, list);
        assertTrue(list2.equals(Arrays.asList(3, 6, 9, 12)));
    }

    @Test
    public void testFilter() {
        List<Integer> list = Arrays.asList(1, 2, 3, 4);
        List<Integer> list2 = (List<Integer>) Collections.filter(ODD, list);
        assertTrue(list2.equals(Arrays.asList(1, 3)));
    }

    @Test
    public void testTakeWhile() {
        List<Integer> list = Arrays.asList(1, 2, 3, 4);
        List<Integer> list2 = (List<Integer>) Collections.takeWhile(LESS_THAN_3, list);
        assertTrue(list2.equals(Arrays.asList(1, 2)));
    }

    @Test
    public void testTakeUnless() {
        List<Integer> list = Arrays.asList(1, 2, 3, 4);
        List<Integer> list2 = (List<Integer>) Collections.takeUnless(LESS_THAN_3.not(), list);
        assertTrue(list2.equals(Arrays.asList(1, 2)));
    }

    @Test
    public void testFoldl() {
        List<Integer> list = Arrays.asList(1, 2, 3, 4);
        Integer x = Collections.foldl(MINUS, 0, list);
        assertEquals(x, (Integer) (-10));
    }

    @Test
    public void testFoldr() {
        List<Integer> list = Arrays.asList(1, 2, 3, 4);
        Integer x = Collections.foldr(MINUS, 0, list);
        assertEquals(x, (Integer) (-2));
    }
}
