package com.riiablo.entity;

import com.badlogic.gdx.math.MathUtils;
import com.riiablo.codec.D2S;

public class CharacterPreview extends Entity {
  final D2S d2s;

  public CharacterPreview(D2S d2s) {
    super(Type.PLR, "char-preview", Player.getToken(d2s.header.charClass), d2s.header.composites, d2s.header.colors);
    this.d2s = d2s;
    setMode(Player.MODE_TN);
    setWeapon(Entity.WEAPON_1HS);
    angle(-MathUtils.PI / 2);
  }
}
