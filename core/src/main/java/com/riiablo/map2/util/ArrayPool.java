package com.riiablo.map2.util;

import java.lang.reflect.Array;
import java.util.Arrays;
import org.apache.commons.lang3.builder.ToStringBuilder;

import com.badlogic.gdx.utils.Pool;

public class ArrayPool<E> extends Pool<E> {
  static <E> E create(Class<E> clazz, int length) {
    return clazz.cast(Array.newInstance(clazz.getComponentType(), length));
  }

  public static <E> ArrayPool<E> get(Class<E> clazz, int length) {
    return get(clazz, length, 16, 16);
  }

  public static <E> ArrayPool<E> get(
      Class<E> clazz,
      int length,
      int initialCapacity,
      int maxCapacity
  ) {
    return new ArrayPool<>(clazz, length, initialCapacity, maxCapacity);
  }

  @Override
  protected void reset(E object) {
    if (object instanceof Object[]) Arrays.fill((Object[]) object, null);
  }

  final Class<E> clazz;
  final int length;

  ArrayPool(Class<E> clazz, int length, int initialCapacity, int maxCapacity) {
    super(initialCapacity, maxCapacity);
    this.clazz = clazz;
    this.length = length;
  }

  @Override
  protected E newObject() {
    return create(clazz, length);
  }

  @Override
  public String toString() {
    return new ToStringBuilder(this)
        .append("clazz", clazz.getSimpleName())
        .append("length", length)
        .append("max", max)
        .append("peak", peak)
        .toString();
  }
}
