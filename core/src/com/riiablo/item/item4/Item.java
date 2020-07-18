package com.riiablo.item.item4;

import java.util.Arrays;
import org.apache.commons.lang3.builder.ToStringBuilder;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Array;

import com.riiablo.Riiablo;
import com.riiablo.codec.DC6;
import com.riiablo.codec.Index;
import com.riiablo.codec.excel.Armor;
import com.riiablo.codec.excel.ItemEntry;
import com.riiablo.codec.excel.ItemTypes;
import com.riiablo.codec.excel.MagicAffix;
import com.riiablo.codec.excel.Misc;
import com.riiablo.codec.excel.Weapons;
import com.riiablo.codec.util.BitStream;
import com.riiablo.item.Attributes;
import com.riiablo.item.BodyLoc;
import com.riiablo.item.Location;
import com.riiablo.item.LowQuality;
import com.riiablo.item.PropertyList;
import com.riiablo.item.Quality;
import com.riiablo.item.Stat;
import com.riiablo.item.StoreLoc;
import com.riiablo.item.Type;

public class Item {
  private static final String TAG = "Item";

  private static final boolean DEBUG = true;
  private static final boolean DEBUG_VERBOSE = DEBUG && true;

  private static final boolean SIMPLE_FLAGS = false;
  private static final boolean ONLY_KNOWN_FLAGS = SIMPLE_FLAGS && true;

  public static final int VERSION_100  = 0;
  public static final int VERSION_108  = 1;
  public static final int VERSION_110  = 2;
  public static final int VERSION_108e = 100;
  public static final int VERSION_110e = 101;
  public static final int VERSION_200e = 200; // Riiablo

  // TODO: Research the unconfirmed flags (prefixed with an extra '_')
  //       Copied from another project -- appears many flags are not in the save files
  //       It's probably safe to re-purpose unused flags for this project during runtime, but some
  //         of these may indicate flags that will be needed down the line.
  public static final int ITEMFLAG__RELOAD     = 0x00000001; // Note: Updates client side stats
  public static final int ITEMFLAG__BOUGHT     = 0x00000002;
  public static final int ITEMFLAG__CURSOR     = 0x00000004;
  public static final int ITEMFLAG__IGNORE     = 0x00000008; // Note: Tells client not to reset the cursor when the update packed is received
  public static final int ITEMFLAG_IDENTIFIED  = 0x00000010;
  public static final int ITEMFLAG__REMOVED    = 0x00000020;
  public static final int ITEMFLAG__ADDED      = 0x00000040;
  public static final int ITEMFLAG__TAKEN      = 0x00000080;
  public static final int ITEMFLAG_BROKEN      = 0x00000100;
  public static final int ITEMFLAG__RESTORED   = 0x00000200;
  public static final int ITEMFLAG__SORTED     = 0x00000400;
  public static final int ITEMFLAG_SOCKETED    = 0x00000800;
  public static final int ITEMFLAG__MONSTER    = 0x00001000;
  public static final int ITEMFLAG__NEW        = 0x00002000;
  public static final int ITEMFLAG__DISABLED   = 0x00004000;
  public static final int ITEMFLAG__HARDCORE   = 0x00008000;
  public static final int ITEMFLAG_BODYPART    = 0x00010000;
  public static final int ITEMFLAG_BEGINNER    = 0x00020000;
  public static final int ITEMFLAG__RESTRICT   = 0x00040000; // Note: Blocks RELOAD, i.e., mutex with RELOAD
  public static final int ITEMFLAG__SERVER     = 0x00080000;
  public static final int ITEMFLAG__1000000    = 0x00100000;
  public static final int ITEMFLAG_COMPACT     = 0x00200000;
  public static final int ITEMFLAG_ETHEREAL    = 0x00400000;
  public static final int ITEMFLAG__SAVED      = 0x00800000;
  public static final int ITEMFLAG_INSCRIBED   = 0x01000000;
  public static final int ITEMFLAG__CRUDE      = 0x02000000;
  public static final int ITEMFLAG_RUNEWORD    = 0x04000000;
  public static final int ITEMFLAG__MAGICAL    = 0x08000000;
  public static final int ITEMFLAG__STAFFMODS  = 0x10000000; // Note: New (Unconfirmed?)
  public static final int ITEMFLAG__CURSED     = 0x20000000; // Note: New (Unconfirmed?)
  public static final int ITEMFLAG__DROW       = 0x40000000; // Note: New (Unconfirmed?)
  public static final int ITEMFLAG__TAGGED     = 0x80000000; // Note: New (Unconfirmed?) Use depends on item type

