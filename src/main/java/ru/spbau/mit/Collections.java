package ru.spbau.mit;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by rebryk on 9/29/15.
 */

public abstract class Collections {
    public static <A, C> Iterable<C> map(final Function1<? super A, ? extends C> function, final Iterable<A> data) {
        List<C> list = new ArrayList<C>();
        for (A element : data) {
            list.add(function.apply(element));
        }
        return list;
    }

    public static <A> Iterable<A> filter(final Predicate<? super A> predicate, final Iterable<A> data) {
        List<A> list = new ArrayList<A>();
        for (A element : data) {
            if (predicate.apply(element)) {
                list.add(element);
            }
        }
        return list;
    }

    public static <A> Iterable<A> takeWhile(final Predicate<? super A> predicate, final Iterable<A> data) {
        List<A> list = new ArrayList<A>();
        for (A element : data) {
            if (predicate.apply(element)) {
                list.add(element);
            } else {
                break;
            }
        }
        return list;
    }

    public static <A> Iterable<A> takeUnless(final Predicate<? super A> predicate, final Iterable<A> data) {
        return takeWhile(predicate.not(), data);
    }

    public static <A, B> B foldl(final Function2<? super B, ? super A, ? extends B> function, B value, final Iterable<A> data) {
        for (A element: data) {
            value = function.apply(value, element);
        }
        return value;
    }

    private static <A, B> B foldr(final Function2<? super A, ? super B, ? extends B> function, B value, final Iterator<A> it) {
        if (it.hasNext()) {
            return function.apply(it.next(), foldr(function, value, it));
        } else {
            return value;
        }
    }

    public static <A, B> B foldr(final Function2<? super A, ? super B, ? extends B> function, B value, final Iterable<A> data) {
        return foldr(function, value, data.iterator());
    }
}
