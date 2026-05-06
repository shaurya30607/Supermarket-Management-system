-- ================================================================
--  Fresh Mitra — Complete Seed Data  (Self-Contained Bootstrap)
--  Run this script on a blank MySQL instance (or after DROP DATABASE)
--  and the entire application will work exactly as before.
--
--  Tables created:
--    products      : 310 products  (IDs 101–180, 301–345, 401–440,
--                                   451–485, 501–565, 601–645)
--    customers     : 25 customers  (IDs 1001–1025)
--    transactions  : 43 transactions spanning 60 days
--    txn_items     : line items for every transaction
--    cashiers      :  9 staff members
--
--  Category distribution:
--    Food        : 80  (IDs 101–180)
--    Sports      : 65  (IDs 501–565)
--    Electronics : 45  (IDs 301–345)
--    Stationery  : 45  (IDs 601–645)
--    Clothing    : 40  (IDs 401–440)
--    Beauty      : 35  (IDs 451–485)
--    Total       : 310
-- ================================================================

-- ----------------------------------------------------------------
-- 1. DATABASE
-- ----------------------------------------------------------------
CREATE DATABASE IF NOT EXISTS freshmitra_dblc
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;
USE freshmitra_dblc;

-- ----------------------------------------------------------------
-- 2. TABLE DEFINITIONS
--    Using CREATE TABLE IF NOT EXISTS so the script is idempotent.
--    ALTER TABLE blocks add columns that may be missing from older
--    schema versions — errors are silently ignored.
-- ----------------------------------------------------------------

