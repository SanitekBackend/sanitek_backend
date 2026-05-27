package org.acme.infrastructure.messaging.kafka;

import io.quarkus.kafka.client.serialization.ObjectMapperDeserializer;
import org.acme.infrastructure.messaging.events.IrsaCalculationMessage;

public class IrsaCalculationMessageDeserializer extends ObjectMapperDeserializer<IrsaCalculationMessage> {

    public IrsaCalculationMessageDeserializer() {
        super(IrsaCalculationMessage.class);
    }
}
