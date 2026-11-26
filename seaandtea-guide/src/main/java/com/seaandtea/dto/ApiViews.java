package com.seaandtea.dto;

/**
 * Jackson views for guide payloads.
 * Public responses omit contact details and verification files.
 * Internal responses (the guide themselves, or an admin) include them.
 */
public final class ApiViews {

    private ApiViews() {
    }

    public interface Public {
    }

    public interface Internal extends Public {
    }
}
