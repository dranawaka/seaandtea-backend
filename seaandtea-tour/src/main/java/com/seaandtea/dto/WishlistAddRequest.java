package com.seaandtea.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class WishlistAddRequest {

    @NotNull(message = "Tour is required")
    private Long tourId;
}
