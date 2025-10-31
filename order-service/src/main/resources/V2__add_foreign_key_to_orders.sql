-- Add foreign key constraint to link orders.product_id → products.id
ALTER TABLE orders
ADD CONSTRAINT fk_orders_product
FOREIGN KEY (product_id)
REFERENCES products(id)
ON UPDATE CASCADE
ON DELETE RESTRICT;
