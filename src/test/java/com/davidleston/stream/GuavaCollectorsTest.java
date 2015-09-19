package com.davidleston.stream;

import com.google.common.collect.*;
import org.junit.Test;

import java.util.Comparator;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

public class GuavaCollectorsTest {

  @Test
  public void multimap() {
    multimap(GuavaCollectors.multimap(String::length, ArrayListMultimap::create));
  }

  @Test
  public void table() {
    table(GuavaCollectors.table(HashBasedTable::create, Function.identity(), Function.identity()));
  }

  @Test
  public void multiset() {
    multiset(GuavaCollectors.multiset(HashMultiset::create));
  }

  @Test
  public void immutableBiMap() {
    ImmutableBiMap<Integer, String> map = Stream.of("a", "bb")
        .collect(GuavaCollectors.immutableBiMap(String::length));
    assertThat(map).containsOnlyKeys(1, 2);
  }

  @Test
  public void immutableClassToInstanceMapConsumingClasses() {
    ImmutableClassToInstanceMap<String> map = Stream.of(String.class)
        .collect(GuavaCollectors.immutableClassToInstanceMapConsumingClasses(clazz -> "a"));
    assertThat(map).containsOnlyKeys(String.class);
  }

  @Test
  public void immutableClassToInstanceMapConsumingInstances() {
    ImmutableClassToInstanceMap<String> map = Stream.of("a")
        .collect(GuavaCollectors.immutableClassToInstanceMapConsumingInstances(instance -> String.class));
    assertThat(map).containsOnlyKeys(String.class);
  }

  @Test
  public void immutableList() {
    ImmutableList<String> list = Stream.of("a", "b")
        .collect(GuavaCollectors.immutableList());
    assertThat(list).containsExactly("a", "b");
  }

  @Test
  public void immutableListMultimap() {
    multimap(GuavaCollectors.immutableListMultimap(String::length));
  }

  @Test
  public void immutableMap() {
    ImmutableMap<Integer, String> map = Stream.of("a")
        .collect(GuavaCollectors.immutableMap(String::length));
    assertThat(map).containsOnlyKeys(1);
    assertThat(map).containsValues("a");
  }

  @Test
  public void immutableMultimap() {
    multimap(GuavaCollectors.immutableMultimap(String::length));
  }

  @Test
  public void immutableMultimapSorting() {
    ImmutableMultimap<Integer, String> map = Stream.of("a", "b", "cc")
        .collect(GuavaCollectors
            .immutableMultimap(String::length)
            .orderKeysBy(Comparator.reverseOrder())
            .orderValuesBy(Comparator.reverseOrder())
        );
    assertThat(map.keySet()).containsExactly(2, 1);
    assertThat(map.get(1)).containsExactly("b", "a");
    assertThat(map.get(2)).containsExactly("cc");
  }

  @Test
  public void immutableMultiset() {
    multiset(GuavaCollectors.immutableMultiset());
  }

  @Test
  public void immutableRangeMap() {
    ImmutableRangeMap<String, String> map = Stream.of("a")
        .collect(GuavaCollectors.immutableRangeMap(s -> Range.open("a", "z"), Function.<String>identity()));
    assertThat(map.get("c")).isEqualTo("a");
  }

  @Test
  public void immutableRangeSet() {
    ImmutableRangeSet<String> set = Stream.of(Range.open("a", "z"))
        .collect(GuavaCollectors.immutableRangeSet());
    assertThat(set.contains("c")).isTrue();
  }

  @Test
  public void immutableSet() {
    ImmutableSet<String> set = Stream.of("a", "a")
        .collect(GuavaCollectors.immutableSet());
    assertThat(set).containsExactly("a");
  }

  @Test
  public void immutableSetMultimap() {
    multimap(GuavaCollectors.immutableSetMultimap(String::length));
  }

  @Test
  public void immutableSortedMap() {
    ImmutableSortedMap<Integer, String> map = Stream.of("a", "bb")
        .collect(GuavaCollectors.immutableSortedMap(String::length));
    assertThat(map.keySet()).containsExactly(1, 2);
  }

  @Test
  public void immutableSortedMapWithKeyComparator() {
    ImmutableSortedMap<Integer, String> map = Stream.of("a", "bb")
        .collect(GuavaCollectors.immutableSortedMap(String::length, Comparator.reverseOrder()));
    assertThat(map.keySet()).containsExactly(2, 1);
  }

  @Test
  public void immutableSortedMultiset() {
    ImmutableSortedMultiset<String> set = Stream.of("a", "b", "a")
        .collect(GuavaCollectors.immutableSortedMultiset());
    assertThat(set.count("a")).isEqualTo(2);
    assertThat(set).containsExactly("a", "a", "b");
  }

  @Test
  public void immutableSortedMultisetWithComparator() {
    ImmutableSortedMultiset<String> set = Stream.of("a", "b", "a")
        .collect(GuavaCollectors.immutableSortedMultiset(Comparator.<String>reverseOrder()));
    assertThat(set.count("a")).isEqualTo(2);
    assertThat(set).containsExactly("b", "a", "a");
  }

  @Test
  public void immutableSortedSet() {
    ImmutableSortedSet<String> set = Stream.of("b", "a", "b")
        .collect(GuavaCollectors.immutableSortedSet());
    assertThat(set).containsExactly("a", "b");
  }

  @Test
  public void immutableSortedSetWithComparator() {
    ImmutableSortedSet<String> set = Stream.of("b", "a", "b")
        .collect(GuavaCollectors.immutableSortedSet(Comparator.<String>reverseOrder()));
    assertThat(set).containsExactly("b", "a");
  }

  @Test
  public void immutableTable() {
    table(GuavaCollectors.immutableTable(Function.identity(), Function.identity()));
  }

  @Test
  public void immutableTableWithSorting() {
    ImmutableTable<String, String, String> table = Stream
        .of("a", "b")
        .collect(GuavaCollectors
                .<String, String, String>immutableTable(Function.identity(), Function.identity())
                .orderRowsBy(Comparator.reverseOrder())
                .orderColumnsBy(Comparator.reverseOrder())
        );
    assertThat(table.rowKeySet()).containsExactly("b", "a");
    assertThat(table.columnKeySet()).containsExactly("b", "a");
    assertThat(table.values()).containsExactly("b", "a");
  }

  private <R extends Multimap<Integer, String>> void multimap(Collector<String, ?, R> collector) {
    R multimap = Stream.of("a", "b", "cc")
        .collect(collector);
    assertThat(multimap.keys()).containsExactly(1, 1, 2);
  }

  private <R extends Table<String, String, String>> void table(Collector<String, ?, R> collector) {
    R table = Stream.of("a", "b")
        .collect(collector);
    assertThat(table.rowKeySet()).containsExactly("a", "b");
    assertThat(table.columnKeySet()).containsExactly("a", "b");
    assertThat(table.values()).containsExactly("a", "b");
  }

  private <R extends Multiset<String>> void multiset(Collector<String, ?, R> collector) {
    R multiset = Stream.of("a", "a")
        .collect(collector);
    assertThat(multiset.count("a")).isEqualTo(2);
  }
}