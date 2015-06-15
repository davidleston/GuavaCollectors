package com.davidleston.stream;


import com.google.common.collect.*;

import java.util.Collections;
import java.util.Comparator;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

import static com.google.common.base.Preconditions.checkNotNull;

public final class GuavaCollectors {
  private GuavaCollectors() {
    throw new UnsupportedOperationException();
  }

  public static <K, V, R extends Multimap<K, V>> Collector<V, ?, R> multimap(
      Function<? super V, ? extends K> classifier, Supplier<R> multimapSupplier) {
    return multimap(classifier, Function.identity(), multimapSupplier);
  }

  public static <K, T, U, R extends Multimap<K, U>> Collector<T, ?, R> multimap(
      Function<? super T, ? extends K> keyMapper, Function<? super T, ? extends U> valueMapper, Supplier<R> multimapSupplier) {
    return Collector.of(
        multimapSupplier,
        (multimap, element) -> multimap.put(keyMapper.apply(element), valueMapper.apply(element)),
        (left, right) -> {
          left.putAll(right);
          return left;
        }
    );
  }

  public static <R, C, V, T extends Table<R, C, V>> Collector<V, ?, T> table(
      Supplier<T> tableSupplier,
      Function<? super V, ? extends R> rowKeyMapper,
      Function<? super V, ? extends C> columnKeyMapper) {
    return table(tableSupplier, rowKeyMapper, columnKeyMapper, Function.identity());
  }

  public static <R, C, V, T extends Table<R, C, V>, E> Collector<E, ?, T> table(
      Supplier<T> tableSupplier,
      Function<? super E, ? extends R> rowKeyMapper,
      Function<? super E, ? extends C> columnKeyMapper,
      Function<? super E, ? extends V> valueMapper) {
    return Collector.of(
        tableSupplier,
        (table, element) -> table.put(rowKeyMapper.apply(element), columnKeyMapper.apply(element), valueMapper.apply(element)),
        (left, right) -> {
          left.putAll(right);
          return left;
        }
    );
  }

  public static <T, R extends Multiset<T>> Collector<T, ?, R> multiset(Supplier<R> multisetSupplier) {
    return Collector.of(
        multisetSupplier,
        Multiset::add,
        (left, right) -> {
          left.addAll(right);
          return left;
        }
    );
  }

  public static <K, V> Collector<V, ?, ImmutableBiMap<K, V>> immutableBiMap(
      Function<? super V, ? extends K> classifier) {
    return immutableBiMap(classifier, Function.identity());
  }

  public static <K, T, U> Collector<T, ?, ImmutableBiMap<K, U>> immutableBiMap(
      Function<? super T, ? extends K> keyMapper, Function<? super T, ? extends U> valueMapper) {
    return Collector.of(
        ImmutableBiMap::<K, U>builder,
        (builder, element) -> builder.put(keyMapper.apply(element), valueMapper.apply(element)),
        Combiners::immutableMapBuilder,
        ImmutableBiMap.Builder::build
    );
  }

  public static <B, T extends B> Collector<Class<T>, ?, ImmutableClassToInstanceMap<B>>
  immutableClassToInstanceMapConsumingClasses(Function<Class<T>, T> instantiator) {
    return immutableClassToInstanceMap(Function.identity(), instantiator);
  }

  public static <B, T extends B> Collector<T, ?, ImmutableClassToInstanceMap<B>>
  immutableClassToInstanceMapConsumingInstances(Function<T, Class<T>> classifier) {
    return immutableClassToInstanceMap(classifier, Function.identity());
  }

  public static <B, T, U extends B> Collector<T, ?, ImmutableClassToInstanceMap<B>>
  immutableClassToInstanceMap(Function<T, Class<U>> classifier, Function<T, U> instantiator) {
    return Collector.of(
        ImmutableClassToInstanceMap::<B>builder,
        (builder, element) -> builder.put(classifier.apply(element), instantiator.apply(element)),
        (left, right) -> left.putAll(right.build()),
        ImmutableClassToInstanceMap.Builder::build
    );
  }

