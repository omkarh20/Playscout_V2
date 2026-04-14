package com.example.backend.factory;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class VenueActionFactory {

    private final Map<VenueActionType, VenueActionHandler<?, ?>> handlers;

    public VenueActionFactory(List<VenueActionHandler<?, ?>> handlers) {
        this.handlers = new EnumMap<>(VenueActionType.class);
        for (VenueActionHandler<?, ?> handler : handlers) {
            this.handlers.put(handler.getActionType(), handler);
        }
    }

    @SuppressWarnings("unchecked")
    public <T, R> VenueActionHandler<T, R> getHandler(VenueActionType actionType, Class<T> requestType) {
        VenueActionHandler<?, ?> handler = handlers.get(actionType);
        if (handler == null) {
            throw new IllegalArgumentException("Unsupported venue action: " + actionType);
        }
        if (!handler.getRequestType().equals(requestType)) {
            throw new IllegalArgumentException("Invalid request type for action: " + actionType);
        }
        return (VenueActionHandler<T, R>) handler;
    }
}
