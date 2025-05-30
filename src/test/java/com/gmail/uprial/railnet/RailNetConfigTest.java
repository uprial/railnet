package com.gmail.uprial.railnet;

import com.gmail.uprial.railnet.config.InvalidConfigException;
import com.gmail.uprial.railnet.helpers.TestConfigBase;
import org.bukkit.configuration.InvalidConfigurationException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.*;

public class RailNetConfigTest extends TestConfigBase {
    @Rule
    public final ExpectedException e = ExpectedException.none();

    @Test
    public void testEmptyDebug() throws Exception {
        e.expect(RuntimeException.class);
        e.expectMessage("Empty 'debug' flag. Use default value false");
        RailNetConfig.isDebugMode(getPreparedConfig(""), getDebugFearingCustomLogger());
    }

    @Test
    public void testNormalDebug() throws Exception {
        assertTrue(RailNetConfig.isDebugMode(getPreparedConfig("debug: true"), getDebugFearingCustomLogger()));
    }

    @Test
    public void testEmpty() throws Exception {
        e.expect(InvalidConfigException.class);
        e.expectMessage("Empty 'underground-railways' flag");
        loadConfig(getDebugFearingCustomLogger(), "");
    }

    @Test
    public void testNotMap() throws Exception {
        e.expect(InvalidConfigurationException.class);
        e.expectMessage("Top level is not a Map.");
        loadConfig("x");
    }


    @Test
    public void testDynamicLootDensity() throws Exception {
        e.expect(InvalidConfigException.class);
        e.expectMessage("Empty 'dynamic-loot-density' flag");
        loadConfig(getDebugFearingCustomLogger(), "underground-railways: true");
    }

    @Test
    public void testNormalConfig() throws Exception {
        assertEquals(
                "underground-railways: true," +
                        " dynamic-loot-density: true",
                loadConfig(getCustomLogger(),
                        "underground-railways: true",
                        "dynamic-loot-density: true").toString());
    }
}