CREATE TABLE IF NOT EXISTS products (
    product_id   INT PRIMARY KEY,
    name         VARCHAR(120)   NOT NULL,
    category     VARCHAR(30)    NOT NULL,
    price        DECIMAL(10,2)  NOT NULL,
    quantity     INT            NOT NULL DEFAULT 0,
    extra_info   VARCHAR(120)   NOT NULL,
    created_at   TIMESTAMP      DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS customers (
    customer_id       INT PRIMARY KEY,
    name              VARCHAR(100)  NOT NULL,
    phone             VARCHAR(15)   NOT NULL,
    email             VARCHAR(120),
    transaction_count INT           NOT NULL DEFAULT 0,
    created_at        TIMESTAMP     DEFAULT CURRENT_TIMESTAMP
);

-- transaction_count may be missing in very old installs
ALTER TABLE customers ADD COLUMN IF NOT EXISTS transaction_count INT NOT NULL DEFAULT 0;

CREATE TABLE IF NOT EXISTS cashiers (
    cashier_id  INT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(100) NOT NULL,
    username    VARCHAR(50)  NOT NULL UNIQUE,
    shift       VARCHAR(20)  NOT NULL DEFAULT 'Morning',
    status      VARCHAR(15)  NOT NULL DEFAULT 'Active',
    join_date   DATE         NOT NULL,
    password    VARCHAR(100) NOT NULL DEFAULT 'cashier123'
);

CREATE TABLE IF NOT EXISTS transactions (
    txn_id       VARCHAR(20)    PRIMARY KEY,
    customer_id  INT            NOT NULL,
    total_amount DECIMAL(10,2)  NOT NULL,
    coupon_pct   DECIMAL(5,2)   DEFAULT 0,
    txn_time     TIMESTAMP      DEFAULT CURRENT_TIMESTAMP,
    cashier_id   INT            NULL,
    FOREIGN KEY (customer_id) REFERENCES customers(customer_id),
    FOREIGN KEY (cashier_id)  REFERENCES cashiers(cashier_id)
);

-- cashier_id may be missing in older schema versions
ALTER TABLE transactions ADD COLUMN IF NOT EXISTS cashier_id INT NULL;

CREATE TABLE IF NOT EXISTS txn_items (
    id           INT AUTO_INCREMENT PRIMARY KEY,
    txn_id       VARCHAR(20)    NOT NULL,
    product_id   INT            NOT NULL,
    product_name VARCHAR(120)   NOT NULL,
    quantity     INT            NOT NULL,
    unit_price   DECIMAL(10,2)  NOT NULL,
    subtotal     DECIMAL(10,2)  NOT NULL,
    FOREIGN KEY (txn_id) REFERENCES transactions(txn_id)
);

-- ================================================================
-- 3. CASHIERS (9 staff members)
-- ================================================================
INSERT IGNORE INTO cashiers (cashier_id, name, username, shift, status, join_date, password) VALUES
(1, 'Rahul Sharma',  'rahul',  'Morning',   'Active',   '2023-01-15', 'rahul123'),
(2, 'Priya Mehta',   'priya',  'Afternoon', 'Active',   '2023-03-08', 'priya123'),
(3, 'Amit Kumar',    'amit',   'Evening',   'Active',   '2022-11-20', 'amit123'),
(4, 'Sneha Patel',   'sneha',  'Morning',   'Active',   '2023-06-01', 'sneha123'),
(5, 'Vikram Singh',  'vikram', 'Afternoon', 'Active',   '2023-07-14', 'vikram123'),
(6, 'Anita Devi',    'anita',  'Evening',   'Inactive', '2022-08-30', 'anita123'),
(7, 'Rohan Gupta',   'rohan',  'Morning',   'Active',   '2024-01-10', 'rohan123'),
(8, 'Kavita Rao',    'kavita', 'Afternoon', 'Active',   '2024-02-22', 'kavita123'),
(9, 'Suresh Nair',   'suresh', 'Evening',   'Active',   '2023-09-05', 'suresh123');

-- ================================================================
-- 4. CUSTOMERS (25)
-- ================================================================
INSERT IGNORE INTO customers (customer_id, name, phone, email, transaction_count) VALUES
(1001, 'Shaurya Mehta',      '9876543210', 'shaurya.mehta@gmail.com',    0),
(1002, 'Priya Sharma',       '8765432109', 'priya.sharma@outlook.com',   0),
(1003, 'Rohan Verma',        '7654321098', 'rohan.verma@yahoo.com',      0),
(1004, 'Ananya Iyer',        '6543210987', 'ananya.iyer@gmail.com',      0),
(1005, 'Kiran Patil',        '9988776655', 'kiran.patil@hotmail.com',    0),
(1006, 'Aditya Joshi',       '9123456780', 'aditya.joshi@gmail.com',     0),
(1007, 'Sneha Kulkarni',     '8234567891', 'sneha.kulkarni@yahoo.com',   0),
(1008, 'Vikram Singh',       '7345678902', 'vikram.singh@hotmail.com',   0),
(1009, 'Meera Nair',         '6456789013', 'meera.nair@gmail.com',       0),
(1010, 'Arjun Desai',        '9567890124', 'arjun.desai@outlook.com',    0),
(1011, 'Pooja Mishra',       '8678901235', 'pooja.mishra@gmail.com',     0),
(1012, 'Rahul Gupta',        '7789012346', 'rahul.gupta@rediffmail.com', 0),
(1013, 'Kavita Rao',         '6890123457', 'kavita.rao@gmail.com',       0),
(1014, 'Suresh Pandey',      '9901234568', 'suresh.pandey@yahoo.com',    0),
(1015, 'Deepika Menon',      '8012345679', 'deepika.menon@gmail.com',    0),
(1016, 'Nikhil Jain',        '9112233445', 'nikhil.jain@outlook.com',    0),
(1017, 'Swati Bhatt',        '8223344556', 'swati.bhatt@gmail.com',      0),
(1018, 'Manish Tiwari',      '7334455667', 'manish.tiwari@hotmail.com',  0),
(1019, 'Divya Shetty',       '6445566778', 'divya.shetty@gmail.com',     0),
(1020, 'Aakash Reddy',       '9556677889', 'aakash.reddy@yahoo.com',     0),
(1021, 'Ritika Choudhary',   '8667788990', 'ritika.choudhary@gmail.com', 0),
(1022, 'Siddharth Malhotra', '7778899001', 'siddharth.m@outlook.com',    0),
(1023, 'Ishita Kapoor',      '9889900112', 'ishita.kapoor@gmail.com',    0),
(1024, 'Gaurav Saxena',      '8990011223', 'gaurav.saxena@rediffmail.com',0),
(1025, 'Pallavi Tripathi',   '7001122334', 'pallavi.t@gmail.com',        0);

-- ================================================================
-- 5. PRODUCTS
-- ================================================================

-- ----------------------------------------------------------------
-- FOOD (80 products, IDs 101–180)  — extra_info = "Expiry: MM/YYYY"
-- ----------------------------------------------------------------
INSERT IGNORE INTO products (product_id, name, category, price, quantity, extra_info) VALUES
(101, 'Basmati Rice 5kg',            'Food', 250.00, 30, 'Expiry: 12/2026'),
(102, 'Amul Butter 500g',            'Food',  95.00, 20, 'Expiry: 08/2026'),
(103, 'Tata Salt 1kg',               'Food',  20.00, 50, 'Expiry: 12/2027'),
(104, 'Fortune Sunflower Oil 1L',    'Food', 145.00, 25, 'Expiry: 06/2027'),
(105, 'Maggi Noodles 70g x6',        'Food',  95.00, 40, 'Expiry: 03/2027'),
(106, 'Aashirvaad Atta 5kg',         'Food', 280.00, 18, 'Expiry: 09/2027'),
(107, 'Amul Full Cream Milk 1L',     'Food',  68.00, 12, 'Expiry: 02/2026'),
(108, 'Parle-G Biscuits 800g',       'Food',  85.00, 35, 'Expiry: 07/2027'),
(109, 'Kissan Mixed Fruit Jam',      'Food', 115.00,  8, 'Expiry: 11/2026'),
(110, 'Haldiram Bhujia 400g',        'Food',  99.00, 22, 'Expiry: 05/2027'),
(111, 'Tata Tea Gold 500g',          'Food', 250.00, 14, 'Expiry: 01/2028'),
(112, 'Nescafe Classic 50g',         'Food', 295.00,  6, 'Expiry: 04/2028'),
(113, 'Bru Instant Coffee 50g',      'Food', 175.00, 12, 'Expiry: 09/2027'),
(114, 'Red Label Tea 500g',          'Food', 235.00, 16, 'Expiry: 11/2027'),
(115, 'Lijjat Papad 200g',           'Food',  45.00, 16, 'Expiry: 10/2027'),
(116, 'MDH Garam Masala 100g',       'Food', 110.00,  9, 'Expiry: 08/2027'),
(117, 'Everest Chana Masala 100g',   'Food',  55.00, 30, 'Expiry: 09/2028'),
(118, 'Amul Cheese Slices 200g',     'Food',  99.00, 15, 'Expiry: 01/2026'),
(119, 'Britannia Brown Bread',       'Food',  50.00, 40, 'Expiry: 11/2025'),
(120, 'Mother Dairy Curd 400g',      'Food',  50.00, 20, 'Expiry: 12/2025'),
(121, 'Horlicks 500g',               'Food', 299.00, 10, 'Expiry: 06/2028'),
(122, 'Bournvita 500g',              'Food', 285.00, 12, 'Expiry: 09/2028'),
(123, 'Real Fruit Juice 1L',         'Food',  99.00, 25, 'Expiry: 04/2027'),
(124, 'Minute Maid Pulpy Orange 1L', 'Food',  60.00, 30, 'Expiry: 02/2027'),
(125, 'Maaza Mango 1L',              'Food',  55.00, 28, 'Expiry: 03/2027'),
(126, 'Cadbury Dairy Milk 100g',     'Food',  80.00, 50, 'Expiry: 07/2027'),
(127, 'KitKat 4-Finger Pack',        'Food',  45.00, 60, 'Expiry: 05/2027'),
(128, 'Lays Classic Salted 80g',     'Food',  30.00,100, 'Expiry: 03/2027'),
(129, 'Kurkure Masala Munch 90g',    'Food',  25.00, 80, 'Expiry: 04/2027'),
(130, 'Britannia Good Day 200g',     'Food',  60.00, 45, 'Expiry: 06/2027'),
(131, 'Oreo Original 300g',          'Food',  70.00, 40, 'Expiry: 08/2027'),
(132, 'Maggi Tomato Sauce 400g',     'Food',  89.00, 18, 'Expiry: 12/2027'),
(133, 'Kissan Tomato Ketchup 1kg',   'Food', 149.00, 14, 'Expiry: 01/2028'),
(134, 'Heinz Mayonnaise 300g',       'Food', 199.00, 10, 'Expiry: 11/2027'),
(135, 'Patanjali Ghee 1L',           'Food', 599.00,  8, 'Expiry: 03/2028'),
(136, 'Saffola Gold Oil 1L',         'Food', 175.00, 18, 'Expiry: 08/2027'),
(137, 'Sundrop Oil 1L',              'Food', 155.00, 22, 'Expiry: 07/2027'),
(138, 'Quaker Oats 1kg',             'Food', 179.00, 16, 'Expiry: 04/2028'),
(139, 'Kellogg Corn Flakes 875g',    'Food', 299.00, 10, 'Expiry: 06/2028'),
(140, 'MTR Idli Mix 500g',           'Food', 129.00, 15, 'Expiry: 05/2028'),
(141, 'Gits Dosa Mix 500g',          'Food', 119.00, 12, 'Expiry: 04/2028'),
(142, 'Swad Poha 500g',              'Food',  55.00, 25, 'Expiry: 03/2028'),
(143, 'Nestle Milkmaid 400g',        'Food',  89.00, 18, 'Expiry: 02/2028'),
(144, 'Dabur Honey 500g',            'Food', 259.00, 12, 'Expiry: 12/2028'),
(145, 'Saffola Masala Oats 39g',     'Food',  25.00, 35, 'Expiry: 09/2028'),
(146, 'Del Monte Pasta 500g',        'Food',  99.00, 20, 'Expiry: 05/2028'),
(147, 'Knorr Tomato Soup Mix',       'Food',  35.00, 40, 'Expiry: 04/2028'),
(148, 'Maggi Masala Noodles 70g',    'Food',  14.00,100, 'Expiry: 02/2027'),
(149, 'Yippee Noodles Masala 70g',   'Food',  14.00, 80, 'Expiry: 03/2027'),
(150, 'Top Ramen Noodles 70g',       'Food',  15.00, 75, 'Expiry: 01/2027'),
(151, 'Haldiram Rasgulla 1kg',       'Food', 175.00, 12, 'Expiry: 12/2026'),
(152, 'Gits Gulab Jamun Mix 500g',   'Food',  89.00, 18, 'Expiry: 06/2028'),
(153, 'Nutella 350g',                'Food', 499.00,  7, 'Expiry: 04/2028'),
(154, 'JIF Peanut Butter 454g',      'Food', 399.00,  9, 'Expiry: 08/2028'),
(155, 'Kissan Pineapple Jam 200g',   'Food', 119.00, 12, 'Expiry: 07/2027'),
(156, 'Amul Dark Chocolate 150g',    'Food', 149.00, 18, 'Expiry: 06/2027'),
(157, 'Cadbury Silk 145g',           'Food', 185.00, 20, 'Expiry: 08/2027'),
(158, 'Ferrero Rocher 12pc',         'Food', 450.00,  5, 'Expiry: 11/2026'),
(159, 'Lindt Excellence 100g',       'Food', 299.00,  8, 'Expiry: 12/2026'),
(160, 'Borges Olive Oil 500ml',      'Food', 499.00,  6, 'Expiry: 08/2028'),
(161, 'Lays Magic Masala 80g',       'Food',  30.00, 90, 'Expiry: 03/2027'),
(162, 'Bingo Mad Angles 90g',        'Food',  25.00, 80, 'Expiry: 06/2027'),
(163, 'Doritos Nacho Cheese 120g',   'Food',  80.00, 40, 'Expiry: 07/2027'),
(164, 'Ching Schezwan Sauce 250g',   'Food', 149.00, 15, 'Expiry: 09/2027'),
(165, 'Veeba Thousand Island 300g',  'Food', 179.00, 10, 'Expiry: 06/2028'),
(166, 'Sneakers 4-Pack',             'Food', 149.00, 15, 'Expiry: 01/2027'),
(167, 'Amul Ice Cream Vanilla 1L',   'Food', 140.00, 10, 'Expiry: 11/2025'),
(168, 'Kwality Walls Cornetto 4pk',  'Food',  60.00, 20, 'Expiry: 10/2025'),
(169, 'Mother Dairy Mishti Doi 400g','Food',  60.00, 15, 'Expiry: 12/2025'),
(170, 'Everest Pav Bhaji Masala',    'Food',  65.00, 22, 'Expiry: 08/2028'),
(171, 'Lee Kum Kee Oyster Sauce',    'Food', 299.00,  8, 'Expiry: 07/2028'),
(172, 'Sujata Chakki Fresh Atta 5kg','Food', 245.00, 20, 'Expiry: 01/2028'),
(173, 'Complan Chocolate 200g',      'Food', 199.00,  8, 'Expiry: 08/2028'),
(174, 'Aashirvaad Masala 100g',      'Food',  35.00, 50, 'Expiry: 10/2028'),
(175, 'Catch Black Pepper 50g',      'Food',  79.00, 20, 'Expiry: 12/2028'),
(176, 'Balaji Wafers Tomato 45g',    'Food',  20.00,100, 'Expiry: 04/2027'),
(177, 'Too Yumm Multigrain 60g',     'Food',  30.00, 75, 'Expiry: 05/2027'),
(178, 'Amul Mozzarella 200g',        'Food', 199.00,  7, 'Expiry: 01/2026'),
(179, 'Taaza Tea 1kg',               'Food', 245.00, 10, 'Expiry: 07/2028'),
(180, 'Parle Monaco Biscuits 400g',  'Food',  65.00, 38, 'Expiry: 09/2027');

-- ----------------------------------------------------------------
-- ELECTRONICS (45 products, IDs 301–345) — extra_info = "Warranty: N mo"
-- ----------------------------------------------------------------
INSERT IGNORE INTO products (product_id, name, category, price, quantity, extra_info) VALUES
(301, 'Samsung Galaxy Buds2',            'Electronics',  5999.00,  8, 'Warranty: 12 mo'),
(302, 'boAt Rockerz 450 Headphones',     'Electronics',  1499.00, 15, 'Warranty: 12 mo'),
(303, 'JBL Go 3 Bluetooth Speaker',      'Electronics',  2499.00, 10, 'Warranty: 6 mo'),
(304, 'Anker PowerCore 10000mAh',        'Electronics',  1799.00, 12, 'Warranty: 12 mo'),
(305, 'Belkin USB-C Hub 7-in-1',         'Electronics',  3499.00,  6, 'Warranty: 12 mo'),
(306, 'TP-Link Mini WiFi Adapter',       'Electronics',   699.00, 20, 'Warranty: 12 mo'),
(307, 'Logitech M185 Wireless Mouse',    'Electronics',   799.00, 18, 'Warranty: 24 mo'),
(308, 'HP USB Wired Keyboard',           'Electronics',   599.00, 15, 'Warranty: 12 mo'),
(309, 'Zebronics Webcam 720p',           'Electronics',   999.00, 10, 'Warranty: 12 mo'),
(310, 'Syska LED Bulb 9W Pack 4',        'Electronics',   299.00, 30, 'Warranty: 12 mo'),
(311, 'Philips Trimmer BT1230',          'Electronics',  1099.00, 12, 'Warranty: 24 mo'),
(312, 'Havells Hair Dryer 1200W',        'Electronics',  1299.00,  8, 'Warranty: 12 mo'),
(313, 'Pigeon Induction Cooktop 1800W',  'Electronics',  1899.00,  6, 'Warranty: 12 mo'),
(314, 'Prestige Electric Kettle 1.5L',   'Electronics',   799.00, 10, 'Warranty: 12 mo'),
(315, 'Usha Table Fan 3-Speed',          'Electronics',  1599.00,  7, 'Warranty: 24 mo'),
(316, 'Portronics Bluetooth Dongle',     'Electronics',   399.00, 25, 'Warranty: 6 mo'),
(317, 'Mi Smart Band 7',                 'Electronics',  3499.00,  9, 'Warranty: 12 mo'),
(318, 'Fastrack Reflex 3.0 Smartwatch',  'Electronics',  2495.00,  8, 'Warranty: 12 mo'),
(319, 'Noise ColorFit Pro 4',            'Electronics',  2999.00,  7, 'Warranty: 12 mo'),
(320, 'realme Watch 2 Pro',              'Electronics',  3499.00,  6, 'Warranty: 12 mo'),
(321, 'Ant Esports MK1000 Keyboard',     'Electronics',  1299.00, 12, 'Warranty: 12 mo'),
(322, 'RedGear Wired Gaming Mouse',      'Electronics',   899.00, 14, 'Warranty: 12 mo'),
(323, 'Cosmic Byte Headset 7.1',         'Electronics',   999.00, 10, 'Warranty: 12 mo'),
(324, 'Zebronics Gaming Mouse Pad XL',   'Electronics',   499.00, 18, 'Warranty: 6 mo'),
(325, 'ViewSonic VA1655 Portable Mon.',  'Electronics', 12999.00,  3, 'Warranty: 36 mo'),
(326, 'Lenovo USB-C 65W Adapter',        'Electronics',  1799.00,  8, 'Warranty: 12 mo'),
(327, 'Samsung 128GB MicroSD Card',      'Electronics',   799.00, 20, 'Warranty: 3 mo'),
(328, 'SanDisk 64GB USB 3.1',            'Electronics',   499.00, 25, 'Warranty: 5 mo'),
(329, 'Seagate 1TB External HDD',        'Electronics',  3499.00,  8, 'Warranty: 36 mo'),
(330, 'WD Elements 2TB External HDD',    'Electronics',  5499.00,  5, 'Warranty: 36 mo'),
(331, 'HP DeskJet Ink Cartridge Black',  'Electronics',   699.00, 14, 'Warranty: 6 mo'),
(332, 'Epson USB Printer Cable 1.8m',    'Electronics',   199.00, 22, 'Warranty: 6 mo'),
(333, 'D-Link 8-Port Switch',            'Electronics',  1299.00,  8, 'Warranty: 24 mo'),
(334, 'Tenda N300 WiFi Router',          'Electronics',   999.00, 10, 'Warranty: 12 mo'),
(335, 'Syska 20000mAh Power Bank',       'Electronics',  1299.00, 12, 'Warranty: 12 mo'),
(336, 'boAt Stone 650 Speaker',          'Electronics',  3499.00,  6, 'Warranty: 12 mo'),
(337, 'Sony WH-CH510 Headphones',        'Electronics',  4990.00,  5, 'Warranty: 12 mo'),
(338, 'Realme Buds 2 Wired',             'Electronics',   599.00, 20, 'Warranty: 6 mo'),
(339, 'Zebronics BT Headphones Zeb-Duke','Electronics',  1299.00, 10, 'Warranty: 12 mo'),
(340, 'Mi True Wireless Earphones 2',    'Electronics',  2799.00,  7, 'Warranty: 12 mo'),
(341, 'Portronics MX8 Mouse Pad',        'Electronics',   399.00, 20, 'Warranty: 6 mo'),
(342, 'AmazonBasics USB Hub 4-Port',     'Electronics',   699.00, 15, 'Warranty: 12 mo'),
(343, 'Intex IT-306W 2.0 Speaker',       'Electronics',   899.00, 10, 'Warranty: 12 mo'),
(344, 'Syska LED Strip 5m',              'Electronics',   549.00, 14, 'Warranty: 12 mo'),
(345, 'Redmi Note 13 USB-C Cable 2m',    'Electronics',   299.00, 30, 'Warranty: 6 mo');

-- ----------------------------------------------------------------
-- CLOTHING (40 products, IDs 401–440) — extra_info = "Size: X"
-- ----------------------------------------------------------------
INSERT IGNORE INTO products (product_id, name, category, price, quantity, extra_info) VALUES
(401, 'Levis 511 Slim Fit Jeans',       'Clothing', 2499.00,  8, 'Size: M'),
(402, 'Peter England Formal Shirt',     'Clothing', 1299.00, 10, 'Size: L'),
(403, 'Jockey Cotton T-Shirt',          'Clothing',  699.00, 20, 'Size: M'),
(404, 'Adidas Track Pants',             'Clothing', 1499.00, 12, 'Size: L'),
(405, 'Puma Crew Neck Sweatshirt',      'Clothing', 1999.00,  8, 'Size: XL'),
(406, 'Wrangler Regular Fit Jeans',     'Clothing', 1999.00,  6, 'Size: 32'),
(407, 'Arrow Full-Sleeve Formal Shirt', 'Clothing', 1599.00,  9, 'Size: L'),
(408, 'US Polo Assn T-Shirt Pack 2',    'Clothing', 1299.00, 15, 'Size: M'),
(409, 'Woodland Casual Jacket',         'Clothing', 3499.00,  5, 'Size: L'),
(410, 'Monte Carlo Knit Sweater',       'Clothing', 1799.00,  7, 'Size: XL'),
(411, 'Biba Kurta Set',                 'Clothing', 1599.00, 10, 'Size: M'),
(412, 'W Women Printed Kurti',          'Clothing',  899.00, 15, 'Size: S'),
(413, 'Global Desi Palazzo Set',        'Clothing', 1199.00,  8, 'Size: M'),
(414, 'ONLY Women Jogger Pants',        'Clothing', 1299.00, 10, 'Size: S'),
(415, 'AND A-Line Midi Dress',          'Clothing', 1799.00,  6, 'Size: M'),
(416, 'Jockey Sports Bra',              'Clothing',  599.00, 18, 'Size: M'),
(417, 'Enamor Comfort Bra',             'Clothing',  799.00, 12, 'Size: M'),
(418, 'VIP Frenchie Plus Briefs 3pk',   'Clothing',  399.00, 20, 'Size: L'),
(419, 'Lux Cozi Vest Pack 3',           'Clothing',  299.00, 25, 'Size: XL'),
(420, 'Dollar Bigboss Trunk Pack 3',    'Clothing',  349.00, 22, 'Size: L'),
(421, 'Hanes Crew Socks 6pk',           'Clothing',  399.00, 18, 'Size: Free'),
(422, 'Reebok Running Socks 3pk',       'Clothing',  499.00, 15, 'Size: Free'),
(423, 'Woodland Leather Belt',          'Clothing',  799.00, 10, 'Size: 36'),
(424, 'Van Heusen Leather Wallet',      'Clothing',  999.00, 12, 'Size: Free'),
(425, 'Fastrack UV-Protected Sunglass', 'Clothing', 1299.00,  8, 'Size: Free'),
(426, 'Wildcraft Backpack 30L',         'Clothing', 1899.00,  6, 'Size: Free'),
(427, 'Skybags Trolley 24 inch',        'Clothing', 3499.00,  4, 'Size: Free'),
(428, 'Caprese Structured Handbag',     'Clothing', 2999.00,  5, 'Size: Free'),
(429, 'Lavie Large Tote Bag',           'Clothing', 1799.00,  7, 'Size: Free'),
(430, 'Baggit Vegan Leather Clutch',    'Clothing',  999.00, 10, 'Size: Free'),
(431, 'Highlander Cargo Pants',         'Clothing', 1199.00,  9, 'Size: 32'),
(432, 'Flying Machine Slim Jeans',      'Clothing', 1699.00,  8, 'Size: 30'),
(433, 'Pepe Jeans Polo T-Shirt',        'Clothing', 1099.00, 12, 'Size: M'),
(434, 'Allen Solly Chinos',             'Clothing', 1899.00,  7, 'Size: 32'),
(435, 'Louis Philippe Tie',             'Clothing',  699.00, 14, 'Size: Free'),
(436, 'Tommy Hilfiger Belt',            'Clothing', 1499.00,  6, 'Size: 34'),
(437, 'Adidas Running Shorts',          'Clothing',  799.00, 16, 'Size: M'),
(438, 'Nike Dri-Fit Cap',               'Clothing',  999.00, 10, 'Size: Free'),
(439, 'Puma Low-Cut Ankle Socks 3pk',   'Clothing',  499.00, 20, 'Size: Free'),
(440, 'Levi Strauss Canvas Tote',       'Clothing', 1299.00,  8, 'Size: Free');

-- ----------------------------------------------------------------
-- BEAUTY (35 products, IDs 451–485) — extra_info = "Brand: X"
-- ----------------------------------------------------------------
INSERT IGNORE INTO products (product_id, name, category, price, quantity, extra_info) VALUES
(451, 'Lakme Eyeconic Kajal',            'Beauty',  169.00, 30, 'Brand: Lakme'),
(452, 'Maybelline Fit Me Foundation',    'Beauty',  499.00, 20, 'Brand: Maybelline'),
(453, 'LOreal Revitalift Day Cream',     'Beauty',  799.00, 12, 'Brand: LOreal'),
(454, 'Neutrogena Moisturizer SPF15',    'Beauty',  649.00, 15, 'Brand: Neutrogena'),
(455, 'Himalaya Neem Face Wash 150ml',   'Beauty',  149.00, 25, 'Brand: Himalaya'),
(456, 'Garnier Micellar Water 400ml',    'Beauty',  299.00, 18, 'Brand: Garnier'),
(457, 'Biotique Bio Honey Face Pack',    'Beauty',  199.00, 15, 'Brand: Biotique'),
(458, 'Plum Vitamin C Serum 30ml',       'Beauty',  699.00, 10, 'Brand: Plum'),
(459, 'Minimalist 2% Salicylic Acid',    'Beauty',  499.00, 12, 'Brand: Minimalist'),
(460, 'Wow ACV Shampoo',                 'Beauty',  349.00, 18, 'Brand: Wow'),
(461, 'Head and Shoulders Anti-Dandruff','Beauty',  349.00, 20, 'Brand: H&S'),
(462, 'TRESemme Keratin Smooth Shampoo', 'Beauty',  399.00, 15, 'Brand: TRESemme'),
(463, 'Pantene Pro-V Conditioner 340ml', 'Beauty',  299.00, 18, 'Brand: Pantene'),
(464, 'Mamaearth Onion Hair Oil 250ml',  'Beauty',  349.00, 14, 'Brand: Mamaearth'),
(465, 'Parachute Coconut Oil 500ml',     'Beauty',  175.00, 25, 'Brand: Parachute'),
(466, 'Indulekha Bringha Hair Oil',      'Beauty',  699.00,  8, 'Brand: Indulekha'),
(467, 'Vaseline Intensive Care 400ml',   'Beauty',  299.00, 20, 'Brand: Vaseline'),
(468, 'Cetaphil Moisturizing Lotion',    'Beauty',  499.00, 12, 'Brand: Cetaphil'),
(469, 'Dove Body Wash 500ml',            'Beauty',  449.00, 15, 'Brand: Dove'),
(470, 'Fiama Di Wills Shower Gel 250ml', 'Beauty',  299.00, 18, 'Brand: Fiama'),
(471, 'Lakme Absolute Lipstick',         'Beauty',  599.00, 14, 'Brand: Lakme'),
(472, 'Maybelline Color Sensational Lip','Beauty',  399.00, 18, 'Brand: Maybelline'),
(473, 'LOreal Paris Kajal Magique',      'Beauty',  299.00, 20, 'Brand: LOreal'),
(474, 'Colorbar Matte Touch Lipstick',   'Beauty',  499.00, 10, 'Brand: Colorbar'),
(475, 'NYX Soft Matte Lip Cream',        'Beauty',  699.00,  8, 'Brand: NYX'),
(476, 'Biotique Morning Nectar Moist.',  'Beauty',  249.00, 22, 'Brand: Biotique'),
(477, 'St. Botanica Vitamin C Serum',    'Beauty',  799.00,  9, 'Brand: St.Botanica'),
(478, 'Simple Kind to Skin Toner',       'Beauty',  349.00, 16, 'Brand: Simple'),
(479, 'Neutrogena Hydro Boost Gel',      'Beauty',  799.00, 10, 'Brand: Neutrogena'),
(480, 'Ponds Light Moisturiser 100ml',   'Beauty',  149.00, 30, 'Brand: Ponds'),
(481, 'Garnier Light Complete Cream',    'Beauty',  199.00, 25, 'Brand: Garnier'),
(482, 'Himalaya Under Eye Cream 15ml',   'Beauty',  199.00, 14, 'Brand: Himalaya'),
(483, 'Mamaearth Ubtan Face Wash',       'Beauty',  299.00, 18, 'Brand: Mamaearth'),
(484, 'Plum Green Tea Toner 200ml',      'Beauty',  399.00, 12, 'Brand: Plum'),
(485, 'Dove Intense Repair Conditioner', 'Beauty',  349.00, 16, 'Brand: Dove');

-- ----------------------------------------------------------------
-- SPORTS (65 products, IDs 501–565) — extra_info = "Sport: X"
-- ----------------------------------------------------------------
INSERT IGNORE INTO products (product_id, name, category, price, quantity, extra_info) VALUES
(501, 'Cosco Football Size 5',           'Sports',  799.00, 10, 'Sport: Football'),
(502, 'Yonex Mavis Shuttle 6pk',         'Sports',  499.00, 20, 'Sport: Badminton'),
(503, 'Li-Ning Badminton Racket',        'Sports', 1299.00,  8, 'Sport: Badminton'),
(504, 'Nivia Cricket Bat Kashmir Willow','Sports', 1499.00,  6, 'Sport: Cricket'),
(505, 'SG Cricket Bat English Willow',   'Sports', 3999.00,  3, 'Sport: Cricket'),
(506, 'Nivia Volleyball Size 4',         'Sports',  899.00,  8, 'Sport: Volleyball'),
(507, 'Cosco Basketball Size 7',         'Sports',  999.00,  6, 'Sport: Basketball'),
(508, 'Nivia Hydro Track Water Bottle 1L','Sports',  349.00, 18, 'Sport: General'),
(509, 'Cricket Leather Ball Red',        'Sports',  299.00, 12, 'Sport: Cricket'),
(510, 'SS Cricket Batting Gloves',       'Sports',  699.00,  6, 'Sport: Cricket'),
(511, 'Shrey Cricket Helmet ABS',        'Sports', 1499.00,  4, 'Sport: Cricket'),
(512, 'BDM Cricket Batting Pad Pair',    'Sports', 1299.00,  5, 'Sport: Cricket'),
(513, 'Cosco Tennis Racket',             'Sports', 1999.00,  4, 'Sport: Tennis'),
(514, 'Wilson Tennis Balls 3pk',         'Sports',  299.00, 15, 'Sport: Tennis'),
(515, 'Victor Squash Racket',            'Sports', 1499.00,  5, 'Sport: Squash'),
(516, 'Stiga Table Tennis Bat Pair',     'Sports',  799.00,  8, 'Sport: TT'),
(517, 'Stiga TT Ball 6-Pack',            'Sports',  199.00, 20, 'Sport: TT'),
(518, 'Joola TT Net and Post',           'Sports',  499.00, 10, 'Sport: TT'),
(519, 'Mikasa Volleyball Size 5',        'Sports',  699.00,  8, 'Sport: Volleyball'),
(520, 'Spalding Basketball Size 7',      'Sports',  899.00,  6, 'Sport: Basketball'),
(521, 'Nivia Wall Mount Basketball Hoop','Sports', 2999.00,  3, 'Sport: Basketball'),
(522, 'Grays Hockey Stick Composite',    'Sports', 1299.00,  5, 'Sport: Hockey'),
(523, 'Sanspareils Hockey Ball',         'Sports',  199.00, 18, 'Sport: Hockey'),
(524, 'Adidas Hockey Goalkeeper Gloves', 'Sports',  999.00,  5, 'Sport: Hockey'),
(525, 'Everlast Boxing Gloves 12oz',     'Sports', 1199.00,  6, 'Sport: Boxing'),
(526, 'Everlast Boxing Hand Wraps',      'Sports',  349.00, 12, 'Sport: Boxing'),
(527, 'Adidas Punching Bag 4ft',         'Sports', 3999.00,  3, 'Sport: Boxing'),
(528, 'Strauss Skipping Rope w/ Counter','Sports',  399.00, 14, 'Sport: Fitness'),
(529, 'Kore Pull-Up Bar Doorway',        'Sports',  999.00,  8, 'Sport: Fitness'),
(530, 'Yes4All Push-Up Board',           'Sports',  599.00, 10, 'Sport: Fitness'),
(531, 'Kore Ab Roller Wheel',            'Sports',  499.00, 12, 'Sport: Fitness'),
(532, 'Kore Rubber Dumbbell 5kg Pair',   'Sports', 1499.00,  5, 'Sport: Gym'),
(533, 'Vesta Kettlebell 8kg',            'Sports', 1199.00,  6, 'Sport: Gym'),
(534, 'Kore PVC Barbell Set 20kg',       'Sports', 4999.00,  2, 'Sport: Gym'),
(535, 'Kore Weight Plates 5kg',          'Sports',  799.00,  8, 'Sport: Gym'),
(536, 'RDX Gym Gloves Wrist Support',    'Sports',  399.00, 14, 'Sport: Gym'),
(537, 'RDX Genuine Leather Gym Belt',    'Sports',  699.00,  8, 'Sport: Gym'),
(538, 'RDX Knee Sleeve Pair',            'Sports',  499.00, 10, 'Sport: Gym'),
(539, 'Harbinger Wrist Wraps Pair',      'Sports',  349.00, 12, 'Sport: Gym'),
(540, 'GNC Protein Shaker Bottle 700ml', 'Sports',  299.00, 18, 'Sport: Gym'),
(541, 'TheraBand Foam Roller 60cm',      'Sports',  599.00, 10, 'Sport: Recovery'),
(542, 'Lifelong Massage Gun LLM57',      'Sports', 3999.00,  3, 'Sport: Recovery'),
(543, 'Cool-It Reusable Ice Pack',       'Sports',  199.00, 20, 'Sport: Recovery'),
(544, 'Strauss Compression Arm Sleeve',  'Sports',  349.00, 12, 'Sport: Recovery'),
(545, 'Mueller Kinesiology Tape 5m',     'Sports',  249.00, 16, 'Sport: Recovery'),
(546, 'Speedo Vanquisher Swim Goggles',  'Sports',  399.00, 10, 'Sport: Swimming'),
(547, 'Speedo Silicone Swim Cap',        'Sports',  199.00, 15, 'Sport: Swimming'),
(548, 'Arena Jammer Men Swimwear',       'Sports',  699.00,  7, 'Sport: Swimming'),
(549, 'Arena Women Swimsuit',            'Sports',  799.00,  6, 'Sport: Swimming'),
(550, 'Speedo Kickboard Float',          'Sports',  349.00, 10, 'Sport: Swimming'),
(551, 'Adidas Predator Football Boots',  'Sports', 3499.00,  5, 'Sport: Football'),
(552, 'Nivia Football Shin Guards',      'Sports',  349.00, 12, 'Sport: Football'),
(553, 'Yonex Badminton Grip Tape 3pk',   'Sports',  199.00, 20, 'Sport: Badminton'),
(554, 'Victor Badminton Bag',            'Sports', 1299.00,  6, 'Sport: Badminton'),
(555, 'SG Cricket Kit Bag',              'Sports', 2499.00,  4, 'Sport: Cricket'),
(556, 'Kookaburra Cricket Thigh Guard',  'Sports',  599.00,  8, 'Sport: Cricket'),
(557, 'Head Tennis Bag',                 'Sports', 1999.00,  4, 'Sport: Tennis'),
(558, 'Babolat Tennis Dampener 2pk',     'Sports',  199.00, 18, 'Sport: Tennis'),
(559, 'Nivia Sprint Running Shoes Insol','Sports',  299.00, 15, 'Sport: Running'),
(560, 'Strauss Yoga Mat 6mm',            'Sports',  699.00, 12, 'Sport: Yoga'),
(561, 'Nivia Yoga Block Pair',           'Sports',  399.00, 10, 'Sport: Yoga'),
(562, 'Strauss Resistance Band Set 5pk', 'Sports',  599.00, 14, 'Sport: Fitness'),
(563, 'Kore PVC Speed Bag',              'Sports', 1299.00,  5, 'Sport: Boxing'),
(564, 'Vesta Adjustable Bench Press',    'Sports', 5999.00,  2, 'Sport: Gym'),
(565, 'Nivia Gym Towel Microfibre',      'Sports',  299.00, 20, 'Sport: Gym');

-- ----------------------------------------------------------------
-- STATIONERY (45 products, IDs 601–645) — extra_info = "Use: X"
-- ----------------------------------------------------------------
INSERT IGNORE INTO products (product_id, name, category, price, quantity, extra_info) VALUES
(601, 'Classmate Notebook A4 200pg x5', 'Stationery', 199.00, 25, 'Use: Study'),
(602, 'Reynolds 045 Pen 10-Pack',       'Stationery',  99.00, 40, 'Use: Writing'),
(603, 'Kangaro Stapler with 1000 Pins', 'Stationery', 199.00, 15, 'Use: Office'),
(604, 'Scotch Magic Tape 6-Roll',       'Stationery', 149.00, 18, 'Use: Office'),
(605, 'JK Copier A4 Paper 500 Sheets',  'Stationery', 299.00, 10, 'Use: Printing'),
(606, 'Camlin Geometry Box Metal',      'Stationery', 149.00, 22, 'Use: Study'),
(607, 'Luxor Whiteboard Marker 8 Cols', 'Stationery',  99.00, 16, 'Use: Teaching'),
(608, '3M Sticky Notes Neon 6pk',       'Stationery',  99.00, 30, 'Use: Office'),
(609, 'Faber-Castell Pencil Set 12B',   'Stationery',  99.00, 35, 'Use: Drawing'),
(610, 'Staedtler Mars Lumograph Set',   'Stationery', 149.00, 25, 'Use: Drawing'),
(611, 'Camlin Oil Pastels 25 Shades',   'Stationery',  99.00, 30, 'Use: Art'),
(612, 'Apsara Wax Crayons 16-Color',    'Stationery',  55.00, 40, 'Use: Art'),
(613, 'Camlin Watercolor Cakes 12 Set', 'Stationery', 149.00, 22, 'Use: Art'),
(614, 'Fevistik Glue Stick Large',      'Stationery',  49.00, 50, 'Use: Craft'),
(615, 'Fevicol SH Adhesive 250ml',      'Stationery',  99.00, 30, 'Use: Craft'),
(616, 'UHU All Purpose Glue Stick',     'Stationery',  79.00, 35, 'Use: Craft'),
(617, 'Classmate Stainless Scissors',   'Stationery',  99.00, 25, 'Use: Office'),
(618, 'Olex Paper Cutter A4',           'Stationery', 149.00, 18, 'Use: Office'),
(619, 'Nataraj Steel Ruler 30cm',       'Stationery',  49.00, 40, 'Use: Study'),
(620, 'Apsara Set Square Pair',         'Stationery',  79.00, 28, 'Use: Study'),
(621, 'Camlin Protractor 180 Deg',      'Stationery',  39.00, 35, 'Use: Study'),
(622, 'Faber-Castell Compass Divider',  'Stationery',  99.00, 25, 'Use: Study'),
(623, 'Brustro Drawing Board A2',       'Stationery', 499.00, 10, 'Use: Art'),
(624, 'Faber-Castell Sketch Pen 24-Col','Stationery', 149.00, 20, 'Use: Art'),
(625, 'Luxor Permanent Marker 10pk',    'Stationery', 199.00, 16, 'Use: Office'),
(626, 'Stabilo Highlighter 6-Color',    'Stationery',  99.00, 25, 'Use: Office'),
(627, 'Camlin Correction Fluid 20ml',   'Stationery',  29.00, 50, 'Use: Office'),
(628, 'Faber-Castell Correction Tape',  'Stationery',  49.00, 40, 'Use: Office'),
(629, 'Leitz Expanding File Folder',    'Stationery', 149.00, 18, 'Use: Office'),
(630, 'Archies File Folder A4 10pk',    'Stationery',  99.00, 22, 'Use: Office'),
(631, 'Kores Ring Binder 2-inch',       'Stationery', 149.00, 15, 'Use: Office'),
(632, 'Maped Sheet Protector 50pk',     'Stationery', 149.00, 18, 'Use: Office'),
(633, 'Rexel Index Dividers 10pk',      'Stationery',  99.00, 22, 'Use: Office'),
(634, 'Avery Label Sticker A4 20sh',    'Stationery',  99.00, 18, 'Use: Office'),
(635, 'Cello Pen Stand Desk',           'Stationery', 149.00, 14, 'Use: Office'),
(636, 'Fellowes Mesh Desk Organiser',   'Stationery', 299.00, 10, 'Use: Office'),
(637, 'Logitech Desk Mouse Pad XL',     'Stationery', 299.00, 12, 'Use: Office'),
(638, 'Velcro Cable Organiser Clips',   'Stationery',  99.00, 20, 'Use: Office'),
(639, 'Elan Book Stand Wooden',         'Stationery', 399.00,  8, 'Use: Study'),
(640, 'Nulaxy Laptop Riser Stand',      'Stationery', 499.00,  8, 'Use: Office'),
(641, 'AT-A-GLANCE Daily Planner 2025', 'Stationery', 299.00, 12, 'Use: Planning'),
(642, 'Rhodia Hard Cover Diary',        'Stationery', 199.00, 15, 'Use: Planning'),
(643, 'Leuchtturm1917 Bullet Journal',  'Stationery', 249.00, 12, 'Use: Planning'),
(644, 'Post-It Pop-Up Note Refill 90sh','Stationery', 149.00, 20, 'Use: Office'),
(645, 'Oxford Memo Cube 500 Sheets',    'Stationery',  99.00, 25, 'Use: Office');

-- ================================================================
-- 6. TRANSACTIONS  (43 transactions spanning the past 60 days)
--    cashier_id is assigned deterministically via CRC32 mod 9.
--    INSERT IGNORE makes this safe to re-run on an existing DB.
-- ================================================================

INSERT IGNORE INTO transactions (txn_id,customer_id,total_amount,coupon_pct,txn_time,cashier_id) VALUES
('FMA3B2C1D4E5F6', 1002,  556.25,  5, DATE_SUB(NOW(), INTERVAL 60 DAY), (ABS(CRC32('FMA3B2C1D4E5F6')) % 9)+1);
INSERT IGNORE INTO txn_items (txn_id,product_id,product_name,quantity,unit_price,subtotal) VALUES
('FMA3B2C1D4E5F6', 103, 'Tata Salt 1kg',            2,  19.00,  38.00),
('FMA3B2C1D4E5F6', 105, 'Maggi Noodles 70g x6',     3,  90.25, 270.75),
('FMA3B2C1D4E5F6', 128, 'Lays Classic Salted 80g',  4,  28.50, 114.00),
('FMA3B2C1D4E5F6', 148, 'Maggi Masala Noodles 70g', 5,  13.30,  66.50),
('FMA3B2C1D4E5F6', 614, 'Fevistik Glue Stick Large',2,  46.55,  93.10);

INSERT IGNORE INTO transactions (txn_id,customer_id,total_amount,coupon_pct,txn_time,cashier_id) VALUES
('FM7E8F9A0B1C2D', 1005, 5648.00, 10, DATE_SUB(NOW(), INTERVAL 59 DAY), (ABS(CRC32('FM7E8F9A0B1C2D')) % 9)+1);
INSERT IGNORE INTO txn_items (txn_id,product_id,product_name,quantity,unit_price,subtotal) VALUES
('FM7E8F9A0B1C2D', 301, 'Samsung Galaxy Buds2',      1, 5399.10, 5399.10),
('FM7E8F9A0B1C2D', 128, 'Lays Classic Salted 80g',   5,   28.50,  142.50),
('FM7E8F9A0B1C2D', 148, 'Maggi Masala Noodles 70g',  8,   13.30,  106.40);

INSERT IGNORE INTO transactions (txn_id,customer_id,total_amount,coupon_pct,txn_time,cashier_id) VALUES
('FM2D4F6A8C0E1B', 1001, 3591.63,  0, DATE_SUB(NOW(), INTERVAL 58 DAY), (ABS(CRC32('FM2D4F6A8C0E1B')) % 9)+1);
INSERT IGNORE INTO txn_items (txn_id,product_id,product_name,quantity,unit_price,subtotal) VALUES
('FM2D4F6A8C0E1B', 401, 'Levis 511 Slim Fit Jeans',  1, 2124.15, 2124.15),
('FM2D4F6A8C0E1B', 403, 'Jockey Cotton T-Shirt',     2,  594.15, 1188.30),
('FM2D4F6A8C0E1B', 602, 'Reynolds 045 Pen 10-Pack',  3,   93.06,  279.18);

INSERT IGNORE INTO transactions (txn_id,customer_id,total_amount,coupon_pct,txn_time,cashier_id) VALUES
('FM9C1A3E5B7D8F', 1010,  448.65,  0, DATE_SUB(NOW(), INTERVAL 57 DAY), (ABS(CRC32('FM9C1A3E5B7D8F')) % 9)+1);
INSERT IGNORE INTO txn_items (txn_id,product_id,product_name,quantity,unit_price,subtotal) VALUES
('FM9C1A3E5B7D8F', 451, 'Lakme Eyeconic Kajal',       2, 160.55, 321.10),
('FM9C1A3E5B7D8F', 455, 'Himalaya Neem Face Wash 150ml',1,141.55,141.55),
('FM9C1A3E5B7D8F', 614, 'Fevistik Glue Stick Large',  2,  46.55,  93.10);

INSERT IGNORE INTO transactions (txn_id,customer_id,total_amount,coupon_pct,txn_time,cashier_id) VALUES
('FM4B6D8F0A2C3E', 1003, 1763.35,  5, DATE_SUB(NOW(), INTERVAL 56 DAY), (ABS(CRC32('FM4B6D8F0A2C3E')) % 9)+1);
INSERT IGNORE INTO txn_items (txn_id,product_id,product_name,quantity,unit_price,subtotal) VALUES
('FM4B6D8F0A2C3E', 302, 'boAt Rockerz 450 Headphones',1, 1349.10, 1349.10),
('FM4B6D8F0A2C3E', 108, 'Parle-G Biscuits 800g',      3,   80.75,  242.25),
('FM4B6D8F0A2C3E', 126, 'Cadbury Dairy Milk 100g',    2,   76.00,  152.00);

INSERT IGNORE INTO transactions (txn_id,customer_id,total_amount,coupon_pct,txn_time,cashier_id) VALUES
('FM0E2A4C6F8B9D', 1008, 2076.66,  0, DATE_SUB(NOW(), INTERVAL 55 DAY), (ABS(CRC32('FM0E2A4C6F8B9D')) % 9)+1);
INSERT IGNORE INTO txn_items (txn_id,product_id,product_name,quantity,unit_price,subtotal) VALUES
('FM0E2A4C6F8B9D', 501, 'Cosco Football Size 5',     1,  735.08,  735.08),
('FM0E2A4C6F8B9D', 503, 'Li-Ning Badminton Racket',  1, 1199.08, 1199.08),
('FM0E2A4C6F8B9D', 128, 'Lays Classic Salted 80g',   5,   28.50,  142.50);

INSERT IGNORE INTO transactions (txn_id,customer_id,total_amount,coupon_pct,txn_time,cashier_id) VALUES
('FM5A7C9E1B3D4F', 1015,  514.82,  0, DATE_SUB(NOW(), INTERVAL 54 DAY), (ABS(CRC32('FM5A7C9E1B3D4F')) % 9)+1);
INSERT IGNORE INTO txn_items (txn_id,product_id,product_name,quantity,unit_price,subtotal) VALUES
('FM5A7C9E1B3D4F', 601, 'Classmate Notebook A4 200pg x5',1,189.05,189.05),
('FM5A7C9E1B3D4F', 609, 'Faber-Castell Pencil Set 12B',  2, 93.06, 186.12),
('FM5A7C9E1B3D4F', 614, 'Fevistik Glue Stick Large',     3, 46.55, 139.65);

INSERT IGNORE INTO transactions (txn_id,customer_id,total_amount,coupon_pct,txn_time,cashier_id) VALUES
('FM8F0B2D4E6A7C', 1020, 2391.40,  0, DATE_SUB(NOW(), INTERVAL 53 DAY), (ABS(CRC32('FM8F0B2D4E6A7C')) % 9)+1);
INSERT IGNORE INTO txn_items (txn_id,product_id,product_name,quantity,unit_price,subtotal) VALUES
('FM8F0B2D4E6A7C', 304, 'Anker PowerCore 10000mAh',   1, 1619.10, 1619.10),
('FM8F0B2D4E6A7C', 307, 'Logitech M185 Wireless Mouse',1,  719.10,  719.10),
('FM8F0B2D4E6A7C', 148, 'Maggi Masala Noodles 70g',   4,   13.30,   53.20);

INSERT IGNORE INTO transactions (txn_id,customer_id,total_amount,coupon_pct,txn_time,cashier_id) VALUES
('FM3C5E7A9D1F2B', 1002,  866.40, 10, DATE_SUB(NOW(), INTERVAL 52 DAY), (ABS(CRC32('FM3C5E7A9D1F2B')) % 9)+1);
INSERT IGNORE INTO txn_items (txn_id,product_id,product_name,quantity,unit_price,subtotal) VALUES
('FM3C5E7A9D1F2B', 452, 'Maybelline Fit Me Foundation',1, 449.10, 449.10),
('FM3C5E7A9D1F2B', 460, 'Wow ACV Shampoo',             1, 331.65, 331.65),
('FM3C5E7A9D1F2B', 128, 'Lays Classic Salted 80g',     3,  28.50,  85.50);

INSERT IGNORE INTO transactions (txn_id,customer_id,total_amount,coupon_pct,txn_time,cashier_id) VALUES
('FM6A8C0D2F4E5B', 1007, 2435.45,  0, DATE_SUB(NOW(), INTERVAL 51 DAY), (ABS(CRC32('FM6A8C0D2F4E5B')) % 9)+1);
INSERT IGNORE INTO txn_items (txn_id,product_id,product_name,quantity,unit_price,subtotal) VALUES
('FM6A8C0D2F4E5B', 404, 'Adidas Track Pants',          1, 1274.15, 1274.15),
('FM6A8C0D2F4E5B', 408, 'US Polo Assn T-Shirt Pack 2', 1, 1104.15, 1104.15),
('FM6A8C0D2F4E5B', 103, 'Tata Salt 1kg',               3,   19.00,   57.00);

INSERT IGNORE INTO transactions (txn_id,customer_id,total_amount,coupon_pct,txn_time,cashier_id) VALUES
('FM1D3F5B7E9A0C', 1011,  455.05,  0, DATE_SUB(NOW(), INTERVAL 50 DAY), (ABS(CRC32('FM1D3F5B7E9A0C')) % 9)+1);
INSERT IGNORE INTO txn_items (txn_id,product_id,product_name,quantity,unit_price,subtotal) VALUES
('FM1D3F5B7E9A0C', 105, 'Maggi Noodles 70g x6',      2,  90.25, 180.50),
('FM1D3F5B7E9A0C', 126, 'Cadbury Dairy Milk 100g',   3,  76.00, 228.00),
('FM1D3F5B7E9A0C', 614, 'Fevistik Glue Stick Large', 1,  46.55,  46.55);

INSERT IGNORE INTO transactions (txn_id,customer_id,total_amount,coupon_pct,txn_time,cashier_id) VALUES
('FM4E6A8B0C2D3F', 1016, 3210.45,  5, DATE_SUB(NOW(), INTERVAL 49 DAY), (ABS(CRC32('FM4E6A8B0C2D3F')) % 9)+1);
INSERT IGNORE INTO txn_items (txn_id,product_id,product_name,quantity,unit_price,subtotal) VALUES
('FM4E6A8B0C2D3F', 303, 'JBL Go 3 Bluetooth Speaker', 1, 2249.10, 2249.10),
('FM4E6A8B0C2D3F', 327, 'Samsung 128GB MicroSD Card',  1,  719.10,  719.10),
('FM4E6A8B0C2D3F', 108, 'Parle-G Biscuits 800g',       3,   80.75,  242.25);

INSERT IGNORE INTO transactions (txn_id,customer_id,total_amount,coupon_pct,txn_time,cashier_id) VALUES
('FM9B1C3D5A7F8E', 1004,  286.86,  0, DATE_SUB(NOW(), INTERVAL 48 DAY), (ABS(CRC32('FM9B1C3D5A7F8E')) % 9)+1);
INSERT IGNORE INTO txn_items (txn_id,product_id,product_name,quantity,unit_price,subtotal) VALUES
('FM9B1C3D5A7F8E', 128, 'Lays Classic Salted 80g',     4,  28.50, 114.00),
('FM9B1C3D5A7F8E', 148, 'Maggi Masala Noodles 70g',    6,  13.30,  79.80),
('FM9B1C3D5A7F8E', 626, 'Stabilo Highlighter 6-Color', 1,  93.06,  93.06);

INSERT IGNORE INTO transactions (txn_id,customer_id,total_amount,coupon_pct,txn_time,cashier_id) VALUES
('FM2F4A6B8C0E1D', 1009, 2593.92,  0, DATE_SUB(NOW(), INTERVAL 47 DAY), (ABS(CRC32('FM2F4A6B8C0E1D')) % 9)+1);
INSERT IGNORE INTO txn_items (txn_id,product_id,product_name,quantity,unit_price,subtotal) VALUES
('FM2F4A6B8C0E1D', 401, 'Levis 511 Slim Fit Jeans',    1, 2124.15, 2124.15),
('FM2F4A6B8C0E1D', 467, 'Vaseline Intensive Care 400ml',1,  283.65,  283.65),
('FM2F4A6B8C0E1D', 602, 'Reynolds 045 Pen 10-Pack',    2,   93.06,  186.12);

INSERT IGNORE INTO transactions (txn_id,customer_id,total_amount,coupon_pct,txn_time,cashier_id) VALUES
('FM7C9D1E3F5A6B', 1022,  518.70,  0, DATE_SUB(NOW(), INTERVAL 46 DAY), (ABS(CRC32('FM7C9D1E3F5A6B')) % 9)+1);
INSERT IGNORE INTO txn_items (txn_id,product_id,product_name,quantity,unit_price,subtotal) VALUES
('FM7C9D1E3F5A6B', 111, 'Tata Tea Gold 500g',          1, 237.50, 237.50),
('FM7C9D1E3F5A6B', 110, 'Haldiram Bhujia 400g',        2,  94.05, 188.10),
('FM7C9D1E3F5A6B', 626, 'Stabilo Highlighter 6-Color', 1,  93.06,  93.06);

INSERT IGNORE INTO transactions (txn_id,customer_id,total_amount,coupon_pct,txn_time,cashier_id) VALUES
('FM0A2B4C6D8E9F', 1001, 3161.45, 10, DATE_SUB(NOW(), INTERVAL 45 DAY), (ABS(CRC32('FM0A2B4C6D8E9F')) % 9)+1);
INSERT IGNORE INTO txn_items (txn_id,product_id,product_name,quantity,unit_price,subtotal) VALUES
('FM0A2B4C6D8E9F', 302, 'boAt Rockerz 450 Headphones', 1, 1349.10, 1349.10),
('FM0A2B4C6D8E9F', 310, 'Syska LED Bulb 9W Pack 4',    2,  269.10,  538.20),
('FM0A2B4C6D8E9F', 404, 'Adidas Track Pants',           1, 1274.15, 1274.15);

INSERT IGNORE INTO transactions (txn_id,customer_id,total_amount,coupon_pct,txn_time,cashier_id) VALUES
('FM5D7F9A0B1C2E', 1002,  197.60,  0, DATE_SUB(NOW(), INTERVAL 44 DAY), (ABS(CRC32('FM5D7F9A0B1C2E')) % 9)+1);
INSERT IGNORE INTO txn_items (txn_id,product_id,product_name,quantity,unit_price,subtotal) VALUES
('FM5D7F9A0B1C2E', 103, 'Tata Salt 1kg',               3,  19.00,  57.00),
('FM5D7F9A0B1C2E', 148, 'Maggi Masala Noodles 70g',    5,  13.30,  66.50),
('FM5D7F9A0B1C2E', 614, 'Fevistik Glue Stick Large',   2,  46.55,  93.10);

INSERT IGNORE INTO transactions (txn_id,customer_id,total_amount,coupon_pct,txn_time,cashier_id) VALUES
('FM8E0F2A4B5C6D', 1013, 6318.20,  0, DATE_SUB(NOW(), INTERVAL 43 DAY), (ABS(CRC32('FM8E0F2A4B5C6D')) % 9)+1);
INSERT IGNORE INTO txn_items (txn_id,product_id,product_name,quantity,unit_price,subtotal) VALUES
('FM8E0F2A4B5C6D', 301, 'Samsung Galaxy Buds2',        1, 5399.10, 5399.10),
('FM8E0F2A4B5C6D', 308, 'HP USB Wired Keyboard',       1,  539.10,  539.10),
('FM8E0F2A4B5C6D', 126, 'Cadbury Dairy Milk 100g',     5,   76.00,  380.00);

INSERT IGNORE INTO transactions (txn_id,customer_id,total_amount,coupon_pct,txn_time,cashier_id) VALUES
('FM3A5B7C9D0E1F', 1017,  863.26,  5, DATE_SUB(NOW(), INTERVAL 42 DAY), (ABS(CRC32('FM3A5B7C9D0E1F')) % 9)+1);
INSERT IGNORE INTO txn_items (txn_id,product_id,product_name,quantity,unit_price,subtotal) VALUES
('FM3A5B7C9D0E1F', 452, 'Maybelline Fit Me Foundation', 1, 449.10, 449.10),
('FM3A5B7C9D0E1F', 451, 'Lakme Eyeconic Kajal',         2, 160.55, 321.10),
('FM3A5B7C9D0E1F', 626, 'Stabilo Highlighter 6-Color',  1,  93.06,  93.06);

INSERT IGNORE INTO transactions (txn_id,customer_id,total_amount,coupon_pct,txn_time,cashier_id) VALUES
('FM6B8C0E2F3A4D', 1005, 1527.49,  0, DATE_SUB(NOW(), INTERVAL 41 DAY), (ABS(CRC32('FM6B8C0E2F3A4D')) % 9)+1);
INSERT IGNORE INTO txn_items (txn_id,product_id,product_name,quantity,unit_price,subtotal) VALUES
('FM6B8C0E2F3A4D', 502, 'Yonex Mavis Shuttle 6pk',           2,  459.08,  918.16),
('FM6B8C0E2F3A4D', 528, 'Strauss Skipping Rope w/ Counter',  1,  367.08,  367.08),
('FM6B8C0E2F3A4D', 108, 'Parle-G Biscuits 800g',             3,   80.75,  242.25);

INSERT IGNORE INTO transactions (txn_id,customer_id,total_amount,coupon_pct,txn_time,cashier_id) VALUES
('FM1E3F5A7B8C9D', 1018, 1420.25,  0, DATE_SUB(NOW(), INTERVAL 40 DAY), (ABS(CRC32('FM1E3F5A7B8C9D')) % 9)+1);
INSERT IGNORE INTO txn_items (txn_id,product_id,product_name,quantity,unit_price,subtotal) VALUES
('FM1E3F5A7B8C9D', 106, 'Aashirvaad Atta 5kg',         2, 266.00, 532.00),
('FM1E3F5A7B8C9D', 101, 'Basmati Rice 5kg',             2, 237.50, 475.00),
('FM1E3F5A7B8C9D', 104, 'Fortune Sunflower Oil 1L',     3, 137.75, 413.25);

INSERT IGNORE INTO transactions (txn_id,customer_id,total_amount,coupon_pct,txn_time,cashier_id) VALUES
('FM4F6A8D0E1B2C', 1024, 2577.72,  0, DATE_SUB(NOW(), INTERVAL 39 DAY), (ABS(CRC32('FM4F6A8D0E1B2C')) % 9)+1);
INSERT IGNORE INTO txn_items (txn_id,product_id,product_name,quantity,unit_price,subtotal) VALUES
('FM4F6A8D0E1B2C', 303, 'JBL Go 3 Bluetooth Speaker', 1, 2249.10, 2249.10),
('FM4F6A8D0E1B2C', 128, 'Lays Classic Salted 80g',    5,   28.50,  142.50),
('FM4F6A8D0E1B2C', 602, 'Reynolds 045 Pen 10-Pack',   2,   93.06,  186.12);

INSERT IGNORE INTO transactions (txn_id,customer_id,total_amount,coupon_pct,txn_time,cashier_id) VALUES
('FM9C0D1F3A5B6E', 1006, 1906.50,  5, DATE_SUB(NOW(), INTERVAL 38 DAY), (ABS(CRC32('FM9C0D1F3A5B6E')) % 9)+1);
INSERT IGNORE INTO txn_items (txn_id,product_id,product_name,quantity,unit_price,subtotal) VALUES
('FM9C0D1F3A5B6E', 403, 'Jockey Cotton T-Shirt',          2,  594.15, 1188.30),
('FM9C0D1F3A5B6E', 418, 'VIP Frenchie Plus Briefs 3pk',   2,  339.15,  678.30),
('FM9C0D1F3A5B6E', 148, 'Maggi Masala Noodles 70g',       3,   13.30,   39.90);

INSERT IGNORE INTO transactions (txn_id,customer_id,total_amount,coupon_pct,txn_time,cashier_id) VALUES
('FM2A4C6D8F9E0B', 1002,  569.15,  0, DATE_SUB(NOW(), INTERVAL 37 DAY), (ABS(CRC32('FM2A4C6D8F9E0B')) % 9)+1);
INSERT IGNORE INTO txn_items (txn_id,product_id,product_name,quantity,unit_price,subtotal) VALUES
('FM2A4C6D8F9E0B', 460, 'Wow ACV Shampoo',                1, 331.65, 331.65),
('FM2A4C6D8F9E0B', 126, 'Cadbury Dairy Milk 100g',        2,  76.00, 152.00),
('FM2A4C6D8F9E0B', 128, 'Lays Classic Salted 80g',        3,  28.50,  85.50);

INSERT IGNORE INTO transactions (txn_id,customer_id,total_amount,coupon_pct,txn_time,cashier_id) VALUES
('FM7B9E0F2A3C4D', 1019, 1736.90, 10, DATE_SUB(NOW(), INTERVAL 36 DAY), (ABS(CRC32('FM7B9E0F2A3C4D')) % 9)+1);
INSERT IGNORE INTO txn_items (txn_id,product_id,product_name,quantity,unit_price,subtotal) VALUES
('FM7B9E0F2A3C4D', 304, 'Anker PowerCore 10000mAh',   1, 1619.10, 1619.10),
('FM7B9E0F2A3C4D', 148, 'Maggi Masala Noodles 70g',   6,   13.30,   79.80),
('FM7B9E0F2A3C4D', 103, 'Tata Salt 1kg',               2,   19.00,   38.00);

INSERT IGNORE INTO transactions (txn_id,customer_id,total_amount,coupon_pct,txn_time,cashier_id) VALUES
('FM0C2D4E6B7F8A', 1001,  760.25,  0, DATE_SUB(NOW(), INTERVAL 35 DAY), (ABS(CRC32('FM0C2D4E6B7F8A')) % 9)+1);
INSERT IGNORE INTO txn_items (txn_id,product_id,product_name,quantity,unit_price,subtotal) VALUES
('FM0C2D4E6B7F8A', 111, 'Tata Tea Gold 500g',    1, 237.50, 237.50),
('FM0C2D4E6B7F8A', 112, 'Nescafe Classic 50g',   1, 280.25, 280.25),
('FM0C2D4E6B7F8A', 108, 'Parle-G Biscuits 800g', 3,  80.75, 242.25);

INSERT IGNORE INTO transactions (txn_id,customer_id,total_amount,coupon_pct,txn_time,cashier_id) VALUES
('FM5F7A9B0D1E2C', 1012, 4502.45,  0, DATE_SUB(NOW(), INTERVAL 34 DAY), (ABS(CRC32('FM5F7A9B0D1E2C')) % 9)+1);
INSERT IGNORE INTO txn_items (txn_id,product_id,product_name,quantity,unit_price,subtotal) VALUES
('FM5F7A9B0D1E2C', 401, 'Levis 511 Slim Fit Jeans',   1, 2124.15, 2124.15),
('FM5F7A9B0D1E2C', 402, 'Peter England Formal Shirt',  1, 1104.15, 1104.15),
('FM5F7A9B0D1E2C', 404, 'Adidas Track Pants',           1, 1274.15, 1274.15);

INSERT IGNORE INTO transactions (txn_id,customer_id,total_amount,coupon_pct,txn_time,cashier_id) VALUES
('FM8A0B1C3D4F5E', 1010,  346.71,  0, DATE_SUB(NOW(), INTERVAL 33 DAY), (ABS(CRC32('FM8A0B1C3D4F5E')) % 9)+1);
INSERT IGNORE INTO txn_items (txn_id,product_id,product_name,quantity,unit_price,subtotal) VALUES
('FM8A0B1C3D4F5E', 451, 'Lakme Eyeconic Kajal',       1, 160.55, 160.55),
('FM8A0B1C3D4F5E', 626, 'Stabilo Highlighter 6-Color',1,  93.06,  93.06),
('FM8A0B1C3D4F5E', 614, 'Fevistik Glue Stick Large',  2,  46.55,  93.10);

INSERT IGNORE INTO transactions (txn_id,customer_id,total_amount,coupon_pct,txn_time,cashier_id) VALUES
('FM3E5F7A9C0D1B', 1023, 2116.40,  5, DATE_SUB(NOW(), INTERVAL 32 DAY), (ABS(CRC32('FM3E5F7A9C0D1B')) % 9)+1);
INSERT IGNORE INTO txn_items (txn_id,product_id,product_name,quantity,unit_price,subtotal) VALUES
('FM3E5F7A9C0D1B', 302, 'boAt Rockerz 450 Headphones',1, 1349.10, 1349.10),
('FM3E5F7A9C0D1B', 327, 'Samsung 128GB MicroSD Card', 1,  719.10,  719.10),
('FM3E5F7A9C0D1B', 148, 'Maggi Masala Noodles 70g',   4,   13.30,   53.20);

INSERT IGNORE INTO transactions (txn_id,customer_id,total_amount,coupon_pct,txn_time,cashier_id) VALUES
('FM6D8E9F0A2B3C', 1005,  639.58,  0, DATE_SUB(NOW(), INTERVAL 31 DAY), (ABS(CRC32('FM6D8E9F0A2B3C')) % 9)+1);
INSERT IGNORE INTO txn_items (txn_id,product_id,product_name,quantity,unit_price,subtotal) VALUES
('FM6D8E9F0A2B3C', 502, 'Yonex Mavis Shuttle 6pk',    1, 459.08, 459.08),
('FM6D8E9F0A2B3C', 128, 'Lays Classic Salted 80g',    4,  28.50, 114.00),
('FM6D8E9F0A2B3C', 148, 'Maggi Masala Noodles 70g',   5,  13.30,  66.50);

INSERT IGNORE INTO transactions (txn_id,customer_id,total_amount,coupon_pct,txn_time,cashier_id) VALUES
('FM1F3A5B6C7D8E', 1002, 1064.40,  0, DATE_SUB(NOW(), INTERVAL 30 DAY), (ABS(CRC32('FM1F3A5B6C7D8E')) % 9)+1);
INSERT IGNORE INTO txn_items (txn_id,product_id,product_name,quantity,unit_price,subtotal) VALUES
('FM1F3A5B6C7D8E', 460, 'Wow ACV Shampoo',               1, 331.65, 331.65),
('FM1F3A5B6C7D8E', 467, 'Vaseline Intensive Care 400ml',  1, 283.65, 283.65),
('FM1F3A5B6C7D8E', 452, 'Maybelline Fit Me Foundation',   1, 449.10, 449.10);

INSERT IGNORE INTO transactions (txn_id,customer_id,total_amount,coupon_pct,txn_time,cashier_id) VALUES
('FM4A6C8D0E1F2B', 1001, 2638.60,  0, DATE_SUB(NOW(), INTERVAL 29 DAY), (ABS(CRC32('FM4A6C8D0E1F2B')) % 9)+1);
INSERT IGNORE INTO txn_items (txn_id,product_id,product_name,quantity,unit_price,subtotal) VALUES
('FM4A6C8D0E1F2B', 303, 'JBL Go 3 Bluetooth Speaker',1, 2249.10, 2249.10),
('FM4A6C8D0E1F2B', 108, 'Parle-G Biscuits 800g',     2,   80.75,  161.50),
('FM4A6C8D0E1F2B', 126, 'Cadbury Dairy Milk 100g',   3,   76.00,  228.00);

INSERT IGNORE INTO transactions (txn_id,customer_id,total_amount,coupon_pct,txn_time,cashier_id) VALUES
('FM9B0C1D2E3F4A', 1014,  750.34,  5, DATE_SUB(NOW(), INTERVAL 28 DAY), (ABS(CRC32('FM9B0C1D2E3F4A')) % 9)+1);
INSERT IGNORE INTO txn_items (txn_id,product_id,product_name,quantity,unit_price,subtotal) VALUES
('FM9B0C1D2E3F4A', 601, 'Classmate Notebook A4 200pg x5',2, 189.05, 378.10),
('FM9B0C1D2E3F4A', 602, 'Reynolds 045 Pen 10-Pack',      2,  93.06, 186.12),
('FM9B0C1D2E3F4A', 609, 'Faber-Castell Pencil Set 12B',  2,  93.06, 186.12);

INSERT IGNORE INTO transactions (txn_id,customer_id,total_amount,coupon_pct,txn_time,cashier_id) VALUES
('FM2C4E5F6A7B8D', 1007, 2047.10,  0, DATE_SUB(NOW(), INTERVAL 27 DAY), (ABS(CRC32('FM2C4E5F6A7B8D')) % 9)+1);
INSERT IGNORE INTO txn_items (txn_id,product_id,product_name,quantity,unit_price,subtotal) VALUES
('FM2C4E5F6A7B8D', 403, 'Jockey Cotton T-Shirt',          2,  594.15, 1188.30),
('FM2C4E5F6A7B8D', 418, 'VIP Frenchie Plus Briefs 3pk',   2,  339.15,  678.30),
('FM2C4E5F6A7B8D', 105, 'Maggi Noodles 70g x6',           2,   90.25,  180.50);

INSERT IGNORE INTO transactions (txn_id,customer_id,total_amount,coupon_pct,txn_time,cashier_id) VALUES
('FM7D9F0A1B2C3E', 1011,  652.65,  0, DATE_SUB(NOW(), INTERVAL 26 DAY), (ABS(CRC32('FM7D9F0A1B2C3E')) % 9)+1);
INSERT IGNORE INTO txn_items (txn_id,product_id,product_name,quantity,unit_price,subtotal) VALUES
('FM7D9F0A1B2C3E', 110, 'Haldiram Bhujia 400g',     3,  94.05, 282.15),
('FM7D9F0A1B2C3E', 126, 'Cadbury Dairy Milk 100g',  3,  76.00, 228.00),
('FM7D9F0A1B2C3E', 128, 'Lays Classic Salted 80g',  5,  28.50, 142.50);

INSERT IGNORE INTO transactions (txn_id,customer_id,total_amount,coupon_pct,txn_time,cashier_id) VALUES
('FM0E2F3A4C5B6D', 1025, 2427.30, 10, DATE_SUB(NOW(), INTERVAL 25 DAY), (ABS(CRC32('FM0E2F3A4C5B6D')) % 9)+1);
INSERT IGNORE INTO txn_items (txn_id,product_id,product_name,quantity,unit_price,subtotal) VALUES
('FM0E2F3A4C5B6D', 304, 'Anker PowerCore 10000mAh',   1, 1619.10, 1619.10),
('FM0E2F3A4C5B6D', 308, 'HP USB Wired Keyboard',       1,  539.10,  539.10),
('FM0E2F3A4C5B6D', 310, 'Syska LED Bulb 9W Pack 4',    1,  269.10,  269.10);

INSERT IGNORE INTO transactions (txn_id,customer_id,total_amount,coupon_pct,txn_time,cashier_id) VALUES
('FM5A7B8C9D0E1F', 1002,  209.00,  0, DATE_SUB(NOW(), INTERVAL 24 DAY), (ABS(CRC32('FM5A7B8C9D0E1F')) % 9)+1);
INSERT IGNORE INTO txn_items (txn_id,product_id,product_name,quantity,unit_price,subtotal) VALUES
('FM5A7B8C9D0E1F', 103, 'Tata Salt 1kg',               3,  19.00,  57.00),
('FM5A7B8C9D0E1F', 148, 'Maggi Masala Noodles 70g',    5,  13.30,  66.50),
('FM5A7B8C9D0E1F', 128, 'Lays Classic Salted 80g',     3,  28.50,  85.50);

INSERT IGNORE INTO transactions (txn_id,customer_id,total_amount,coupon_pct,txn_time,cashier_id) VALUES
('FM8C0D1E2F3A4B', 1003, 3615.75,  5, DATE_SUB(NOW(), INTERVAL 23 DAY), (ABS(CRC32('FM8C0D1E2F3A4B')) % 9)+1);
INSERT IGNORE INTO txn_items (txn_id,product_id,product_name,quantity,unit_price,subtotal) VALUES
('FM8C0D1E2F3A4B', 401, 'Levis 511 Slim Fit Jeans',    1, 2124.15, 2124.15),
('FM8C0D1E2F3A4B', 302, 'boAt Rockerz 450 Headphones', 1, 1349.10, 1349.10),
('FM8C0D1E2F3A4B', 128, 'Lays Classic Salted 80g',     5,   28.50,  142.50);

INSERT IGNORE INTO transactions (txn_id,customer_id,total_amount,coupon_pct,txn_time,cashier_id) VALUES
('FM3F5A6B7C8D9E', 1008, 1110.01,  0, DATE_SUB(NOW(), INTERVAL 22 DAY), (ABS(CRC32('FM3F5A6B7C8D9E')) % 9)+1);
INSERT IGNORE INTO txn_items (txn_id,product_id,product_name,quantity,unit_price,subtotal) VALUES
('FM3F5A6B7C8D9E', 501, 'Cosco Football Size 5',              1, 735.08, 735.08),
('FM3F5A6B7C8D9E', 508, 'Nivia Hydro Track Water Bottle 1L',  1, 321.65, 321.65),
('FM3F5A6B7C8D9E', 148, 'Maggi Masala Noodles 70g',           4,  13.30,  53.20);

INSERT IGNORE INTO transactions (txn_id,customer_id,total_amount,coupon_pct,txn_time,cashier_id) VALUES
('FM6B8C9D0E1A2F', 1021,  702.75,  0, DATE_SUB(NOW(), INTERVAL 21 DAY), (ABS(CRC32('FM6B8C9D0E1A2F')) % 9)+1);
INSERT IGNORE INTO txn_items (txn_id,product_id,product_name,quantity,unit_price,subtotal) VALUES
('FM6B8C9D0E1A2F', 452, 'Maybelline Fit Me Foundation',1, 449.10, 449.10),
('FM6B8C9D0E1A2F', 451, 'Lakme Eyeconic Kajal',        1, 160.55, 160.55),
('FM6B8C9D0E1A2F', 614, 'Fevistik Glue Stick Large',   2,  46.55,  93.10);

INSERT IGNORE INTO transactions (txn_id,customer_id,total_amount,coupon_pct,txn_time,cashier_id) VALUES
('FM1D3E4F5A6B7C', 1001, 2416.30,  0, DATE_SUB(NOW(), INTERVAL 14 DAY), (ABS(CRC32('FM1D3E4F5A6B7C')) % 9)+1);
INSERT IGNORE INTO txn_items (txn_id,product_id,product_name,quantity,unit_price,subtotal) VALUES
('FM1D3E4F5A6B7C', 404, 'Adidas Track Pants',           1, 1274.15, 1274.15),
('FM1D3E4F5A6B7C', 408, 'US Polo Assn T-Shirt Pack 2',  1, 1104.15, 1104.15),
('FM1D3E4F5A6B7C', 103, 'Tata Salt 1kg',                2,   19.00,   38.00);

INSERT IGNORE INTO transactions (txn_id,customer_id,total_amount,coupon_pct,txn_time,cashier_id) VALUES
('FM4E6F7A8B9C0D', 1002,  586.15,  5, DATE_SUB(NOW(), INTERVAL 13 DAY), (ABS(CRC32('FM4E6F7A8B9C0D')) % 9)+1);
INSERT IGNORE INTO txn_items (txn_id,product_id,product_name,quantity,unit_price,subtotal) VALUES
('FM4E6F7A8B9C0D', 460, 'Wow ACV Shampoo',              1, 331.65, 331.65),
('FM4E6F7A8B9C0D', 126, 'Cadbury Dairy Milk 100g',      2,  76.00, 152.00),
('FM4E6F7A8B9C0D', 128, 'Lays Classic Salted 80g',      5,  28.50, 142.50);

INSERT IGNORE INTO transactions (txn_id,customer_id,total_amount,coupon_pct,txn_time,cashier_id) VALUES
('FM9F0A1B2C3D4E', 1016, 3041.26,  0, DATE_SUB(NOW(), INTERVAL 12 DAY), (ABS(CRC32('FM9F0A1B2C3D4E')) % 9)+1);
INSERT IGNORE INTO txn_items (txn_id,product_id,product_name,quantity,unit_price,subtotal) VALUES
('FM9F0A1B2C3D4E', 303, 'JBL Go 3 Bluetooth Speaker', 1, 2249.10, 2249.10),
('FM9F0A1B2C3D4E', 327, 'Samsung 128GB MicroSD Card',  1,  719.10,  719.10),
('FM9F0A1B2C3D4E', 626, 'Stabilo Highlighter 6-Color', 1,   93.06,   93.06);

INSERT IGNORE INTO transactions (txn_id,customer_id,total_amount,coupon_pct,txn_time,cashier_id) VALUES
('FM2A3B4C5D6E7F', 1005, 1606.89,  0, DATE_SUB(NOW(), INTERVAL 11 DAY), (ABS(CRC32('FM2A3B4C5D6E7F')) % 9)+1);
INSERT IGNORE INTO txn_items (txn_id,product_id,product_name,quantity,unit_price,subtotal) VALUES
('FM2A3B4C5D6E7F', 502, 'Yonex Mavis Shuttle 6pk',            2, 459.08, 918.16),
('FM2A3B4C5D6E7F', 528, 'Strauss Skipping Rope w/ Counter',   1, 367.08, 367.08),
('FM2A3B4C5D6E7F', 508, 'Nivia Hydro Track Water Bottle 1L',  1, 321.65, 321.65);

INSERT IGNORE INTO transactions (txn_id,customer_id,total_amount,coupon_pct,txn_time,cashier_id) VALUES
('FM7B8C9D0F1A2E', 1009,  468.23,  0, DATE_SUB(NOW(), INTERVAL 10 DAY), (ABS(CRC32('FM7B8C9D0F1A2E')) % 9)+1);
INSERT IGNORE INTO txn_items (txn_id,product_id,product_name,quantity,unit_price,subtotal) VALUES
('FM7B8C9D0F1A2E', 601, 'Classmate Notebook A4 200pg x5',1, 189.05, 189.05),
('FM7B8C9D0F1A2E', 602, 'Reynolds 045 Pen 10-Pack',      2,  93.06, 186.12),
('FM7B8C9D0F1A2E', 609, 'Faber-Castell Pencil Set 12B',  1,  93.06,  93.06);

INSERT IGNORE INTO transactions (txn_id,customer_id,total_amount,coupon_pct,txn_time,cashier_id) VALUES
('FM0C1D2E3A4B5F', 1002, 2242.05, 10, DATE_SUB(NOW(), INTERVAL 9 DAY), (ABS(CRC32('FM0C1D2E3A4B5F')) % 9)+1);
INSERT IGNORE INTO txn_items (txn_id,product_id,product_name,quantity,unit_price,subtotal) VALUES
('FM0C1D2E3A4B5F', 302, 'boAt Rockerz 450 Headphones', 1, 1349.10, 1349.10),
('FM0C1D2E3A4B5F', 467, 'Vaseline Intensive Care 400ml',2,  283.65,  567.30),
('FM0C1D2E3A4B5F', 128, 'Lays Classic Salted 80g',      5,   28.50,  142.50);

INSERT IGNORE INTO transactions (txn_id,customer_id,total_amount,coupon_pct,txn_time,cashier_id) VALUES
('FM5D6E7F8A9B0C', 1020, 2696.40,  0, DATE_SUB(NOW(), INTERVAL 8 DAY), (ABS(CRC32('FM5D6E7F8A9B0C')) % 9)+1);
INSERT IGNORE INTO txn_items (txn_id,product_id,product_name,quantity,unit_price,subtotal) VALUES
('FM5D6E7F8A9B0C', 304, 'Anker PowerCore 10000mAh',   1, 1619.10, 1619.10),
('FM5D6E7F8A9B0C', 310, 'Syska LED Bulb 9W Pack 4',   2,  269.10,  538.20),
('FM5D6E7F8A9B0C', 308, 'HP USB Wired Keyboard',       1,  539.10,  539.10);

INSERT IGNORE INTO transactions (txn_id,customer_id,total_amount,coupon_pct,txn_time,cashier_id) VALUES
('FM8F9A0B1C2D3E', 1004,  406.60,  0, DATE_SUB(NOW(), INTERVAL 7 DAY), (ABS(CRC32('FM8F9A0B1C2D3E')) % 9)+1);
INSERT IGNORE INTO txn_items (txn_id,product_id,product_name,quantity,unit_price,subtotal) VALUES
('FM8F9A0B1C2D3E', 110, 'Haldiram Bhujia 400g',     2,  94.05, 188.10),
('FM8F9A0B1C2D3E', 126, 'Cadbury Dairy Milk 100g',  2,  76.00, 152.00),
('FM8F9A0B1C2D3E', 148, 'Maggi Masala Noodles 70g', 5,  13.30,  66.50);

INSERT IGNORE INTO transactions (txn_id,customer_id,total_amount,coupon_pct,txn_time,cashier_id) VALUES
('FM3A4B5C6D7E8F', 1001, 4556.75,  5, DATE_SUB(NOW(), INTERVAL 6 DAY), (ABS(CRC32('FM3A4B5C6D7E8F')) % 9)+1);
INSERT IGNORE INTO txn_items (txn_id,product_id,product_name,quantity,unit_price,subtotal) VALUES
('FM3A4B5C6D7E8F', 401, 'Levis 511 Slim Fit Jeans',    1, 2124.15, 2124.15),
('FM3A4B5C6D7E8F', 303, 'JBL Go 3 Bluetooth Speaker',  1, 2249.10, 2249.10),
('FM3A4B5C6D7E8F', 105, 'Maggi Noodles 70g x6',        2,   90.25,  180.50);

INSERT IGNORE INTO transactions (txn_id,customer_id,total_amount,coupon_pct,txn_time,cashier_id) VALUES
('FM6C7D8E9F0A1B', 1002,  237.50,  0, DATE_SUB(NOW(), INTERVAL 5 DAY), (ABS(CRC32('FM6C7D8E9F0A1B')) % 9)+1);
INSERT IGNORE INTO txn_items (txn_id,product_id,product_name,quantity,unit_price,subtotal) VALUES
('FM6C7D8E9F0A1B', 103, 'Tata Salt 1kg',               3,  19.00,  57.00),
('FM6C7D8E9F0A1B', 128, 'Lays Classic Salted 80g',     4,  28.50, 114.00),
('FM6C7D8E9F0A1B', 148, 'Maggi Masala Noodles 70g',    5,  13.30,  66.50);

INSERT IGNORE INTO transactions (txn_id,customer_id,total_amount,coupon_pct,txn_time,cashier_id) VALUES
('FM1E2F3A4B5C6D', 1013, 7141.30,  0, DATE_SUB(NOW(), INTERVAL 4 DAY), (ABS(CRC32('FM1E2F3A4B5C6D')) % 9)+1);
INSERT IGNORE INTO txn_items (txn_id,product_id,product_name,quantity,unit_price,subtotal) VALUES
('FM1E2F3A4B5C6D', 301, 'Samsung Galaxy Buds2',        1, 5399.10, 5399.10),
('FM1E2F3A4B5C6D', 327, 'Samsung 128GB MicroSD Card',  2,  719.10, 1438.20),
('FM1E2F3A4B5C6D', 126, 'Cadbury Dairy Milk 100g',     4,   76.00,  304.00);

INSERT IGNORE INTO transactions (txn_id,customer_id,total_amount,coupon_pct,txn_time,cashier_id) VALUES
('FM4F5A6B7C8D9E', 1007, 1868.05,  0, DATE_SUB(NOW(), INTERVAL 3 DAY), (ABS(CRC32('FM4F5A6B7C8D9E')) % 9)+1);
INSERT IGNORE INTO txn_items (txn_id,product_id,product_name,quantity,unit_price,subtotal) VALUES
('FM4F5A6B7C8D9E', 402, 'Peter England Formal Shirt',  1, 1104.15, 1104.15),
('FM4F5A6B7C8D9E', 418, 'VIP Frenchie Plus Briefs 3pk',2,  339.15,  678.30),
('FM4F5A6B7C8D9E', 128, 'Lays Classic Salted 80g',     3,   28.50,   85.50);

INSERT IGNORE INTO transactions (txn_id,customer_id,total_amount,coupon_pct,txn_time,cashier_id) VALUES
('FM9A0B1C2D3E4F', 1005, 1325.31,  5, DATE_SUB(NOW(), INTERVAL 1 DAY), (ABS(CRC32('FM9A0B1C2D3E4F')) % 9)+1);
INSERT IGNORE INTO txn_items (txn_id,product_id,product_name,quantity,unit_price,subtotal) VALUES
('FM9A0B1C2D3E4F', 502, 'Yonex Mavis Shuttle 6pk',            2, 459.08, 918.16),
('FM9A0B1C2D3E4F', 508, 'Nivia Hydro Track Water Bottle 1L',  1, 321.65, 321.65),
('FM9A0B1C2D3E4F', 128, 'Lays Classic Salted 80g',            3,  28.50,  85.50);

INSERT IGNORE INTO transactions (txn_id,customer_id,total_amount,coupon_pct,txn_time,cashier_id) VALUES
('FM2B3C4D5E6F7A', 1002,  820.65,  0, DATE_SUB(NOW(), INTERVAL 1 DAY), (ABS(CRC32('FM2B3C4D5E6F7A')) % 9)+1);
INSERT IGNORE INTO txn_items (txn_id,product_id,product_name,quantity,unit_price,subtotal) VALUES
('FM2B3C4D5E6F7A', 452, 'Maybelline Fit Me Foundation',1, 449.10, 449.10),
('FM2B3C4D5E6F7A', 460, 'Wow ACV Shampoo',             1, 331.65, 331.65),
('FM2B3C4D5E6F7A', 148, 'Maggi Masala Noodles 70g',    3,  13.30,  39.90);

INSERT IGNORE INTO transactions (txn_id,customer_id,total_amount,coupon_pct,txn_time,cashier_id) VALUES
('FM5C6D7E8F9A0B', 1001,  911.50,  0, NOW(), (ABS(CRC32('FM5C6D7E8F9A0B')) % 9)+1);
INSERT IGNORE INTO txn_items (txn_id,product_id,product_name,quantity,unit_price,subtotal) VALUES
('FM5C6D7E8F9A0B', 101, 'Basmati Rice 5kg',             2, 237.50, 475.00),
('FM5C6D7E8F9A0B', 104, 'Fortune Sunflower Oil 1L',     2, 137.75, 275.50),
('FM5C6D7E8F9A0B', 108, 'Parle-G Biscuits 800g',        2,  80.75, 161.50);

INSERT IGNORE INTO transactions (txn_id,customer_id,total_amount,coupon_pct,txn_time,cashier_id) VALUES
('FM8D9E0F1A2B3C', 1016, 1760.70, 10, NOW(), (ABS(CRC32('FM8D9E0F1A2B3C')) % 9)+1);
INSERT IGNORE INTO txn_items (txn_id,product_id,product_name,quantity,unit_price,subtotal) VALUES
('FM8D9E0F1A2B3C', 302, 'boAt Rockerz 450 Headphones', 1, 1349.10, 1349.10),
('FM8D9E0F1A2B3C', 310, 'Syska LED Bulb 9W Pack 4',    1,  269.10,  269.10),
('FM8D9E0F1A2B3C', 128, 'Lays Classic Salted 80g',     5,   28.50,  142.50);

INSERT IGNORE INTO transactions (txn_id,customer_id,total_amount,coupon_pct,txn_time,cashier_id) VALUES
('FM0F1A2B3C4D5E', 1002,  238.45,  0, NOW(), (ABS(CRC32('FM0F1A2B3C4D5E')) % 9)+1);
INSERT IGNORE INTO txn_items (txn_id,product_id,product_name,quantity,unit_price,subtotal) VALUES
('FM0F1A2B3C4D5E', 126, 'Cadbury Dairy Milk 100g',     2,  76.00, 152.00),
('FM0F1A2B3C4D5E', 148, 'Maggi Masala Noodles 70g',    3,  13.30,  39.90),
('FM0F1A2B3C4D5E', 614, 'Fevistik Glue Stick Large',   1,  46.55,  46.55);

-- ================================================================
-- 7. SYNC TRANSACTION COUNTS
--    Recompute transaction_count for every customer from
--    the actual transactions table.
-- ================================================================
UPDATE customers c
SET c.transaction_count = (
    SELECT COUNT(*) FROM transactions t WHERE t.customer_id = c.customer_id
);
