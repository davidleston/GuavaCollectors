package com.google.common.collect;

public final class Combiners {
  public static <B extends ImmutableCollection.Builder<T>, T> B immutableArrayBasedBuilder(B left, B right) {
    @SuppressWarnings("unchecked")
    ImmutableCollection.ArrayBasedBuilder<T> leftBuilder = (ImmutableCollection.ArrayBasedBuilder<T>) left;
    @SuppressWarnings("unchecked")
    ImmutableCollection.ArrayBasedBuilder<T> rightBuilder = (ImmutableCollection.ArrayBasedBuilder<T>) right;
    int combinedSize = leftBuilder.size + rightBuilder.size;
    ensureCapacity(leftBuilder, combinedSize);
    System.arraycopy(rightBuilder.contents, 0, leftBuilder.contents, leftBuilder.size, rightBuilder.size);
    leftBuilder.size = combinedSize;
    return left;
  }

  public static <B extends ImmutableMap.Builder<K, V>, K, V> B immutableMapBuilder(B left, B right) {
    int combinedSize = left.size + right.size;
    ensureCapacity(left, combinedSize);
    System.arraycopy(right.entries, 0, left.entries, left.size, right.size);
    left.size = combinedSize;
    return left;
  }

  public static <B extends ImmutableMultimap.Builder<K, V>, K, V> B immutableMultimapBuilder(B left, B right) {
    left.builderMultimap.putAll(right.builderMultimap);
    return left;
  }

  public static <T, B extends ImmutableMultiset.Builder<T>> B immutableMultisetBuilder(B left, B right) {
    left.addAll(right.contents);
    return left;
  }

  /**
   * Expand the absolute capacity of the builder so it can accept at least
   * the specified number of elements without being resized.
   */
  private static void ensureCapacity(ImmutableCollection.ArrayBasedBuilder<?> builder, int combinedSize) {
    if (builder.contents.length < combinedSize) {
      builder.contents = ObjectArrays.arraysCopyOf(
          builder.contents, ImmutableCollection.Builder.expandedCapacity(builder.contents.length, combinedSize));
    }
  }

  private static <K, V> void ensureCapacity(ImmutableMap.Builder<K, V> builder, int combinedSize) {
    if (builder.entries.length < combinedSize) {
      builder.entries = ObjectArrays.arraysCopyOf(
          builder.entries, ImmutableCollection.Builder.expandedCapacity(builder.entries.length, combinedSize));
    }
  }
}
