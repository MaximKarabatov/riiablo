package com.riiablo.asset;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.*;
import org.junit.jupiter.params.provider.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.TestInstance.Lifecycle.*;

import io.netty.util.ReferenceCountUtil;
import io.netty.util.concurrent.Future;
import java.time.Duration;

import com.badlogic.gdx.files.FileHandle;

import com.riiablo.RiiabloTest;
import com.riiablo.asset.adapter.GdxFileHandleAdapter;
import com.riiablo.asset.adapter.MpqFileHandleAdapter;
import com.riiablo.asset.loader.DccLoader;
import com.riiablo.asset.param.DcParams;
import com.riiablo.asset.resolver.GdxFileHandleResolver;
import com.riiablo.file.Dc;
import com.riiablo.file.Dcc;
import com.riiablo.logger.Level;
import com.riiablo.logger.LogManager;
import com.riiablo.mpq_bytebuf.MpqFileHandle;
import com.riiablo.mpq_bytebuf.MpqFileResolver;

public class AssetManagerTest extends RiiabloTest {
  @BeforeAll
  public static void before() {
    LogManager.setLevel("com.riiablo.asset", Level.TRACE);
    LogManager.setLevel("com.riiablo.mpq_bytebuf.MpqFileResolver", Level.TRACE);
    LogManager.setLevel("com.riiablo.file", Level.TRACE);
  }

  @Test
  void construct_and_initialize() {
    AssetManager assets = new AssetManager();
    try {
      assets
          .resolver(GdxFileHandleResolver.Internal, 0)
          .resolver(new MpqFileResolver(), 1)
          .paramResolver(Dc.class, DcParams.class)
          .adapter(FileHandle.class, new GdxFileHandleAdapter())
          .adapter(MpqFileHandle.class, new MpqFileHandleAdapter())
          ;
    } finally {
      assets.dispose();
    }
  }

  @Nested
  @TestInstance(PER_CLASS)
  class resolve {
    AssetManager assets;

    @BeforeAll
    void beforeAll() {
      assets = new AssetManager()
          .resolver(GdxFileHandleResolver.Internal, 0)
          .resolver(new MpqFileResolver(), 1)
          .paramResolver(Dc.class, DcParams.class)
          .adapter(FileHandle.class, new GdxFileHandleAdapter())
          .adapter(MpqFileHandle.class, new MpqFileHandleAdapter())
          ;
    }

    @AfterAll
    void afterAll() {
      assets.dispose();
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "test/test.txt",
    })
    void resolve_internal(String path) {
      AssetDesc asset = AssetDesc.of(path, Object.class, new AssetParams<>());
      FileHandle handle = assets.resolve(asset);
      assertNotNull(handle);
      assertTrue(handle.exists());
      assertNotEquals(MpqFileHandle.class, handle.getClass());
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "data\\global\\CHARS\\BA\\LG\\BALGLITTNHTH.DCC",
    })
    void resolve_mpq(String path) {
      AssetDesc<Dcc> asset = AssetDesc.of(path, Dcc.class, DcParams.of(0));
      FileHandle handle = assets.resolve(asset);
      try {
        assertNotNull(handle);
        assertTrue(handle.exists());
        assertEquals(MpqFileHandle.class, handle.getClass());
      } finally {
        ReferenceCountUtil.release(handle);
      }
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "chars\\PA\\LA\\PALALITTN1HS.DCC",
    })
    void resolve_fail(String path) {
      AssetDesc asset = AssetDesc.of(path, Dc.class, DcParams.of(0));
      assertThrows(ResolverNotFound.class, () -> assets.resolve(asset));
    }
  }

  @Nested
  @TestInstance(PER_CLASS)
  class load {
    AssetManager assets;

    @BeforeAll
    void beforeAll() {
      assets = new AssetManager()
          .resolver(GdxFileHandleResolver.Internal, 0)
          .resolver(new MpqFileResolver(), 1)
          .paramResolver(Dc.class, DcParams.class)
          .adapter(FileHandle.class, new GdxFileHandleAdapter())
          .adapter(MpqFileHandle.class, new MpqFileHandleAdapter())
          .loader(Dcc.class, new DccLoader(null))
      ;
    }

    @AfterAll
    void afterAll() {
      assets.dispose();
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "test/test.txt",
    })
    void load_internal(String path) {
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "data\\global\\chars\\ba\\hd\\bahdbhma11hs.dcc",
        // "data\\global\\CHARS\\BA\\LG\\BALGLITTNHTH.DCC",
    })
    void load_mpq(String path) {
      AssetDesc<Dcc> asset = AssetDesc.of(path, Dcc.class, DcParams.of(0));
      Future<Dcc> handle = assets.load(asset);
      try {
        assertNotNull(handle);
        while (!assets.update()); // dir -1
        while (!assets.update()); // dir 1, throws NPE because headless LibGDX -> no Gdx.gl object
        assertTimeout(Duration.ofMillis(100), () -> {
          Dcc object = handle.get();
          assertNotNull(object);
        });
        handle.syncUninterruptibly();
        assertEquals(2, assets.loadedAssets.size);
        /** TODO: sync of direction throws NPE in {@link Dcc#uploadTextures(int)} */
      } finally {
        assets.unload(asset);
      }
    }

    @Disabled
    @ParameterizedTest
    @ValueSource(strings = {
        "data\\global\\chars\\ba\\hd\\bahdbhma11hs.dcc",
        // "data\\global\\CHARS\\BA\\LG\\BALGLITTNHTH.DCC",
    })
    void load_mpq0(String path) {
      AssetDesc<Dcc> asset = AssetDesc.of(path, Dcc.class, DcParams.of(-1));
      AssetDesc<Dcc> asset0 = AssetDesc.of(path, Dcc.class, DcParams.of(0));
      Future<Dcc> handle = assets.load(asset);
      try {
        assertNotNull(handle);
        while (!assets.update());
        assertTimeout(Duration.ofMillis(100), () -> {
          Dcc object = handle.get();
          assertNotNull(object);
        });
        handle.syncUninterruptibly();
        // assets.loadedAssets.put(asset, AssetContainer.wrap(asset, (Promise<?>) handle));
        assertEquals(1, assets.loadedAssets.size);
        Future<Dcc> handle0 = assets.load(asset0);
        while (!assets.update()); // throws NPE because headless LibGDX -> no Gdx.gl object
        assertTimeout(Duration.ofMillis(100), () -> {
          Dcc object = handle0.get();
          assertNotNull(object);
        });
        handle0.syncUninterruptibly();
        /** TODO: sync of direction throws NPE in {@link Dcc#uploadTextures(int)} */
      } finally {
        assets.unload(asset);
        assets.unload(asset0);
      }
    }
  }
}
