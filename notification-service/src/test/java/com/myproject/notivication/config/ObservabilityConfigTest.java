package com.myproject.notivication.config;

import io.micrometer.observation.ObservationRegistry;
import io.micrometer.observation.aop.ObservedAspect;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class ObservabilityConfigTest {

    @Test
    void shouldCreateObservedAspectBean() {
        ConcurrentKafkaListenerContainerFactory<Object, Object> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        ObservabilityConfig config = new ObservabilityConfig(factory);

        ObservationRegistry registry = ObservationRegistry.create();
        ObservedAspect observedAspect = config.observedAspect(registry);

        assertNotNull(observedAspect);
    }

    @Test
    void shouldEnableObservationOnKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<Object, Object> factory =
                new ConcurrentKafkaListenerContainerFactory<>();

        ObservabilityConfig config = new ObservabilityConfig(factory);
        config.setObservationForKafkaTemplate();

        assertTrue(factory.getContainerProperties().isObservationEnabled());
    }
}