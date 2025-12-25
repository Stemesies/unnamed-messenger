package utils.extensions;

import utils.kt.CheckIf;

import java.util.Collection;

public class CollectionExt {

    public static <T> T findBy(Collection<T> collection, CheckIf<T> filter) {
        for (T element : collection) {
            if (filter.check(element))
                return element;
        }
        return null;
    }
}
