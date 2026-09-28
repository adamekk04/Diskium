package org.diskium.objects;

import org.bukkit.Chunk;
import org.bukkit.World;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RegionTest {

    @Test
    void storesWorldAndTracksChunks() {
        World world = fake(World.class, "world");
        Chunk firstChunk = fake(Chunk.class, "first chunk");
        Chunk secondChunk = fake(Chunk.class, "second chunk");
        Region region = new Region(2, -3, world, List.of(firstChunk, secondChunk));

        assertEquals(2, region.getX());
        assertEquals(-3, region.getZ());
        assertSame(world, region.getWorld());
        assertEquals(List.of(firstChunk, secondChunk), region.getChunks());
        assertTrue(region.canDeleteEntireRegion(Map.of(firstChunk, true, secondChunk, true)));
        assertFalse(region.canDeleteEntireRegion(Map.of(firstChunk, true)));
        assertFalse(region.canDeleteEntireRegion(Map.of(firstChunk, true, secondChunk, false)));
    }

    private static <T> T fake(Class<T> type, String name) {
        Object proxy = Proxy.newProxyInstance(
                type.getClassLoader(),
                new Class<?>[]{type},
                (instance, method, arguments) -> switch (method.getName()) {
                    case "equals" -> instance == arguments[0];
                    case "hashCode" -> System.identityHashCode(instance);
                    case "toString" -> name;
                    default -> throw new UnsupportedOperationException(method.getName());
                }
        );

        return type.cast(proxy);
    }
}
