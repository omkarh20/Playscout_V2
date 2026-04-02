package com.example.backend.util;

import com.example.backend.model.Game;
import com.example.backend.model.User;

/**
 * Chain of Responsibility pattern for validating join requests.
 * Each validator encapsulates a single validation rule and can delegate to the next validator in the chain.
 */
public abstract class JoinRequestValidators {

    protected JoinRequestValidators next;

    public JoinRequestValidators setNext(JoinRequestValidators next) {
        this.next = next;
        return next;
    }

    public void validate(User user, Game game, boolean pendingExists, boolean acceptedExists) {
        doValidate(user, game, pendingExists, acceptedExists);
        
        if (next != null) {
            next.validate(user, game, pendingExists, acceptedExists);
        }
    }

    protected abstract void doValidate(User user, Game game, boolean pendingExists, boolean acceptedExists);

    /**
     * Validator: Check if game has available slots
     */
    public static class GameNotFullValidator extends JoinRequestValidators {
        @Override
        protected void doValidate(User user, Game game, boolean pendingExists, boolean acceptedExists) {
            if (game.getMembersJoined() >= game.getTotalMembers()) {
                throw new IllegalArgumentException("Game is already full");
            }
        }
    }

    /**
     * Validator: Prevent game creator from joining their own game
     */
    public static class SenderNotCreatorValidator extends JoinRequestValidators {
        @Override
        protected void doValidate(User user, Game game, boolean pendingExists, boolean acceptedExists) {
            if (game.getCreatedBy().getId().equals(user.getId())) {
                throw new IllegalArgumentException("You cannot join your own game");
            }
        }
    }

    /**
     * Validator: Prevent duplicate pending join requests
     */
    public static class NoPendingRequestValidator extends JoinRequestValidators {
        @Override
        protected void doValidate(User user, Game game, boolean pendingExists, boolean acceptedExists) {
            if (pendingExists) {
                throw new IllegalArgumentException("Pending join request already exists for this game");
            }
        }
    }

    /**
     * Validator: Prevent re-joining if already accepted
     */
    public static class NotAlreadyAcceptedValidator extends JoinRequestValidators {
        @Override
        protected void doValidate(User user, Game game, boolean pendingExists, boolean acceptedExists) {
            if (acceptedExists) {
                throw new IllegalArgumentException("You are already accepted for this game");
            }
        }
    }
}
