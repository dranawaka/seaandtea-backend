package com.seaandtea.oauth;

import org.springframework.util.StringUtils;

public class OAuthProperties {

    private Provider google = new Provider();
    private Provider facebook = new Provider();

    public Provider getGoogle() {
        return google;
    }

    public void setGoogle(Provider google) {
        this.google = google == null ? new Provider() : google;
    }

    public Provider getFacebook() {
        return facebook;
    }

    public void setFacebook(Provider facebook) {
        this.facebook = facebook == null ? new Provider() : facebook;
    }

    public boolean configured(String provider) {
        if ("google".equals(provider)) {
            return google.isConfigured();
        }
        if ("facebook".equals(provider)) {
            return facebook.isConfigured();
        }
        return false;
    }

    public static class Provider {
        private String clientId = "";
        private String clientSecret = "";

        public String getClientId() {
            return clientId;
        }

        public void setClientId(String clientId) {
            this.clientId = clientId == null ? "" : clientId.trim();
        }

        public String getClientSecret() {
            return clientSecret;
        }

        public void setClientSecret(String clientSecret) {
            this.clientSecret = clientSecret == null ? "" : clientSecret.trim();
        }

        public boolean isConfigured() {
            return StringUtils.hasText(clientId) && StringUtils.hasText(clientSecret);
        }
    }
}
