package com.riiablo.table.schema;

import com.riiablo.codec.excel.Excel;

@Excel.Binned
public class UniqueSuffix extends Excel<UniqueSuffix.Entry> {
  public static class Entry extends Excel.Entry {
    @Override
    public String toString() {
      return Name;
    }

    @Key
    @Column
    public String  Name;
  }
}
