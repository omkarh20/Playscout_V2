package com.example.backend.factory;

public interface VenueActionHandler<T, R> {
    VenueActionType getActionType();

    Class<T> getRequestType();

    R execute(String actor, T request);
}