  public static final int ITEMFLAG_SAVE_MASK   = 0xFFFFFFFF; // TODO: remove flags which should not be saved

  public static final int NO_PICTURE_ID = -1;
  public static final int NO_CLASS_ONLY = -1;

  static final int MAGIC_AFFIX_SIZE = 11;
  static final int MAGIC_AFFIX_MASK = 0x7FF;

  static final int RARE_AFFIX_SIZE  = 8;
  static final int RARE_AFFIX_MASK  = 0xFF;

  static final int SET_ID_SIZE      = 12;
  static final int UNIQUE_ID_SIZE   = 12;

  static final int MAGIC_PROPS = 0;
  static final int SET_PROPS   = 1;
  static final int RUNE_PROPS  = 6;
  static final int NUM_PROPS   = 7;

  static final int MAGIC_PROPS_FLAG = 1 << MAGIC_PROPS;
  static final int SET_2_PROPS_FLAG = 1 << SET_PROPS + 0;
  static final int SET_3_PROPS_FLAG = 1 << SET_PROPS + 1;
  static final int SET_4_PROPS_FLAG = 1 << SET_PROPS + 2;
  static final int SET_5_PROPS_FLAG = 1 << SET_PROPS + 3;
  static final int SET_6_PROPS_FLAG = 1 << SET_PROPS + 4;
  static final int RUNE_PROPS_FLAG  = 1 << RUNE_PROPS;

  static final int GEMPROPS_WEAPON  = 0;
  static final int GEMPROPS_ARMOR   = 1;
  static final int GEMPROPS_SHIELD  = 2;
  static final int NUM_GEMPROPS     = 3;

  static final PropertyList[] EMPTY_PROPERTY_ARRAY = new PropertyList[NUM_PROPS];

  static final Array<Item> EMPTY_SOCKETS_ARRAY = new Array<Item>(0) {
    @Override
    public void add(Item value) {
      throw new UnsupportedOperationException();
    }
  };

  // Basic fields
  public int         flags;
  public int         version;
  public Location    location;
  public BodyLoc     bodyLoc;
  public StoreLoc    storeLoc;
  public byte        gridX;
  public byte        gridY;
  public String      code;
  public int         socketsFilled;
  public Array<Item> sockets; // derived

  ItemEntry base;
  ItemTypes.Entry typeEntry;
  ItemTypes.Entry type2Entry;
  Type type;

  // Extended fields
  public int     id;
  public byte    ilvl;
  public Quality quality;
  public byte    pictureId;
  public short   classOnly;
  public int     qualityId;
  public Object  qualityData;
  public int     runewordData;
  public String  inscription;

  Attributes   attrs;
  PropertyList stats[];

  String name; // cache?
  Table header; // needed?

  // Cursor and 2d image stuff
  AssetDescriptor<DC6> invFileDescriptor;
  DC6 invFile;
  Index invColormap;
  int invColorIndex;
  Index charColormap;
  int charColorIndex;

  Item() {}

  void reset() {
    flags         = 0;
    version       = 0;
    location      = Location.STORED;
    bodyLoc       = BodyLoc.NONE;
    storeLoc      = StoreLoc.NONE;
    gridX         = 0;
    gridY         = 0;
    code          = "";
    socketsFilled = 0;
    sockets       = EMPTY_SOCKETS_ARRAY;

    base          = null;
    typeEntry     = null;
    type2Entry    = null;
    type          = null;

    id            = 0;
    ilvl          = 0;
    quality       = Quality.NONE;
    pictureId     = NO_PICTURE_ID;
    classOnly     = NO_CLASS_ONLY;
    qualityId     = 0;
    qualityData   = null;
    runewordData  = 0;
    inscription   = null;

    attrs = null;
    stats = EMPTY_PROPERTY_ARRAY;
  }

