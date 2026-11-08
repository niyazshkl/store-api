INSERT INTO categories (name)
VALUES
    ('Electronics'),
    ('Clothing'),
    ('Books'),
    ('Home & Kitchen'),
    ('Sports & Outdoors'),
    ('Beauty'),
    ('Grocery'),
    ('Toys'),
    ('Furniture'),
    ('Automotive');
INSERT INTO products (name, price, description, category_id)
VALUES
    ('Apple AirPods Pro 2', 249.00,
     'Wireless earbuds with active noise cancellation, transparency mode, and USB-C charging.',
     1),

    ('Nike Air Force 1 07', 115.00,
     'Classic white leather sneakers featuring a durable rubber sole and timeless design.',
     2),

    ('Clean Code by Robert C. Martin', 39.99,
     'A software engineering book covering clean coding practices, readability, and maintainable software design.',
     3),

    ('Ninja Professional Blender', 99.99,
     'High-powered kitchen blender suitable for smoothies, frozen drinks, and food preparation.',
     4),

    ('Wilson Evolution Basketball', 69.99,
     'Indoor basketball with microfiber composite leather construction and excellent grip.',
     5),

    ('CeraVe Moisturizing Cream', 18.99,
     'Moisturizing cream formulated with ceramides and hyaluronic acid for dry skin.',
     6),

    ('Lavazza Super Crema Coffee Beans', 24.99,
     'Medium-roast whole coffee beans with a balanced flavor and creamy finish.',
     7),

    ('LEGO Classic Creative Bricks', 29.99,
     'Creative building brick set containing colorful LEGO pieces for imaginative construction.',
     8),

    ('IKEA LACK Side Table', 14.99,
     'Minimalist side table with a lightweight design suitable for living rooms and bedrooms.',
     9),

    ('Michelin Defender2 Tire', 189.99,
     'All-season passenger car tire designed for durability, traction, and long tread life.',
     10);