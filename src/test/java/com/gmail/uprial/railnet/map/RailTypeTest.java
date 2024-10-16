package com.gmail.uprial.railnet.map;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class RailTypeTest {
    @Test
    public void testAll() throws Exception {
        assertEquals(1, RailType.SURFACE.getHashCode());
        assertEquals(2, RailType.SURFACE.getMaxHashCode());

        assertEquals(2, RailType.UNDERGROUND.getHashCode());
        assertEquals(2, RailType.UNDERGROUND.getMaxHashCode());
    }
}