  void setBase(String code) {
    assert base == null : "setBase called on unrecycled Item?";
    this.code = code;
    base = ItemUtils.getBase(code);
    type = Type.get(
        typeEntry  = Riiablo.files.ItemTypes.get(base.type),
        type2Entry = Riiablo.files.ItemTypes.get(base.type2));

    this.attrs = new Attributes();
    PropertyList baseProps = attrs.base();
    baseProps.put(Stat.item_levelreq, base.levelreq);
    switch (getBaseType()) {
      case WEAPON: {
        Weapons.Entry weapon = getBase();
        baseProps.put(Stat.mindamage, weapon.mindam);
        baseProps.put(Stat.maxdamage, weapon.maxdam);
        baseProps.put(Stat.secondary_mindamage, weapon._2handmindam);
        baseProps.put(Stat.secondary_maxdamage, weapon._2handmaxdam);
        baseProps.put(Stat.item_throw_mindamage, weapon.minmisdam);
        baseProps.put(Stat.item_throw_maxdamage, weapon.maxmisdam);
        baseProps.put(Stat.reqstr, weapon.reqstr);
        baseProps.put(Stat.reqdex, weapon.reqdex);
        break;
      }
      case ARMOR: {
        Armor.Entry armor = getBase();
        baseProps.put(Stat.reqstr, armor.reqstr);
        baseProps.put(Stat.reqdex, 0);
        baseProps.put(Stat.toblock, armor.block); // FIXME: apply Riiablo.charData.getCharacterClass().entry().BlockFactor for view stats
        baseProps.put(Stat.mindamage, armor.mindam);
        baseProps.put(Stat.maxdamage, armor.maxdam);
        break;
      }
      case MISC: {
        Misc.Entry misc = getBase();
        break;
      }
      default: throw new AssertionError();
    }
    // TODO: copy base item stats
  }

  void setEar(int charClass, int charLevel, String charName) {
    setBase("ear");
    qualityId   = charClass;
    qualityData = charLevel;
    inscription = charName;
  }

  public boolean hasFlag(int flag) {
    return (flags & flag) == flag;
  }

  public boolean isIdentified() {
    return hasFlag(ITEMFLAG_IDENTIFIED);
  }

  public boolean isEthereal() {
    return hasFlag(ITEMFLAG_ETHEREAL);
  }

  @SuppressWarnings("unchecked")
  public <T extends ItemEntry> T getBase() {
    return (T) base;
  }

  public boolean isBase(Class type) {
    return base.getClass().isAssignableFrom(type);
  }

  enum ItemEntryType { WEAPON, ARMOR, MISC }
  public ItemEntryType getBaseType() {
    if (base instanceof Weapons.Entry) {
      return ItemEntryType.WEAPON;
    } else if (base instanceof Armor.Entry) {
      return ItemEntryType.ARMOR;
    } else {
      assert base instanceof Misc.Entry;
      return ItemEntryType.MISC;
    }
  }

