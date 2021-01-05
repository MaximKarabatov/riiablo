package com.riiablo.table;

import java.util.Iterator;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntMap;
import com.badlogic.gdx.utils.ObjectIntMap;

/**
 * Stores a table of records.
 *
 * @param <R> record type
 */
public abstract class Table<R> implements Iterable<R> {
  public static final int DEFAULT_INITIAL_CAPACITY = 53;
  public static final float DEFAULT_LOAD_FACTOR = 0.8f;

  protected final Class<R> recordClass;
  protected ObjectIntMap<String> lookup;
  protected IntMap<R> records;
  protected Array<R> ordered;

  protected Injector<R, ?> injector;
  protected Parser<R> parser;

  protected Table(Class<R> recordClass) {
    this(recordClass, DEFAULT_INITIAL_CAPACITY, DEFAULT_LOAD_FACTOR);
  }

  protected Table(Class<R> recordClass, int initialCapacity) {
    this(recordClass, initialCapacity, DEFAULT_LOAD_FACTOR);
  }

  protected Table(Class<R> recordClass, int initialCapacity, float loadFactor) {
    this(recordClass, initialCapacity, loadFactor, false);
  }

  protected Table(Class<R> recordClass, int initialCapacity, float loadFactor, boolean stringLookup) {
    this.recordClass = recordClass;
    records = new IntMap<>(initialCapacity, loadFactor);
    ordered = new Array<>(true, (int) (initialCapacity * loadFactor), recordClass);
    lookup = stringLookup
        ? new ObjectIntMap<String>(initialCapacity, loadFactor)
        : null;
  }

  protected abstract R newRecord();
  protected abstract Parser<R> newParser(ParserInput parser);
  protected abstract Serializer<R> newSerializer();

  protected Injector<R, ?> newInjector() {
    return null;
  }

  public Class<R> recordClass() {
    return recordClass;
  }

  @Override
  public Iterator<R> iterator() {
    return ordered.iterator();
  }

  /**
   * Called when this table has been constructed and initialized. Used to
   * set-up table-specific configurations.
   */
  protected void initialize() {}

  /**
   * Assigns a record to a specified id. Implementations can override this and
   * call this super function to re-map indexes.
   */
  protected void put(int id, R record) {
    records.put(id, record);
    ordered.add(record);
    if (lookup != null) {
      // if put is overridden in subclass to remap id, this will not work
      // this operation asserts that no remapping occurs
      lookup.put(parser.recordName(id), id);
    }
  }

  protected int offset() {
    return 0;
  }

  protected boolean indexed() {
    return false;
  }

  protected boolean preload() {
    return false;
  }

  protected String primaryKey() {
    return null;
  }

  protected R inject(R record) {
    if (injector == null) injector = newInjector();
    if (injector != null) return injector.inject(null, record);
    return record;
  }

  protected Parser<R> parser() {
    return parser;
  }

  /**
   * Initializes this table for loading records via a {@link ParserInput parser}.
   */
  protected final void initialize(ParserInput in) {
    if (parser != null) throw new IllegalStateException("parser already set");
    this.parser = newParser(in).parseFields();
    if (preload()) {
      for (int i = 0, s = in.numRecords(); i < s; i++) {
        put(i, parseRecord(i));
      }
    }
  }

  private R parseRecord(int recordId) {
    final R record = newRecord();
    parser.parseRecord(recordId, record);
    inject(record);
    return record;
  }

  public R get(int id) {
    R record = records.get(id);
    if (record == null && !preload() && parser != null) {
      if (id < 0 || id >= parser.parser().numRecords()) return null;
      put(id, record = parseRecord(id));
    }

    return record;
  }

  public int index(String id) {
    return lookup.get(toUpper(id), -1);
  }

  public R get(String id) {
    return get(lookup.get(toUpper(id), -1));
  }

  private String toUpper(String id) {
    return id == null ? null : id.toUpperCase();
  }

  public int size() {
    return records.size;
  }
}
