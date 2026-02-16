package com.fulfilment.application.monolith.stores;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.event.TransactionPhase;
import jakarta.inject.Inject;
import jakarta.transaction.TransactionScoped;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class StoreEventListener {

    @Inject
    LegacyStoreManagerGateway legacyStoreManagerGateway;

    public void onStoreCreated(@Observes(during = TransactionPhase.AFTER_SUCCESS) Store store) {
        legacyStoreManagerGateway.createStoreOnLegacySystem(store);
    }

    public void onStoreUpdated(@Observes(during = TransactionPhase.AFTER_SUCCESS) Store updatedStore) {
        legacyStoreManagerGateway.updateStoreOnLegacySystem(updatedStore);
    }

    public void onStorePatched(@Observes(during = TransactionPhase.AFTER_SUCCESS) Store updatedStore) {
        legacyStoreManagerGateway.updateStoreOnLegacySystem(updatedStore);
    }

}