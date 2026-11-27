package com.seaandtea.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "things-to-do")
public class ThingsToDoProperties {

    private final Seed seed = new Seed();
    private String deployHookUrl = "";

    public Seed getSeed() {
        return seed;
    }

    public String getDeployHookUrl() {
        return deployHookUrl;
    }

    public void setDeployHookUrl(String deployHookUrl) {
        this.deployHookUrl = deployHookUrl;
    }

    public static class Seed {
        private boolean enabled = true;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }
}
