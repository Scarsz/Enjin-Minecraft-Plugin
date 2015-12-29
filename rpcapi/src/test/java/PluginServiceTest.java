import com.enjin.core.Enjin;
import com.enjin.core.EnjinServices;
import com.enjin.core.config.EnjinConfig;
import com.enjin.rpc.EnjinRPC;
import com.enjin.rpc.mappings.mappings.general.RPCData;
import com.enjin.rpc.mappings.mappings.plugin.*;
import com.enjin.rpc.mappings.services.PluginService;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@FixMethodOrder(value = MethodSorters.NAME_ASCENDING)
public class PluginServiceTest {
    private static final String API_URL = "http://api.enjinpink.com/api/v1/";
    private static final String KEY = "cfc9718c515f63e26804af7f56b1c966de13501ecdad1ad41e";
    private static final int PORT = 25565;
    private static final String PLAYER = "Favorlock";

    @Test
    public void test1Auth() {
        PluginService service = EnjinServices.getService(PluginService.class);
        RPCData<Boolean> data = service.auth(Optional.empty(), PORT, true);

        Assert.assertNotNull("data is null", data);
        Assert.assertNotNull("result is null", data.getResult());
        Assert.assertTrue("result is not true", data.getResult());

        System.out.println("Successfully authenticated: " + data.getResult().booleanValue());
    }

    @Test
    public void test2Sync() {
        Status status = new Status("UNKNOWN",
                null,
                true,
                "3.0.0-bukkit",
                new ArrayList<String>() {{
                    add("world");
                    add("end");
                    add("nether");
                }},
                new ArrayList<String>() {{
                    add("default");
                    add("creeper");
                }},
                50,
                2,
                new ArrayList<PlayerInfo>(){{
                    add(new PlayerInfo("Favorlock", UUID.fromString("8b7a881c-6ccb-4ada-8f6a-60cc99e6aa20")));
                    add(new PlayerInfo("AlmightyToaster", UUID.fromString("5b6cf5cd-d1c8-4f54-a06e-9c4462095706")));
                }},
                null,
                null,
                null,
                null);
        PluginService service = EnjinServices.getService(PluginService.class);
        RPCData<SyncResponse> data = service.sync(status);

        Assert.assertNotNull("data is null", data);
        Assert.assertNotNull("result is null", data.getResult());

        System.out.println(data.getResult().toString());
    }

    @Test
    public void test3GetTags() {
        PluginService service = EnjinServices.getService(PluginService.class);
        RPCData<List<TagData>> data = service.getTags(PLAYER);

        Assert.assertNotNull("data is null", data);
        Assert.assertNotNull("result is null", data.getResult());

        System.out.println("# of tags: " + data.getResult().size());
    }

    @Test
    public void test4GetStats() {
        PluginService service = EnjinServices.getService(PluginService.class);
        RPCData<Stats> data = service.getStats(Optional.ofNullable(new ArrayList<Integer>(){{
            add(1584937);
            add(1604379);
        }}));

        Assert.assertNotNull("data is null", data);
        Assert.assertNotNull("result is null", data.getResult());

        System.out.println(data.getResult().toString());
    }

    @BeforeClass
    public static void prepare() {
        Enjin.setConfiguration(new EnjinConfig() {
            @Override
            public boolean isDebug() {
                return true;
            }

            @Override
            public void setDebug(boolean debug) {}

            @Override
            public String getAuthKey() {
                return KEY;
            }

            @Override
            public void setAuthKey(String key) {}

            @Override
            public boolean isHttps() {
                return false;
            }

            @Override
            public void setHttps(boolean https) {}

            @Override
            public boolean isAutoUpdate() {
                return false;
            }

            @Override
            public void setAutoUpdate(boolean autoUpdate) {}

            @Override
            public boolean isLoggingEnabled() {
                return false;
            }

            @Override
            public void setLoggingEnabled(boolean loggingEnabled) {}

            @Override
            public String getApiUrl() {
                return API_URL;
            }

            @Override
            public void setApiUrl(String apiUrl) {}

            @Override
            public boolean save(File file) {
                return true;
            }

            @Override
            public boolean update(File file, Object data) {
                return true;
            }
        });
    }
}