  private String getFlagsString() {
    StringBuilder builder = new StringBuilder();
    if (ONLY_KNOWN_FLAGS && (flags & ITEMFLAG__RELOAD   ) == ITEMFLAG__RELOAD   ) builder.append("ITEMFLAG__RELOAD"   ).append('|');
    if (ONLY_KNOWN_FLAGS && (flags & ITEMFLAG__BOUGHT   ) == ITEMFLAG__BOUGHT   ) builder.append("ITEMFLAG__BOUGHT"   ).append('|');
    if (ONLY_KNOWN_FLAGS && (flags & ITEMFLAG__CURSOR   ) == ITEMFLAG__CURSOR   ) builder.append("ITEMFLAG__CURSOR"   ).append('|');
    if (ONLY_KNOWN_FLAGS && (flags & ITEMFLAG__IGNORE   ) == ITEMFLAG__IGNORE   ) builder.append("ITEMFLAG__IGNORE"   ).append('|');
    if ((flags & ITEMFLAG_IDENTIFIED) == ITEMFLAG_IDENTIFIED) builder.append("ITEMFLAG_IDENTIFIED").append('|');
    if (ONLY_KNOWN_FLAGS && (flags & ITEMFLAG__REMOVED  ) == ITEMFLAG__REMOVED  ) builder.append("ITEMFLAG__REMOVED"  ).append('|');
    if (ONLY_KNOWN_FLAGS && (flags & ITEMFLAG__ADDED    ) == ITEMFLAG__ADDED    ) builder.append("ITEMFLAG__ADDED"    ).append('|');
    if (ONLY_KNOWN_FLAGS && (flags & ITEMFLAG__TAKEN    ) == ITEMFLAG__TAKEN    ) builder.append("ITEMFLAG__TAKEN"    ).append('|');
    if ((flags & ITEMFLAG_BROKEN    ) == ITEMFLAG_BROKEN    ) builder.append("ITEMFLAG_BROKEN"    ).append('|');
    if (ONLY_KNOWN_FLAGS && (flags & ITEMFLAG__RESTORED ) == ITEMFLAG__RESTORED ) builder.append("ITEMFLAG__RESTORED" ).append('|');
    if (ONLY_KNOWN_FLAGS && (flags & ITEMFLAG__SORTED   ) == ITEMFLAG__SORTED   ) builder.append("ITEMFLAG__SORTED"   ).append('|');
    if ((flags & ITEMFLAG_SOCKETED  ) == ITEMFLAG_SOCKETED  ) builder.append("ITEMFLAG_SOCKETED"  ).append('|');
    if (ONLY_KNOWN_FLAGS && (flags & ITEMFLAG__MONSTER  ) == ITEMFLAG__MONSTER  ) builder.append("ITEMFLAG__MONSTER"  ).append('|');
    if (ONLY_KNOWN_FLAGS && (flags & ITEMFLAG__NEW      ) == ITEMFLAG__NEW      ) builder.append("ITEMFLAG__NEW"      ).append('|');
    if (ONLY_KNOWN_FLAGS && (flags & ITEMFLAG__DISABLED ) == ITEMFLAG__DISABLED ) builder.append("ITEMFLAG__DISABLED" ).append('|');
    if (ONLY_KNOWN_FLAGS && (flags & ITEMFLAG__HARDCORE ) == ITEMFLAG__HARDCORE ) builder.append("ITEMFLAG__HARDCORE" ).append('|');
    if ((flags & ITEMFLAG_BODYPART  ) == ITEMFLAG_BODYPART  ) builder.append("ITEMFLAG_BODYPART"  ).append('|');
    if ((flags & ITEMFLAG_BEGINNER  ) == ITEMFLAG_BEGINNER  ) builder.append("ITEMFLAG_BEGINNER"  ).append('|');
    if (ONLY_KNOWN_FLAGS && (flags & ITEMFLAG__RESTRICT ) == ITEMFLAG__RESTRICT ) builder.append("ITEMFLAG__RESTRICT" ).append('|');
    if (ONLY_KNOWN_FLAGS && (flags & ITEMFLAG__SERVER   ) == ITEMFLAG__SERVER   ) builder.append("ITEMFLAG__SERVER"   ).append('|');
    if (ONLY_KNOWN_FLAGS && (flags & ITEMFLAG__1000000  ) == ITEMFLAG__1000000  ) builder.append("ITEMFLAG__1000000"  ).append('|');
    if ((flags & ITEMFLAG_COMPACT   ) == ITEMFLAG_COMPACT   ) builder.append("ITEMFLAG_COMPACT"   ).append('|');
    if ((flags & ITEMFLAG_ETHEREAL  ) == ITEMFLAG_ETHEREAL  ) builder.append("ITEMFLAG_ETHEREAL"  ).append('|');
    if (ONLY_KNOWN_FLAGS && (flags & ITEMFLAG__SAVED    ) == ITEMFLAG__SAVED    ) builder.append("ITEMFLAG__SAVED"    ).append('|');
    if ((flags & ITEMFLAG_INSCRIBED ) == ITEMFLAG_INSCRIBED ) builder.append("ITEMFLAG_INSCRIBED" ).append('|');
    if (ONLY_KNOWN_FLAGS && (flags & ITEMFLAG__CRUDE    ) == ITEMFLAG__CRUDE    ) builder.append("ITEMFLAG__CRUDE"    ).append('|');
    if ((flags & ITEMFLAG_RUNEWORD  ) == ITEMFLAG_RUNEWORD  ) builder.append("ITEMFLAG_RUNEWORD"  ).append('|');
    if (ONLY_KNOWN_FLAGS && (flags & ITEMFLAG__MAGICAL  ) == ITEMFLAG__MAGICAL  ) builder.append("ITEMFLAG__MAGICAL"  ).append('|');
    if (ONLY_KNOWN_FLAGS && (flags & ITEMFLAG__STAFFMODS) == ITEMFLAG__STAFFMODS) builder.append("ITEMFLAG__STAFFMODS").append('|');
    if (ONLY_KNOWN_FLAGS && (flags & ITEMFLAG__CURSED   ) == ITEMFLAG__CURSED   ) builder.append("ITEMFLAG__CURSED"   ).append('|');
    if (ONLY_KNOWN_FLAGS && (flags & ITEMFLAG__DROW     ) == ITEMFLAG__DROW     ) builder.append("ITEMFLAG__DROW"     ).append('|');
    if (ONLY_KNOWN_FLAGS && (flags & ITEMFLAG__TAGGED   ) == ITEMFLAG__TAGGED   ) builder.append("ITEMFLAG__TAGGED"   ).append('|');
    if (builder.length() > 0) builder.setLength(builder.length() - 1);
    return builder.toString();
  }

