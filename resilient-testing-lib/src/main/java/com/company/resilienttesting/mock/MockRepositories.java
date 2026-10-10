package com.company.resilienttesting.mock;

import java.util.List;
import java.util.function.Function;
import org.mockito.Mockito;
import org.springframework.data.repository.CrudRepository;

/** A ready-made fake Spring Data repository that holds a list of sample items. */
public final class MockRepositories {

    private MockRepositories() {
        // helper class: not meant to be created
    }

    /**
     * Creates a fake repository where findAll() returns the items, count() returns how many there
     * are, and findById(id) returns the item whose id matches (or an empty result).
     *
     * @param idOf tells the helper how to read the id of an item, for example Widget::id
     */
    public static <T, ID, R extends CrudRepository<T, ID>> R withItems(
            Class<R> type, List<T> items, Function<T, ID> idOf) {
        R repository = Mockito.mock(type);
        Mockito.when(repository.findAll()).thenReturn(items);
        Mockito.when(repository.count()).thenReturn((long) items.size());
        Mockito.when(repository.findById(Mockito.any())).thenAnswer(invocation -> {
            ID wanted = invocation.getArgument(0);
            return items.stream()
                    .filter(item -> idOf.apply(item).equals(wanted))
                    .findFirst();
        });
        return repository;
    }
}