  public static <T> Collector<T, ?, ImmutableList<T>> immutableList() {
    return Collector.of(
        ImmutableList::<T>builder,
        ImmutableList.Builder::add,
        Combiners::immutableArrayBasedBuilder,
        ImmutableList.Builder::build
    );
  }

  public static <K, V> ImmutableMultimapCollector<K, V, V, ImmutableListMultimap<K, V>> immutableListMultimap(
      Function<? super V, ? extends K> classifier) {
    return immutableListMultimap(classifier, Function.identity());
  }

  public static <K, T, U> ImmutableMultimapCollector<K, T, U, ImmutableListMultimap<K, U>> immutableListMultimap(
      Function<? super T, ? extends K> keyMapper, Function<? super T, ? extends U> valueMapper) {
    return new ImmutableMultimapCollector<>(
        ImmutableListMultimap::<K, U>builder,
        keyMapper,
        valueMapper
    );
  }

  public static <K, V> Collector<V, ?, ImmutableMap<K, V>> immutableMap(
      Function<? super V, ? extends K> classifier) {
    return immutableMap(classifier, Function.identity());
  }

  public static <K, T, U> Collector<T, ?, ImmutableMap<K, U>> immutableMap(
      Function<? super T, ? extends K> keyMapper, Function<? super T, ? extends U> valueMapper) {
    return Collector.of(
        ImmutableMap::<K, U>builder,
        mapAccumulator(keyMapper, valueMapper),
        Combiners::immutableMapBuilder,
        ImmutableMap.Builder::build
    );
  }

  public static <K, V> ImmutableMultimapCollector<K, V, V, ImmutableMultimap<K, V>> immutableMultimap(
      Function<? super V, ? extends K> classifier) {
    return immutableMultimap(classifier, Function.identity());
  }

  public static <K, T, U> ImmutableMultimapCollector<K, T, U, ImmutableMultimap<K, U>> immutableMultimap(
      Function<? super T, ? extends K> keyMapper, Function<? super T, ? extends U> valueMapper) {
    return new ImmutableMultimapCollector<>(
        ImmutableMultimap::<K, U>builder,
        keyMapper,
        valueMapper
    );
  }

  public static <T> Collector<T, ?, ImmutableMultiset<T>> immutableMultiset() {
    return Collector.of(
        ImmutableMultiset::<T>builder,
        ImmutableMultiset.Builder::add,
        Combiners::immutableMultisetBuilder,
        ImmutableMultiset.Builder::build
    );
  }

  public static <K extends Comparable<K>, V> Collector<V, ?, ImmutableRangeMap<K, V>> immutableRangeMap(
      Function<? super V, Range<K>> classifier) {
    return Collector.of(
        ImmutableRangeMap::<K, V>builder,
        (builder, element) -> builder.put(classifier.apply(element), element),
        (left, right) -> left.putAll(right.build()),
        ImmutableRangeMap.Builder::build
    );
  }

  public static <T extends Comparable<T>> Collector<Range<T>, ?, ImmutableRangeSet<T>> immutableRangeSet() {
    return Collector.of(
        ImmutableRangeSet::<T>builder,
        ImmutableRangeSet.Builder::add,
        (left, right) -> left.addAll(right.build()),
        ImmutableRangeSet.Builder::build
    );
  }

  public static <T> Collector<T, ?, ImmutableSet<T>> immutableSet() {
    return Collector.of(
        ImmutableSet::<T>builder,
        ImmutableSet.Builder::add,
        Combiners::immutableArrayBasedBuilder,
        ImmutableSet.Builder::build
    );
  }

  public static <K, V> ImmutableMultimapCollector<K, V, V, ImmutableSetMultimap<K, V>> immutableSetMultimap(
      Function<? super V, ? extends K> classifier) {
    return immutableSetMultimap(classifier, Function.identity());
  }

  public static <K, T, U> ImmutableMultimapCollector<K, T, U, ImmutableSetMultimap<K, U>> immutableSetMultimap(
      Function<? super T, ? extends K> keyMapper, Function<? super T, ? extends U> valueMapper) {
    return new ImmutableMultimapCollector<>(
        ImmutableSetMultimap::<K, U>builder,
        keyMapper,
        valueMapper
    );
  }

