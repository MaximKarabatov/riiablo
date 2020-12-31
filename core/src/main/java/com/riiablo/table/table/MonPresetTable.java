// automatically generated by TableCodeGenerator, do not modify
package com.riiablo.table.table;

import javax.annotation.Generated;

import com.badlogic.gdx.utils.IntArray;

import com.riiablo.Riiablo;
import com.riiablo.logger.LogManager;
import com.riiablo.logger.Logger;
import com.riiablo.table.Parser;
import com.riiablo.table.ParserInput;
import com.riiablo.table.Serializer;
import com.riiablo.table.Table;
import com.riiablo.table.parser.MonPresetParser;
import com.riiablo.table.schema.MonPreset;
import com.riiablo.table.serializer.MonPresetSerializer;

@Generated(
    value = "com.riiablo.table.annotation.TableCodeGenerator",
    date = "2020-12-30T23:38:56-08:00",
    comments = "com.riiablo.table.schema.MonPreset"
)
public final class MonPresetTable extends Table<MonPreset> {
  private static final Logger log = LogManager.getLogger(MonPresetTable.class);

  public MonPresetTable() {
    super(MonPreset.class, 53, 0.8f);
  }

  @Override
  protected MonPreset newRecord() {
    return new MonPreset();
  }

  @Override
  protected Parser<MonPreset> newParser(ParserInput arg0) {
    return new MonPresetParser(arg0);
  }

  @Override
  protected Serializer<MonPreset> newSerializer() {
    return new MonPresetSerializer();
  }

  @Override
  protected int offset() {
    return 0;
  }

  @Override
  protected boolean indexed() {
    return true;
  }

  @Override
  protected boolean preload() {
    return true;
  }

  @Override
  protected String primaryKey() {
    return "Act";
  }

  private static final int NUM_ACTS = Riiablo.NUM_ACTS;
  private static final int INITIAL_ENTRIES = 60;

  /** stores act-specific list of mon place codes */
  private static final IntArray[] lookup = new IntArray[NUM_ACTS + 1]; {
    for (int act = 1; act <= NUM_ACTS; act++) {
      lookup[act] = new IntArray(INITIAL_ENTRIES);
    }
  }

  @Override
  protected void put(int id, MonPreset record) {
    super.put(id, record);
    final IntArray lookup = this.lookup[record.Act];
    lookup.add(id);
    log.trace("lookup[{}][{}] = {} ({})", record.Act, lookup.size - 1, record, id);
  }

  public MonPreset get(int act, int id) {
    return get(lookup[act].get(id));
  }

  public String getPlace(int act, int id) {
    return get(act, id).Place;
  }

  public int getSize(int act) {
    return lookup[act].size;
  }
}
