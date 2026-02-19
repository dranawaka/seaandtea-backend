package com.seaandtea.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageCreateRequest {

    @NotNull(message = "Receiver ID is required")
    private Long receiverId;

    @NotBlank(message = "Message content is required")
    @Size(max = 5000, message = "Message cannot exceed 5000 characters")
    private String message;

    /** Optional: link message to a booking (e.g. customer–guide chat about a tour). */
    private Long bookingId;
}
