-- Idempotency table for deduplicating RabbitMQ at-least-once delivery
CREATE TABLE processed_commands (
    id UUID PRIMARY KEY,
    saga_id UUID NOT NULL UNIQUE,
    processed_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_processed_commands_saga_id ON processed_commands(saga_id);