  public String getNameString() {
    if (name == null) updateName();
    return name;
  }

  private void updateName() {
    StringBuilder name = new StringBuilder();
    int prefix, suffix;
    MagicAffix affix;
    switch (quality) {
      case LOW:
      case NORMAL:
      case HIGH:
        if ((flags & ITEMFLAG_RUNEWORD) == ITEMFLAG_RUNEWORD) {
          int runeword = RunewordData.id(runewordData);
          name.append(Riiablo.string.lookup(Riiablo.files.Runes.get(runeword).Name));
          break;
        } else if (socketsFilled > 0) {
          name.append(Riiablo.string.lookup(1728)) // Gemmed
              .append(' ')
              .append(Riiablo.string.lookup(base.namestr));
          break;
        }

        switch (quality) {
          case LOW:
            name.append(Riiablo.string.lookup(LowQuality.valueOf(qualityId).stringId))
                .append(' ')
                .append(Riiablo.string.lookup(base.namestr));
            break;

          case HIGH:
            name.append(Riiablo.string.lookup(1727)) // Superior
                .append(' ')
                .append(Riiablo.string.lookup(base.namestr));
            break;

          default:
            name.append(Riiablo.string.lookup(base.namestr));
        }
        break;

      case MAGIC:
        prefix = qualityId &   MAGIC_AFFIX_MASK;
        suffix = qualityId >>> MAGIC_AFFIX_SIZE;
        if ((affix = Riiablo.files.MagicPrefix.get(prefix)) != null) name.append(Riiablo.string.lookup(affix.name)).append(' ');
        name.append(Riiablo.string.lookup(base.namestr));
        if ((affix = Riiablo.files.MagicSuffix.get(suffix)) != null) name.append(' ').append(Riiablo.string.lookup(affix.name));
        break;

      case RARE:
      case CRAFTED:
        prefix = qualityId &   RARE_AFFIX_MASK;
        suffix = qualityId >>> RARE_AFFIX_SIZE;
        name.append(Riiablo.string.lookup(Riiablo.files.RarePrefix.get(prefix).name))
            .append(' ')
            .append(Riiablo.string.lookup(Riiablo.files.RareSuffix.get(suffix).name));
        break;

      case SET:
        if (qualityId != (1 << SET_ID_SIZE) - 1) {
          name.append(Riiablo.string.lookup(Riiablo.files.SetItems.get(qualityId).index));
        } else {
          name.append(Riiablo.string.lookup(base.namestr));
        }
        break;

      case UNIQUE:
        if (qualityId != (1 << UNIQUE_ID_SIZE) - 1) {
          name.append(Riiablo.string.lookup(Riiablo.files.UniqueItems.get(qualityId).index));
        } else {
          name.append(Riiablo.string.lookup(base.namestr));
        }
        break;

      default:
        name.append(Riiablo.string.lookup(base.namestr));
    }

    this.name = name.toString();
  }

