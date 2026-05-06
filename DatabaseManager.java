import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.sql.*;
import java.util.Base64;

// Fresh Mitra — MySQL Database Manager

public class DatabaseManager {

    private static final String DB_URL = getEnvOrProperty("FRESHMITRA_DB_URL", "jdbc:mysql://localhost:3306/freshmitra_dblc");
    private static final String DB_USER = getEnvOrProperty("FRESHMITRA_DB_USER", "root");
    private static final String DB_PASS = getEnvOrProperty("FRESHMITRA_DB_PASS", "root");
    private static final String HASH_PREFIX = "sha256$";

    /** Formats a SQL Timestamp to dd/MM/yyyy HH:mm for display. */
    private static String fmtTs(java.sql.Timestamp ts) {
        if (ts == null) return "—";
        return new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm").format(ts);
    }

    private static Connection conn;

    /** Call once at startup. Creates tables if they don't exist. */
    public static void init() throws SQLException {
        conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
        System.out.println("[DB] Connected to MySQL.");
        createTablesIfAbsent();
        seedSampleData();
        migrateLegacyCashierPasswords();
        backfillTransactionCounts();
    }

    /**
     * Inserts sample products with correct categories using INSERT IGNORE.
     * Safe to call on every startup — existing rows are never overwritten.
     * Bug fix: previous seed data incorrectly used "Food" for non-food items
     * (e.g. Yoga Mat, Protein Powder, Notebook). Correct categories are now used.
     */
    private static void seedSampleData() throws SQLException {
        String sql = "INSERT IGNORE INTO products (product_id, name, category, price, quantity, extra_info) VALUES (?,?,?,?,?,?)";
        Object[][] seeds = {
                // id, name, category, price, qty, extra_info
                { 801, "Basmati Rice 5kg", "Food", 320.00, 50, "Expiry: 31/12/2026" },
                { 802, "Whole Wheat Bread", "Food", 45.00, 30, "Expiry: 15/04/2026" },
                { 803, "Organic Honey 500g", "Food", 280.00, 25, "Expiry: 01/06/2027" },
                { 804, "Samsung 65\" 4K TV", "Electronics", 55000.00, 8, "Warranty: 24 mo" },
                { 805, "Boat Wireless Earbuds", "Electronics", 1499.00, 20, "Warranty: 12 mo" },
                { 806, "Men's Slim Fit Jeans", "Clothing", 999.00, 35, "Size: 32" },
                { 807, "Women's Kurti", "Clothing", 649.00, 40, "Size: M" },
                { 808, "Lakme Lipstick", "Beauty", 349.00, 60, "Brand: Lakme" },
                { 809, "Nivea Face Wash", "Beauty", 199.00, 45, "Brand: Nivea" },
                { 810, "Cricket Bat (Kashmir)", "Sports", 1800.00, 12, "Sport: Cricket" },
                { 811, "Yoga Mat 6mm", "Sports", 699.00, 18, "Sport: Yoga" }, // was wrongly "Food"
                { 812, "Protein Powder 1kg", "Sports", 1299.00, 15, "Sport: Fitness" }, // was wrongly "Food"
                { 813, "Dumbbell Set 10kg", "Sports", 2200.00, 10, "Sport: Gym" }, // was wrongly "Food"
                { 814, "Classmate Notebook 200pg", "Stationery", 85.00, 80, "Use: Writing" }, // was wrongly "Food"
                { 815, "Stapler with Pins", "Stationery", 120.00, 30, "Use: Office" },
        };
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (Object[] row : seeds) {
                ps.setInt(1, (int) row[0]);
                ps.setString(2, (String) row[1]);
                ps.setString(3, (String) row[2]);
                ps.setDouble(4, (double) row[3]);
                ps.setInt(5, (int) row[4]);
                ps.setString(6, (String) row[5]);
                ps.addBatch();
            }
            ps.executeBatch();
        }
        seedCashiers();
    }

    private static void seedCashiers() throws SQLException {
        String sql = "INSERT IGNORE INTO cashiers (cashier_id, name, username, shift, status, join_date, password) VALUES (?,?,?,?,?,?,?)";
        Object[][] cashiers = {
            {1, "Rahul Sharma",   "rahul",   "Morning",   "Active",   "2023-01-15", "rahul123"},
            {2, "Priya Mehta",    "priya",   "Afternoon", "Active",   "2023-03-08", "priya123"},
            {3, "Amit Kumar",     "amit",    "Evening",   "Active",   "2022-11-20", "amit123"},
            {4, "Sneha Patel",    "sneha",   "Morning",   "Active",   "2023-06-01", "sneha123"},
            {5, "Vikram Singh",   "vikram",  "Afternoon", "Active",   "2023-07-14", "vikram123"},
            {6, "Anita Devi",     "anita",   "Evening",   "Inactive", "2022-08-30", "anita123"},
            {7, "Rohan Gupta",    "rohan",   "Morning",   "Active",   "2024-01-10", "rohan123"},
            {8, "Kavita Rao",     "kavita",  "Afternoon", "Active",   "2024-02-22", "kavita123"},
            {9, "Suresh Nair",    "suresh",  "Evening",   "Active",   "2023-09-05", "suresh123"},
        };
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (Object[] row : cashiers) {
                ps.setInt(1,    (Integer) row[0]);
                ps.setString(2, (String)  row[1]);
                ps.setString(3, (String)  row[2]);
                ps.setString(4, (String)  row[3]);
                ps.setString(5, (String)  row[4]);
                ps.setDate(6,   java.sql.Date.valueOf((String) row[5]));
                ps.setString(7, hashPassword((String) row[6]));
                ps.addBatch();
            }
            ps.executeBatch();
        }
        // Assign cashier_ids to transactions that have none (deterministic round-robin)
        try (Statement st = conn.createStatement()) {
            st.executeUpdate("""
                UPDATE transactions
                SET cashier_id = (ABS(CRC32(txn_id)) % 9) + 1
                WHERE cashier_id IS NULL
            """);
        }
    }

    /**
     * Recomputes transaction_count for every customer from the transactions table.
     */
    private static void backfillTransactionCounts() throws SQLException {
        String sql = """
                    UPDATE customers c
                    SET c.transaction_count = (
                        SELECT COUNT(*) FROM transactions t WHERE t.customer_id = c.customer_id
                    )
                """;
        try (Statement st = conn.createStatement()) {
            st.executeUpdate(sql);
        }
        System.out.println("[DB] Transaction counts synced.");
    }

    public static Connection getConnection() {
        return conn;
    }

    public static void close() {
        try {
            if (conn != null && !conn.isClosed())
                conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void createTablesIfAbsent() throws SQLException {
        try (Statement st = conn.createStatement()) {
            st.execute("""
                        CREATE TABLE IF NOT EXISTS products (
                            product_id   INT PRIMARY KEY,
                            name         VARCHAR(120)   NOT NULL,
                            category     VARCHAR(30)    NOT NULL,
                            price        DECIMAL(10,2)  NOT NULL,
                            quantity     INT            NOT NULL DEFAULT 0,
                            extra_info   VARCHAR(120)   NOT NULL,
                            created_at   TIMESTAMP      DEFAULT CURRENT_TIMESTAMP
                        )
                    """);
            st.execute("""
                        CREATE TABLE IF NOT EXISTS customers (
                            customer_id       INT PRIMARY KEY,
                            name              VARCHAR(100)  NOT NULL,
                            phone             VARCHAR(15)   NOT NULL,
                            email             VARCHAR(120),
                            transaction_count INT           NOT NULL DEFAULT 0,
                            created_at        TIMESTAMP     DEFAULT CURRENT_TIMESTAMP
                        )
                    """);
            try {
                st.execute("ALTER TABLE customers ADD COLUMN transaction_count INT NOT NULL DEFAULT 0");
            } catch (SQLException ignored) {
            }
            st.execute("""
                        CREATE TABLE IF NOT EXISTS transactions (
                            txn_id       VARCHAR(20)    PRIMARY KEY,
                            customer_id  INT            NOT NULL,
                            total_amount DECIMAL(10,2)  NOT NULL,
                            coupon_pct   DECIMAL(5,2)   DEFAULT 0,
                            txn_time     TIMESTAMP      DEFAULT CURRENT_TIMESTAMP,
                            FOREIGN KEY (customer_id) REFERENCES customers(customer_id)
                        )
                    """);
            st.execute("""
                        CREATE TABLE IF NOT EXISTS txn_items (
                            id           INT AUTO_INCREMENT PRIMARY KEY,
                            txn_id       VARCHAR(20)    NOT NULL,
                            product_id   INT            NOT NULL,
                            product_name VARCHAR(120)   NOT NULL,
                            quantity     INT            NOT NULL,
                            unit_price   DECIMAL(10,2)  NOT NULL,
                            subtotal     DECIMAL(10,2)  NOT NULL,
                            FOREIGN KEY (txn_id) REFERENCES transactions(txn_id)
                        )
                    """);
            st.execute("""
                        CREATE TABLE IF NOT EXISTS cashiers (
                            cashier_id  INT AUTO_INCREMENT PRIMARY KEY,
                            name        VARCHAR(100) NOT NULL,
                            username    VARCHAR(50)  NOT NULL UNIQUE,
                            shift       VARCHAR(20)  NOT NULL DEFAULT 'Morning',
                            status      VARCHAR(15)  NOT NULL DEFAULT 'Active',
                            join_date   DATE         NOT NULL
                        )
                    """);
            // Add cashier_id to transactions if not present
            try { st.execute("ALTER TABLE transactions ADD COLUMN cashier_id INT NULL"); }
            catch (SQLException ignored) {}
            // Add password column to cashiers if not present
            try { st.execute("ALTER TABLE cashiers ADD COLUMN password VARCHAR(255) NOT NULL DEFAULT ''"); }
            catch (SQLException ignored) {}
        }
        System.out.println("[DB] Tables verified.");
    }

    // ================================================================
    // PRODUCTS
    // ================================================================

    public static void insertProduct(Product p) {
        String sql = "INSERT IGNORE INTO products (product_id, name, category, price, quantity, extra_info) VALUES (?,?,?,?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, p.getProductId());
            ps.setString(2, p.getProductName());
            ps.setString(3, p.getCategory());
            ps.setDouble(4, p.getPrice());
            ps.setInt(5, p.getQuantity());
            ps.setString(6, p.getExtraInfo());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert product: " + e.getMessage(), e);
        }
    }

    public static void updateProduct(int productId, String name, double price, int quantity, String extraInfo) {
        String sql = "UPDATE products SET name=?, price=?, quantity=?, extra_info=? WHERE product_id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setDouble(2, price);
            ps.setInt(3, quantity);
            ps.setString(4, extraInfo);
            ps.setInt(5, productId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update product: " + e.getMessage(), e);
        }
    }

    public static void updateProductStock(int productId, int newQuantity) {
        String sql = "UPDATE products SET quantity=? WHERE product_id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, newQuantity);
            ps.setInt(2, productId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update stock: " + e.getMessage(), e);
        }
    }

    public static void deleteProduct(int productId) {
        String sql = "DELETE FROM products WHERE product_id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, productId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete product: " + e.getMessage(), e);
        }
    }

    public static void loadProducts(Inventory inv) {
        String sql = "SELECT * FROM products ORDER BY product_id";
        try (Statement st = conn.createStatement();
                ResultSet rs = st.executeQuery(sql)) {
            int maxId = 800;
            while (rs.next()) {
                int id = rs.getInt("product_id");
                String name = rs.getString("name");
                String category = rs.getString("category");
                double price = rs.getDouble("price");
                int qty = rs.getInt("quantity");
                String extra = rs.getString("extra_info");
                Product p = buildProduct(id, name, category, price, qty, extra);
                if (p != null) {
                    inv.addProduct(p);
                    if (id > maxId)
                        maxId = id;
                }
            }
            syncProductIdCounter(inv, maxId);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ================================================================
    // CUSTOMERS
    // ================================================================

    public static void insertCustomer(Customer c) {
        String sql = "INSERT IGNORE INTO customers (customer_id, name, phone, email, transaction_count) VALUES (?,?,?,?,0)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, c.getCustomerId());
            ps.setString(2, c.getName());
            ps.setString(3, c.getPhone());
            ps.setString(4, c.getEmail());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert customer: " + e.getMessage(), e);
        }
    }

    public static void updateCustomer(int customerId, String name, String phone, String email) {
        String sql = "UPDATE customers SET name=?, phone=?, email=? WHERE customer_id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setString(2, phone);
            ps.setString(3, email);
            ps.setInt(4, customerId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update customer: " + e.getMessage(), e);
        }
    }

    public static void loadCustomers(java.util.List<Customer> list) {
        String sql = "SELECT * FROM customers ORDER BY customer_id";
        int maxId = 1000;
        try (Statement st = conn.createStatement();
                ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                int id = rs.getInt("customer_id");
                String name = rs.getString("name");
                String phone = rs.getString("phone");
                String email = rs.getString("email");
                if (email == null || email.isEmpty())
                    email = "—";
                Customer c = new Customer(name, phone, email);
                setCustomerId(c, id);
                list.add(c);
                if (id > maxId)
                    maxId = id;
            }
            syncCustomerIdCounter(maxId);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Verifies cashier credentials against the DB.
     * Returns:  cashier_id (>0) on success
     *           -1 if wrong username/password
     *           -2 if found but account is Inactive
     */
    public static int verifyCashierLogin(String username, String password) {
        String sql = "SELECT cashier_id, status, password FROM cashiers WHERE username = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return -1;
                if ("Inactive".equals(rs.getString("status"))) return -2;
                int cashierId = rs.getInt("cashier_id");
                String storedPassword = rs.getString("password");
                if (verifyPassword(password, storedPassword)) {
                    if (isLegacyPlaintextPassword(storedPassword))
                        upgradeCashierPasswordHash(cashierId, password);
                    return cashierId;
                }
                return -1;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }

    /** Returns cashier name for a given cashier_id, or null. */
    public static String getCashierName(int cashierId) {
        try (PreparedStatement ps = conn.prepareStatement("SELECT name FROM cashiers WHERE cashier_id=?")) {
            ps.setInt(1, cashierId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getString("name") : null;
            }
        } catch (SQLException e) { return null; }
    }

    // ================================================================
    // TRANSACTIONS
    // ================================================================

    public static void insertTransaction(String txnId, int customerId, double total,
            double couponPct, int cashierId, java.util.List<CartItem> items) throws SQLException {
        if (conn != null) {
            boolean originalAutoCommit = conn.getAutoCommit();
            String sqlTxn = "INSERT INTO transactions (txn_id, customer_id, total_amount, coupon_pct, cashier_id) VALUES (?,?,?,?,?)";
            String sqlItem = "INSERT INTO txn_items (txn_id, product_id, product_name, quantity, unit_price, subtotal) VALUES (?,?,?,?,?,?)";
            String sqlStock = "UPDATE products SET quantity=? WHERE product_id=?";
            try {
                conn.setAutoCommit(false);
                try (PreparedStatement psTxn = conn.prepareStatement(sqlTxn);
                        PreparedStatement psItem = conn.prepareStatement(sqlItem);
                        PreparedStatement psStock = conn.prepareStatement(sqlStock)) {
                    psTxn.setString(1, txnId);
                    psTxn.setInt(2, customerId);
                    psTxn.setDouble(3, total);
                    psTxn.setDouble(4, couponPct);
                    if (cashierId > 0)
                        psTxn.setInt(5, cashierId);
                    else
                        psTxn.setNull(5, java.sql.Types.INTEGER);
                    psTxn.executeUpdate();
                    for (CartItem ci : items) {
                        double dp = ci.getProduct().applyDiscount(ci.getProduct().getPrice());
                        psItem.setString(1, txnId);
                        psItem.setInt(2, ci.getProduct().getProductId());
                        psItem.setString(3, ci.getProduct().getProductName());
                        psItem.setInt(4, ci.getQuantity());
                        psItem.setDouble(5, dp);
                        psItem.setDouble(6, ci.getSubtotal());
                        psItem.addBatch();
                        psStock.setInt(1, ci.getProduct().getQuantity());
                        psStock.setInt(2, ci.getProduct().getProductId());
                        psStock.addBatch();
                    }
                    psItem.executeBatch();
                    psStock.executeBatch();
                }
                refreshTransactionCount(customerId);
                conn.commit();
                return;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(originalAutoCommit);
            }
        }
        String sqlTxn = "INSERT INTO transactions (txn_id, customer_id, total_amount, coupon_pct, cashier_id) VALUES (?,?,?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sqlTxn)) {
            ps.setString(1, txnId);
            ps.setInt(2, customerId);
            ps.setDouble(3, total);
            ps.setDouble(4, couponPct);
            if (cashierId > 0) ps.setInt(5, cashierId); else ps.setNull(5, java.sql.Types.INTEGER);
            ps.executeUpdate();
        }
        String sqlItem = "INSERT INTO txn_items (txn_id, product_id, product_name, quantity, unit_price, subtotal) VALUES (?,?,?,?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sqlItem)) {
            for (CartItem ci : items) {
                double dp = ci.getProduct().applyDiscount(ci.getProduct().getPrice());
                ps.setString(1, txnId);
                ps.setInt(2, ci.getProduct().getProductId());
                ps.setString(3, ci.getProduct().getProductName());
                ps.setInt(4, ci.getQuantity());
                ps.setDouble(5, dp);
                ps.setDouble(6, ci.getSubtotal());
                ps.addBatch();
            }
            ps.executeBatch();
        }
        // transaction_count is recomputed authoritatively via refreshTransactionCount()
        // after this call returns — do NOT also do +1 here or it will be
        // double-counted.
    }

    public static java.util.List<String[]> loadRecentTransactions(int limit) {
        java.util.List<String[]> list = new java.util.ArrayList<>();
        String sql = """
                    SELECT t.txn_id, c.name, t.total_amount, t.txn_time,
                           t.cashier_id,
                           COALESCE(ca.name, '\u2014') AS cashier_name
                    FROM transactions t
                    JOIN customers c ON t.customer_id = c.customer_id
                    LEFT JOIN cashiers ca ON t.cashier_id = ca.cashier_id
                    ORDER BY t.txn_time DESC
                    LIMIT ?
                """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int cid = rs.getInt("cashier_id");
                    String cashierIdStr = rs.wasNull() ? "\u2014" : String.valueOf(cid);
                    list.add(new String[] {
                            rs.getString("txn_id"),
                            rs.getString("name"),
                            "Rs. " + String.format("%.2f", rs.getDouble("total_amount")),
                            fmtTs(rs.getTimestamp("txn_time")),
                            cashierIdStr,
                            rs.getString("cashier_name")
                    });
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Loads all transactions with optional filters.
     * period: "today", "month", or "" for all time.
     * customerQuery: substring to match against customer name (case-insensitive),
     * or "".
     * Returns String[6]: {txn_id, customer_name, phone, date_time, coupon_pct,
     * total_amount}
     */
    /**
     * period: "", "today", "week", "month", or "custom:YYYY-MM-DD:YYYY-MM-DD"
     * customerQuery: name substring filter, or ""
     * Returns String[6]: {txn_id, customer_name, phone, date_time, coupon_pct,
     * total_amount}
     */
    public static java.util.List<String[]> loadTransactions(String period, String customerQuery) {
        return loadTransactions(period, customerQuery, null);
    }

    public static java.util.List<String[]> loadTransactions(String period, String customerQuery, Integer cashierId) {
        java.util.List<String[]> list = new java.util.ArrayList<>();
        String periodClause;
        String[] customDates = null;
        if (period.startsWith("custom:")) {
            customDates = period.substring(7).split(":");
            periodClause = " AND DATE(t.txn_time) BETWEEN ? AND ?";
        } else {
            periodClause = switch (period) {
                case "today" -> " AND DATE(t.txn_time) = CURDATE()";
                case "week" -> " AND t.txn_time >= DATE_SUB(CURDATE(), INTERVAL 7 DAY)";
                case "month" -> " AND t.txn_time >= DATE_FORMAT(NOW(),'%Y-%m-01')";
                default -> "";
            };
        }
        boolean hasCust = customerQuery != null && !customerQuery.isEmpty();
        String custClause = hasCust ? " AND LOWER(c.name) LIKE ?" : "";
        String cashierClause = cashierId != null && cashierId > 0 ? " AND t.cashier_id = ?" : "";
        String sql = "SELECT t.txn_id, c.name, c.phone, t.txn_time, t.coupon_pct, t.total_amount "
                + "FROM transactions t JOIN customers c ON t.customer_id = c.customer_id "
                + "WHERE 1=1" + cashierClause + periodClause + custClause + " ORDER BY t.txn_time DESC";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            int idx = 1;
            if (cashierId != null && cashierId > 0)
                ps.setInt(idx++, cashierId);
            if (customDates != null) {
                ps.setString(idx++, customDates[0]);
                ps.setString(idx++, customDates[1]);
            }
            if (hasCust)
                ps.setString(idx, "%" + customerQuery.toLowerCase() + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new String[] {
                            rs.getString("txn_id"),
                            rs.getString("name"),
                            rs.getString("phone"),
                            fmtTs(rs.getTimestamp("txn_time")),
                            String.format("%.0f%%", rs.getDouble("coupon_pct")),
                            "Rs. " + String.format("%.2f", rs.getDouble("total_amount"))
                    });
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Builds and returns a formatted receipt string for the given txn_id, or null
     * if not found.
     */
    public static String getBillForTxn(String txnId) {
        String sqlTxn = "SELECT c.name, c.phone, t.total_amount, t.coupon_pct, t.txn_time, "
                + "t.cashier_id, COALESCE(ca.name, '\u2014') AS cashier_name "
                + "FROM transactions t JOIN customers c ON t.customer_id=c.customer_id "
                + "LEFT JOIN cashiers ca ON t.cashier_id=ca.cashier_id WHERE t.txn_id=?";
        String sqlItems = "SELECT product_name, quantity, subtotal FROM txn_items WHERE txn_id=? ORDER BY id";
        try (PreparedStatement ps = conn.prepareStatement(sqlTxn)) {
            ps.setString(1, txnId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next())
                    return null;
                String custName = rs.getString("name");
                String phone = rs.getString("phone");
                double total = rs.getDouble("total_amount");
                double coupon = rs.getDouble("coupon_pct");
                String time = fmtTs(rs.getTimestamp("txn_time"));
                StringBuilder sb = new StringBuilder();
                sb.append("==============================\n");
                sb.append("         FRESH MITRA\n");
                sb.append("==============================\n\n");
                sb.append("  TXN ID   : ").append(txnId).append("\n");
                sb.append("  Customer : ").append(custName).append("\n");
                sb.append("  Phone    : ").append(phone).append("\n");
                sb.append("  Date     : ").append(time).append("\n");
                // Cashier info
                int cid = rs.getInt("cashier_id");
                if (rs.wasNull() || cid <= 0) {
                    sb.append("  Cashier  : Manager\n");
                    sb.append("  Cash ID  : admin\n");
                } else {
                    sb.append("  Cashier  : ").append(rs.getString("cashier_name")).append("\n");
                    sb.append("  Cash ID  : ").append(cid).append("\n");
                }
                sb.append("\n------------------------------\n");
                if (coupon > 0)
                    sb.append("  Coupon Discount : ").append((int) coupon).append("%\n");
                sb.append(String.format("  %-24s %5s  %10s%n", "ITEM", "QTY", "AMOUNT"));
                sb.append("  ------------------------------\n");
                try (PreparedStatement ps2 = conn.prepareStatement(sqlItems)) {
                    ps2.setString(1, txnId);
                    try (ResultSet ri = ps2.executeQuery()) {
                        while (ri.next()) {
                            String name = ri.getString("product_name");
                            sb.append(String.format("  %-24s x%-4d  Rs.%7.2f%n",
                                    name.length() > 23 ? name.substring(0, 22) + "..." : name,
                                    ri.getInt("quantity"), ri.getDouble("subtotal")));
                        }
                    }
                }
                sb.append("  ------------------------------\n\n");
                sb.append("  TOTAL PAYABLE\n");
                sb.append("  Rs. ").append(String.format("%.2f", total)).append("\n");
                sb.append("\n==============================\n");
                sb.append("    Thank you for shopping!\n");
                return sb.toString();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static java.util.List<String[]> loadCustomerHistory(int customerId) {
        java.util.List<String[]> list = new java.util.ArrayList<>();
        String sqlTxn = "SELECT txn_id, total_amount, coupon_pct, txn_time FROM transactions WHERE customer_id=? ORDER BY txn_time DESC";
        String sqlItems = "SELECT product_name, quantity, unit_price, subtotal FROM txn_items WHERE txn_id=? ORDER BY id";
        try (PreparedStatement psTxn = conn.prepareStatement(sqlTxn)) {
            psTxn.setInt(1, customerId);
            try (ResultSet rsTxn = psTxn.executeQuery()) {
                while (rsTxn.next()) {
                    String txnId = rsTxn.getString("txn_id");
                    double total = rsTxn.getDouble("total_amount");
                    double coupon = rsTxn.getDouble("coupon_pct");
                    String time = fmtTs(rsTxn.getTimestamp("txn_time"));
                    String line = "TXN#" + txnId + "  |  Rs." + String.format("%.2f", total) + "  |  " + time;

                    StringBuilder bill = new StringBuilder();
                    bill.append("==============================\n");
                    bill.append("         FRESH MITRA\n");
                    bill.append("==============================\n\n");
                    bill.append("  TXN ID   : ").append(txnId).append("\n");
                    bill.append("  Date     : ").append(time).append("\n");
                    bill.append("\n------------------------------\n");
                    bill.append(String.format("  %-24s %5s  %10s%n", "ITEM", "QTY", "AMOUNT"));
                    bill.append("  ------------------------------\n");

                    try (PreparedStatement psItems = conn.prepareStatement(sqlItems)) {
                        psItems.setString(1, txnId);
                        try (ResultSet rsItems = psItems.executeQuery()) {
                            while (rsItems.next()) {
                                String name = rsItems.getString("product_name");
                                int qty = rsItems.getInt("quantity");
                                double sub = rsItems.getDouble("subtotal");
                                bill.append(String.format("  %-24s x%-4d  Rs.%7.2f%n",
                                        name.length() > 23 ? name.substring(0, 22) + "..." : name,
                                        qty, sub));
                            }
                        }
                    }

                    bill.append("  ------------------------------\n");
                    if (coupon > 0)
                        bill.append("  Coupon Discount : ").append((int) coupon).append("%\n");
                    bill.append("\n  TOTAL PAYABLE\n");
                    bill.append("  Rs. ").append(String.format("%.2f", total)).append("\n");
                    bill.append("\n==============================\n");
                    bill.append("    Thank you for shopping!\n");

                    list.add(new String[] { line, bill.toString() });
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // ================================================================
    // HELPERS
    // ================================================================

    private static Product buildProduct(int id, String name, String category,
            double price, int qty, String extra) {
        return switch (category) {
            case "Food" -> new FoodProduct(id, name, price, qty, stripPrefix(extra, "Expiry: "));
            case "Electronics" -> new ElectronicsProduct(id, name, price, qty,
                    safeInt(stripPrefix(extra, "Warranty: ").replace(" mo", "")));
            case "Clothing" -> new ClothingProduct(id, name, price, qty, stripPrefix(extra, "Size: "));
            case "Beauty" -> new BeautyProduct(id, name, price, qty, stripPrefix(extra, "Brand: "));
            case "Sports" -> new SportsProduct(id, name, price, qty, stripPrefix(extra, "Sport: "));
            case "Stationery" -> new StationeryProduct(id, name, price, qty, stripPrefix(extra, "Use: "));
            default -> {
                System.err.println("[DB] Warning: unknown category '" + category + "' for product ID " + id + " ('"
                        + name + "') — skipped.");
                yield null;
            }
        };
    }

    private static String stripPrefix(String s, String prefix) {
        return s.startsWith(prefix) ? s.substring(prefix.length()) : s;
    }

    private static int safeInt(String s) {
        try {
            return Integer.parseInt(s.replaceAll("[^0-9]", ""));
        } catch (Exception e) {
            return 0;
        }
    }

    private static void setCustomerId(Customer c, int id) {
        try {
            java.lang.reflect.Field f = Customer.class.getDeclaredField("id");
            f.setAccessible(true);
            f.setInt(c, id);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void syncCustomerIdCounter(int maxLoadedId) {
        try {
            java.lang.reflect.Field f = Customer.class.getDeclaredField("nid");
            f.setAccessible(true);
            f.setInt(null, maxLoadedId + 1);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void syncProductIdCounter(Inventory inv, int maxLoadedId) {
        try {
            java.lang.reflect.Field f = Inventory.class.getDeclaredField("nid");
            f.setAccessible(true);
            f.setInt(inv, maxLoadedId + 1);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static int getTransactionCount(int customerId) {
        String sql = "SELECT transaction_count FROM customers WHERE customer_id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, customerId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next())
                    return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static void refreshTransactionCount(int customerId) {
        String sql = """
                    UPDATE customers
                    SET transaction_count = (
                        SELECT COUNT(*) FROM transactions WHERE customer_id = ?
                    )
                    WHERE customer_id = ?
                """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, customerId);
            ps.setInt(2, customerId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static String getEnvOrProperty(String key, String defaultValue) {
        String value = System.getenv(key);
        if (value == null || value.isBlank())
            value = System.getProperty(key);
        return (value == null || value.isBlank()) ? defaultValue : value;
    }

    private static String hashPassword(String rawPassword) {
        try {
            byte[] salt = new byte[16];
            new SecureRandom().nextBytes(salt);
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(salt);
            byte[] hash = digest.digest(rawPassword.getBytes(StandardCharsets.UTF_8));
            return HASH_PREFIX + Base64.getEncoder().encodeToString(salt) + "$"
                    + Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            throw new RuntimeException("Failed to hash password", e);
        }
    }

    private static boolean verifyPassword(String rawPassword, String storedPassword) {
        if (storedPassword == null || storedPassword.isBlank())
            return false;
        if (isLegacyPlaintextPassword(storedPassword))
            return storedPassword.equals(rawPassword);
        String[] parts = storedPassword.split("\\$");
        if (parts.length != 3 || !storedPassword.startsWith(HASH_PREFIX))
            return false;
        try {
            byte[] salt = Base64.getDecoder().decode(parts[1]);
            byte[] expectedHash = Base64.getDecoder().decode(parts[2]);
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(salt);
            byte[] actualHash = digest.digest(rawPassword.getBytes(StandardCharsets.UTF_8));
            return MessageDigest.isEqual(expectedHash, actualHash);
        } catch (Exception e) {
            return false;
        }
    }

    private static boolean isLegacyPlaintextPassword(String storedPassword) {
        return storedPassword != null && !storedPassword.startsWith(HASH_PREFIX);
    }

    private static void migrateLegacyCashierPasswords() throws SQLException {
        String selectSql = "SELECT cashier_id, password FROM cashiers";
        String updateSql = "UPDATE cashiers SET password=? WHERE cashier_id=?";
        try (Statement st = conn.createStatement();
                ResultSet rs = st.executeQuery(selectSql);
                PreparedStatement ps = conn.prepareStatement(updateSql)) {
            while (rs.next()) {
                String storedPassword = rs.getString("password");
                if (!isLegacyPlaintextPassword(storedPassword))
                    continue;
                ps.setString(1, hashPassword(storedPassword));
                ps.setInt(2, rs.getInt("cashier_id"));
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private static void upgradeCashierPasswordHash(int cashierId, String rawPassword) {
        String sql = "UPDATE cashiers SET password=? WHERE cashier_id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, hashPassword(rawPassword));
            ps.setInt(2, cashierId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ================================================================
    // ANALYTICS QUERIES
    // ================================================================

    /**
     * Returns sales summary grouped by date.
     * period: "daily" = today, "weekly" = last 7 days, "monthly" = last 30 days.
     * Returns list of String[4]: {date, txn_count, total_revenue, avg_bill}
     */
    public static java.util.List<String[]> getSalesSummary(String period) {
        return getSalesSummary(period, null);
    }

    public static java.util.List<String[]> getSalesSummary(String period, Integer cashierId) {
        java.util.List<String[]> list = new java.util.ArrayList<>();
        String interval = switch (period) {
            case "weekly" -> "7";
            case "monthly" -> "30";
            default -> "1";
        };
        String cashierClause = cashierId != null && cashierId > 0 ? " AND cashier_id = ?" : "";
        String sql = """
                    SELECT
                        DATE(txn_time)                          AS sale_date,
                        COUNT(*)                                AS txn_count,
                        ROUND(SUM(total_amount), 2)             AS total_rev,
                        ROUND(AVG(total_amount), 2)             AS avg_bill
                    FROM transactions
                    WHERE txn_time >= DATE_SUB(CURDATE(), INTERVAL ? DAY)
                """ + cashierClause + """
                    GROUP BY DATE(txn_time)
                    ORDER BY sale_date DESC
                """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, interval);
            if (cashierId != null && cashierId > 0)
                ps.setInt(2, cashierId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new String[] {
                            rs.getString("sale_date"),
                            rs.getString("txn_count"),
                            rs.getString("total_rev"),
                            rs.getString("avg_bill")
                    });
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /** Custom date range sales summary. from/to are "YYYY-MM-DD" strings. */
    public static java.util.List<String[]> getSalesSummaryCustom(String from, String to) {
        return getSalesSummaryCustom(from, to, null);
    }

    public static java.util.List<String[]> getSalesSummaryCustom(String from, String to, Integer cashierId) {
        java.util.List<String[]> list = new java.util.ArrayList<>();
        String cashierClause = cashierId != null && cashierId > 0 ? " AND cashier_id = ?" : "";
        String sql = """
                    SELECT
                        DATE(txn_time)                      AS sale_date,
                        COUNT(*)                            AS txn_count,
                        ROUND(SUM(total_amount), 2)         AS total_rev,
                        ROUND(AVG(total_amount), 2)         AS avg_bill
                    FROM transactions
                    WHERE DATE(txn_time) BETWEEN ? AND ?
                """ + cashierClause + """
                    GROUP BY DATE(txn_time)
                    ORDER BY sale_date DESC
                """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, from);
            ps.setString(2, to);
            if (cashierId != null && cashierId > 0)
                ps.setInt(3, cashierId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new String[] {
                            rs.getString("sale_date"),
                            rs.getString("txn_count"),
                            rs.getString("total_rev"),
                            rs.getString("avg_bill")
                    });
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /** Returns sales summary grouped by date for all time. */
    public static java.util.List<String[]> getSalesSummaryAll() {
        return getSalesSummaryAll(null);
    }

    public static java.util.List<String[]> getSalesSummaryAll(Integer cashierId) {
        java.util.List<String[]> list = new java.util.ArrayList<>();
        String cashierClause = cashierId != null && cashierId > 0 ? " WHERE cashier_id = ?" : "";
        String sql = """
                    SELECT
                        DATE(txn_time)                      AS sale_date,
                        COUNT(*)                            AS txn_count,
                        ROUND(SUM(total_amount), 2)         AS total_rev,
                        ROUND(AVG(total_amount), 2)         AS avg_bill
                    FROM transactions
                """ + cashierClause + """
                    GROUP BY DATE(txn_time)
                    ORDER BY sale_date DESC
                """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            if (cashierId != null && cashierId > 0)
                ps.setInt(1, cashierId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new String[] {
                            rs.getString("sale_date"),
                            rs.getString("txn_count"),
                            rs.getString("total_rev"),
                            rs.getString("avg_bill")
                    });
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * sortBy: "units_sold" or "total_rev"
     */
    public static java.util.List<String[]> getBestSellingProducts(int limit, String sortBy) {
        java.util.List<String[]> list = new java.util.ArrayList<>();
        String orderCol = sortBy.equals("total_rev") ? "total_rev" : "units_sold";
        String sql = """
                SELECT
                    ti.product_name,
                    p.category,
                    SUM(ti.quantity)             AS units_sold,
                    ROUND(SUM(ti.subtotal), 2)   AS total_rev
                FROM txn_items ti
                LEFT JOIN products p ON ti.product_id = p.product_id
                GROUP BY ti.product_name, p.category
                ORDER BY \s""" + orderCol + " DESC LIMIT ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new String[] {
                            rs.getString("product_name"),
                            rs.getString("category") != null ? rs.getString("category") : "—",
                            rs.getString("units_sold"),
                            rs.getString("total_rev")
                    });
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /** Convenience overload — defaults to units_sold sort. */
    public static java.util.List<String[]> getBestSellingProducts(int limit) {
        return getBestSellingProducts(limit, "units_sold");
    }

    /**
     * Returns revenue breakdown by product category.
     * Returns list of String[3]: {category, units_sold, total_revenue}
     */
    public static java.util.List<String[]> getCategoryRevenue() {
        java.util.List<String[]> list = new java.util.ArrayList<>();
        String sql = """
                    SELECT
                        p.category,
                        SUM(ti.quantity)            AS units_sold,
                        ROUND(SUM(ti.subtotal), 2)  AS total_rev
                    FROM txn_items ti
                    JOIN products p ON ti.product_id = p.product_id
                    GROUP BY p.category
                    ORDER BY total_rev DESC
                """;
        try (Statement st = conn.createStatement();
                ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new String[] {
                        rs.getString("category"),
                        rs.getString("units_sold"),
                        rs.getString("total_rev")
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Returns top N customers.
     * sortBy: "txn_count" = most recurring, "total_spent" = highest spender.
     */
    public static java.util.List<String[]> getTopCustomers(int limit, String sortBy) {
        java.util.List<String[]> list = new java.util.ArrayList<>();
        String orderCol = sortBy.equals("total_spent") ? "total_spent DESC, txn_count DESC"
                : "txn_count DESC, total_spent DESC";
        String sql = """
                SELECT
                    c.name,
                    c.phone,
                    COUNT(t.txn_id)              AS txn_count,
                    ROUND(SUM(t.total_amount),2)  AS total_spent
                FROM customers c
                JOIN transactions t ON c.customer_id = t.customer_id
                GROUP BY c.customer_id, c.name, c.phone
                ORDER BY \s""" + orderCol + " LIMIT ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new String[] {
                            rs.getString("name"),
                            rs.getString("phone"),
                            rs.getString("txn_count"),
                            rs.getString("total_spent")
                    });
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /** Convenience overload — defaults to most recurring sort. */
    public static java.util.List<String[]> getTopCustomers(int limit) {
        return getTopCustomers(limit, "txn_count");
    }

    // ================================================================
    // CASHIERS
    // ================================================================

    /**
     * Returns one row per cashier: [cashier_id, name, username, shift, status,
     * join_date(DD/MM/YYYY), txn_count, total_revenue, avg_bill, last_active]
     */
    public static java.util.List<String[]> loadCashiers() {
        java.util.List<String[]> list = new java.util.ArrayList<>();
        String sql = """
            SELECT c.cashier_id, c.name, c.username, c.shift, c.status, c.join_date,
                   COUNT(t.txn_id)                   AS txn_count,
                   COALESCE(SUM(t.total_amount), 0)  AS total_rev,
                   COALESCE(AVG(t.total_amount), 0)  AS avg_bill,
                   MAX(t.txn_time)                   AS last_active
            FROM cashiers c
            LEFT JOIN transactions t ON c.cashier_id = t.cashier_id
            GROUP BY c.cashier_id
            ORDER BY txn_count DESC
        """;
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                java.sql.Date jd = rs.getDate("join_date");
                String joinFmt = jd == null ? "—" :
                    new java.text.SimpleDateFormat("dd/MM/yyyy").format(jd);
                Timestamp la = rs.getTimestamp("last_active");
                list.add(new String[]{
                    String.valueOf(rs.getInt("cashier_id")),
                    rs.getString("name"),
                    rs.getString("username"),
                    rs.getString("shift"),
                    rs.getString("status"),
                    joinFmt,
                    String.valueOf(rs.getInt("txn_count")),
                    "Rs. " + String.format("%.2f", rs.getDouble("total_rev")),
                    "Rs. " + String.format("%.2f", rs.getDouble("avg_bill")),
                    la == null ? "—" : fmtTs(la)
                });
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    /**
     * Returns transactions handled by the given cashier:
     * [txn_id, customer_name, total_amount, txn_time]
     */
    public static java.util.List<String[]> getCashierTxns(int cashierId) {
        java.util.List<String[]> list = new java.util.ArrayList<>();
        String sql = """
            SELECT t.txn_id, cu.name AS cust_name, t.total_amount, t.txn_time
            FROM transactions t
            JOIN customers cu ON t.customer_id = cu.customer_id
            WHERE t.cashier_id = ?
            ORDER BY t.txn_time DESC
        """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, cashierId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new String[]{
                        rs.getString("txn_id"),
                        rs.getString("cust_name"),
                        "Rs. " + String.format("%.2f", rs.getDouble("total_amount")),
                        fmtTs(rs.getTimestamp("txn_time"))
                    });
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    /** Toggles a cashier's status between Active and Inactive. Returns new status string. */
    public static String toggleCashierStatus(int cashierId) {
        String getSql = "SELECT status FROM cashiers WHERE cashier_id=?";
        String current = "Active";
        try (PreparedStatement ps = conn.prepareStatement(getSql)) {
            ps.setInt(1, cashierId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) current = rs.getString("status");
            }
        } catch (SQLException e) { e.printStackTrace(); }
        String newStatus = "Active".equals(current) ? "Inactive" : "Active";
        String updSql = "UPDATE cashiers SET status=? WHERE cashier_id=?";
        try (PreparedStatement ps = conn.prepareStatement(updSql)) {
            ps.setString(1, newStatus);
            ps.setInt(2, cashierId);
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
        return newStatus;
    }

    /** Inserts a new cashier row. Returns generated cashier_id, or -1 on error. */
    public static int insertCashier(String name, String username, String shift, String joinDate, String password) {
        String sql = "INSERT INTO cashiers (name, username, shift, status, join_date, password) VALUES (?,?,?,'Active',?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, name);
            ps.setString(2, username);
            ps.setString(3, shift);
            ps.setDate(4, java.sql.Date.valueOf(joinDate)); // YYYY-MM-DD
            ps.setString(5, hashPassword(password));
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) { throw new RuntimeException("Failed to insert cashier: " + e.getMessage(), e); }
        return -1;
    }

    /** Resets the password for the given cashier. Throws RuntimeException on DB error. */
    public static void resetCashierPassword(int cashierId, String newPassword) {
        String sql = "UPDATE cashiers SET password=? WHERE cashier_id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, hashPassword(newPassword));
            ps.setInt(2, cashierId);
            ps.executeUpdate();
        } catch (SQLException e) { throw new RuntimeException("Failed to reset password: " + e.getMessage(), e); }
    }
}