  public static <K extends Comparable<K>, V> Collector<V, ?, ImmutableSortedMap<K, V>> immutableSortedMap(
      Function<? super V, ? extends K> classifier) {
    return immutableSortedMap(classifier, Function.identity());
  }

  public static <K extends Comparable<K>, T, U> Collector<T, ?, ImmutableSortedMap<K, U>> immutableSortedMap(
      Function<? super T, ? extends K> keyMapper, Function<? super T, ? extends U> valueMapper) {
    return Collector.of(
        ImmutableSortedMap::<K, U>naturalOrder,
        mapAccumulator(keyMapper, valueMapper),
        Combiners::immutableMapBuilder,
        ImmutableSortedMap.Builder::build
    );
  }

  public static <K, V> Collector<V, ?, ImmutableSortedMap<K, V>> immutableSortedMap(
      Function<? super V, ? extends K> classifier, Comparator<K> keyComparator) {
    return immutableSortedMap(classifier, Function.identity(), keyComparator);
  }

  public static <K, T, U> Collector<T, ?, ImmutableSortedMap<K, U>> immutableSortedMap(
      Function<? super T, ? extends K> keyMapper, Function<? super T, ? extends U> valueMapper, Comparator<K> keyComparator) {
    return Collector.of(
        () -> ImmutableSortedMap.orderedBy(keyComparator),
        mapAccumulator(keyMapper, valueMapper),
        Combiners::immutableMapBuilder,
        ImmutableSortedMap.Builder::build
    );
  }

  public static <T extends Comparable<T>> Collector<T, ?, ImmutableSortedMultiset<T>> immutableSortedMultiset() {
    return Collector.of(
        ImmutableSortedMultiset::<T>naturalOrder,
        ImmutableSortedMultiset.Builder::add,
        Combiners::immutableMultisetBuilder,
        ImmutableSortedMultiset.Builder::build
    );
  }

  public static <T> Collector<T, ?, ImmutableSortedMultiset<T>> immutableSortedMultiset(Comparator<T> comparator) {
    return Collector.of(
        () -> ImmutableSortedMultiset.orderedBy(comparator),
        ImmutableSortedMultiset.Builder::add,
        Combiners::immutableMultisetBuilder,
        ImmutableSortedMultiset.Builder::build
    );
  }

  public static <T extends Comparable<T>> Collector<T, ?, ImmutableSortedSet<T>> immutableSortedSet() {
    return Collector.of(
        ImmutableSortedSet::<T>naturalOrder,
        ImmutableSortedSet.Builder::add,
        Combiners::immutableArrayBasedBuilder,
        ImmutableSortedSet.Builder::build
    );
  }

  public static <T> Collector<T, ?, ImmutableSortedSet<T>> immutableSortedSet(Comparator<T> comparator) {
    return Collector.of(
        () -> ImmutableSortedSet.orderedBy(comparator),
        ImmutableSortedSet.Builder::add,
        Combiners::immutableArrayBasedBuilder,
        ImmutableSortedSet.Builder::build
    );
  }

  public static <R, C, V> ImmutableTableCollector<V, R, C, V> immutableTable(
      Function<? super V, ? extends R> rowKeyMapper, Function<? super V, ? extends C> columnKeyMapper) {
    return immutableTable(rowKeyMapper, columnKeyMapper, Function.identity());
  }

  public static <T, R, C, V> ImmutableTableCollector<T, R, C, V> immutableTable(
      Function<? super T, ? extends R> rowKeyMapper,
      Function<? super T, ? extends C> columnKeyMapper,
      Function<? super T, ? extends V> valueMapper) {
    return new ImmutableTableCollector<>(rowKeyMapper, columnKeyMapper, valueMapper);
  }

  private static <K, T, U, B extends ImmutableMap.Builder<K, U>> BiConsumer<B, T> mapAccumulator(
      Function<? super T, ? extends K> keyMapper, Function<? super T, ? extends U> valueMapper) {
    return (builder, element) -> builder.put(keyMapper.apply(element), valueMapper.apply(element));
  }

