package com.company.resilienttesting.mock;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.data.repository.CrudRepository;

class MockRepositoriesTest {

    /** A made-up item and repository, only used to test the helper. */
    record Widget(Long id, String name) {
    }

    interface WidgetRepository extends CrudRepository<Widget, Long> {
    }

    private final List<Widget> widgets = List.of(new Widget(1L, "first"), new Widget(2L, "second"));

    private final WidgetRepository repository =
            MockRepositories.withItems(WidgetRepository.class, widgets, Widget::id);

    @Test
    void findAll_returnsAllTheItems() {
        assertEquals(widgets, repository.findAll());
    }

    @Test
    void count_returnsHowManyItemsThereAre() {
        assertEquals(2L, repository.count());
    }

    @Test
    void findById_returnsTheMatchingItem() {
        assertEquals(Optional.of(widgets.get(1)), repository.findById(2L));
    }

    @Test
    void findById_returnsNothingForAnUnknownId() {
        assertTrue(repository.findById(99L).isEmpty());
    }
}