  @Override
  public String toString() {
    ToStringBuilder builder = new ToStringBuilder(this)
        .append("name", getNameString())
        .append("code", code)
        .append("flags", SIMPLE_FLAGS ? getFlagsString() : String.format("0x%08x", flags))
        .append("version", version);
    if (DEBUG_VERBOSE) {
      builder
          .append("location", location)
          .append("bodyLoc", bodyLoc)
          .append("storeLoc", storeLoc)
          .append("gridX", gridX)
          .append("gridY", gridY)
          ;
    } else {
      builder.append("location", location);
      switch (location) {
        case EQUIPPED:
          builder.append("bodyLoc", bodyLoc);
          break;

        case STORED:
          builder
              .append("storeLoc", storeLoc)
              .append("gridX", gridX)
              .append("gridY", gridY);
          break;

        case BELT:
          builder.append("gridX", gridX);
          break;

        default:
          // ignored
      }

      if ((flags & ITEMFLAG_COMPACT) == 0) {
        builder
            .append("id", String.format("0x%08X", (int) id))
            .append("ilvl", ilvl)
            .append("quality", quality);
        if (pictureId >= 0) builder.append("pictureId", pictureId);
        if (classOnly >= 0) builder.append("classOnly", String.format("0x%04X", classOnly));
        switch (quality) {
          case LOW:
            builder.append("qualityId", LowQuality.valueOf(qualityId));
            break;

          case NORMAL:
            break;

          case HIGH:
            builder.append("qualityId", Riiablo.files.QualityItems.get((int) qualityId));
            break;

          case MAGIC:
            builder.append("qualityId", String.format("0x%06X", qualityId));
            break;

          case RARE:
          case CRAFTED:
            builder
                .append("qualityId", String.format("0x%02X", qualityId))
                .append("affixes", qualityData);
            break;

          case SET:
          case UNIQUE:
          default:
            builder.append("qualityId", qualityId);
        }

        if ((flags & ITEMFLAG_RUNEWORD) == ITEMFLAG_RUNEWORD) {
          builder.append("runewordData", String.format("[id=%d, extra=%d]",
              RunewordData.id(runewordData), RunewordData.extra(runewordData)));
        }

        if ((flags & ITEMFLAG_INSCRIBED) == ITEMFLAG_INSCRIBED) {
          builder.append("inscription", inscription);
        }

        if ((flags & ITEMFLAG_SOCKETED) == ITEMFLAG_SOCKETED && socketsFilled > 0) {
          builder.append("sockets", sockets);
        }

        builder.append("attrs", Arrays.toString(stats));
      }
    }
    return builder.build();
  }

  static class RareQualityData {
    static final int NUM_AFFIXES = 3;
    int[] prefixes, suffixes;
    RareQualityData(BitStream bitStream) {
      prefixes = new int[NUM_AFFIXES];
      suffixes = new int[NUM_AFFIXES];
      for (int i = 0; i < NUM_AFFIXES; i++) {
        prefixes[i] = bitStream.readBoolean() ? bitStream.readUnsigned15OrLess(MAGIC_AFFIX_SIZE) : 0;
        suffixes[i] = bitStream.readBoolean() ? bitStream.readUnsigned15OrLess(MAGIC_AFFIX_SIZE) : 0;
      }
    }

    @Override
    public String toString() {
      return new ToStringBuilder(this)
          .append("prefixes", prefixes)
          .append("suffixes", suffixes)
          .build();
    }
  }

  static class RunewordData {
    static final int RUNEWORD_ID_SHIFT    = 0;
    static final int RUNEWORD_ID_MASK     = 0xFFF << RUNEWORD_ID_SHIFT;
    static final int RUNEWORD_EXTRA_SHIFT = 12;
    static final int RUNEWORD_EXTRA_MASK  = 0xF << RUNEWORD_EXTRA_SHIFT;

    static int id(int pack) {
      return (pack & RUNEWORD_ID_MASK) >>> RUNEWORD_ID_SHIFT;
    }

    static int extra(int pack) {
      return (pack & RUNEWORD_EXTRA_MASK) >>> RUNEWORD_EXTRA_SHIFT;
    }
  }
}
