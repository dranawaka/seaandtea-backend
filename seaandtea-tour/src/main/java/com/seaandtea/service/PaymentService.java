package com.seaandtea.service;

import com.seaandtea.config.StripeProperties;
import com.seaandtea.dto.PaymentConfirmRequest;
import com.seaandtea.dto.PaymentIntentResponse;
import com.seaandtea.entity.Booking;
import com.seaandtea.entity.Booking.PaymentStatus;
import com.seaandtea.entity.Payment;
import com.seaandtea.entity.User;
import com.seaandtea.exception.ResourceNotFoundException;
import com.seaandtea.repository.BookingRepository;
import com.seaandtea.repository.PaymentRepository;
import com.seaandtea.repository.UserRepository;
import com.stripe.Stripe;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.model.StripeObject;
import com.stripe.net.Webhook;
import com.stripe.param.PaymentIntentCreateParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.RoundingMode;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final BookingService bookingService;
    private final StripeProperties stripeProperties;

    public String publishableKey() {
        return stripeProperties.getPublishableKey();
    }

    @Transactional
    public PaymentIntentResponse createIntent(Long bookingId, String userEmail) {
        Booking booking = ownedBooking(bookingId, userEmail);
        if (booking.getPaymentStatus() == PaymentStatus.PAID) {
            throw new IllegalStateException("This booking is already paid");
        }
        if (booking.getStatus() == Booking.BookingStatus.CANCELLED) {
            throw new IllegalStateException("This booking is cancelled");
        }
        ensureStripeConfigured();

        Payment payment = paymentRepository.findByBookingId(booking.getId()).orElse(null);
        if (payment != null && payment.getStripePaymentIntentId() != null
                && payment.getStatus() == Payment.PaymentStatus.PENDING) {
            try {
                Stripe.apiKey = stripeProperties.getSecretKey();
                PaymentIntent existing = PaymentIntent.retrieve(payment.getStripePaymentIntentId());
                if ("succeeded".equals(existing.getStatus())) {
                    applySuccess(payment, booking);
                    throw new IllegalStateException("This booking is already paid");
                }
                if (!"canceled".equals(existing.getStatus())) {
                    return response(booking, existing);
                }
            } catch (StripeException ex) {
                log.warn("Could not reuse payment intent for booking {}: {}", booking.getId(), ex.getMessage());
            }
        }

        long cents = booking.getTotalAmount()
                .movePointRight(2)
                .setScale(0, RoundingMode.HALF_UP)
                .longValueExact();
        if (cents < 50) {
            throw new IllegalArgumentException("The booking total is below the minimum card charge");
        }

        try {
            Stripe.apiKey = stripeProperties.getSecretKey();
            PaymentIntent intent = PaymentIntent.create(PaymentIntentCreateParams.builder()
                    .setAmount(cents)
                    .setCurrency("usd")
                    .putMetadata("bookingId", String.valueOf(booking.getId()))
                    .setAutomaticPaymentMethods(PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                            .setEnabled(true)
                            .build())
                    .build());

            if (payment == null) {
                payment = Payment.builder()
                        .booking(booking)
                        .amount(booking.getTotalAmount())
                        .currency("USD")
                        .status(Payment.PaymentStatus.PENDING)
                        .paymentMethod("card")
                        .build();
            }
            payment.setAmount(booking.getTotalAmount());
            payment.setStripePaymentIntentId(intent.getId());
            payment.setStatus(Payment.PaymentStatus.PENDING);
            paymentRepository.save(payment);
            return response(booking, intent);
        } catch (StripeException ex) {
            log.error("Stripe payment intent failed for booking {}", booking.getId(), ex);
            throw new IllegalArgumentException("Card payments are not available. Check the Stripe test secret key.");
        }
    }

    @Transactional
    public PaymentIntentResponse confirm(PaymentConfirmRequest request, String userEmail) {
        Booking booking = ownedBooking(request.getBookingId(), userEmail);
        Payment payment = paymentRepository.findByBookingId(booking.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Payment for booking", booking.getId()));
        if (payment.getStripePaymentIntentId() == null
                || !payment.getStripePaymentIntentId().equals(request.getPaymentIntentId())) {
            throw new IllegalArgumentException("Payment intent does not match this booking");
        }
        ensureStripeConfigured();
        try {
            Stripe.apiKey = stripeProperties.getSecretKey();
            PaymentIntent intent = PaymentIntent.retrieve(request.getPaymentIntentId());
            if ("succeeded".equals(intent.getStatus())) {
                applySuccess(payment, booking);
            }
            return response(bookingRepository.findById(booking.getId()).orElse(booking), intent);
        } catch (StripeException ex) {
            log.error("Could not confirm payment for booking {}", booking.getId(), ex);
            throw new IllegalArgumentException("Could not confirm the card payment with Stripe");
        }
    }

    @Transactional
    public void handleWebhook(String payload, String signature) {
        if (stripeProperties.getWebhookSecret() == null || stripeProperties.getWebhookSecret().isBlank()) {
            throw new IllegalArgumentException("Stripe webhook secret is not configured");
        }
        final Event event;
        try {
            event = Webhook.constructEvent(payload, signature, stripeProperties.getWebhookSecret());
        } catch (SignatureVerificationException ex) {
            throw new IllegalArgumentException("Invalid Stripe signature");
        }
        if (!"payment_intent.succeeded".equals(event.getType())) {
            return;
        }
        StripeObject stripeObject = event.getDataObjectDeserializer().getObject().orElse(null);
        if (!(stripeObject instanceof PaymentIntent intent) || intent.getId() == null) {
            log.warn("Stripe webhook missing payment intent payload");
            return;
        }
        paymentRepository.findByStripePaymentIntentId(intent.getId()).ifPresent(payment -> {
            Booking booking = payment.getBooking();
            applySuccess(payment, booking);
        });
    }

    private void applySuccess(Payment payment, Booking booking) {
        payment.setStatus(Payment.PaymentStatus.SUCCEEDED);
        paymentRepository.save(payment);
        if (booking.getPaymentStatus() != PaymentStatus.PAID) {
            bookingService.markPaid(booking.getId());
        }
    }

    private Booking ownedBooking(Long bookingId, String userEmail) {
        if (userEmail == null || userEmail.isBlank()) {
            throw new AccessDeniedException("Sign in required");
        }
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return bookingRepository.findByIdAndTouristId(bookingId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Booking", bookingId));
    }

    private void ensureStripeConfigured() {
        String key = stripeProperties.getSecretKey();
        if (key == null || key.isBlank() || key.contains("dummy")) {
            throw new IllegalArgumentException("Card payments are not configured. Set STRIPE_SECRET_KEY to a Stripe test secret key.");
        }
    }

    private PaymentIntentResponse response(Booking booking, PaymentIntent intent) {
        return PaymentIntentResponse.builder()
                .bookingId(booking.getId())
                .paymentIntentId(intent.getId())
                .clientSecret(intent.getClientSecret())
                .publishableKey(stripeProperties.getPublishableKey())
                .status(intent.getStatus())
                .build();
    }
}
