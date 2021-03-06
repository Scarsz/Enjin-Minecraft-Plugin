package com.enjin.sponge.config;

import com.enjin.core.config.JsonConfig;
import com.enjin.rpc.mappings.mappings.plugin.PlayerGroupInfo;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

public class RankUpdatesConfig extends JsonConfig {
    @Getter
    private Map<String, PlayerGroupInfo> playerPerms = new HashMap<>();
}
