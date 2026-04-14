package com.example.backend.factory;

import com.example.backend.dto.BookingRequest;
import com.example.backend.model.Booking;
import com.example.backend.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BookVenueActionHandler implements VenueActionHandler<BookingRequest, Booking> {

    private final BookingService bookingService;

    @Override
    public VenueActionType getActionType() {
        return VenueActionType.BOOK;
    }

    @Override
    public Class<BookingRequest> getRequestType() {
        return BookingRequest.class;
    }

    @Override
    public Booking execute(String actor, BookingRequest request) {
        String resolvedUserId = actor != null && !actor.isBlank() ? actor : request.getUserId();
        request.setUserId(resolvedUserId);
        return bookingService.createBooking(request);
    }
}
