-- Existing installations created this check before CANCELLED was added to the enum.
ALTER TABLE order_logs DROP CONSTRAINT IF EXISTS order_logs_action_check;

ALTER TABLE order_logs ADD CONSTRAINT order_logs_action_check
    CHECK (action IN ('CREATED', 'ASSIGNED', 'STARTED', 'COMPLETED', 'READY_FOR_PICKUP',
                      'PICKED_UP', 'PAYMENT_RECORDED', 'CANCELLED'));
