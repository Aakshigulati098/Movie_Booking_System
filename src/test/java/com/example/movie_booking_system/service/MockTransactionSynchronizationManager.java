package com.example.movie_booking_system.service;


import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.List;

/**
 * Extension for mocking TransactionSynchronizationManager in tests
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@ExtendWith(MockTransactionSynchronizationManager.Extension.class)
public @interface MockTransactionSynchronizationManager {

    class Extension implements BeforeEachCallback, AfterEachCallback {
        private static MockedStatic<TransactionSynchronizationManager> mockedStatic;
        private static final List<TransactionSynchronization> synchronizations = new ArrayList<>();

        @Override
        public void beforeEach(ExtensionContext context) {
            synchronizations.clear();
            mockedStatic = Mockito.mockStatic(TransactionSynchronizationManager.class);

            mockedStatic.when(TransactionSynchronizationManager::isSynchronizationActive).thenReturn(true);

            mockedStatic.when(() -> TransactionSynchronizationManager.registerSynchronization(Mockito.any()))
                    .thenAnswer(invocation -> {
                        TransactionSynchronization sync = invocation.getArgument(0);
                        synchronizations.add(sync);
                        return null;
                    });
        }

        @Override
        public void afterEach(ExtensionContext context) {
            if (mockedStatic != null) {
                mockedStatic.close();
            }
        }

        public static void runAfterCommitCallbacks() {
            for (TransactionSynchronization sync : synchronizations) {
                sync.afterCommit();
            }
        }
    }
}