  public static final class ImmutableMultimapCollector<K, T, U, R extends ImmutableMultimap<K, U>>
      extends CollectorImpl<T, ImmutableMultimap.Builder<K, U>, R> {
    private Comparator<K> keyComparator;
    private Comparator<U> valueComparator;

    private ImmutableMultimapCollector(Supplier<ImmutableMultimap.Builder<K, U>> supplier,
                                       Function<? super T, ? extends K> keyMapper,
                                       Function<? super T, ? extends U> valueMapper) {
      super(
          supplier,
          (builder, element) -> builder.put(keyMapper.apply(element), valueMapper.apply(element)),
          Combiners::immutableMultimapBuilder
      );
    }

    public ImmutableMultimapCollector<K, T, U, R> orderKeysBy(Comparator<K> keyComparator) {
      this.keyComparator = checkNotNull(keyComparator);
      return this;
    }

    public ImmutableMultimapCollector<K, T, U, R> orderValuesBy(Comparator<U> valueComparator) {
      this.valueComparator = checkNotNull(valueComparator);
      return this;
    }

    @Override
    public Function<ImmutableMultimap.Builder<K, U>, R> finisher() {
      return builder -> {
        if (keyComparator != null) {
          builder.orderKeysBy(keyComparator);
        }
        if (valueComparator != null) {
          builder.orderValuesBy(valueComparator);
        }
        //noinspection unchecked
        return (R) builder.build();
      };
    }
  }

  public static final class ImmutableTableCollector<T, R, C, V>
      extends CollectorImpl<T, ImmutableTable.Builder<R, C, V>, ImmutableTable<R, C, V>> {
    private Comparator<R> rowComparator;
    private Comparator<C> columnComparator;

    private ImmutableTableCollector(
        Function<? super T, ? extends R> rowKeyMapper,
        Function<? super T, ? extends C> columnKeyMapper,
        Function<? super T, ? extends V> valueMapper) {
      super(
          ImmutableTable::<R, C, V>builder,
          (builder, element) -> builder.put(
              rowKeyMapper.apply(element),
              columnKeyMapper.apply(element),
              valueMapper.apply(element)
          ),
          (left, right) -> left.putAll(right.build())
      );
    }

    public ImmutableTableCollector<T, R, C, V> orderRowsBy(Comparator<R> rowComparator) {
      this.rowComparator = checkNotNull(rowComparator);
      return this;
    }

    public ImmutableTableCollector<T, R, C, V> orderColumnsBy(Comparator<C> columnComparator) {
      this.columnComparator = checkNotNull(columnComparator);
      return this;
    }

    @Override
    public Function<ImmutableTable.Builder<R, C, V>, ImmutableTable<R, C, V>> finisher() {
      return builder -> {
        if (rowComparator != null) {
          builder.orderRowsBy(rowComparator);
        }
        if (columnComparator != null) {
          builder.orderColumnsBy(columnComparator);
        }
        return builder.build();
      };
    }
  }

  /**
   * Simple implementation class for {@code Collector}.
   *
   * @param <T> the type of elements to be collected
   * @param <R> the type of the result
   */
  private abstract static class CollectorImpl<T, A, R> implements Collector<T, A, R> {
    private final Supplier<A> supplier;
    private final BiConsumer<A, T> accumulator;
    private final BinaryOperator<A> combiner;

    private CollectorImpl(Supplier<A> supplier,
                          BiConsumer<A, T> accumulator,
                          BinaryOperator<A> combiner) {
      this.supplier = supplier;
      this.accumulator = accumulator;
      this.combiner = combiner;
    }

    @Override
    public final BiConsumer<A, T> accumulator() {
      return accumulator;
    }

    @Override
    public final Supplier<A> supplier() {
      return supplier;
    }

    @Override
    public final BinaryOperator<A> combiner() {
      return combiner;
    }

    @Override
    public final Set<Characteristics> characteristics() {
      return Collections.emptySet();
    }
  }
}
