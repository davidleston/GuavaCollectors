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

  /**
   * @param classifier      the classifier function mapping input elements to keys
   * @param multimapFactory produces a new empty {@link Multimap} of the desired type
   * @param <K>             classification (key) of elements
   * @param <V>             element in the stream being collected
   * @param <R>             multimap
   * @return Collector that collects elements into a {@link Multimap}
   */
  public static <K, V, R extends Multimap<K, V>> Collector<V, ?, R> multimap(
      Function<? super V, ? extends K> classifier, Supplier<R> multimapFactory) {
    return multimap(classifier, Function.identity(), multimapFactory);
  }

  /**
   * @param keyMapper       a mapping function to produce keys
   * @param valueMapper     a mapping function to produce values
   * @param multimapFactory produces a new empty {@link Multimap} of the desired type
   * @param <K>             classification (key) of elements
   * @param <T>             element in the stream being collected
   * @param <U>             output of the value mapping function
   * @param <R>             multimap
   * @return Collector that collects elements into a {@link Multimap}
   */
  public static <K, T, U, R extends Multimap<K, U>> Collector<T, ?, R> multimap(
      Function<? super T, ? extends K> keyMapper,
      Function<? super T, ? extends U> valueMapper,
      Supplier<R> multimapFactory) {
    return Collector.of(
        multimapFactory,
        (multimap, element) -> multimap.put(keyMapper.apply(element), valueMapper.apply(element)),
        (left, right) -> {
          left.putAll(right);
          return left;
        }
    );
  }

  /**
   * @param tableFactory    produces a new empty {@link Table} of the desired type
   * @param rowKeyMapper    a mapping function to produce row keys
   * @param columnKeyMapper a mapping function to produce column keys
   * @param <R>             table row
   * @param <C>             table column
   * @param <V>             element in the stream and table value
   * @param <T>             table
   * @return Collector that collects elements into a {@link Table}
   */
  public static <R, C, V, T extends Table<R, C, V>> Collector<V, ?, T> table(
      Supplier<T> tableFactory,
      Function<? super V, ? extends R> rowKeyMapper,
      Function<? super V, ? extends C> columnKeyMapper) {
    return table(tableFactory, rowKeyMapper, columnKeyMapper, Function.identity());
  }

  /**
   * @param tableFactory    produces a new empty {@link Table} of the desired type
   * @param rowKeyMapper    a mapping function to produce row keys
   * @param columnKeyMapper a mapping function to produce column keys
   * @param valueMapper     a mapping function to produce table values
   * @param <R>             table row
   * @param <C>             table column
   * @param <V>             table value
   * @param <T>             table
   * @param <E>             element in the stream
   * @return Collector that collects elements into a {@link Table}
   */
  public static <R, C, V, T extends Table<R, C, V>, E> Collector<E, ?, T> table(
      Supplier<T> tableFactory,
      Function<? super E, ? extends R> rowKeyMapper,
      Function<? super E, ? extends C> columnKeyMapper,
      Function<? super E, ? extends V> valueMapper) {
    return Collector.of(
        tableFactory,
        (table, element) -> table
            .put(rowKeyMapper.apply(element), columnKeyMapper.apply(element), valueMapper.apply(element)),
        (left, right) -> {
          left.putAll(right);
          return left;
        }
    );
  }

  /**
   * @param multisetFactory produces a new empty {@link Multiset} of the desired type
   * @param <T>             element in the stream
   * @param <R>             multiset
   * @return Collector that collects elements into a {@link Multiset}
   */
  public static <T, R extends Multiset<T>> Collector<T, ?, R> multiset(Supplier<R> multisetFactory) {
    return Collector.of(
        multisetFactory,
        Multiset::add,
        (left, right) -> {
          left.addAll(right);
          return left;
        }
    );
  }

  /**
   * @param classifier the classifier function mapping input elements to keys
   * @param <K>        classification (key) of elements
   * @param <V>        element in the stream
   * @return Collector that collects elements into an {@link ImmutableBiMap}
   */
  public static <K, V> Collector<V, ?, ImmutableBiMap<K, V>> immutableBiMap(
      Function<? super V, ? extends K> classifier) {
    return immutableBiMap(classifier, Function.identity());
  }

  /**
   * @param keyMapper   a mapping function to produce keys
   * @param valueMapper a mapping function to produce values
   * @param <K>         classification (key) of elements
   * @param <T>         element in the stream being collected
   * @param <U>         output of the value mapping function
   * @return Collector that collects elements into an {@link ImmutableBiMap}
   */
  public static <K, T, U> Collector<T, ?, ImmutableBiMap<K, U>> immutableBiMap(
      Function<? super T, ? extends K> keyMapper, Function<? super T, ? extends U> valueMapper) {
    return Collector.of(
        ImmutableBiMap::<K, U>builder,
        (builder, element) -> builder.put(keyMapper.apply(element), valueMapper.apply(element)),
        Combiners::immutableMapBuilder,
        ImmutableBiMap.Builder::build
    );
  }

  /**
   * @param instanceProvider returns an instance of a class, possibly creating that instance
   * @param <B>              least-specific common type of all classes in the stream
   * @param <T>              least-specific common type of all classes in the stream
   * @return Collector that collects Classes into an {@link ImmutableClassToInstanceMap}
   */
  public static <B, T extends B> Collector<Class<T>, ?, ImmutableClassToInstanceMap<B>>
  immutableClassToInstanceMapConsumingClasses(Function<Class<T>, T> instanceProvider) {
    return immutableClassToInstanceMap(Function.identity(), instanceProvider);
  }

  /**
   * @param classifier returns the Class with which an object will be associated
   * @param <B>        least-specific common type of all classes in the stream
   * @param <T>        least-specific common type of all classes in the stream
   * @return Collector that collects objects into an {@link ImmutableClassToInstanceMap}
   */
  public static <B, T extends B> Collector<T, ?, ImmutableClassToInstanceMap<B>>
  immutableClassToInstanceMapConsumingInstances(Function<T, Class<T>> classifier) {
    return immutableClassToInstanceMap(classifier, Function.identity());
  }

  /**
   * @param classifier       returns the Class the element represents
   * @param instanceProvider returns an instance of the class returned by the classifier,
   *                         possibly creating that instance
   * @param <B>              least-specific common type of all classes in the stream
   * @param <T>              element in the stream
   * @param <U>              least-specific common type of all classes in the stream
   * @return Collector that converts elements into Classes and instances of those classes in an
   * {@link ImmutableClassToInstanceMap}
   */
  public static <B, T, U extends B> Collector<T, ?, ImmutableClassToInstanceMap<B>>
  immutableClassToInstanceMap(Function<T, Class<U>> classifier, Function<T, U> instanceProvider) {
    return Collector.of(
        ImmutableClassToInstanceMap::<B>builder,
        (builder, element) -> builder.put(classifier.apply(element), instanceProvider.apply(element)),
        (left, right) -> left.putAll(right.build()),
        ImmutableClassToInstanceMap.Builder::build
    );
  }

  /**
   * @param <T> element
   * @return Collector that collects elements into an {@link ImmutableList}
   */
  public static <T> Collector<T, ?, ImmutableList<T>> immutableList() {
    return Collector.of(
        ImmutableList::<T>builder,
        ImmutableList.Builder::add,
        Combiners::immutableArrayBasedBuilder,
        ImmutableList.Builder::build
    );
  }

  /**
   * @param classifier the classifier function mapping input elements to keys
   * @param <K>        classification (key) of elements
   * @param <V>        element in the stream being collected
   * @return Collector that collects elements into an {@link ImmutableListMultimap}
   */
  public static <K, V> ImmutableMultimapCollector<K, V, V, ImmutableListMultimap<K, V>> immutableListMultimap(
      Function<? super V, ? extends K> classifier) {
    return immutableListMultimap(classifier, Function.identity());
  }

  /**
   * @param keyMapper   a mapping function to produce keys
   * @param valueMapper a mapping function to produce values
   * @param <K>         classification (key) of elements
   * @param <T>         element in the stream being collected
   * @param <U>         output of the value mapping function
   * @return Collector that collects elements into an {@link ImmutableListMultimap}
   */
  public static <K, T, U> ImmutableMultimapCollector<K, T, U, ImmutableListMultimap<K, U>> immutableListMultimap(
      Function<? super T, ? extends K> keyMapper, Function<? super T, ? extends U> valueMapper) {
    return new ImmutableMultimapCollector<>(
        ImmutableListMultimap::<K, U>builder,
        keyMapper,
        valueMapper
    );
  }

  /**
   * @param classifier the classifier function mapping input elements to keys
   * @param <K>        classification (key) of elements
   * @param <V>        element in the stream being collected
   * @return Collector that collects elements into an {@link ImmutableMap}
   */
  public static <K, V> Collector<V, ?, ImmutableMap<K, V>> immutableMap(
      Function<? super V, ? extends K> classifier) {
    return immutableMap(classifier, Function.identity());
  }

  /**
   * @param keyMapper   a mapping function to produce keys
   * @param valueMapper a mapping function to produce values
   * @param <K>         classification (key) of elements
   * @param <T>         element in the stream being collected
   * @param <U>         output of the value mapping function
   * @return Collector that collects elements into an {@link ImmutableMap}
   */
  public static <K, T, U> Collector<T, ?, ImmutableMap<K, U>> immutableMap(
      Function<? super T, ? extends K> keyMapper, Function<? super T, ? extends U> valueMapper) {
    return Collector.of(
        ImmutableMap::<K, U>builder,
        mapAccumulator(keyMapper, valueMapper),
        Combiners::immutableMapBuilder,
        ImmutableMap.Builder::build
    );
  }

  /**
   * @param classifier the classifier function mapping input elements to keys
   * @param <K>        classification (key) of elements
   * @param <V>        element in the stream being collected
   * @return Collector that collects elements into an {@link ImmutableMultimap}
   */
  public static <K, V> ImmutableMultimapCollector<K, V, V, ImmutableMultimap<K, V>> immutableMultimap(
      Function<? super V, ? extends K> classifier) {
    return immutableMultimap(classifier, Function.identity());
  }

  /**
   * @param keyMapper   a mapping function to produce keys
   * @param valueMapper a mapping function to produce values
   * @param <K>         classification (key) of elements
   * @param <T>         element in the stream being collected
   * @param <U>         output of the value mapping function
   * @return Collector that collects elements into an {@link ImmutableMultimap}
   */
  public static <K, T, U> ImmutableMultimapCollector<K, T, U, ImmutableMultimap<K, U>> immutableMultimap(
      Function<? super T, ? extends K> keyMapper, Function<? super T, ? extends U> valueMapper) {
    return new ImmutableMultimapCollector<>(
        ImmutableMultimap::<K, U>builder,
        keyMapper,
        valueMapper
    );
  }

  /**
   * @param <T> element
   * @return Collector that collects elements into an {@link ImmutableMultiset}
   */
  public static <T> Collector<T, ?, ImmutableMultiset<T>> immutableMultiset() {
    return Collector.of(
        ImmutableMultiset::<T>builder,
        ImmutableMultiset.Builder::add,
        Combiners::immutableMultisetBuilder,
        ImmutableMultiset.Builder::build
    );
  }

  /**
   * @param rangeMapper a mapping function to produce ranges
   * @param valueMapper a mapping function to produce values
   * @param <K>         type ranges cover
   * @param <T>         element in stream
   * @param <U>         value
   * @return Collector that collects elements into an {@link ImmutableRangeMap}
   */
  public static <K extends Comparable<K>, T, U> Collector<T, ?, ImmutableRangeMap<K, U>> immutableRangeMap(
      Function<? super T, Range<K>> rangeMapper, Function<? super T, ? extends U> valueMapper) {
    return Collector.of(
        ImmutableRangeMap::<K, U>builder,
        (builder, element) -> builder.put(rangeMapper.apply(element), valueMapper.apply(element)),
        (left, right) -> left.putAll(right.build()),
        ImmutableRangeMap.Builder::build
    );
  }

  /**
   * @param <T> type ranges cover
   * @return Collector that collects ranges into {@link ImmutableRangeSet}
   */
  public static <T extends Comparable<T>> Collector<Range<T>, ?, ImmutableRangeSet<T>> immutableRangeSet() {
    return Collector.of(
        ImmutableRangeSet::<T>builder,
        ImmutableRangeSet.Builder::add,
        (left, right) -> left.addAll(right.build()),
        ImmutableRangeSet.Builder::build
    );
  }

  /**
   * @param <T> element in stream
   * @return Collector that collects elements into {@link ImmutableSet}
   */
  public static <T> Collector<T, ?, ImmutableSet<T>> immutableSet() {
    return Collector.of(
        ImmutableSet::<T>builder,
        ImmutableSet.Builder::add,
        Combiners::immutableArrayBasedBuilder,
        ImmutableSet.Builder::build
    );
  }

  /**
   * @param classifier the classifier function mapping input elements to keys
   * @param <K>        classification (key) of elements
   * @param <V>        element in the stream being collected
   * @return Collector that collects elements into an {@link ImmutableSetMultimap}
   */
  public static <K, V> ImmutableMultimapCollector<K, V, V, ImmutableSetMultimap<K, V>> immutableSetMultimap(
      Function<? super V, ? extends K> classifier) {
    return immutableSetMultimap(classifier, Function.identity());
  }

  /**
   * @param keyMapper   a mapping function to produce keys
   * @param valueMapper a mapping function to produce values
   * @param <K>         classification (key) of elements
   * @param <T>         element in the stream being collected
   * @param <U>         output of the value mapping function
   * @return Collector that collects elements into an {@link ImmutableMultimap}
   */
  public static <K, T, U> ImmutableMultimapCollector<K, T, U, ImmutableSetMultimap<K, U>> immutableSetMultimap(
      Function<? super T, ? extends K> keyMapper, Function<? super T, ? extends U> valueMapper) {
    return new ImmutableMultimapCollector<>(
        ImmutableSetMultimap::<K, U>builder,
        keyMapper,
        valueMapper
    );
  }

  /**
   * Sorts keys by natural order.
   *
   * @param classifier the classifier function mapping input elements to keys
   * @param <K>        classification (key) of elements
   * @param <V>        element in the stream being collected
   * @return Collector that collects elements into an {@link ImmutableSortedMap}
   */
  public static <K extends Comparable<K>, V> Collector<V, ?, ImmutableSortedMap<K, V>> immutableSortedMap(
      Function<? super V, ? extends K> classifier) {
    return immutableSortedMap(classifier, Function.identity());
  }

  /**
   * Sorts keys by natural order.
   *
   * @param keyMapper   a mapping function to produce keys
   * @param valueMapper a mapping function to produce values
   * @param <K>         classification (key) of elements
   * @param <T>         element in the stream being collected
   * @param <U>         output of the value mapping function
   * @return Collector that collects elements into an {@link ImmutableMap}
   */
  public static <K extends Comparable<K>, T, U> Collector<T, ?, ImmutableSortedMap<K, U>> immutableSortedMap(
      Function<? super T, ? extends K> keyMapper, Function<? super T, ? extends U> valueMapper) {
    return Collector.of(
        ImmutableSortedMap::<K, U>naturalOrder,
        mapAccumulator(keyMapper, valueMapper),
        Combiners::immutableMapBuilder,
        ImmutableSortedMap.Builder::build
    );
  }

  /**
   * @param classifier    the classifier function mapping input elements to keys
   * @param keyComparator to order keys
   * @param <K>           classification (key) of elements
   * @param <V>           element in the stream being collected
   * @return Collector that collects elements into an {@link ImmutableSortedMap}
   */
  public static <K, V> Collector<V, ?, ImmutableSortedMap<K, V>> immutableSortedMap(
      Function<? super V, ? extends K> classifier, Comparator<K> keyComparator) {
    return immutableSortedMap(classifier, Function.identity(), keyComparator);
  }

  /**
   * @param keyMapper     a mapping function to produce keys
   * @param valueMapper   a mapping function to produce values
   * @param keyComparator to order keys
   * @param <K>           classification (key) of elements
   * @param <T>           element in the stream being collected
   * @param <U>           output of the value mapping function
   * @return Collector that collects elements into an {@link ImmutableSortedMap}
   */
  public static <K, T, U> Collector<T, ?, ImmutableSortedMap<K, U>> immutableSortedMap(
      Function<? super T, ? extends K> keyMapper, Function<? super T, ? extends U> valueMapper, Comparator<K> keyComparator) {
    return Collector.of(
        () -> ImmutableSortedMap.orderedBy(keyComparator),
        mapAccumulator(keyMapper, valueMapper),
        Combiners::immutableMapBuilder,
        ImmutableSortedMap.Builder::build
    );
  }

  /**
   * Sorts by natural order.
   *
   * @param <T> element
   * @return Collector that collects elements into an {@link ImmutableSortedMultiset}
   */
  public static <T extends Comparable<T>> Collector<T, ?, ImmutableSortedMultiset<T>> immutableSortedMultiset() {
    return Collector.of(
        ImmutableSortedMultiset::<T>naturalOrder,
        ImmutableSortedMultiset.Builder::add,
        Combiners::immutableMultisetBuilder,
        ImmutableSortedMultiset.Builder::build
    );
  }

  /**
   * @param comparator to sort elements
   * @param <T>        element
   * @return Collector that collects elements into an {@link ImmutableSortedMultiset}
   */
  public static <T> Collector<T, ?, ImmutableSortedMultiset<T>> immutableSortedMultiset(Comparator<T> comparator) {
    return Collector.of(
        () -> ImmutableSortedMultiset.orderedBy(comparator),
        ImmutableSortedMultiset.Builder::add,
        Combiners::immutableMultisetBuilder,
        ImmutableSortedMultiset.Builder::build
    );
  }

  /**
   * Sorts by natural order.
   *
   * @param <T> element
   * @return Collector that collects elements into an {@link ImmutableSortedSet}
   */
  public static <T extends Comparable<T>> Collector<T, ?, ImmutableSortedSet<T>> immutableSortedSet() {
    return Collector.of(
        ImmutableSortedSet::<T>naturalOrder,
        ImmutableSortedSet.Builder::add,
        Combiners::immutableArrayBasedBuilder,
        ImmutableSortedSet.Builder::build
    );
  }

  /**
   * @param comparator to sort elements
   * @param <T>        element
   * @return Collector that collects elements into an {@link ImmutableSortedSet}
   */
  public static <T> Collector<T, ?, ImmutableSortedSet<T>> immutableSortedSet(Comparator<T> comparator) {
    return Collector.of(
        () -> ImmutableSortedSet.orderedBy(comparator),
        ImmutableSortedSet.Builder::add,
        Combiners::immutableArrayBasedBuilder,
        ImmutableSortedSet.Builder::build
    );
  }

  /**
   * @param rowKeyMapper    a mapping function to produce row keys
   * @param columnKeyMapper a mapping function to produce column keys
   * @param <R>             table row
   * @param <C>             table column
   * @param <V>             element in the stream and table value
   * @return Collector that collects elements into an {@link ImmutableTable}
   */
  public static <R, C, V> ImmutableTableCollector<V, R, C, V> immutableTable(
      Function<? super V, ? extends R> rowKeyMapper, Function<? super V, ? extends C> columnKeyMapper) {
    return immutableTable(rowKeyMapper, columnKeyMapper, Function.identity());
  }

  /**
   * @param rowKeyMapper    a mapping function to produce row keys
   * @param columnKeyMapper a mapping function to produce column keys
   * @param valueMapper     a mapping function to produce table values
   * @param <R>             table row
   * @param <C>             table column
   * @param <V>             table value
   * @param <T>             element in the stream
   * @return Collector that collects elements into a {@link Table}
   */
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

    /**
     * Specifies the ordering of the generated multimap's keys.
     * @param keyComparator to order keys
     * @return Collector that collects into an ImmutableMultimap
     */
    public ImmutableMultimapCollector<K, T, U, R> orderKeysBy(Comparator<K> keyComparator) {
      this.keyComparator = checkNotNull(keyComparator);
      return this;
    }

    /**
     * Specifies the ordering of the generated multimap's values for each key.
     * @param valueComparator to order values
     * @return Collector that collects into an ImmutableMultimap
     */
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

    /**
     * Specifies the ordering of the generated table's rows.
     * @param rowComparator to order rows
     * @return Collector that collects into an ImmutableTable
     */
    public ImmutableTableCollector<T, R, C, V> orderRowsBy(Comparator<R> rowComparator) {
      this.rowComparator = checkNotNull(rowComparator);
      return this;
    }

    /**
     * Specifies the ordering of the generated table's columns.
     * @param columnComparator to order columns
     * @return Collector that collects into an ImmutableTable
     */
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
   * Simple implementation class for {@link Collector}.
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
