ALTER TABLE order_history
    DROP CONSTRAINT fk_order_history_order;

ALTER TABLE order_history
    ADD CONSTRAINT fk_order_history_order
        FOREIGN KEY (order_id)
        REFERENCES orders (id)
        ON DELETE CASCADE;
