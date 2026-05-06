import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.JobName;
import java.awt.*;
import java.awt.event.*;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.sql.*;

// Fresh Mitra — Store Management System

class ProductNotFoundException extends Exception {
    public ProductNotFoundException(String m) {
        super(m);
    }
}

class InsufficientStockException extends Exception {
    public InsufficientStockException(String m) {
        super(m);
    }
}

class InvalidInputException extends RuntimeException {
    public InvalidInputException(String m) {
        super(m);
    }
}

interface Discountable {
    double applyDiscount(double price);

    String getDiscountInfo();
}

abstract class Product implements Discountable {
    private int productId;
    private String productName;
    private double price;
    private int quantity;
    private String category;

    public Product(int id, String n, double p, int q, String c) {
        productId = id;
        productName = n;
        price = p;
        quantity = q;
        category = c;
    }

    public int getProductId() {
        return productId;
    }

    public String getProductName() {
        return productName;
    }

    public double getPrice() {
        return price;
    }

    public int getQuantity() {
        return quantity;
    }

    public String getCategory() {
        return category;
    }

    public void setQuantity(int q) {
        if (q < 0)
            throw new InvalidInputException("Qty<0");
        quantity = q;
    }

    public void setPrice(double p) {
        if (p <= 0)
            throw new InvalidInputException("Price must be > 0");
        price = p;
    }

    public void setName(String n) {
        if (n == null || n.isEmpty())
            throw new InvalidInputException("Name cannot be empty");
        productName = n;
    }

    public abstract String getExtraInfo();

    public abstract void setDiscount(double d);

    public abstract double getDiscount();

    public void reduceStock(int a) throws InsufficientStockException {
        if (a > quantity)
            throw new InsufficientStockException("Only " + quantity + " unit(s) of '" + productName + "' available.");
        quantity -= a;
    }
}

class FoodProduct extends Product {
    private String exp;
    private double discount = 5;

    public FoodProduct(int i, String n, double p, int q, String e) {
        super(i, n, p, q, "Food");
        exp = e;
    }

    public String getExtraInfo() {
        return "Expiry: " + exp;
    }

    public double applyDiscount(double p) {
        return p - p * discount / 100;
    }

    public String getDiscountInfo() {
        return (int) discount + "% off";
    }

    public void setDiscount(double d) {
        discount = d;
    }

    public double getDiscount() {
        return discount;
    }
}

class ElectronicsProduct extends Product {
    private int war;
    private double discount = 10;

    public ElectronicsProduct(int i, String n, double p, int q, int w) {
        super(i, n, p, q, "Electronics");
        war = w;
    }

    public String getExtraInfo() {
        return "Warranty: " + war + " mo";
    }

    public double applyDiscount(double p) {
        return p - p * discount / 100;
    }

    public String getDiscountInfo() {
        return (int) discount + "% off";
    }

    public void setDiscount(double d) {
        discount = d;
    }

    public double getDiscount() {
        return discount;
    }
}

class ClothingProduct extends Product {
    private String sz;
    private double discount = 15;

    public ClothingProduct(int i, String n, double p, int q, String s) {
        super(i, n, p, q, "Clothing");
        sz = s;
    }

    public String getExtraInfo() {
        return "Size: " + sz;
    }

    public double applyDiscount(double p) {
        return p - p * discount / 100;
    }

    public String getDiscountInfo() {
        return (int) discount + "% off";
    }

    public void setDiscount(double d) {
        discount = d;
    }

    public double getDiscount() {
        return discount;
    }
}

class BeautyProduct extends Product {
    private String brand;
    private double discount = 12;

    public BeautyProduct(int i, String n, double p, int q, String b) {
        super(i, n, p, q, "Beauty");
        brand = b;
    }

    public String getExtraInfo() {
        return "Brand: " + brand;
    }

    public double applyDiscount(double p) {
        return p - p * discount / 100;
    }

    public String getDiscountInfo() {
        return (int) discount + "% off";
    }

    public void setDiscount(double d) {
        discount = d;
    }

    public double getDiscount() {
        return discount;
    }
}

class SportsProduct extends Product {
    private String sport;
    private double discount = 8;

    public SportsProduct(int i, String n, double p, int q, String s) {
        super(i, n, p, q, "Sports");
        sport = s;
    }

    public String getExtraInfo() {
        return "Sport: " + sport;
    }

    public double applyDiscount(double p) {
        return p - p * discount / 100;
    }

    public String getDiscountInfo() {
        return (int) discount + "% off";
    }

    public void setDiscount(double d) {
        discount = d;
    }

    public double getDiscount() {
        return discount;
    }
}

class StationeryProduct extends Product {
    private String use;
    private double discount = 6;

    public StationeryProduct(int i, String n, double p, int q, String u) {
        super(i, n, p, q, "Stationery");
        use = u;
    }

    public String getExtraInfo() {
        return "Use: " + use;
    }

    public double applyDiscount(double p) {
        return p - p * discount / 100;
    }

    public String getDiscountInfo() {
        return (int) discount + "% off";
    }

    public void setDiscount(double d) {
        discount = d;
    }

    public double getDiscount() {
        return discount;
    }
}

class CartItem {
    private Product p;
    private int q;

    public CartItem(Product pr, int qty) {
        p = pr;
        q = qty;
    }

    public Product getProduct() {
        return p;
    }

    public int getQuantity() {
        return q;
    }

    public double getSubtotal() {
        return p.applyDiscount(p.getPrice()) * q;
    }
}

class Customer {
    private static int nid = 1001;
    private int id;
    private String name, phone, email;
    private ArrayList<CartItem> cart = new ArrayList<>();
    private LinkedList<String> hist = new LinkedList<>();

    public Customer(String n, String p, String e) {
        id = nid++;
        name = n;
        phone = p;
        email = e;
    }

    public int getCustomerId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getPhone() {
        return phone;
    }

    public String getEmail() {
        return email;
    }

    public ArrayList<CartItem> getCart() {
        return cart;
    }

    public LinkedList<String> getHistory() {
        return hist;
    }

    public void addToCart(Product p, int q) {
        cart.add(new CartItem(p, q));
    }

    public void clearCart() {
        cart.clear();
    }

    public double checkout() {
        return checkout(0);
    }

    public double checkout(double pct) {
        return checkout(pct, generateTxnId());
    }

    public double checkout(double pct, String txnId) {
        double t = 0;
        for (CartItem c : cart)
            t += c.getSubtotal();
        if (pct > 0)
            t -= t * pct / 100;
        hist.addFirst("TXN#" + txnId + "  |  " + cart.size() + " item(s)  |  Rs." + String.format("%.2f", t));
        cart.clear();
        return t;
    }

    public static String generateTxnId() {
        String uuid = java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
        return "FM" + uuid;
    }

    private String lastTxnId = "";

    public String getLastTxnId() {
        return lastTxnId;
    }

    public void setLastTxnId(String id) {
        lastTxnId = id;
    }
}

class Inventory {
    private ArrayList<Product> products = new ArrayList<>();
    private int nid = 801;

    public void addProduct(Product p) {
        products.add(p);
    }

    public ArrayList<Product> getAll() {
        return products;
    }

    public int getNextId() {
        return nid++;
    }

    public Product findById(int id) throws ProductNotFoundException {
        for (Product p : products)
            if (p.getProductId() == id)
                return p;
        throw new ProductNotFoundException("Product ID " + id + " not found.");
    }

    public void replaceProduct(int id, Product newP) {
        for (int i = 0; i < products.size(); i++)
            if (products.get(i).getProductId() == id) {
                products.set(i, newP);
                return;
            }
    }
}

public class FreshMitra extends JFrame {

    Inventory inv = new Inventory();
    ArrayList<Customer> customers = new ArrayList<>();

    JPanel content;
    CardLayout cards;
    JLabel statusLbl;
    DefaultTableModel prodModel = new DefaultTableModel(
            new String[] { "ID", "Name", "Category", "Price (Rs.)", "Stock", "Discount", "Extra Info" }, 0) {
        public boolean isCellEditable(int r, int c) {
            return false;
        }
    };
    DefaultTableModel custModel = new DefaultTableModel(
            new String[] { "ID", "Full Name", "Phone", "Email", "Transactions" }, 0) {
        public boolean isCellEditable(int r, int c) {
            return false;
        }
    };
    DefaultTableModel cartModel = new DefaultTableModel(
            new String[] { "ID", "Product Name", "Category", "Qty", "Unit Price", "Subtotal" }, 0) {
        public boolean isCellEditable(int r, int c) {
            return false;
        }
    };

    java.util.LinkedList<String[]> globalTxns = new java.util.LinkedList<>();
    DefaultTableModel recentTxnModel = new DefaultTableModel(
            new String[] { "TXN ID", "Customer", "Amount", "Date/Time", "Cashier ID", "Cashier" }, 0) {
        public boolean isCellEditable(int r, int c) {
            return false;
        }
    };

    JComboBox<String> custCombo;
    JTextField custSearchField;
    java.util.List<Customer> custComboList = new java.util.ArrayList<>();
    JLabel totalLbl, statTotalLbl;
    JLabel statCustNumLbl;
    JLabel statLowStockLbl;
    String activeCatFilter = "All";
    String activePanel = "Dashboard";
    java.util.Map<Integer, String> editedNames = new java.util.HashMap<>();
    java.util.Map<Integer, String> editedExtras = new java.util.HashMap<>();

    static final java.util.Map<String, Color> CAT_COLORS = new java.util.LinkedHashMap<>() {
        {
            put("All", new Color(180, 120, 0));
            put("Food", new Color(0, 140, 100));
            put("Electronics", new Color(30, 100, 200));
            put("Clothing", new Color(120, 60, 200));
            put("Beauty", new Color(200, 60, 120));
            put("Sports", new Color(200, 120, 0));
            put("Stationery", new Color(40, 160, 80));
        }
    };

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            // Init DB before login so LoginDialog can verify cashier credentials
            try {
                DatabaseManager.init();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(null,
                        "Cannot connect to MySQL:\n" + e.getMessage(),
                        "Database Error", JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }
            LoginDialog login = new LoginDialog();
            login.setVisible(true);
            String role = login.getGrantedRole();
            if (role != null)
                new FreshMitra(role, login.getCashierId()).setVisible(true);
        });
    }

    String role; // "MANAGER" or "CASHIER"
    int cashierId; // DB cashier_id when logged in as cashier, -1 for manager

    boolean isManager() {
        return "MANAGER".equals(role);
    }

    public FreshMitra(String role, int cashierId) {
        this.role = role;
        this.cashierId = cashierId;
        loadData();
        setTitle("Fresh Mitra");
        setSize(1280, 800);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setMinimumSize(new Dimension(1000, 650));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                int r = JOptionPane.showConfirmDialog(FreshMitra.this, "Exit Fresh Mitra?", "Confirm Exit",
                        JOptionPane.YES_NO_OPTION);
                if (r == JOptionPane.YES_OPTION) {
                    // Restore in-memory stock for any items still in the cart
                    for (int i = 0; i < cartModel.getRowCount(); i++) {
                        int pid = (int) cartModel.getValueAt(i, 0);
                        int qty = (int) cartModel.getValueAt(i, 3);
                        try {
                            inv.findById(pid).setQuantity(inv.findById(pid).getQuantity() + qty);
                        } catch (ProductNotFoundException ignored) {
                        }
                    }
                    cartModel.setRowCount(0);
                    DatabaseManager.close();
                    System.exit(0);
                }
            }
        });
        setLayout(new BorderLayout(0, 0));
        add(buildHeader(), BorderLayout.NORTH);
        add(buildSidebar(), BorderLayout.WEST);
        add(buildContent(), BorderLayout.CENTER);
        add(buildStatusBar(), BorderLayout.SOUTH);
        showPanel(isManager() ? "Dashboard" : "Products");
    }

    // ================================================================
    // HEADER
    // ================================================================
    JPanel buildHeader() {
        JPanel h = new JPanel(new BorderLayout());
        h.setPreferredSize(new Dimension(0, 54));
        h.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, Color.GRAY),
                new EmptyBorder(0, 18, 0, 18)));
        JLabel title = new JLabel("  Fresh Mitra");
        title.setFont(new Font("SansSerif", Font.BOLD, 22));
        h.add(title, BorderLayout.WEST);

        // Right side: role badge + logout
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 12));
        Color roleColor = isManager() ? new Color(30, 100, 200) : new Color(0, 140, 100);
        String roleLabel = isManager() ? "\u25cf  Manager"
                : "\u25cf  " + (DatabaseManager.getCashierName(cashierId) != null
                        ? DatabaseManager.getCashierName(cashierId) + "  (Cashier)"
                        : "Cashier");
        JLabel roleLbl = new JLabel(roleLabel);
        roleLbl.setFont(new Font("SansSerif", Font.BOLD, 13));
        roleLbl.setForeground(roleColor);
        JButton logoutBtn = new JButton("Logout");
        logoutBtn.setFont(new Font("SansSerif", Font.PLAIN, 12));
        logoutBtn.setFocusPainted(false);
        logoutBtn.addActionListener(e -> {
            int r = JOptionPane.showConfirmDialog(FreshMitra.this,
                    "Logout and return to login screen?", "Logout", JOptionPane.YES_NO_OPTION);
            if (r == JOptionPane.YES_OPTION) {
                dispose();
                SwingUtilities.invokeLater(() -> {
                    LoginDialog login = new LoginDialog();
                    login.setVisible(true);
                    String newRole = login.getGrantedRole();
                    if (newRole != null)
                        new FreshMitra(newRole, login.getCashierId()).setVisible(true);
                });
            }
        });
        right.add(roleLbl);
        right.add(logoutBtn);
        h.add(right, BorderLayout.EAST);
        return h;
    }

    // ================================================================
    // SIDEBAR
    // ================================================================
    java.util.List<JButton> navBtns = new java.util.ArrayList<>();

    JPanel buildSidebar() {
        JPanel side = new JPanel();
        side.setLayout(new BoxLayout(side, BoxLayout.Y_AXIS));
        side.setPreferredSize(new Dimension(180, 0));
        side.setBorder(new CompoundBorder(
                new MatteBorder(0, 0, 0, 1, Color.GRAY),
                new EmptyBorder(16, 0, 16, 0)));

        String[][] navItems = isManager()
                ? new String[][] {
                        { "Dashboard", "Dashboard" },
                        { "Products", "Products" },
                        { "Customers", "Customers" },
                        { "Billing", "Billing" },
                        { "Cashiers", "Cashiers" },
                        { "Analytics", "Analytics" },
                        { "Reports", "Reports" } }
                : new String[][] {
                        { "Products", "Products" },
                        { "Customers", "Customers" },
                        { "Billing", "Billing" },
                        { "My Analytics", "Analytics" } };
        for (String[] item : navItems) {
            side.add(sideBtn(item[0], item[1]));
            side.add(Box.createVerticalStrut(2));
        }
        side.add(Box.createVerticalGlue());
        return side;
    }

    JButton sideBtn(String label, String panel) {
        JButton b = new JButton(label);
        b.setActionCommand(panel);
        b.setFont(new Font("SansSerif", Font.PLAIN, 14));
        b.setMaximumSize(new Dimension(180, 38));
        b.setPreferredSize(new Dimension(180, 38));
        b.setAlignmentX(Component.LEFT_ALIGNMENT);
        b.setHorizontalAlignment(SwingConstants.LEFT);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.addActionListener(e -> {
            showPanel(b.getActionCommand());
            for (JButton nb : navBtns) {
                nb.setFont(new Font("SansSerif",
                        activePanel.equals(nb.getActionCommand()) ? Font.BOLD : Font.PLAIN, 14));
            }
        });
        navBtns.add(b);
        return b;
    }

    JPanel buildContent() {
        cards = new CardLayout();
        content = new JPanel(cards);
        content.add(dashPanel(), "Dashboard");
        content.add(productsPanel(), "Products");
        content.add(custPanel(), "Customers");
        content.add(billingPanel(), "Billing");
        content.add(cashiersPanel(), "Cashiers");
        content.add(reportsPanel(), "Reports");
        content.add(analyticsPanel(), "Analytics");
        return content;
    }

    JPanel buildStatusBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBorder(new CompoundBorder(
                new MatteBorder(1, 0, 0, 0, Color.GRAY),
                new EmptyBorder(2, 8, 2, 8)));
        bar.setPreferredSize(new Dimension(0, 24));
        statusLbl = new JLabel("Ready");
        statusLbl.setFont(new Font("Monospaced", Font.PLAIN, 11));
        bar.add(statusLbl, BorderLayout.WEST);
        return bar;
    }

    void status(String m) {
        statusLbl.setText(m);
    }

    void showPanel(String n) {
        activePanel = n;
        if ("Billing".equals(n))
            refreshCustCombo();
        if ("Dashboard".equals(n))
            refreshDash();
        cards.show(content, n);
        status("Viewing: " + n);
        for (JButton nb : navBtns) {
            nb.setFont(new Font("SansSerif", activePanel.equals(nb.getActionCommand()) ? Font.BOLD : Font.PLAIN, 14));
        }
    }

    // ================================================================
    // DASHBOARD
    // ================================================================
    void refreshDash() {
        if (statTotalLbl == null)
            return;
        int lowStock = 0;
        for (Product p : inv.getAll())
            if (p.getQuantity() < 5)
                lowStock++;
        statTotalLbl.setText(String.valueOf(inv.getAll().size()));
        if (statCustNumLbl != null)
            statCustNumLbl.setText(String.valueOf(customers.size()));
        if (statLowStockLbl != null)
            statLowStockLbl.setText(String.valueOf(lowStock));
        if (recentTxnModel != null) {
            recentTxnModel.setRowCount(0);
            if (globalTxns.isEmpty()) {
                recentTxnModel.addRow(new Object[] { "No recent transactions", "—", "—", "—", "—", "—" });
            } else {
                for (String[] row : globalTxns)
                    recentTxnModel.addRow(row);
            }
        }
    }

    JPanel dashPanel() {
        JPanel p = new JPanel(new BorderLayout(0, 0));
        p.setBorder(new EmptyBorder(24, 24, 24, 24));

        JLabel titleLbl = new JLabel("Dashboard");
        titleLbl.setFont(new Font("SansSerif", Font.BOLD, 24));
        titleLbl.setBorder(new EmptyBorder(0, 0, 16, 0));
        p.add(titleLbl, BorderLayout.NORTH);

        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));

        // Stats row
        JPanel row1 = new JPanel(new GridLayout(1, 3, 12, 0));
        row1.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));
        row1.setPreferredSize(new Dimension(0, 120));

        statTotalLbl = new JLabel("0", SwingConstants.CENTER);
        statCustNumLbl = new JLabel("0", SwingConstants.CENTER);
        statLowStockLbl = new JLabel("0", SwingConstants.CENTER);
        statTotalLbl.setFont(new Font("SansSerif", Font.BOLD, 36));
        statCustNumLbl.setFont(new Font("SansSerif", Font.BOLD, 36));
        statLowStockLbl.setFont(new Font("SansSerif", Font.BOLD, 36));
        statLowStockLbl.setForeground(Color.RED);

        row1.add(makeStatCard(statTotalLbl, "Total Products", null));
        row1.add(makeStatCard(statCustNumLbl, "Customers", null));
        row1.add(makeStatCard(statLowStockLbl, "Low Stock Items", null));

        // Bottom split
        JPanel bottomSplit = new JPanel(new BorderLayout(12, 0));
        bottomSplit.setMaximumSize(new Dimension(Integer.MAX_VALUE, 260));
        bottomSplit.setPreferredSize(new Dimension(0, 260));

        JPanel leftActions = new JPanel(new GridLayout(3, 1, 0, 8));
        leftActions.setPreferredSize(new Dimension(240, 0));

        JButton tile1 = new JButton("+ Add Product");
        JButton tile2 = new JButton("+ Register Customer");
        JButton tile3 = new JButton("Open Billing");
        for (JButton b : new JButton[] { tile1, tile2, tile3 }) {
            b.setFont(new Font("SansSerif", Font.PLAIN, 14));
            b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }
        tile3.setFont(new Font("SansSerif", Font.BOLD, 14));
        tile1.addActionListener(e -> {
            showPanel("Products");
            openAddProduct();
        });
        tile2.addActionListener(e -> {
            showPanel("Customers");
            openAddCustomer();
        });
        tile3.addActionListener(e -> showPanel("Billing"));
        leftActions.add(tile1);
        leftActions.add(tile2);
        leftActions.add(tile3);

        JPanel recentPanel = new JPanel(new BorderLayout());
        recentPanel.setBorder(BorderFactory.createTitledBorder("Recent Transactions"));
        JTable recentTable = new JTable(recentTxnModel);
        recentTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        recentTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        recentTable.getColumnModel().getColumn(0).setPreferredWidth(140);
        recentTable.getColumnModel().getColumn(1).setPreferredWidth(160);
        recentTable.getColumnModel().getColumn(2).setPreferredWidth(90);
        recentTable.getColumnModel().getColumn(3).setPreferredWidth(120);
        recentTable.getColumnModel().getColumn(4).setPreferredWidth(75);
        recentTable.getColumnModel().getColumn(5).setPreferredWidth(120);
        recentPanel.add(new JScrollPane(recentTable), BorderLayout.CENTER);

        bottomSplit.add(leftActions, BorderLayout.WEST);
        bottomSplit.add(recentPanel, BorderLayout.CENTER);

        body.add(row1);
        body.add(Box.createVerticalStrut(12));
        body.add(bottomSplit);
        p.add(body, BorderLayout.CENTER);
        refreshDash();
        return p;
    }

    JPanel makeStatCard(JLabel numLbl, String caption, Color accent) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.GRAY),
                new EmptyBorder(14, 14, 14, 14)));
        JLabel cap = new JLabel(caption, SwingConstants.CENTER);
        cap.setFont(new Font("SansSerif", Font.PLAIN, 13));
        card.add(numLbl, BorderLayout.CENTER);
        card.add(cap, BorderLayout.SOUTH);
        return card;
    }

    // ================================================================
    // PRODUCTS
    // ================================================================
    JTable prodTable;

    JPanel productsPanel() {
        JPanel p = new JPanel(new BorderLayout(0, 0));
        p.setBorder(new EmptyBorder(20, 24, 24, 24));

        JPanel top = new JPanel(new BorderLayout(0, 8));
        JLabel titleLbl = new JLabel("Products");
        titleLbl.setFont(new Font("SansSerif", Font.BOLD, 24));
        top.add(titleLbl, BorderLayout.WEST);

        JPanel tb = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        JTextField sf = new JTextField(16);
        sf.setToolTipText("Search products...");
        sf.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                activeCatFilter = "All";
                applyProdsFilter(sf.getText().trim());
            }
        });
        tb.add(new JLabel("Search:"));
        tb.add(sf);

        if (isManager()) {
            JButton addB = new JButton("+ Add");
            JButton editB = new JButton("Edit");
            JButton rsB = new JButton("Restock");
            JButton delB = new JButton("Remove");
            JButton discB = new JButton("% Category Discounts");
            discB.setFont(new Font("SansSerif", Font.BOLD, 12));
            discB.setForeground(new Color(30, 100, 200));
            addB.addActionListener(e -> {
                openAddProduct();
                refreshProds();
            });
            editB.addActionListener(e -> editProd());
            rsB.addActionListener(e -> restockProd());
            delB.addActionListener(e -> deleteProd());
            discB.addActionListener(e -> openCategoryDiscountDialog());
            tb.add(discB);
            tb.add(addB);
            tb.add(editB);
            tb.add(rsB);
            tb.add(delB);
        }
        top.add(tb, BorderLayout.EAST);

        // Category filter bar
        JPanel catBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        catBar.setBorder(new EmptyBorder(4, 0, 4, 0));
        java.util.List<JButton> catBtns = new java.util.ArrayList<>();
        for (String cat : CAT_COLORS.keySet()) {
            JButton cb = new JButton(cat);
            cb.setFont(new Font("SansSerif", Font.PLAIN, 12));
            cb.addActionListener(e -> {
                activeCatFilter = cat;
                applyProdsFilter("");
                for (JButton b2 : catBtns)
                    b2.setFont(
                            new Font("SansSerif", activeCatFilter.equals(b2.getText()) ? Font.BOLD : Font.PLAIN, 12));
            });
            catBtns.add(cb);
            catBar.add(cb);
        }
        top.add(catBar, BorderLayout.SOUTH);
        p.add(top, BorderLayout.NORTH);

        prodTable = new JTable(prodModel);
        prodTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        prodTable.getColumnModel().getColumn(0).setPreferredWidth(50); // ID
        prodTable.getColumnModel().getColumn(1).setPreferredWidth(230); // Name
        prodTable.getColumnModel().getColumn(2).setPreferredWidth(100); // Category
        prodTable.getColumnModel().getColumn(3).setPreferredWidth(95); // Price
        prodTable.getColumnModel().getColumn(4).setPreferredWidth(60); // Stock
        prodTable.getColumnModel().getColumn(5).setPreferredWidth(90); // Discount
        prodTable.getColumnModel().getColumn(6).setPreferredWidth(175); // Extra Info
        p.add(new JScrollPane(prodTable), BorderLayout.CENTER);
        refreshProds();
        return p;
    }

    void refreshProds() {
        applyProdsFilter("");
    }

    void applyProdsFilter(String q) {
        prodModel.setRowCount(0);
        for (Product p : inv.getAll()) {
            boolean catOk = activeCatFilter.equals("All") || p.getCategory().equals(activeCatFilter);
            boolean txtOk = q.isEmpty() || p.getProductName().toLowerCase().contains(q.toLowerCase())
                    || p.getCategory().toLowerCase().contains(q.toLowerCase());
            if (catOk && txtOk) {
                String dispName = editedNames.getOrDefault(p.getProductId(), p.getProductName());
                String dispExtra = editedExtras.getOrDefault(p.getProductId(), p.getExtraInfo());
                prodModel.addRow(new Object[] { p.getProductId(), dispName, p.getCategory(),
                        String.format("%.2f", p.getPrice()), p.getQuantity(), p.getDiscountInfo(), dispExtra });
            }
        }
    }

    void openAddProduct() {
        JDialog d = new JDialog(this, "Add New Product", true);
        d.setSize(420, 360);
        d.setLocationRelativeTo(this);
        JPanel form = new JPanel(new GridLayout(0, 2, 8, 10));
        form.setBorder(new EmptyBorder(16, 16, 8, 16));
        JComboBox<String> typeBox = new JComboBox<>(
                new String[] { "Food", "Electronics", "Clothing", "Beauty", "Sports", "Stationery" });
        JLabel extraHint = new JLabel("Expiry date");
        extraHint.setFont(new Font("SansSerif", Font.ITALIC, 11));
        typeBox.addActionListener(e -> {
            switch ((String) typeBox.getSelectedItem()) {
                case "Food" -> extraHint.setText("Expiry date (DD/MM/YYYY)");
                case "Electronics" -> extraHint.setText("Warranty in months (number)");
                case "Clothing" -> extraHint.setText("Size (S/M/L/XL/number)");
                case "Beauty" -> extraHint.setText("Brand name");
                case "Sports" -> extraHint.setText("Sport type (e.g. Cricket)");
                case "Stationery" -> extraHint.setText("Use (e.g. Writing, Drawing)");
            }
        });
        JTextField nf = new JTextField(), pf = new JTextField(), qf = new JTextField(), ef = new JTextField();
        form.add(new JLabel("Type:"));
        form.add(typeBox);
        form.add(new JLabel("Name:"));
        form.add(nf);
        form.add(new JLabel("Price (Rs.):"));
        form.add(pf);
        form.add(new JLabel("Quantity:"));
        form.add(qf);
        form.add(new JLabel("Extra Info:"));
        form.add(ef);
        form.add(new JLabel());
        form.add(extraHint);

        JPanel bp = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        JButton save = new JButton("Save");
        JButton cancel = new JButton("Cancel");
        cancel.addActionListener(e -> d.dispose());
        save.addActionListener(e -> {
            try {
                String type = (String) typeBox.getSelectedItem(), name = nf.getText().trim(),
                        extra = ef.getText().trim();
                double price = Double.parseDouble(pf.getText().trim());
                int qty = Integer.parseInt(qf.getText().trim());
                if (name.isEmpty() || extra.isEmpty())
                    throw new InvalidInputException("All fields required.");
                if (price <= 0 || qty < 0)
                    throw new InvalidInputException("Price>0, Qty>=0.");
                int id = inv.getNextId();
                Product newProd = null;
                switch (type) {
                    case "Food" -> {
                        newProd = new FoodProduct(id, name, price, qty, extra);
                        inv.addProduct(newProd);
                    }
                    case "Electronics" -> {
                        newProd = new ElectronicsProduct(id, name, price, qty, Integer.parseInt(extra));
                        inv.addProduct(newProd);
                    }
                    case "Clothing" -> {
                        newProd = new ClothingProduct(id, name, price, qty, extra);
                        inv.addProduct(newProd);
                    }
                    case "Beauty" -> {
                        newProd = new BeautyProduct(id, name, price, qty, extra);
                        inv.addProduct(newProd);
                    }
                    case "Sports" -> {
                        newProd = new SportsProduct(id, name, price, qty, extra);
                        inv.addProduct(newProd);
                    }
                    case "Stationery" -> {
                        newProd = new StationeryProduct(id, name, price, qty, extra);
                        inv.addProduct(newProd);
                    }
                }
                if (newProd != null) {
                    try {
                        DatabaseManager.insertProduct(newProd);
                    } catch (RuntimeException ex) {
                        JOptionPane.showMessageDialog(d, "Database error saving product:\n" + ex.getMessage(),
                                "DB Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                }
                refreshProds();
                refreshDash();
                status("Product '" + name + "' added (ID:" + id + ")");
                d.dispose();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(d,
                        "Invalid number — for Electronics enter warranty months as a plain integer.", "Error",
                        JOptionPane.ERROR_MESSAGE);
            } catch (InvalidInputException ex) {
                JOptionPane.showMessageDialog(d, ex.getMessage(), "Validation", JOptionPane.WARNING_MESSAGE);
            }
        });
        bp.add(cancel);
        bp.add(save);
        d.setLayout(new BorderLayout());
        d.add(form, BorderLayout.CENTER);
        d.add(bp, BorderLayout.SOUTH);
        d.setVisible(true);
    }

    void restockProd() {
        int row = prodTable.getSelectedRow();
        if (row < 0) {
            msg("Select a product first.");
            return;
        }
        int id = (int) prodModel.getValueAt(row, 0);
        String in = JOptionPane.showInputDialog(this, "Additional quantity to add:", "Restock",
                JOptionPane.QUESTION_MESSAGE);
        if (in == null)
            return;
        try {
            Product p = inv.findById(id);
            int newQty = p.getQuantity() + Integer.parseInt(in.trim());
            p.setQuantity(newQty);
            try {
                DatabaseManager.updateProductStock(p.getProductId(), newQty);
            } catch (RuntimeException ex) {
                JOptionPane.showMessageDialog(this, "Database error during restock:\n" + ex.getMessage(), "DB Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
            refreshProds();
            status("Restocked '" + p.getProductName() + "'.");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    void deleteProd() {
        int[] rows = prodTable.getSelectedRows();
        if (rows.length == 0) {
            msg("Select at least one product first.");
            return;
        }
        if (JOptionPane.showConfirmDialog(this, "Remove " + rows.length + " selected product(s)?", "Confirm",
                JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            java.util.List<Integer> idsToDelete = new java.util.ArrayList<>();
            for (int row : rows)
                idsToDelete.add((int) prodModel.getValueAt(row, 0));
            inv.getAll().removeIf(p -> idsToDelete.contains(p.getProductId()));
            for (int pid : idsToDelete) {
                try {
                    DatabaseManager.deleteProduct(pid);
                } catch (RuntimeException ex) {
                    JOptionPane.showMessageDialog(this, "Database error removing product:\n" + ex.getMessage(),
                            "DB Error", JOptionPane.ERROR_MESSAGE);
                }
            }
            refreshProds();
            refreshDash();
            status(rows.length + " product(s) removed.");
        }
    }

    void editProd() {
        int row = prodTable.getSelectedRow();
        if (row < 0) {
            msg("Select a product to edit.");
            return;
        }
        int id = (int) prodModel.getValueAt(row, 0);
        Product p;
        try {
            p = inv.findById(id);
        } catch (ProductNotFoundException ex) {
            msg(ex.getMessage());
            return;
        }

        JDialog d = new JDialog(this, "Edit Product #" + id, true);
        d.setSize(440, 380);
        d.setLocationRelativeTo(this);
        JPanel form = new JPanel(new GridLayout(0, 2, 8, 10));
        form.setBorder(new EmptyBorder(16, 16, 8, 16));

        JTextField nameF = new JTextField(p.getProductName());
        JTextField priceF = new JTextField(String.format("%.2f", p.getPrice()));
        JTextField qtyF = new JTextField(String.valueOf(p.getQuantity()));
        JTextField extraF = new JTextField(p.getExtraInfo()
                .replace("Expiry: ", "").replace("Warranty: ", "").replace(" mo", "")
                .replace("Size: ", "").replace("Brand: ", "").replace("Sport: ", "").replace("Use: ", ""));

        JComboBox<String> catBox = new JComboBox<>(
                new String[] { "Food", "Electronics", "Clothing", "Beauty", "Sports", "Stationery" });
        catBox.setSelectedItem(p.getCategory());

        SpinnerNumberModel discModel = new SpinnerNumberModel(p.getDiscount(), 0.0, 99.0, 0.5);
        JSpinner discSpinner = new JSpinner(discModel);
        discSpinner.setFont(new Font("SansSerif", Font.PLAIN, 13));

        // Update Extra Info hint when category changes
        JLabel extraHint = new JLabel(getExtraHint(p.getCategory()));
        extraHint.setFont(new Font("SansSerif", Font.ITALIC, 11));
        extraHint.setForeground(Color.GRAY);
        catBox.addActionListener(e -> extraHint.setText(getExtraHint((String) catBox.getSelectedItem())));

        form.add(new JLabel("Name:"));
        form.add(nameF);
        form.add(new JLabel("Category:"));
        form.add(catBox);
        form.add(new JLabel("Price (Rs.):"));
        form.add(priceF);
        form.add(new JLabel("Discount %:"));
        form.add(discSpinner);
        form.add(new JLabel("Stock Qty:"));
        form.add(qtyF);
        form.add(new JLabel("Extra Info:"));
        form.add(extraF);
        form.add(new JLabel());
        form.add(extraHint);

        JPanel bp = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        JButton save = new JButton("Save Changes");
        JButton cancel = new JButton("Cancel");
        cancel.addActionListener(e -> d.dispose());
        save.addActionListener(e -> {
            try {
                String newName = nameF.getText().trim();
                String newCat = (String) catBox.getSelectedItem();
                double newPrice = Double.parseDouble(priceF.getText().trim());
                double newDisc = ((Number) discSpinner.getValue()).doubleValue();
                int newQty = Integer.parseInt(qtyF.getText().trim());
                String newExtra = extraF.getText().trim();
                if (newName.isEmpty() || newExtra.isEmpty())
                    throw new InvalidInputException("Name and Extra Info cannot be empty.");
                if (newPrice <= 0)
                    throw new InvalidInputException("Price must be greater than 0.");
                if (newQty < 0)
                    throw new InvalidInputException("Stock cannot be negative.");

                Product updated;
                if (!newCat.equals(p.getCategory())) {
                    // Category changed — build a new product of the right subclass
                    updated = buildEditedProduct(id, newName, newCat, newPrice, newQty, newExtra);
                    if (updated == null)
                        throw new InvalidInputException("Invalid extra info for selected category.");
                    updated.setDiscount(newDisc);
                    inv.replaceProduct(id, updated);
                } else {
                    // Same category — just update fields in place
                    p.setName(newName);
                    p.setPrice(newPrice);
                    p.setQuantity(newQty);
                    p.setDiscount(newDisc);
                    updated = p;
                }
                editedExtras.put(id, newExtra);
                editedNames.put(id, newName);
                try {
                    DatabaseManager.updateProduct(id, newName, newPrice, newQty, newExtra);
                } catch (RuntimeException ex) {
                    JOptionPane.showMessageDialog(d, "Database error saving changes:\n" + ex.getMessage(), "DB Error",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }
                refreshProds();
                refreshDash();
                status("Updated: " + newName);
                d.dispose();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(d,
                        "Invalid number — for Electronics enter warranty months as a plain integer.", "Error",
                        JOptionPane.ERROR_MESSAGE);
            } catch (InvalidInputException ex) {
                JOptionPane.showMessageDialog(d, ex.getMessage(), "Validation", JOptionPane.WARNING_MESSAGE);
            }
        });
        bp.add(cancel);
        bp.add(save);
        d.setLayout(new BorderLayout());
        d.add(form, BorderLayout.CENTER);
        d.add(bp, BorderLayout.SOUTH);
        d.setVisible(true);
    }

    String getExtraHint(String cat) {
        return switch (cat) {
            case "Food" -> "Expiry date (DD/MM/YYYY)";
            case "Electronics" -> "Warranty in months (number)";
            case "Clothing" -> "Size (S/M/L/XL/number)";
            case "Beauty" -> "Brand name";
            case "Sports" -> "Sport type (e.g. Cricket)";
            default -> "Use (e.g. Writing, Drawing)";
        };
    }

    Product buildEditedProduct(int id, String name, String cat, double price, int qty, String extra) {
        try {
            return switch (cat) {
                case "Food" -> new FoodProduct(id, name, price, qty, extra);
                case "Electronics" ->
                    new ElectronicsProduct(id, name, price, qty, Integer.parseInt(extra.replaceAll("[^0-9]", "")));
                case "Clothing" -> new ClothingProduct(id, name, price, qty, extra);
                case "Beauty" -> new BeautyProduct(id, name, price, qty, extra);
                case "Sports" -> new SportsProduct(id, name, price, qty, extra);
                default -> new StationeryProduct(id, name, price, qty, extra);
            };
        } catch (Exception e) {
            return null;
        }
    }

    // ================================================================
    // CATEGORY DISCOUNTS
    // ================================================================
    void openCategoryDiscountDialog() {
        String[] cats = { "Food", "Electronics", "Clothing", "Beauty", "Sports", "Stationery" };
        double[] defaults = { 5, 10, 15, 12, 8, 6 };

        // Find current discount from first product of each category (or use default)
        double[] current = new double[cats.length];
        for (int i = 0; i < cats.length; i++) {
            final String cat = cats[i];
            current[i] = defaults[i];
            for (Product p : inv.getAll()) {
                if (p.getCategory().equals(cat)) {
                    current[i] = p.getDiscount();
                    break;
                }
            }
        }

        JDialog d = new JDialog(this, "Edit Category Discounts", true);
        d.setSize(420, 360);
        d.setLocationRelativeTo(this);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(new EmptyBorder(16, 20, 8, 20));
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(6, 8, 6, 8);
        gc.fill = GridBagConstraints.HORIZONTAL;

        // Header row
        gc.gridy = 0;
        gc.gridx = 0;
        gc.weightx = 1;
        JLabel hCat = new JLabel("Category");
        hCat.setFont(new Font("SansSerif", Font.BOLD, 13));
        form.add(hCat, gc);
        gc.gridx = 1;
        gc.weightx = 0;
        JLabel hDisc = new JLabel("Discount %");
        hDisc.setFont(new Font("SansSerif", Font.BOLD, 13));
        form.add(hDisc, gc);

        JSpinner[] spinners = new JSpinner[cats.length];
        Color[] rowColors = {
                new Color(0, 140, 100, 20), new Color(30, 100, 200, 20), new Color(120, 60, 200, 20),
                new Color(200, 60, 120, 20), new Color(200, 120, 0, 20), new Color(40, 160, 80, 20)
        };

        for (int i = 0; i < cats.length; i++) {
            gc.gridy = i + 1;
            gc.gridx = 0;
            gc.weightx = 1;
            JLabel lbl = new JLabel(cats[i]);
            lbl.setFont(new Font("SansSerif", Font.PLAIN, 13));
            lbl.setOpaque(true);
            lbl.setBackground(rowColors[i]);
            lbl.setBorder(new EmptyBorder(2, 6, 2, 6));
            form.add(lbl, gc);

            gc.gridx = 1;
            gc.weightx = 0;
            SpinnerNumberModel model = new SpinnerNumberModel(current[i], 0.0, 99.0, 0.5);
            spinners[i] = new JSpinner(model);
            spinners[i].setFont(new Font("SansSerif", Font.PLAIN, 13));
            spinners[i].setPreferredSize(new Dimension(90, 28));
            form.add(spinners[i], gc);
        }

        JPanel bp = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        JButton cancel = new JButton("Cancel");
        JButton apply = new JButton("Apply to All");
        apply.setFont(new Font("SansSerif", Font.BOLD, 13));
        apply.setForeground(new Color(30, 100, 200));

        cancel.addActionListener(e -> d.dispose());
        apply.addActionListener(e -> {
            StringBuilder summary = new StringBuilder("<html><b>Discounts updated:</b><br>");
            for (int i = 0; i < cats.length; i++) {
                double newDisc = ((Number) spinners[i].getValue()).doubleValue();
                String cat = cats[i];
                int count = 0;
                for (Product p : inv.getAll()) {
                    if (p.getCategory().equals(cat)) {
                        p.setDiscount(newDisc);
                        count++;
                    }
                }
                summary.append("&nbsp;&nbsp;").append(cat).append(": <b>")
                        .append(String.format("%.1f", newDisc)).append("%</b> (").append(count)
                        .append(" products)<br>");
            }
            summary.append("</html>");
            refreshProds();
            status("Category discounts updated.");
            JOptionPane.showMessageDialog(d, summary.toString(), "Discounts Applied", JOptionPane.INFORMATION_MESSAGE);
            d.dispose();
        });

        bp.add(cancel);
        bp.add(apply);
        d.setLayout(new BorderLayout());
        d.add(form, BorderLayout.CENTER);
        d.add(bp, BorderLayout.SOUTH);
        d.setVisible(true);
    }

    // ================================================================
    // CUSTOMERS
    // ================================================================
    JTable custTable;

    JPanel custPanel() {
        JPanel p = new JPanel(new BorderLayout(0, 0));
        p.setBorder(new EmptyBorder(20, 24, 24, 24));

        JPanel top = new JPanel(new BorderLayout(0, 8));
        JLabel titleLbl = new JLabel("Customers");
        titleLbl.setFont(new Font("SansSerif", Font.BOLD, 24));
        top.add(titleLbl, BorderLayout.WEST);

        JPanel tb = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        JTextField sf = new JTextField(16);
        JButton addB = new JButton("+ Register");
        JButton histB = new JButton("View History");
        JButton editB = new JButton("Edit");
        sf.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                filterCust(sf.getText().trim());
            }
        });
        addB.addActionListener(e -> {
            openAddCustomer();
            refreshCust();
        });
        histB.addActionListener(e -> custHist());
        editB.addActionListener(e -> editCustomer());
        tb.add(new JLabel("Search:"));
        tb.add(sf);
        tb.add(addB);
        tb.add(histB);
        tb.add(editB);
        top.add(tb, BorderLayout.EAST);
        p.add(top, BorderLayout.NORTH);

        custTable = new JTable(custModel);
        custTable.getColumnModel().getColumn(0).setPreferredWidth(50);
        custTable.getColumnModel().getColumn(1).setPreferredWidth(180);
        custTable.getColumnModel().getColumn(2).setPreferredWidth(120);
        custTable.getColumnModel().getColumn(3).setPreferredWidth(220);
        custTable.getColumnModel().getColumn(4).setPreferredWidth(100);
        p.add(new JScrollPane(custTable), BorderLayout.CENTER);
        refreshCust();
        return p;
    }

    void refreshCust() {
        custModel.setRowCount(0);
        for (Customer c : customers)
            custModel.addRow(new Object[] {
                    c.getCustomerId(), c.getName(), c.getPhone(), c.getEmail(),
                    DatabaseManager.getTransactionCount(c.getCustomerId()) + " txn(s)"
            });
    }

    void filterCust(String q) {
        custModel.setRowCount(0);
        for (Customer c : customers) {
            String ql = q.toLowerCase();
            if (q.isEmpty() || c.getName().toLowerCase().contains(ql) || c.getPhone().contains(ql)
                    || c.getEmail().toLowerCase().contains(ql))
                custModel.addRow(new Object[] { c.getCustomerId(), c.getName(), c.getPhone(), c.getEmail(),
                        DatabaseManager.getTransactionCount(c.getCustomerId()) + " txn(s)" });
        }
    }

    void openAddCustomer() {
        JDialog d = new JDialog(this, "Register New Customer", true);
        d.setSize(380, 260);
        d.setLocationRelativeTo(this);
        JPanel form = new JPanel(new GridLayout(0, 2, 8, 10));
        form.setBorder(new EmptyBorder(16, 16, 8, 16));

        JTextField nf = new JTextField();
        JTextField pf = new JTextField();
        JTextField ef = new JTextField();
        form.add(new JLabel("Full Name *:"));
        form.add(nf);
        form.add(new JLabel("Phone *:"));
        form.add(pf);
        form.add(new JLabel("Email:"));
        form.add(ef);

        JPanel bp = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        JButton save = new JButton("Register");
        JButton cancel = new JButton("Cancel");
        cancel.addActionListener(e -> d.dispose());
        save.addActionListener(e -> {
            try {
                String name = nf.getText().trim(), phone = pf.getText().trim(), email = ef.getText().trim();
                if (name.isEmpty())
                    throw new InvalidInputException("Full Name is required.");
                if (phone.length() != 10 || !phone.matches("[0-9]+"))
                    throw new InvalidInputException("Phone must be exactly 10 digits.");
                Customer c = new Customer(name, phone, email.isEmpty() ? "—" : email);
                customers.add(c);
                try {
                    DatabaseManager.insertCustomer(c);
                } catch (RuntimeException ex) {
                    customers.remove(c);
                    JOptionPane.showMessageDialog(d, "Database error registering customer:\n" + ex.getMessage(),
                            "DB Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                refreshCust();
                refreshCustCombo();
                status("Registered: " + name + " (ID:#" + c.getCustomerId() + ")");
                d.dispose();
            } catch (InvalidInputException ex) {
                JOptionPane.showMessageDialog(d, ex.getMessage(), "Validation", JOptionPane.WARNING_MESSAGE);
            }
        });
        bp.add(cancel);
        bp.add(save);
        d.setLayout(new BorderLayout());
        d.add(form, BorderLayout.CENTER);
        d.add(bp, BorderLayout.SOUTH);
        d.setVisible(true);
    }

    void editCustomer() {
        int row = custTable.getSelectedRow();
        if (row < 0) {
            msg("Select a customer to edit.");
            return;
        }
        int cid = (int) custModel.getValueAt(row, 0);
        Customer c = customers.stream().filter(x -> x.getCustomerId() == cid).findFirst().orElse(null);
        if (c == null)
            return;

        JDialog d = new JDialog(this, "Edit Customer #" + cid, true);
        d.setSize(380, 240);
        d.setLocationRelativeTo(this);
        JPanel form = new JPanel(new GridLayout(0, 2, 8, 10));
        form.setBorder(new EmptyBorder(16, 16, 8, 16));

        JTextField nf = new JTextField(c.getName());
        JTextField pf = new JTextField(c.getPhone());
        JTextField ef = new JTextField(c.getEmail().equals("—") ? "" : c.getEmail());
        form.add(new JLabel("Full Name *:"));
        form.add(nf);
        form.add(new JLabel("Phone *:"));
        form.add(pf);
        form.add(new JLabel("Email:"));
        form.add(ef);

        JPanel bp = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        JButton save = new JButton("Save Changes");
        JButton cancel = new JButton("Cancel");
        cancel.addActionListener(e -> d.dispose());
        save.addActionListener(e -> {
            String newName = nf.getText().trim(), newPhone = pf.getText().trim(), newEmail = ef.getText().trim();
            if (newName.isEmpty() || newPhone.isEmpty()) {
                JOptionPane.showMessageDialog(d, "Name and Phone are required.");
                return;
            }
            if (newPhone.length() != 10 || !newPhone.matches("[0-9]+")) {
                JOptionPane.showMessageDialog(d, "Phone must be exactly 10 digits.", "Error",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            String finalEmail = newEmail.isEmpty() ? "—" : newEmail;
            try {
                java.lang.reflect.Field fn = Customer.class.getDeclaredField("name");
                java.lang.reflect.Field fp = Customer.class.getDeclaredField("phone");
                java.lang.reflect.Field fe = Customer.class.getDeclaredField("email");
                fn.setAccessible(true);
                fp.setAccessible(true);
                fe.setAccessible(true);
                fn.set(c, newName);
                fp.set(c, newPhone);
                fe.set(c, finalEmail);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            try {
                DatabaseManager.updateCustomer(c.getCustomerId(), newName, newPhone, finalEmail);
            } catch (RuntimeException ex) {
                JOptionPane.showMessageDialog(d, "Database error updating customer:\n" + ex.getMessage(), "DB Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
            refreshCust();
            refreshCustCombo();
            status("Updated: " + newName);
            d.dispose();
        });
        bp.add(cancel);
        bp.add(save);
        d.setLayout(new BorderLayout());
        d.add(form, BorderLayout.CENTER);
        d.add(bp, BorderLayout.SOUTH);
        d.setVisible(true);
    }

    void custHist() {
        int row = custTable.getSelectedRow();
        if (row < 0) {
            msg("Select a customer first.");
            return;
        }
        int id = (int) custModel.getValueAt(row, 0);
        Customer c = customers.stream().filter(x -> x.getCustomerId() == id).findFirst().orElse(null);
        if (c == null)
            return;

        JDialog d = new JDialog(this, "Purchase History - " + c.getName(), true);
        d.setSize(680, 460);
        d.setLocationRelativeTo(this);
        d.setLayout(new BorderLayout(0, 0));

        java.util.List<String[]> dbRows = DatabaseManager.loadCustomerHistory(c.getCustomerId());

        if (dbRows.isEmpty()) {
            JLabel empty = new JLabel("No transactions yet.", SwingConstants.CENTER);
            empty.setFont(new Font("SansSerif", Font.ITALIC, 14));
            d.add(empty, BorderLayout.CENTER);
        } else {
            String[] cols = { "#", "TXN ID", "Date/Time", "Amount", "View Bill" };
            DefaultTableModel hm = new DefaultTableModel(cols, 0) {
                public boolean isCellEditable(int r, int c2) {
                    return c2 == 4;
                }
            };
            java.util.List<String> billTexts = new java.util.ArrayList<>();
            java.util.List<String> txnIds = new java.util.ArrayList<>();
            int num = 1;
            for (String[] rec : dbRows) {
                String displayLine = rec[0], billText = rec[1];
                String txnId = "—", amtStr = "—", timeStr = "—";
                try {
                    int ts = displayLine.indexOf("TXN#") + 4;
                    int te = displayLine.indexOf("  |  ");
                    if (ts > 3 && te > ts) {
                        txnId = displayLine.substring(ts, te);
                    }
                    int as = displayLine.indexOf("Rs.") + 3;
                    int ae = displayLine.indexOf("  |  ", te + 5);
                    if (as > 2) {
                        amtStr = "Rs. " + (ae > as ? displayLine.substring(as, ae) : displayLine.substring(as)).trim();
                    }
                    int dt = displayLine.lastIndexOf("  |  ");
                    if (dt >= 0)
                        timeStr = displayLine.substring(dt + 5).trim();
                } catch (Exception ignored) {
                }
                txnIds.add(txnId);
                billTexts.add(billText != null ? billText : "");
                hm.addRow(new Object[] { num++, txnId, timeStr, amtStr, "View Bill" });
            }
            JTable ht = new JTable(hm);
            ht.getColumnModel().getColumn(0).setPreferredWidth(35);
            ht.getColumnModel().getColumn(1).setPreferredWidth(150);
            ht.getColumnModel().getColumn(2).setPreferredWidth(150);
            ht.getColumnModel().getColumn(3).setPreferredWidth(100);
            ht.getColumnModel().getColumn(4).setPreferredWidth(80);

            ht.getColumnModel().getColumn(4).setCellEditor(new DefaultCellEditor(new JCheckBox()) {
                public Component getTableCellEditorComponent(JTable tb, Object v, boolean sel, int r, int col) {
                    SwingUtilities.invokeLater(() -> {
                        String txnId = txnIds.get(r);
                        String bill = billTexts.get(r);
                        if (bill == null || bill.isEmpty()) {
                            JOptionPane.showMessageDialog(d, "No detailed bill stored for this transaction.",
                                    "Bill Not Available", JOptionPane.INFORMATION_MESSAGE);
                        } else {
                            openBillDialog(d, "Receipt - TXN# " + txnId, bill);
                        }
                        fireEditingStopped();
                    });
                    return new JLabel();
                }

                public Object getCellEditorValue() {
                    return "View Bill";
                }
            });

            d.add(new JScrollPane(ht), BorderLayout.CENTER);
        }
        JPanel bp = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton cl = new JButton("Close");
        cl.addActionListener(e -> d.dispose());
        bp.add(cl);
        d.add(bp, BorderLayout.SOUTH);
        d.setVisible(true);
    }

    // ================================================================
    // BILLING
    // ================================================================
    JPanel billingPanel() {
        JPanel p = new JPanel(new BorderLayout(0, 0));
        p.setBorder(new EmptyBorder(20, 24, 24, 24));

        JPanel top = new JPanel(new BorderLayout(0, 10));
        JLabel titleLbl = new JLabel("Billing");
        titleLbl.setFont(new Font("SansSerif", Font.BOLD, 24));
        top.add(titleLbl, BorderLayout.WEST);

        JButton addI = new JButton("+ Add Item");
        JButton chk = new JButton("Checkout");
        JButton clr = new JButton("Clear Cart");
        JButton removeTopBtn = new JButton("Remove Item");
        removeTopBtn.setEnabled(false);
        chk.setFont(new Font("SansSerif", Font.BOLD, 13));

        JPanel actionBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actionBtns.add(addI);
        actionBtns.add(chk);
        actionBtns.add(removeTopBtn);
        actionBtns.add(clr);
        top.add(actionBtns, BorderLayout.EAST);

        // Customer selector row
        JPanel custRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        custRow.setBorder(BorderFactory.createTitledBorder("Select Customer"));
        custSearchField = new JTextField(20);
        custSearchField.setToolTipText("Search by name or phone...");
        custCombo = new JComboBox<>();
        custCombo.setPreferredSize(new Dimension(280, 28));

        // "Register New Customer" button — shown only when search yields no results
        JButton regFromBillingBtn = new JButton("+ Register New Customer");
        regFromBillingBtn.setFont(new Font("SansSerif", Font.BOLD, 12));
        regFromBillingBtn.setForeground(new Color(30, 100, 200));
        regFromBillingBtn.setVisible(false);
        regFromBillingBtn.addActionListener(e -> {
            openAddCustomer();
            refreshCustCombo();
        });

        custSearchField.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                String q = custSearchField.getText().trim().toLowerCase();
                custComboList.clear();
                custCombo.removeAllItems();
                for (Customer c : customers) {
                    if (q.isEmpty() || c.getName().toLowerCase().contains(q) || c.getPhone().contains(q)) {
                        custComboList.add(c);
                        custCombo.addItem(
                                String.format("#%d  %s  \u00b7  %s", c.getCustomerId(), c.getName(), c.getPhone()));
                    }
                }
                // Show register button only when a non-empty search yields no results
                regFromBillingBtn.setVisible(!q.isEmpty() && custComboList.isEmpty());
            }
        });

        custRow.add(new JLabel("Search:"));
        custRow.add(custSearchField);
        custRow.add(new JLabel("Select:"));
        custRow.add(custCombo);
        custRow.add(regFromBillingBtn);
        top.add(custRow, BorderLayout.SOUTH);
        p.add(top, BorderLayout.NORTH);

        JTable cartTable = new JTable(cartModel);
        cartTable.getSelectionModel().addListSelectionListener(ev -> {
            if (!ev.getValueIsAdjusting())
                removeTopBtn.setEnabled(cartTable.getSelectedRow() >= 0);
        });

        removeTopBtn.addActionListener(e -> {
            int[] rows = cartTable.getSelectedRows();
            if (rows.length == 0)
                return;
            String msg = rows.length == 1
                    ? "Remove '" + cartModel.getValueAt(rows[0], 1) + "' (x" + cartModel.getValueAt(rows[0], 3)
                            + ") from cart?\nStock will be restored."
                    : "Remove " + rows.length + " selected items from cart?\nStock will be restored.";
            int confirm = JOptionPane.showConfirmDialog(null, msg,
                    "Remove Item", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (confirm != JOptionPane.YES_OPTION)
                return;
            String lastName = "";
            int lastQty = 0;
            for (int i = rows.length - 1; i >= 0; i--) {
                int row = rows[i];
                lastName = (String) cartModel.getValueAt(row, 1);
                lastQty = (int) cartModel.getValueAt(row, 3);
                int pid = (int) cartModel.getValueAt(row, 0);
                try {
                    Product p2 = inv.findById(pid);
                    p2.setQuantity(p2.getQuantity() + lastQty);
                } catch (ProductNotFoundException ignored) {
                }
                cartModel.removeRow(row);
            }
            refreshProds();
            updateTotal();
            removeTopBtn.setEnabled(cartTable.getSelectedRow() >= 0);
            if (cartModel.getRowCount() == 0)
                totalLbl.setText("Total:  Rs. 0.00");
            if (rows.length == 1) {
                status("Removed: " + lastName + " x" + lastQty + " (stock restored)");
            } else {
                status("Removed: " + rows.length + " items (stock restored)");
            }
        });

        JPanel mid = new JPanel(new BorderLayout(0, 0));
        mid.setBorder(new EmptyBorder(12, 0, 0, 0));
        mid.add(new JScrollPane(cartTable), BorderLayout.CENTER);

        JPanel botBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 10));
        botBar.setBorder(new MatteBorder(1, 0, 0, 0, Color.GRAY));
        JLabel taxLbl = new JLabel("(discounts applied)");
        taxLbl.setFont(new Font("SansSerif", Font.ITALIC, 11));
        totalLbl = new JLabel("Total:  Rs. 0.00");
        totalLbl.setFont(new Font("SansSerif", Font.BOLD, 22));
        botBar.add(taxLbl);
        botBar.add(totalLbl);

        JPanel midOuter = new JPanel(new BorderLayout(0, 0));
        midOuter.add(mid, BorderLayout.CENTER);
        midOuter.add(botBar, BorderLayout.SOUTH);
        p.add(midOuter, BorderLayout.CENTER);

        addI.addActionListener(e -> openAddToCart());
        chk.addActionListener(e -> doCheckout());
        clr.addActionListener(e -> {
            if (cartModel.getRowCount() == 0)
                return;
            int confirm = JOptionPane.showConfirmDialog(null, "Clear entire cart? Stock will be restored.",
                    "Clear Cart", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (confirm != JOptionPane.YES_OPTION)
                return;
            for (int i = 0; i < cartModel.getRowCount(); i++) {
                int pid2 = (int) cartModel.getValueAt(i, 0), qty2 = (int) cartModel.getValueAt(i, 3);
                try {
                    Product p2 = inv.findById(pid2);
                    p2.setQuantity(p2.getQuantity() + qty2);
                } catch (ProductNotFoundException ignored) {
                }
            }
            cartModel.setRowCount(0);
            totalLbl.setText("Total:  Rs. 0.00");
            removeTopBtn.setEnabled(false);
            refreshProds();
            status("Cart cleared — stock restored.");
        });
        refreshCustCombo();
        return p;
    }

    void refreshCustCombo() {
        if (custCombo == null)
            return;
        custComboList.clear();
        custCombo.removeAllItems();
        for (Customer c : customers) {
            custComboList.add(c);
            custCombo.addItem(String.format("#%d  %s  ·  %s", c.getCustomerId(), c.getName(), c.getPhone()));
        }
        if (custSearchField != null)
            custSearchField.setText("");
    }

    void openAddToCart() {
        if (customers.isEmpty()) {
            msg("Register a customer first.");
            return;
        }
        JDialog d = new JDialog(this, "Add Item to Cart", true);
        d.setSize(900, 580);
        d.setLocationRelativeTo(this);
        d.setLayout(new BorderLayout(0, 0));

        // Category filter — buttons wired after rebuildList/listSearch are in scope
        JPanel catRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        catRow.setBorder(new MatteBorder(0, 0, 1, 0, Color.GRAY));
        java.util.List<Product> filteredProds = new java.util.ArrayList<>(inv.getAll());
        java.util.List<JButton> cpBtns = new java.util.ArrayList<>();
        String[] catNames = { "All", "Food", "Electronics", "Clothing", "Beauty", "Sports", "Stationery" };
        final String[] activeCat = { "All" };

        // Product list
        DefaultListModel<String> listModel = new DefaultListModel<>();
        JList<String> prodList = new JList<>(listModel);
        prodList.setFont(new Font("Monospaced", Font.PLAIN, 13));
        prodList.setFixedCellHeight(32);
        Runnable rebuildList = () -> {
            listModel.clear();
            for (Product p : filteredProds)
                listModel.addElement(String.format("%-5d  %-26s  %-13s  Rs.%-9.0f  Qty:%-4d  %s",
                        p.getProductId(),
                        p.getProductName().length() > 25 ? p.getProductName().substring(0, 24) + "..."
                                : p.getProductName(),
                        p.getCategory(), p.getPrice(), p.getQuantity(), p.getExtraInfo()));
        };

        JLabel colHeader = new JLabel(String.format("  %-5s  %-26s  %-13s  %-12s  %-8s  %s", "ID", "NAME", "CATEGORY",
                "PRICE", "STOCK", "EXTRA"));
        colHeader.setFont(new Font("Monospaced", Font.BOLD, 12));
        colHeader.setBorder(new CompoundBorder(new MatteBorder(0, 0, 1, 0, Color.GRAY), new EmptyBorder(4, 10, 4, 10)));

        JTextField listSearch = new JTextField();

        // Now wire cat buttons — listSearch and rebuildList are in scope
        for (String cat : catNames) {
            JButton cb = new JButton(cat);
            cb.setFont(new Font("SansSerif", Font.PLAIN, 12));
            cb.addActionListener(e -> {
                activeCat[0] = cat;
                filteredProds.clear();
                for (Product p : inv.getAll())
                    if (cat.equals("All") || p.getCategory().equals(cat))
                        filteredProds.add(p);
                for (JButton b2 : cpBtns)
                    b2.setFont(new Font("SansSerif", activeCat[0].equals(b2.getText()) ? Font.BOLD : Font.PLAIN, 12));
                listSearch.setText("");
                rebuildList.run();
            });
            cpBtns.add(cb);
            catRow.add(cb);
        }
        rebuildList.run();
        listSearch.setToolTipText("Search by name or category...");
        listSearch.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                String q = listSearch.getText().trim().toLowerCase();
                listModel.clear();
                for (Product p : filteredProds)
                    if (q.isEmpty() || p.getProductName().toLowerCase().contains(q)
                            || p.getCategory().toLowerCase().contains(q))
                        listModel.addElement(String.format("%-5d  %-26s  %-13s  Rs.%-9.0f  Qty:%-4d  %s",
                                p.getProductId(),
                                p.getProductName().length() > 25 ? p.getProductName().substring(0, 24) + "..."
                                        : p.getProductName(),
                                p.getCategory(), p.getPrice(), p.getQuantity(), p.getExtraInfo()));
            }
        });

        JPanel listTop = new JPanel(new BorderLayout(0, 2));
        listTop.setBorder(new EmptyBorder(6, 6, 0, 6));
        listTop.add(listSearch, BorderLayout.NORTH);
        listTop.add(colHeader, BorderLayout.SOUTH);

        JPanel listWrapper = new JPanel(new BorderLayout());
        listWrapper.add(listTop, BorderLayout.NORTH);
        listWrapper.add(new JScrollPane(prodList), BorderLayout.CENTER);

        // Info + qty panel
        JPanel infoPanel = new JPanel(new BorderLayout(0, 8));
        infoPanel.setBorder(
                new CompoundBorder(new MatteBorder(0, 1, 0, 0, Color.GRAY), new EmptyBorder(12, 12, 12, 12)));
        infoPanel.setPreferredSize(new Dimension(210, 0));

        JLabel infoName = new JLabel("Select a product");
        infoName.setFont(new Font("SansSerif", Font.BOLD, 14));
        JLabel infoCat = new JLabel(" ");
        infoCat.setFont(new Font("SansSerif", Font.PLAIN, 12));
        JLabel infoPrice = new JLabel(" ");
        infoPrice.setFont(new Font("SansSerif", Font.BOLD, 16));
        JLabel infoDisc = new JLabel(" ");
        infoDisc.setFont(new Font("SansSerif", Font.ITALIC, 11));
        JLabel infoStock = new JLabel(" ");
        infoStock.setFont(new Font("SansSerif", Font.PLAIN, 12));
        JLabel infoExtra = new JLabel(" ");
        infoExtra.setFont(new Font("SansSerif", Font.ITALIC, 11));

        JPanel infoFields = new JPanel(new GridLayout(0, 1, 0, 6));
        infoFields.setBorder(new EmptyBorder(0, 0, 8, 0));
        infoFields.add(infoName);
        infoFields.add(infoCat);
        infoFields.add(infoPrice);
        infoFields.add(infoDisc);
        infoFields.add(infoStock);
        infoFields.add(infoExtra);
        infoPanel.add(infoFields, BorderLayout.NORTH);

        JLabel qtyLbl = new JLabel("Quantity:");
        qtyLbl.setFont(new Font("SansSerif", Font.BOLD, 12));
        JTextField qf = new JTextField("1");
        qf.setHorizontalAlignment(JTextField.CENTER);
        qf.setFont(new Font("SansSerif", Font.BOLD, 18));
        JPanel qtyPanel = new JPanel(new BorderLayout(0, 4));
        qtyPanel.add(qtyLbl, BorderLayout.NORTH);
        qtyPanel.add(qf, BorderLayout.CENTER);
        infoPanel.add(qtyPanel, BorderLayout.CENTER);

        prodList.addListSelectionListener(ev -> {
            if (ev.getValueIsAdjusting())
                return;
            int si = prodList.getSelectedIndex();
            if (si >= 0 && si < filteredProds.size()) {
                Product sel = filteredProds.get(si);
                infoName.setText("<html>" + sel.getProductName() + "</html>");
                infoCat.setText(sel.getCategory());
                infoPrice.setText("Rs. " + String.format("%.2f", sel.applyDiscount(sel.getPrice())));
                infoDisc.setText(sel.getDiscountInfo() + "  (was Rs." + String.format("%.0f", sel.getPrice()) + ")");
                infoStock.setText("In stock: " + sel.getQuantity() + " units");
                infoExtra.setText(sel.getExtraInfo());
            }
        });

        JPanel midPanel = new JPanel(new BorderLayout(0, 0));
        midPanel.add(listWrapper, BorderLayout.CENTER);
        midPanel.add(infoPanel, BorderLayout.EAST);

        JPanel bp = new JPanel(new BorderLayout(0, 0));
        bp.setBorder(new MatteBorder(1, 0, 0, 0, Color.GRAY));

        // Confirmation label shown briefly after each successful add
        JLabel addedLbl = new JLabel(" ", SwingConstants.LEFT);
        addedLbl.setFont(new Font("SansSerif", Font.BOLD, 12));
        addedLbl.setForeground(new Color(0, 140, 60));
        addedLbl.setBorder(new EmptyBorder(0, 10, 0, 0));

        JPanel bpBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        JButton add = new JButton("+ Add to Cart");
        JButton done = new JButton("Done");
        done.addActionListener(e -> d.dispose());
        bpBtns.add(done);
        bpBtns.add(add);

        bp.add(addedLbl, BorderLayout.CENTER);
        bp.add(bpBtns, BorderLayout.EAST);
        add.addActionListener(e -> {
            try {
                int si = prodList.getSelectedIndex();
                if (si < 0)
                    throw new InvalidInputException("Select a product from the list.");
                String selText = prodList.getSelectedValue();
                int pid = Integer.parseInt(selText.trim().substring(0, selText.trim().indexOf(' ')));
                Product pr = inv.findById(pid);
                int qty = Integer.parseInt(qf.getText().trim());
                if (qty <= 0)
                    throw new InvalidInputException("Quantity must be > 0.");
                pr.reduceStock(qty);
                double dp = pr.applyDiscount(pr.getPrice());
                int existingRow = -1;
                for (int r = 0; r < cartModel.getRowCount(); r++) {
                    if ((int) cartModel.getValueAt(r, 0) == pr.getProductId()) {
                        existingRow = r;
                        break;
                    }
                }
                if (existingRow >= 0) {
                    int newQty = (int) cartModel.getValueAt(existingRow, 3) + qty;
                    cartModel.setValueAt(newQty, existingRow, 3);
                    cartModel.setValueAt(String.format("%.2f", dp * newQty), existingRow, 5);
                } else {
                    cartModel.addRow(new Object[] { pr.getProductId(), pr.getProductName(), pr.getCategory(), qty,
                            String.format("%.2f", dp), String.format("%.2f", dp * qty) });
                }
                updateTotal();
                refreshProds();
                String msg = "✔  " + pr.getProductName() + "  ×" + qty + "  added to cart";
                addedLbl.setText(msg);
                status("Added: " + pr.getProductName() + " x" + qty);
                new javax.swing.Timer(2000, ev -> {
                    addedLbl.setText(" ");
                    ((javax.swing.Timer) ev.getSource()).stop();
                }).start();
                qf.setText("1");
                prodList.clearSelection();
                infoName.setText("Select a product");
                infoCat.setText(" ");
                infoPrice.setText(" ");
                infoDisc.setText(" ");
                infoStock.setText(" ");
                infoExtra.setText(" ");
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(d, "Enter a valid integer for quantity.", "Error",
                        JOptionPane.ERROR_MESSAGE);
            } catch (InsufficientStockException ex) {
                JOptionPane.showMessageDialog(d, ex.getMessage(), "Stock Error", JOptionPane.WARNING_MESSAGE);
            } catch (InvalidInputException ex) {
                JOptionPane.showMessageDialog(d, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            } catch (ProductNotFoundException ex) {
                JOptionPane.showMessageDialog(d, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        d.add(catRow, BorderLayout.NORTH);
        d.add(midPanel, BorderLayout.CENTER);
        d.add(bp, BorderLayout.SOUTH);
        d.setVisible(true);
    }

    void updateTotal() {
        double t = 0;
        for (int i = 0; i < cartModel.getRowCount(); i++)
            t += Double.parseDouble(cartModel.getValueAt(i, 5).toString());
        totalLbl.setText("Total:  Rs. " + String.format("%.2f", t));
    }

    // ================================================================
    // CHECKOUT
    // ================================================================
    void doCheckout() {
        if (cartModel.getRowCount() == 0) {
            msg("Cart is empty!");
            return;
        }
        int idx = custCombo.getSelectedIndex();
        if (idx < 0 || idx >= custComboList.size()) {
            msg("Please select a customer.");
            return;
        }
        Customer c = custComboList.get(idx);
        c.clearCart();
        for (int i = 0; i < cartModel.getRowCount(); i++) {
            int pid = (int) cartModel.getValueAt(i, 0), qty = (int) cartModel.getValueAt(i, 3);
            try {
                c.addToCart(inv.findById(pid), qty);
            } catch (ProductNotFoundException ignored) {
            }
        }
        String cs = (String) JOptionPane.showInputDialog(this, "Coupon discount % (enter 0 for none):", "Coupon",
                JOptionPane.QUESTION_MESSAGE, null, null, "0");
        if (cs == null) {
            c.clearCart();
            status("Checkout cancelled.");
            return;
        }
        double coupon = 0;
        try {
            coupon = Double.parseDouble(cs);
        } catch (Exception ignored) {
            JOptionPane.showMessageDialog(this, "Coupon must be a valid number between 0 and 100.", "Invalid Coupon",
                    JOptionPane.WARNING_MESSAGE);
            c.clearCart();
            return;
        }
        if (coupon < 0 || coupon > 100) {
            JOptionPane.showMessageDialog(this, "Coupon discount must be between 0 and 100.", "Invalid Coupon",
                    JOptionPane.WARNING_MESSAGE);
            c.clearCart();
            return;
        }
        double preTotal = 0;
        for (CartItem ci : c.getCart())
            preTotal += ci.getSubtotal();
        if (coupon > 0)
            preTotal -= preTotal * coupon / 100;

        int confirm = JOptionPane.showConfirmDialog(this,
                String.format("Confirm checkout for %s?\nTotal payable: Rs. %.2f", c.getName(), preTotal),
                "Confirm Checkout", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) {
            c.clearCart();
            status("Checkout cancelled.");
            return;
        }

        String txnId = Customer.generateTxnId();
        java.util.List<CartItem> cartSnapshot = new java.util.ArrayList<>(c.getCart());
        double finalTotal = preTotal;

        try {
            DatabaseManager.insertTransaction(txnId, c.getCustomerId(), finalTotal, coupon, cashierId, cartSnapshot);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Transaction could not be saved:\n" + ex.getMessage(), "Database Error",
                    JOptionPane.ERROR_MESSAGE);
            for (CartItem ci : cartSnapshot)
                try {
                    inv.findById(ci.getProduct().getProductId())
                            .setQuantity(ci.getProduct().getQuantity() + ci.getQuantity());
                } catch (ProductNotFoundException ignored) {
                }
            refreshProds();
            return;
        }

        c.checkout(coupon, txnId);
        c.setLastTxnId(txnId);

        String now = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm").format(new java.util.Date());
        globalTxns.addFirst(new String[] { txnId, c.getName(), "Rs. " + String.format("%.2f", finalTotal), now });
        if (globalTxns.size() > 30)
            globalTxns.removeLast();

        cartModel.setRowCount(0);
        totalLbl.setText("Total:  Rs. 0.00");
        refreshCust();
        status("Checkout complete for " + c.getName() + "  Rs." + String.format("%.2f", finalTotal));
        showReceipt(c, finalTotal, coupon, txnId, cartSnapshot);
    }

    void showReceipt(Customer c, double total, double coupon, String txnId, java.util.List<CartItem> snapshot) {
        StringBuilder sb = new StringBuilder();
        sb.append("==============================\n");
        sb.append("         FRESH MITRA\n");
        sb.append("==============================\n\n");
        sb.append("  TXN ID   : ").append(txnId).append("\n");
        sb.append("  Customer : ").append(c.getName()).append("\n");
        sb.append("  Cust ID  : #").append(c.getCustomerId()).append("\n");
        sb.append("  Phone    : ").append(c.getPhone()).append("\n");
        if (cashierId > 0) {
            String cName = DatabaseManager.getCashierName(cashierId);
            sb.append("  Cashier  : ").append(cName != null ? cName : "Cashier").append("\n");
            sb.append("  Cash ID  : ").append(cashierId).append("\n");
        } else {
            sb.append("  Cashier  : Manager\n");
            sb.append("  Cash ID  : admin\n");
        }
        sb.append("\n------------------------------\n");
        if (coupon > 0)
            sb.append("  Coupon Discount : ").append((int) coupon).append("%\n");
        sb.append(String.format("  %-24s %5s  %10s%n", "ITEM", "QTY", "AMOUNT"));
        sb.append("  ------------------------------\n");
        for (CartItem ci : snapshot) {
            String name = ci.getProduct().getProductName();
            int qty = ci.getQuantity();
            double sub = ci.getSubtotal();
            sb.append(String.format("  %-24s x%-4d  Rs.%7.2f%n",
                    name.length() > 23 ? name.substring(0, 22) + "..." : name, qty, sub));
        }
        sb.append("  ------------------------------\n");
        sb.append("\n  TOTAL PAYABLE\n");
        sb.append("  Rs. ").append(String.format("%.2f", total)).append("\n");
        sb.append("\n==============================\n");
        sb.append("    Thank you for shopping!\n");
        openBillDialog(this, "Receipt - TXN# " + txnId, sb.toString());
    }

    // ================================================================
    // REPORTS
    // ================================================================
    JPanel reportsPanel() {
        JPanel p = new JPanel(new BorderLayout(0, 0));
        p.setBorder(new EmptyBorder(20, 24, 24, 24));

        JLabel titleLbl = new JLabel("Reports");
        titleLbl.setFont(new Font("SansSerif", Font.BOLD, 24));
        titleLbl.setBorder(new EmptyBorder(0, 0, 16, 0));
        p.add(titleLbl, BorderLayout.NORTH);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        btns.setBorder(new EmptyBorder(0, 0, 12, 0));
        JTextArea area = new JTextArea();
        area.setFont(new Font("Monospaced", Font.PLAIN, 13));
        area.setEditable(false);
        area.setBorder(new EmptyBorder(12, 14, 12, 14));

        JButton bLow = new JButton("Low Stock Alert");
        JButton bDisc = new JButton("Discount Summary");
        JButton bAll = new JButton("Full Inventory");

        bLow.addActionListener(e -> {
            StringBuilder sb = new StringBuilder("LOW STOCK ALERT — Products with Qty < 5\n");
            sb.append("-".repeat(55)).append("\n\n");
            boolean any = false;
            for (Product pr : inv.getAll())
                if (pr.getQuantity() < 5) {
                    sb.append(String.format("  [%d]  %-24s  Qty: %-4d  (%s)%n", pr.getProductId(), pr.getProductName(),
                            pr.getQuantity(), pr.getCategory()));
                    any = true;
                }
            if (!any)
                sb.append("  All products are adequately stocked.");
            area.setText(sb.toString());
        });
        bDisc.addActionListener(e -> {
            StringBuilder sb = new StringBuilder("DISCOUNT SUMMARY\n");
            sb.append("-".repeat(40)).append("\n");
            sb.append("  Discounts apply automatically at checkout based on category.\n");
            sb.append("-".repeat(40)).append("\n\n");
            sb.append(String.format("  %-14s  %-10s%n", "CATEGORY", "DISCOUNT"));
            Object[][] cats = { { "Food", "5% off" }, { "Electronics", "10% off" }, { "Clothing", "15% off" },
                    { "Beauty", "12% off" }, { "Sports", "8% off" }, { "Stationery", "6% off" } };
            for (Object[] row : cats)
                sb.append(String.format("  %-14s  %-10s%n", row[0], row[1]));
            area.setText(sb.toString());
        });
        bAll.addActionListener(e -> {
            StringBuilder sb = new StringBuilder("FULL INVENTORY\n");
            sb.append("-".repeat(70)).append("\n\n");
            sb.append(String.format("  %-6s  %-24s  %-12s  %-12s  %-6s%n", "ID", "Name", "Category", "Price", "Qty"));
            sb.append("  ").append("-".repeat(68)).append("\n");
            for (Product pr : inv.getAll())
                sb.append(String.format("  %-6d  %-24s  %-12s  Rs.%-9.2f  %-6d  %s%n",
                        pr.getProductId(), pr.getProductName(), pr.getCategory(), pr.getPrice(), pr.getQuantity(),
                        pr.getExtraInfo()));
            area.setText(sb.toString());
        });

        btns.add(bLow);
        btns.add(bDisc);
        btns.add(bAll);

        JPanel outer = new JPanel(new BorderLayout());
        outer.add(btns, BorderLayout.NORTH);
        outer.add(new JScrollPane(area), BorderLayout.CENTER);
        p.add(outer, BorderLayout.CENTER);
        return p;
    }

    // ================================================================
    // DATA LOADING
    // ================================================================
    void loadData() {
        DatabaseManager.loadProducts(inv);
        DatabaseManager.loadCustomers(customers);
        int maxPid = inv.getAll().stream().mapToInt(Product::getProductId).max().orElse(800);
        DatabaseManager.syncProductIdCounter(inv, maxPid);
        int maxCid = customers.stream().mapToInt(Customer::getCustomerId).max().orElse(1000);
        DatabaseManager.syncCustomerIdCounter(maxCid);
        for (String[] row : DatabaseManager.loadRecentTransactions(30))
            globalTxns.addLast(row);
    }

    // ================================================================
    // ANALYTICS PAGE
    // ================================================================
    JPanel analyticsPanel() {
        JPanel p = new JPanel(new BorderLayout(0, 0));
        p.setBorder(new EmptyBorder(20, 24, 24, 24));

        JLabel titleLbl = new JLabel(isManager() ? "Analytics" : "My Analytics");
        titleLbl.setFont(new Font("SansSerif", Font.BOLD, 24));
        titleLbl.setBorder(new EmptyBorder(0, 0, 16, 0));
        p.add(titleLbl, BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("SansSerif", Font.PLAIN, 13));

        tabs.addTab("Sales Summary", buildSalesSummaryTab());
        tabs.addTab(isManager() ? "Transactions" : "My Transactions", buildTransactionsTab());
        if (isManager()) {
            tabs.insertTab("Top Customers", null, buildTopCustomersTab(), null, 1);
            tabs.insertTab("Best-Selling Products", null, buildBestSellersTab(), null, 2);
            tabs.insertTab("Category Revenue", null, buildCategoryRevenueTab(), null, 3);
        }

        // Refresh data when a tab is selected
        tabs.addChangeListener(e -> {
            int idx = tabs.getSelectedIndex();
            if (isManager()) {
                switch (idx) {
                    case 0 -> refreshSalesSummary("daily");
                    case 1 -> refreshTopCustomers();
                    case 2 -> refreshBestSellers();
                    case 3 -> refreshCategoryRevenue();
                    case 4 -> refreshTransactions();
                }
            } else {
                switch (idx) {
                    case 0 -> refreshSalesSummary("daily");
                    case 1 -> refreshTransactions();
                }
            }
        });

        p.add(tabs, BorderLayout.CENTER);
        return p;
    }

    // ── Sales Summary Tab ─────────────────────────────────────────
    DefaultTableModel salesModel;
    JLabel salesTotalLbl, salesTxnLbl, salesAvgLbl;

    JPanel buildSalesSummaryTab() {
        JPanel p = new JPanel(new BorderLayout(0, 10));
        p.setBorder(new EmptyBorder(14, 14, 14, 14));

        JPanel top = new JPanel(new BorderLayout(0, 10));

        // Row 1: period buttons + Custom toggle + All Time
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        JButton bDay = new JButton("Today");
        JButton bWeek = new JButton("This Week");
        JButton bMonth = new JButton("This Month");
        JButton bCustom = new JButton("Custom");
        JButton bAllTime = new JButton("All Time");
        bDay.addActionListener(e -> refreshSalesSummary("daily"));
        bWeek.addActionListener(e -> refreshSalesSummary("weekly"));
        bMonth.addActionListener(e -> refreshSalesSummary("monthly"));
        bAllTime.addActionListener(e -> refreshSalesSummaryAll());
        btnRow.add(bDay);
        btnRow.add(bWeek);
        btnRow.add(bMonth);
        btnRow.add(bCustom);
        btnRow.add(bAllTime);

        // Row 2: custom date range with calendar pickers — hidden by default
        JPanel customRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));

        // Calendar-style spinners using SpinnerDateModel
        java.util.Date defFromDate = java.util.Date.from(
                java.time.LocalDate.now().minusDays(7).atStartOfDay(java.time.ZoneId.systemDefault()).toInstant());
        java.util.Date defToDate = java.util.Date.from(
                java.time.LocalDate.now().atStartOfDay(java.time.ZoneId.systemDefault()).toInstant());
        java.util.Date maxAllowedDate = java.util.Date.from(
                java.time.LocalDate.now().plusDays(1).atStartOfDay(java.time.ZoneId.systemDefault()).minusNanos(1)
                        .toInstant());

        JSpinner fromSpinner = new JSpinner(
                new SpinnerDateModel(defFromDate, null, maxAllowedDate, java.util.Calendar.DAY_OF_MONTH));
        JSpinner toSpinner = new JSpinner(
                new SpinnerDateModel(defToDate, null, maxAllowedDate, java.util.Calendar.DAY_OF_MONTH));

        JSpinner.DateEditor fromEditor = new JSpinner.DateEditor(fromSpinner, "dd/MM/yyyy");
        JSpinner.DateEditor toEditor = new JSpinner.DateEditor(toSpinner, "dd/MM/yyyy");
        fromSpinner.setEditor(fromEditor);
        toSpinner.setEditor(toEditor);
        fromSpinner.setPreferredSize(new Dimension(110, 26));
        toSpinner.setPreferredSize(new Dimension(110, 26));

        JButton bApply = new JButton("Apply");
        bApply.addActionListener(e -> {
            java.util.Date fd = (java.util.Date) fromSpinner.getValue();
            java.util.Date td = (java.util.Date) toSpinner.getValue();
            if (fd.after(td)) {
                JOptionPane.showMessageDialog(this, "'From' date cannot be after 'To' date.", "Invalid Range",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (fd.after(maxAllowedDate) || td.after(maxAllowedDate)) {
                JOptionPane.showMessageDialog(this, "Future dates are not allowed.", "Invalid Range",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd");
            refreshSalesSummaryCustom(sdf.format(fd), sdf.format(td));
        });

        customRow.add(new JLabel("From:"));
        customRow.add(fromSpinner);
        customRow.add(new JLabel("To:"));
        customRow.add(toSpinner);
        customRow.add(bApply);
        customRow.setVisible(false);

        // Toggle custom row visibility when Custom button is clicked
        bCustom.addActionListener(e -> {
            boolean nowVisible = !customRow.isVisible();
            customRow.setVisible(nowVisible);
            bCustom.setFont(new Font("SansSerif", nowVisible ? Font.BOLD : Font.PLAIN, 12));
            top.revalidate();
            top.repaint();
        });

        JPanel controlRows = new JPanel(new BorderLayout(0, 4));
        controlRows.add(btnRow, BorderLayout.NORTH);
        controlRows.add(customRow, BorderLayout.SOUTH);
        top.add(controlRows, BorderLayout.NORTH);

        // Summary stat cards
        JPanel statsRow = new JPanel(new GridLayout(1, 3, 12, 0));
        statsRow.setPreferredSize(new Dimension(0, 80));
        salesTxnLbl = new JLabel("0", SwingConstants.CENTER);
        salesTotalLbl = new JLabel("Rs. 0.00", SwingConstants.CENTER);
        salesAvgLbl = new JLabel("Rs. 0.00", SwingConstants.CENTER);
        salesTxnLbl.setFont(new Font("SansSerif", Font.BOLD, 24));
        salesTotalLbl.setFont(new Font("SansSerif", Font.BOLD, 24));
        salesAvgLbl.setFont(new Font("SansSerif", Font.BOLD, 24));
        statsRow.add(makeAnalyticsCard(salesTxnLbl, "Transactions"));
        statsRow.add(makeAnalyticsCard(salesTotalLbl, "Total Revenue"));
        statsRow.add(makeAnalyticsCard(salesAvgLbl, "Average Bill"));
        top.add(statsRow, BorderLayout.SOUTH);
        p.add(top, BorderLayout.NORTH);

        // Table
        salesModel = new DefaultTableModel(
                new String[] { "Date", "Transactions", "Total Revenue (Rs.)", "Avg Bill (Rs.)" }, 0) {
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        JTable t = new JTable(salesModel);
        t.setFont(new Font("SansSerif", Font.PLAIN, 13));
        t.setRowHeight(26);
        t.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));
        t.getColumnModel().getColumn(0).setPreferredWidth(160);
        t.getColumnModel().getColumn(1).setPreferredWidth(120);
        t.getColumnModel().getColumn(2).setPreferredWidth(180);
        t.getColumnModel().getColumn(3).setPreferredWidth(160);
        p.add(new JScrollPane(t), BorderLayout.CENTER);

        refreshSalesSummary("daily");
        return p;
    }

    void refreshSalesSummary(String period) {
        if (salesModel == null)
            return;
        java.util.List<String[]> rows = isManager()
                ? DatabaseManager.getSalesSummary(period)
                : DatabaseManager.getSalesSummary(period, cashierId);
        salesModel.setRowCount(0);
        double grandTotal = 0;
        int grandTxns = 0;
        for (String[] r : rows) {
            salesModel.addRow(r);
            try {
                grandTxns += Integer.parseInt(r[1]);
                grandTotal += Double.parseDouble(r[2]);
            } catch (Exception ignored) {
            }
        }
        updateSalesCards(grandTxns, grandTotal);
    }

    void refreshSalesSummaryCustom(String from, String to) {
        if (salesModel == null)
            return;
        java.util.List<String[]> rows = isManager()
                ? DatabaseManager.getSalesSummaryCustom(from, to)
                : DatabaseManager.getSalesSummaryCustom(from, to, cashierId);
        salesModel.setRowCount(0);
        double grandTotal = 0;
        int grandTxns = 0;
        for (String[] r : rows) {
            salesModel.addRow(r);
            try {
                grandTxns += Integer.parseInt(r[1]);
                grandTotal += Double.parseDouble(r[2]);
            } catch (Exception ignored) {
            }
        }
        updateSalesCards(grandTxns, grandTotal);
    }

    void refreshSalesSummaryAll() {
        if (salesModel == null)
            return;
        java.util.List<String[]> rows = isManager()
                ? DatabaseManager.getSalesSummaryAll()
                : DatabaseManager.getSalesSummaryAll(cashierId);
        salesModel.setRowCount(0);
        double grandTotal = 0;
        int grandTxns = 0;
        for (String[] r : rows) {
            salesModel.addRow(r);
            try {
                grandTxns += Integer.parseInt(r[1]);
                grandTotal += Double.parseDouble(r[2]);
            } catch (Exception ignored) {
            }
        }
        updateSalesCards(grandTxns, grandTotal);
    }

    void updateSalesCards(int txns, double total) {
        if (salesTxnLbl != null)
            salesTxnLbl.setText(String.valueOf(txns));
        if (salesTotalLbl != null)
            salesTotalLbl.setText("Rs. " + String.format("%.2f", total));
        if (salesAvgLbl != null)
            salesAvgLbl.setText("Rs. " + String.format("%.2f", txns > 0 ? total / txns : 0));
    }

    // ── Best Sellers Tab ──────────────────────────────────────────
    DefaultTableModel bestModel;
    String bestSortBy = "units_sold"; // default

    JPanel buildBestSellersTab() {
        JPanel p = new JPanel(new BorderLayout(0, 10));
        p.setBorder(new EmptyBorder(14, 14, 14, 14));

        JPanel ctrlRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        ctrlRow.add(new JLabel("Sort by:"));
        JButton bUnits = new JButton("Units Sold");
        JButton bRevenue = new JButton("Total Revenue");
        bUnits.setFont(new Font("SansSerif", Font.BOLD, 12)); // active by default

        ctrlRow.add(bUnits);
        ctrlRow.add(bRevenue);
        ctrlRow.add(Box.createHorizontalStrut(16));
        ctrlRow.add(new JLabel("Show top:"));
        JSpinner spinner = new JSpinner(new SpinnerNumberModel(10, 1, 50, 1));
        spinner.setPreferredSize(new Dimension(60, 26));
        JButton bRefresh = new JButton("Refresh");
        ctrlRow.add(spinner);
        ctrlRow.add(bRefresh);
        p.add(ctrlRow, BorderLayout.NORTH);

        bestModel = new DefaultTableModel(
                new String[] { "Rank", "Product Name", "Category", "Units Sold", "Total Revenue (Rs.)" }, 0) {
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        JTable t = new JTable(bestModel);
        t.setFont(new Font("SansSerif", Font.PLAIN, 13));
        t.setRowHeight(26);
        t.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));
        t.getColumnModel().getColumn(0).setPreferredWidth(50);
        t.getColumnModel().getColumn(1).setPreferredWidth(260);
        t.getColumnModel().getColumn(2).setPreferredWidth(120);
        t.getColumnModel().getColumn(3).setPreferredWidth(100);
        t.getColumnModel().getColumn(4).setPreferredWidth(160);

        // Highlight #1 row
        t.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(JTable tb, Object v, boolean sel, boolean foc, int r,
                    int c) {
                super.getTableCellRendererComponent(tb, v, sel, foc, r, c);
                if (r == 0) {
                    setBackground(new Color(255, 240, 180));
                    setFont(getFont().deriveFont(Font.BOLD));
                } else {
                    setBackground(sel ? t.getSelectionBackground() : Color.WHITE);
                    setFont(getFont().deriveFont(Font.PLAIN));
                }
                setBorder(new EmptyBorder(0, 6, 0, 6));
                return this;
            }
        });

        p.add(new JScrollPane(t), BorderLayout.CENTER);

        // Wire sort buttons
        bUnits.addActionListener(e -> {
            bestSortBy = "units_sold";
            bUnits.setFont(new Font("SansSerif", Font.BOLD, 12));
            bRevenue.setFont(new Font("SansSerif", Font.PLAIN, 12));
            refreshBestSellers((int) spinner.getValue());
        });
        bRevenue.addActionListener(e -> {
            bestSortBy = "total_rev";
            bRevenue.setFont(new Font("SansSerif", Font.BOLD, 12));
            bUnits.setFont(new Font("SansSerif", Font.PLAIN, 12));
            refreshBestSellers((int) spinner.getValue());
        });
        bRefresh.addActionListener(e -> refreshBestSellers((int) spinner.getValue()));

        refreshBestSellers(10);
        return p;
    }

    void refreshBestSellers() {
        refreshBestSellers(10);
    }

    void refreshBestSellers(int limit) {
        if (bestModel == null)
            return;
        java.util.List<String[]> rows = DatabaseManager.getBestSellingProducts(limit, bestSortBy);
        bestModel.setRowCount(0);
        int rank = 1;
        for (String[] r : rows)
            bestModel.addRow(new Object[] { rank++, r[0], r[1], r[2], r[3] });
    }

    // ── Category Revenue Tab ──────────────────────────────────────
    DefaultTableModel catRevModel;
    java.util.List<String[]> catRevData = new java.util.ArrayList<>();

    JPanel buildCategoryRevenueTab() {
        JPanel p = new JPanel(new BorderLayout(0, 10));
        p.setBorder(new EmptyBorder(14, 14, 14, 14));

        catRevModel = new DefaultTableModel(
                new String[] { "Category", "Units Sold", "Total Revenue (Rs.)", "% Share" }, 0) {
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        JTable t = new JTable(catRevModel);
        t.setFont(new Font("SansSerif", Font.PLAIN, 13));
        t.setRowHeight(26);
        t.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));
        t.getColumnModel().getColumn(0).setPreferredWidth(140);
        t.getColumnModel().getColumn(1).setPreferredWidth(100);
        t.getColumnModel().getColumn(2).setPreferredWidth(180);
        t.getColumnModel().getColumn(3).setPreferredWidth(100);

        // Bar chart panel
        JPanel chartPanel = new JPanel() {
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (catRevData.isEmpty())
                    return;
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int pw = getWidth() - 80, ph = getHeight() - 40;
                int barH = Math.min(34, (ph - 10 * (catRevData.size() - 1)) / catRevData.size());
                double maxRev = catRevData.stream().mapToDouble(r -> {
                    try {
                        return Double.parseDouble(r[2]);
                    } catch (Exception e) {
                        return 0;
                    }
                }).max().orElse(1);
                Color[] palette = { new Color(0, 140, 100), new Color(30, 100, 200), new Color(120, 60, 200),
                        new Color(200, 60, 120), new Color(200, 120, 0), new Color(40, 160, 80),
                        new Color(180, 120, 0) };
                int y = 20;
                for (int i = 0; i < catRevData.size(); i++) {
                    String[] row = catRevData.get(i);
                    double rev = 0;
                    try {
                        rev = Double.parseDouble(row[2]);
                    } catch (Exception ignored) {
                    }
                    int barW = (int) (rev / maxRev * (pw - 140));
                    Color c = palette[i % palette.length];
                    g2.setColor(c);
                    g2.fillRect(140, y, barW, barH);
                    g2.setColor(Color.BLACK);
                    g2.setFont(new Font("SansSerif", Font.PLAIN, 12));
                    g2.drawString(row[0], 4, y + barH / 2 + 5);
                    g2.setFont(new Font("SansSerif", Font.BOLD, 12));
                    g2.drawString("Rs." + String.format("%.0f", rev), 144 + barW, y + barH / 2 + 5);
                    y += barH + 10;
                }
            }
        };
        chartPanel.setPreferredSize(new Dimension(0, 280));
        chartPanel.setBorder(BorderFactory.createTitledBorder("Revenue by Category"));

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                new JScrollPane(t), chartPanel);
        split.setDividerLocation(220);
        split.setResizeWeight(0.45);
        p.add(split, BorderLayout.CENTER);

        refreshCategoryRevenue();
        return p;
    }

    void refreshCategoryRevenue() {
        if (catRevModel == null)
            return;
        java.util.List<String[]> rows = DatabaseManager.getCategoryRevenue();
        catRevModel.setRowCount(0);
        catRevData.clear();
        double total = rows.stream().mapToDouble(r -> {
            try {
                return Double.parseDouble(r[2]);
            } catch (Exception e) {
                return 0;
            }
        }).sum();
        for (String[] r : rows) {
            double rev = 0;
            try {
                rev = Double.parseDouble(r[2]);
            } catch (Exception ignored) {
            }
            String pct = total > 0 ? String.format("%.1f%%", rev / total * 100) : "0%";
            catRevModel.addRow(new Object[] { r[0], r[1], r[2], pct });
            catRevData.add(new String[] { r[0], r[1], r[2], pct });
        }
    }

    // ── Top Customers Tab ─────────────────────────────────────────
    DefaultTableModel topCustModel;
    JLabel topCustBadge;
    String topCustSortBy = "txn_count"; // default sort

    JPanel buildTopCustomersTab() {
        JPanel p = new JPanel(new BorderLayout(0, 10));
        p.setBorder(new EmptyBorder(14, 14, 14, 14));

        // Badge
        topCustBadge = new JLabel("  Top Customer: —  ");
        topCustBadge.setFont(new Font("SansSerif", Font.BOLD, 14));
        topCustBadge.setBorder(new CompoundBorder(
                BorderFactory.createLineBorder(Color.GRAY),
                new EmptyBorder(6, 12, 6, 12)));

        // Controls row
        JPanel ctrlRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        ctrlRow.add(topCustBadge);
        ctrlRow.add(Box.createHorizontalStrut(16));

        ctrlRow.add(new JLabel("Sort by:"));
        JButton bRecurring = new JButton("Most Recurring");
        JButton bSpent = new JButton("Highest Spent");
        bRecurring.setFont(new Font("SansSerif", Font.BOLD, 12)); // active by default

        bRecurring.addActionListener(e -> {
            topCustSortBy = "txn_count";
            bRecurring.setFont(new Font("SansSerif", Font.BOLD, 12));
            bSpent.setFont(new Font("SansSerif", Font.PLAIN, 12));
            refreshTopCustomers((int) ((JSpinner) ((JPanel) p.getComponent(0)).getComponent(0)).getValue());
        });
        bSpent.addActionListener(e -> {
            topCustSortBy = "total_spent";
            bSpent.setFont(new Font("SansSerif", Font.BOLD, 12));
            bRecurring.setFont(new Font("SansSerif", Font.PLAIN, 12));
            refreshTopCustomers((int) ((JSpinner) ((JPanel) p.getComponent(0)).getComponent(0)).getValue());
        });

        ctrlRow.add(bRecurring);
        ctrlRow.add(bSpent);
        ctrlRow.add(Box.createHorizontalStrut(16));
        ctrlRow.add(new JLabel("Show top:"));
        JSpinner spinner = new JSpinner(new SpinnerNumberModel(10, 1, 50, 1));
        spinner.setPreferredSize(new Dimension(60, 26));
        JButton bRefresh = new JButton("Refresh");
        bRefresh.addActionListener(e -> refreshTopCustomers((int) spinner.getValue()));
        ctrlRow.add(spinner);
        ctrlRow.add(bRefresh);

        // Wrap spinner in a named panel so sort buttons can reach it
        JPanel north = new JPanel(new BorderLayout());
        north.add(ctrlRow, BorderLayout.CENTER);
        // store spinner reference so sort buttons can read it
        north.add(spinner, BorderLayout.EAST); // invisible placeholder — we re-add below
        north.remove(spinner); // remove so layout isn't affected
        // simpler: just capture spinner in lambda directly (already done above)

        p.add(ctrlRow, BorderLayout.NORTH);

        topCustModel = new DefaultTableModel(
                new String[] { "Rank", "Customer Name", "Phone", "Transactions", "Total Spent (Rs.)" }, 0) {
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        JTable t = new JTable(topCustModel);
        t.setFont(new Font("SansSerif", Font.PLAIN, 13));
        t.setRowHeight(26);
        t.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));
        t.getColumnModel().getColumn(0).setPreferredWidth(50);
        t.getColumnModel().getColumn(1).setPreferredWidth(200);
        t.getColumnModel().getColumn(2).setPreferredWidth(120);
        t.getColumnModel().getColumn(3).setPreferredWidth(120);
        t.getColumnModel().getColumn(4).setPreferredWidth(160);

        // Highlight #1 row in gold
        t.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(JTable tb, Object v, boolean sel, boolean foc, int r,
                    int c) {
                super.getTableCellRendererComponent(tb, v, sel, foc, r, c);
                if (r == 0) {
                    setBackground(new Color(255, 240, 180));
                    setFont(getFont().deriveFont(Font.BOLD));
                } else {
                    setBackground(sel ? t.getSelectionBackground() : Color.WHITE);
                    setFont(getFont().deriveFont(Font.PLAIN));
                }
                setBorder(new EmptyBorder(0, 6, 0, 6));
                return this;
            }
        });

        // Fix sort button lambdas — re-wire them cleanly now that spinner is in scope
        bRecurring.addActionListener(null); // clear old
        bSpent.addActionListener(null);
        // remove all listeners and re-add clean ones
        for (java.awt.event.ActionListener al : bRecurring.getActionListeners())
            bRecurring.removeActionListener(al);
        for (java.awt.event.ActionListener al : bSpent.getActionListeners())
            bSpent.removeActionListener(al);
        bRecurring.addActionListener(e -> {
            topCustSortBy = "txn_count";
            bRecurring.setFont(new Font("SansSerif", Font.BOLD, 12));
            bSpent.setFont(new Font("SansSerif", Font.PLAIN, 12));
            refreshTopCustomers((int) spinner.getValue());
        });
        bSpent.addActionListener(e -> {
            topCustSortBy = "total_spent";
            bSpent.setFont(new Font("SansSerif", Font.BOLD, 12));
            bRecurring.setFont(new Font("SansSerif", Font.PLAIN, 12));
            refreshTopCustomers((int) spinner.getValue());
        });
        bRefresh.addActionListener(null);
        for (java.awt.event.ActionListener al : bRefresh.getActionListeners())
            bRefresh.removeActionListener(al);
        bRefresh.addActionListener(e -> refreshTopCustomers((int) spinner.getValue()));

        p.add(new JScrollPane(t), BorderLayout.CENTER);
        refreshTopCustomers(10);
        return p;
    }

    void refreshTopCustomers() {
        refreshTopCustomers(10);
    }

    void refreshTopCustomers(int limit) {
        if (topCustModel == null)
            return;
        java.util.List<String[]> rows = DatabaseManager.getTopCustomers(limit, topCustSortBy);
        topCustModel.setRowCount(0);
        int rank = 1;
        for (String[] r : rows)
            topCustModel.addRow(new Object[] { rank++, r[0], r[1], r[2], r[3] });
        if (!rows.isEmpty() && topCustBadge != null) {
            String[] top = rows.get(0);
            String label = topCustSortBy.equals("txn_count")
                    ? "  Top Customer: " + top[0] + "  —  " + top[2] + " transactions  |  Rs. " + top[3] + "  "
                    : "  Top Spender: " + top[0] + "  —  Rs. " + top[3] + "  |  " + top[2] + " transactions  ";
            topCustBadge.setText(label);
        } else if (topCustBadge != null) {
            topCustBadge.setText("  No transaction data yet.  ");
        }
    }

    // ── Transactions Tab ──────────────────────────────────────────
    DefaultTableModel txnListModel;
    JTextField txnCustSearch;
    String txnPeriodFilter = "";

    JPanel buildTransactionsTab() {
        JPanel p = new JPanel(new BorderLayout(0, 8));
        p.setBorder(new EmptyBorder(14, 14, 14, 14));

        // ── Row 1: period buttons ──────────────────────────────────
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        JButton bAll = new JButton("All Time");
        JButton bToday = new JButton("Today");
        JButton bWeek = new JButton("This Week");
        JButton bMonth = new JButton("This Month");
        JButton bCust = new JButton("Custom");
        bAll.setFont(new Font("SansSerif", Font.BOLD, 12));

        // ── Row 2: custom date pickers (hidden by default) ─────────
        JPanel customRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        java.util.Date defFrom = java.util.Date.from(
                java.time.LocalDate.now().minusDays(7).atStartOfDay(java.time.ZoneId.systemDefault()).toInstant());
        java.util.Date defTo = java.util.Date.from(
                java.time.LocalDate.now().atStartOfDay(java.time.ZoneId.systemDefault()).toInstant());
        java.util.Date maxAllowedDate = java.util.Date.from(
                java.time.LocalDate.now().plusDays(1).atStartOfDay(java.time.ZoneId.systemDefault()).minusNanos(1)
                        .toInstant());
        JSpinner fromSp = new JSpinner(new SpinnerDateModel(defFrom, null, maxAllowedDate, java.util.Calendar.DAY_OF_MONTH));
        JSpinner toSp = new JSpinner(new SpinnerDateModel(defTo, null, maxAllowedDate, java.util.Calendar.DAY_OF_MONTH));
        fromSp.setEditor(new JSpinner.DateEditor(fromSp, "dd/MM/yyyy"));
        fromSp.setPreferredSize(new Dimension(110, 26));
        toSp.setEditor(new JSpinner.DateEditor(toSp, "dd/MM/yyyy"));
        toSp.setPreferredSize(new Dimension(110, 26));
        JButton bApply = new JButton("Apply");
        bApply.addActionListener(e -> {
            java.util.Date fd = (java.util.Date) fromSp.getValue(), td = (java.util.Date) toSp.getValue();
            if (fd.after(td)) {
                JOptionPane.showMessageDialog(this, "'From' cannot be after 'To'.", "Invalid Range",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (fd.after(maxAllowedDate) || td.after(maxAllowedDate)) {
                JOptionPane.showMessageDialog(this, "Future dates are not allowed.", "Invalid Range",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd");
            txnPeriodFilter = "custom:" + sdf.format(fd) + ":" + sdf.format(td);
            refreshTransactions();
        });
        customRow.add(new JLabel("From:"));
        customRow.add(fromSp);
        customRow.add(new JLabel("To:"));
        customRow.add(toSp);
        customRow.add(bApply);
        customRow.setVisible(false);

        // Wire period buttons — all need access to customRow
        bAll.addActionListener(e -> {
            txnPeriodFilter = "";
            customRow.setVisible(false);
            boldOnly(bAll, bToday, bWeek, bMonth, bCust);
            refreshTransactions();
        });
        bToday.addActionListener(e -> {
            txnPeriodFilter = "today";
            customRow.setVisible(false);
            boldOnly(bToday, bAll, bWeek, bMonth, bCust);
            refreshTransactions();
        });
        bWeek.addActionListener(e -> {
            txnPeriodFilter = "week";
            customRow.setVisible(false);
            boldOnly(bWeek, bAll, bToday, bMonth, bCust);
            refreshTransactions();
        });
        bMonth.addActionListener(e -> {
            txnPeriodFilter = "month";
            customRow.setVisible(false);
            boldOnly(bMonth, bAll, bToday, bWeek, bCust);
            refreshTransactions();
        });
        bCust.addActionListener(e -> {
            boolean v = !customRow.isVisible();
            customRow.setVisible(v);
            boldOnly(bCust, bAll, bToday, bWeek, bMonth);
            p.revalidate();
            p.repaint();
        });
        btnRow.add(bAll);
        btnRow.add(bToday);
        btnRow.add(bWeek);
        btnRow.add(bMonth);
        btnRow.add(bCust);

        // ── Row 3: customer search + clear + view bill ─────────────
        JPanel searchRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        txnCustSearch = new JTextField(20);
        txnCustSearch.setToolTipText("Filter by customer name...");
        txnCustSearch.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                refreshTransactions();
            }
        });
        JButton bClear = new JButton("Clear");
        JButton bViewBill = new JButton("View Bill");
        bClear.addActionListener(e -> {
            txnCustSearch.setText("");
            txnPeriodFilter = "";
            customRow.setVisible(false);
            boldOnly(bAll, bToday, bWeek, bMonth, bCust);
            refreshTransactions();
        });
        searchRow.add(new JLabel("Customer:"));
        searchRow.add(txnCustSearch);
        searchRow.add(bClear);
        searchRow.add(Box.createHorizontalStrut(16));
        searchRow.add(bViewBill);

        JPanel north = new JPanel(new BorderLayout(0, 4));
        north.add(btnRow, BorderLayout.NORTH);
        north.add(customRow, BorderLayout.CENTER);
        north.add(searchRow, BorderLayout.SOUTH);
        p.add(north, BorderLayout.NORTH);

        // ── Table ──────────────────────────────────────────────────
        txnListModel = new DefaultTableModel(
                new String[] { "TXN ID", "Customer", "Phone", "Date & Time", "Coupon", "Amount" }, 0) {
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        JTable t = new JTable(txnListModel);
        t.setFont(new Font("SansSerif", Font.PLAIN, 13));
        t.setRowHeight(26);
        t.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));
        t.getColumnModel().getColumn(0).setPreferredWidth(155);
        t.getColumnModel().getColumn(1).setPreferredWidth(160);
        t.getColumnModel().getColumn(2).setPreferredWidth(110);
        t.getColumnModel().getColumn(3).setPreferredWidth(145);
        t.getColumnModel().getColumn(4).setPreferredWidth(65);
        t.getColumnModel().getColumn(5).setPreferredWidth(110);
        t.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        p.add(new JScrollPane(t), BorderLayout.CENTER);

        // View Bill wiring — reads selected row's TXN ID
        bViewBill.addActionListener(e -> {
            int row = t.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "Select a transaction first.", "No Selection",
                        JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            String txnId = (String) txnListModel.getValueAt(row, 0);
            String bill = DatabaseManager.getBillForTxn(txnId);
            if (bill == null) {
                JOptionPane.showMessageDialog(this, "Bill not found for TXN# " + txnId, "Not Found",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            JDialog bd = new JDialog(this, "Receipt — TXN# " + txnId, true);
            bd.setSize(420, 400);
            bd.setLocationRelativeTo(this);
            JTextArea ta = new JTextArea(bill);
            ta.setFont(new Font("Monospaced", Font.PLAIN, 13));
            ta.setEditable(false);
            ta.setBorder(new EmptyBorder(12, 14, 12, 14));
            JPanel bp2 = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            JButton cl = new JButton("Close");
            cl.addActionListener(ev -> bd.dispose());
            bp2.add(cl);
            bd.setLayout(new BorderLayout());
            bd.add(new JScrollPane(ta), BorderLayout.CENTER);
            bd.add(bp2, BorderLayout.SOUTH);
            bd.dispose();
            openBillDialog(this, "Receipt - TXN# " + txnId, bill);
        });

        // Row count at bottom
        JLabel countLbl = new JLabel("", SwingConstants.RIGHT);
        countLbl.setFont(new Font("SansSerif", Font.ITALIC, 11));
        countLbl.setBorder(new EmptyBorder(4, 0, 0, 2));
        txnListModel.addTableModelListener(ev -> countLbl.setText(txnListModel.getRowCount() + " transaction(s)"));
        p.add(countLbl, BorderLayout.SOUTH);

        refreshTransactions();
        return p;
    }

    void refreshTransactions() {
        if (txnListModel == null)
            return;
        String q = txnCustSearch != null ? txnCustSearch.getText().trim() : "";
        java.util.List<String[]> rows = isManager()
                ? DatabaseManager.loadTransactions(txnPeriodFilter, q)
                : DatabaseManager.loadTransactions(txnPeriodFilter, q, cashierId);
        txnListModel.setRowCount(0);
        for (String[] r : rows)
            txnListModel.addRow(r);
    }

    /** Bolds the active period button and un-bolds the others. */
    private void boldOnly(JButton active, JButton... rest) {
        active.setFont(new Font("SansSerif", Font.BOLD, 12));
        for (JButton b : rest)
            b.setFont(new Font("SansSerif", Font.PLAIN, 12));
    }

    // ── Analytics helpers ─────────────────────────────────────────
    JPanel makeAnalyticsCard(JLabel numLbl, String caption) {
        JPanel card = new JPanel(new BorderLayout(0, 4));
        card.setBorder(new CompoundBorder(
                BorderFactory.createLineBorder(Color.GRAY),
                new EmptyBorder(10, 10, 10, 10)));
        JLabel cap = new JLabel(caption, SwingConstants.CENTER);
        cap.setFont(new Font("SansSerif", Font.PLAIN, 12));
        numLbl.setHorizontalAlignment(SwingConstants.CENTER);
        card.add(numLbl, BorderLayout.CENTER);
        card.add(cap, BorderLayout.SOUTH);
        return card;
    }

    // ================================================================
    // CASHIERS PANEL (Manager only)
    // ================================================================
    JPanel cashiersPanel() {
        JPanel p = new JPanel(new BorderLayout(0, 0));
        p.setBorder(new EmptyBorder(20, 24, 24, 24));

        // ── Title + Refresh ──
        JPanel top = new JPanel(new BorderLayout(0, 8));
        JLabel titleLbl = new JLabel("Cashiers");
        titleLbl.setFont(new Font("SansSerif", Font.BOLD, 24));
        top.add(titleLbl, BorderLayout.WEST);
        JButton refreshBtn = new JButton("⟳ Refresh");
        refreshBtn.setFont(new Font("SansSerif", Font.PLAIN, 12));
        top.add(refreshBtn, BorderLayout.EAST);
        p.add(top, BorderLayout.NORTH);

        // ── Body ──
        JPanel body = new JPanel(new BorderLayout(0, 14));

        // Stat cards row (populated on refresh)
        JPanel statRow = new JPanel(new GridLayout(1, 4, 12, 0));
        statRow.setPreferredSize(new Dimension(0, 90));
        JLabel totalLbl = new JLabel("0", SwingConstants.CENTER);
        JLabel activeLbl = new JLabel("0", SwingConstants.CENTER);
        JLabel inactiveLbl = new JLabel("0", SwingConstants.CENTER);
        JLabel topLbl = new JLabel("—", SwingConstants.CENTER);
        for (JLabel l : new JLabel[] { totalLbl, activeLbl, inactiveLbl, topLbl })
            l.setFont(new Font("SansSerif", Font.BOLD, 28));
        inactiveLbl.setForeground(new Color(180, 40, 40));
        topLbl.setFont(new Font("SansSerif", Font.BOLD, 16));
        statRow.add(makeStatCard(totalLbl, "Total Cashiers", null));
        statRow.add(makeStatCard(activeLbl, "Active", null));
        statRow.add(makeStatCard(inactiveLbl, "Inactive", null));
        statRow.add(makeStatCard(topLbl, "Top Performer", null));
        body.add(statRow, BorderLayout.NORTH);

        // ── Cashier table ──
        String[] cols = { "ID", "Name", "Username", "Shift", "Status", "Joined", "Transactions", "Revenue", "Avg Bill",
                "Last Active" };
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        JTable table = new JTable(model);
        table.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        table.setRowHeight(24);
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));

        // Colour code Status cells
        table.getColumnModel().getColumn(4).setCellRenderer(new javax.swing.table.DefaultTableCellRenderer() {
            public java.awt.Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r,
                    int c) {
                super.getTableCellRendererComponent(t, v, sel, foc, r, c);
                String s = v == null ? "" : v.toString();
                setForeground("Active".equals(s) ? new Color(0, 140, 80) : new Color(180, 40, 40));
                setFont(getFont().deriveFont(Font.BOLD));
                return this;
            }
        });

        JScrollPane scroll = new JScrollPane(table);
        body.add(scroll, BorderLayout.CENTER);
        p.add(body, BorderLayout.CENTER);

        // ── Populate helper (declared early so buttons can call it) ──
        // Using array trick to allow lambda capture of effectively-final reference
        @SuppressWarnings("unchecked")
        Runnable[] refreshHolder = new Runnable[1];
        refreshHolder[0] = () -> {
            model.setRowCount(0);
            java.util.List<String[]> rows = DatabaseManager.loadCashiers();
            int tot = rows.size(), act = 0, inact = 0;
            String topName = rows.isEmpty() ? "—" : rows.get(0)[1];
            for (String[] r : rows) {
                model.addRow(r);
                if ("Active".equals(r[4]))
                    act++;
                else
                    inact++;
            }
            totalLbl.setText(String.valueOf(tot));
            activeLbl.setText(String.valueOf(act));
            inactiveLbl.setText(String.valueOf(inact));
            topLbl.setText(topName);
        };
        Runnable refresh = refreshHolder[0];

        // ── Action buttons bar ──
        JPanel botBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 6));
        JButton addCashierBtn = new JButton("+ Add Cashier");
        JButton toggleStatusBtn = new JButton("Toggle Status");
        JButton resetPassBtn = new JButton("Reset Password");
        JButton viewTxnBtn = new JButton("View Transactions ▶");
        addCashierBtn.setFont(new Font("SansSerif", Font.BOLD, 13));
        addCashierBtn.setForeground(new Color(30, 130, 40));
        toggleStatusBtn.setFont(new Font("SansSerif", Font.PLAIN, 13));
        resetPassBtn.setFont(new Font("SansSerif", Font.PLAIN, 13));
        viewTxnBtn.setFont(new Font("SansSerif", Font.BOLD, 13));
        viewTxnBtn.setForeground(new Color(30, 100, 200));
        toggleStatusBtn.setEnabled(false);
        resetPassBtn.setEnabled(false);
        viewTxnBtn.setEnabled(false);
        botBar.add(addCashierBtn);
        botBar.add(toggleStatusBtn);
        botBar.add(resetPassBtn);
        botBar.add(viewTxnBtn);
        p.add(botBar, BorderLayout.SOUTH);

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                boolean sel = table.getSelectedRow() >= 0;
                viewTxnBtn.setEnabled(sel);
                toggleStatusBtn.setEnabled(sel);
                resetPassBtn.setEnabled(sel);
            }
        });

        viewTxnBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0)
                return;
            int cid = Integer.parseInt(model.getValueAt(row, 0).toString());
            String nm = model.getValueAt(row, 1).toString();
            openCashierTxnDialog(cid, nm);
        });

        toggleStatusBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0)
                return;
            int cid = Integer.parseInt(model.getValueAt(row, 0).toString());
            String name = model.getValueAt(row, 1).toString();
            String cur = model.getValueAt(row, 4).toString();
            String next = "Active".equals(cur) ? "Inactive" : "Active";
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Change status of '" + name + "' from " + cur + " → " + next + "?",
                    "Toggle Status", JOptionPane.YES_NO_OPTION);
            if (confirm != JOptionPane.YES_OPTION)
                return;
            String result = DatabaseManager.toggleCashierStatus(cid);
            status("Cashier '" + name + "' is now " + result);
            refresh.run();
        });

        resetPassBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0)
                return;
            int cid = Integer.parseInt(model.getValueAt(row, 0).toString());
            String name = model.getValueAt(row, 1).toString();
            openResetPasswordDialog(cid, name);
        });

        addCashierBtn.addActionListener(e -> {
            openAddCashierDialog(refresh);
        });

        // ── Populate ──
        refresh.run();
        refreshBtn.addActionListener(e -> refresh.run());
        return p;
    }

    /** Dialog to add a new cashier. */
    void openAddCashierDialog(Runnable onSuccess) {
        JDialog d = new JDialog(this, "Add New Cashier", true);
        d.setSize(400, 340);
        d.setLocationRelativeTo(this);
        JPanel form = new JPanel(new GridLayout(0, 2, 8, 10));
        form.setBorder(new EmptyBorder(16, 16, 8, 16));

        JTextField nameF = new JTextField();
        JTextField userF = new JTextField();
        JComboBox<String> shiftCb = new JComboBox<>(new String[] { "Morning", "Afternoon", "Evening" });
        JPasswordField passF = new JPasswordField();
        JPasswordField confF = new JPasswordField();

        form.add(new JLabel("Full Name *:"));
        form.add(nameF);
        form.add(new JLabel("Username *:"));
        form.add(userF);
        form.add(new JLabel("Shift *:"));
        form.add(shiftCb);
        form.add(new JLabel("Password *:"));
        form.add(passF);
        form.add(new JLabel("Confirm Password:"));
        form.add(confF);

        JPanel bp = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        JButton save = new JButton("Add Cashier");
        save.setForeground(new Color(30, 130, 40));
        JButton cancel = new JButton("Cancel");
        cancel.addActionListener(ev -> d.dispose());
        save.addActionListener(ev -> {
            String nm = nameF.getText().trim();
            String usr = userF.getText().trim();
            String shft = (String) shiftCb.getSelectedItem();
            String pw1 = new String(passF.getPassword()).trim();
            String pw2 = new String(confF.getPassword()).trim();
            if (nm.isEmpty() || usr.isEmpty() || pw1.isEmpty()) {
                JOptionPane.showMessageDialog(d, "Name, Username and Password are required.", "Validation",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (!pw1.equals(pw2)) {
                JOptionPane.showMessageDialog(d, "Passwords do not match.", "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }
            String today = new java.text.SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date());
            try {
                int newId = DatabaseManager.insertCashier(nm, usr, shft, today, pw1);
                status("Cashier '" + nm + "' added (ID: " + newId + ")");
                onSuccess.run();
                d.dispose();
            } catch (RuntimeException ex) {
                JOptionPane.showMessageDialog(d, "Error: " + ex.getMessage(), "DB Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        bp.add(cancel);
        bp.add(save);
        d.setLayout(new BorderLayout());
        d.add(form, BorderLayout.CENTER);
        d.add(bp, BorderLayout.SOUTH);
        d.setVisible(true);
    }

    /** Dialog to reset a cashier's password. */
    void openResetPasswordDialog(int cashierId, String cashierName) {
        JDialog d = new JDialog(this, "Reset Password — " + cashierName, true);
        d.setSize(360, 220);
        d.setLocationRelativeTo(this);
        JPanel form = new JPanel(new GridLayout(0, 2, 8, 10));
        form.setBorder(new EmptyBorder(16, 16, 8, 16));

        JPasswordField newPass = new JPasswordField();
        JPasswordField confPass = new JPasswordField();
        form.add(new JLabel("New Password *:"));
        form.add(newPass);
        form.add(new JLabel("Confirm Password *:"));
        form.add(confPass);

        JPanel bp = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        JButton save = new JButton("Reset");
        save.setForeground(new Color(180, 40, 40));
        JButton cancel = new JButton("Cancel");
        cancel.addActionListener(ev -> d.dispose());
        save.addActionListener(ev -> {
            String pw1 = new String(newPass.getPassword()).trim();
            String pw2 = new String(confPass.getPassword()).trim();
            if (pw1.isEmpty()) {
                JOptionPane.showMessageDialog(d, "Password cannot be empty.", "Validation",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (!pw1.equals(pw2)) {
                JOptionPane.showMessageDialog(d, "Passwords do not match.", "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }
            try {
                DatabaseManager.resetCashierPassword(cashierId, pw1);
                status("Password reset for '" + cashierName + "'");
                JOptionPane.showMessageDialog(d, "Password has been reset successfully.", "Success",
                        JOptionPane.INFORMATION_MESSAGE);
                d.dispose();
            } catch (RuntimeException ex) {
                JOptionPane.showMessageDialog(d, "Error: " + ex.getMessage(), "DB Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        bp.add(cancel);
        bp.add(save);
        d.setLayout(new BorderLayout());
        d.add(form, BorderLayout.CENTER);
        d.add(bp, BorderLayout.SOUTH);
        d.setVisible(true);
    }

    void openCashierTxnDialog(int cashierId, String name) {
        JDialog d = new JDialog(this, "Transactions — " + name, true);
        d.setSize(700, 420);
        d.setLocationRelativeTo(this);

        String[] cols = { "TXN ID", "Customer", "Amount", "Date/Time" };
        DefaultTableModel m = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        java.util.List<String[]> rows = DatabaseManager.getCashierTxns(cashierId);
        if (rows.isEmpty()) {
            m.addRow(new Object[] { "No transactions recorded", "—", "—", "—" });
        } else {
            for (String[] r : rows)
                m.addRow(r);
        }
        JTable t = new JTable(m);
        t.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        t.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        t.setRowHeight(22);

        // Summary label
        double total = rows.stream()
                .mapToDouble(r -> {
                    try {
                        return Double.parseDouble(r[2].replace("Rs. ", ""));
                    } catch (Exception ex) {
                        return 0;
                    }
                })
                .sum();
        JLabel sumLbl = new JLabel(String.format(
                "  %d transactions  ·  Total Revenue: Rs. %.2f  ·  Avg Bill: Rs. %.2f",
                rows.size(), total, rows.isEmpty() ? 0 : total / rows.size()));
        sumLbl.setFont(new Font("SansSerif", Font.BOLD, 12));
        sumLbl.setBorder(new EmptyBorder(6, 8, 6, 8));

        JPanel bp = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton viewBillBtn = new JButton("View Bill");
        viewBillBtn.setEnabled(false);
        JButton cl = new JButton("Close");
        cl.addActionListener(ev -> d.dispose());
        bp.add(viewBillBtn);
        bp.add(cl);

        t.getSelectionModel().addListSelectionListener(ev -> {
            if (!ev.getValueIsAdjusting())
                viewBillBtn.setEnabled(t.getSelectedRow() >= 0 && !rows.isEmpty());
        });

        viewBillBtn.addActionListener(ev -> {
            int row = t.getSelectedRow();
            if (row < 0)
                return;
            String txnId = m.getValueAt(row, 0).toString();
            String bill = DatabaseManager.getBillForTxn(txnId);
            if (bill == null) {
                JOptionPane.showMessageDialog(d, "Bill not found for TXN# " + txnId, "Not Found",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            JDialog bd = new JDialog(d, "Receipt — TXN# " + txnId, true);
            bd.setSize(420, 400);
            bd.setLocationRelativeTo(d);
            JTextArea ta = new JTextArea(bill);
            ta.setFont(new Font("Monospaced", Font.PLAIN, 13));
            ta.setEditable(false);
            ta.setBorder(new EmptyBorder(12, 14, 12, 14));
            JPanel bp2 = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            JButton cl2 = new JButton("Close");
            cl2.addActionListener(e2 -> bd.dispose());
            bp2.add(cl2);
            bd.setLayout(new BorderLayout());
            bd.add(new JScrollPane(ta), BorderLayout.CENTER);
            bd.add(bp2, BorderLayout.SOUTH);
            bd.dispose();
            openBillDialog(d, "Receipt - TXN# " + txnId, bill);
        });

        d.setLayout(new BorderLayout(0, 0));
        d.add(sumLbl, BorderLayout.NORTH);
        d.add(new JScrollPane(t), BorderLayout.CENTER);
        d.add(bp, BorderLayout.SOUTH);
        d.setVisible(true);
    }

    // ================================================================
    // HELPERS
    // ================================================================
    void openBillDialog(Window owner, String title, String bill) {
        JDialog d = owner instanceof Dialog
                ? new JDialog((Dialog) owner, title, true)
                : new JDialog((Frame) owner, title, true);
        d.setSize(420, 400);
        d.setLocationRelativeTo(owner);

        JTextArea ta = new JTextArea(bill);
        ta.setFont(new Font("Monospaced", Font.PLAIN, 13));
        ta.setEditable(false);
        ta.setBorder(new EmptyBorder(12, 14, 12, 14));

        JPanel bp = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton savePdfBtn = new JButton("Save PDF");
        savePdfBtn.addActionListener(ev -> saveBillAsPdf(title, bill, d));
        JButton printBtn = new JButton("Print");
        printBtn.addActionListener(ev -> {
            try {
                String printJobName = extractPrintJobName(title);
                PrinterJob job = PrinterJob.getPrinterJob();
                job.setJobName(printJobName);
                job.setPrintable(ta.getPrintable(null, null));
                PrintRequestAttributeSet attrs = new HashPrintRequestAttributeSet();
                attrs.add(new JobName(printJobName, java.util.Locale.getDefault()));
                if (!job.printDialog(attrs)) {
                    status("Print cancelled for " + title);
                    return;
                }
                job.print(attrs);
            } catch (PrinterException ex) {
                JOptionPane.showMessageDialog(d, "Unable to print bill:\n" + ex.getMessage(), "Print Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });
        JButton cl = new JButton("Close");
        cl.addActionListener(ev -> d.dispose());
        bp.add(savePdfBtn);
        bp.add(printBtn);
        bp.add(cl);

        d.setLayout(new BorderLayout());
        d.add(new JScrollPane(ta), BorderLayout.CENTER);
        d.add(bp, BorderLayout.SOUTH);
        d.setVisible(true);
    }

    String extractPrintJobName(String title) {
        int idx = title.indexOf("TXN# ");
        if (idx >= 0) {
            String txnId = title.substring(idx + 5).trim();
            if (!txnId.isEmpty())
                return "TXN#" + txnId;
        }
        return title;
    }

    void saveBillAsPdf(String title, String bill, Component parent) {
        String baseName = extractPrintJobName(title);
        File defaultDir = new File(System.getProperty("user.home"), "Documents/FreshMitra Bills");
        if (!defaultDir.exists())
            defaultDir.mkdirs();
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Save Receipt as PDF");
        chooser.setFileFilter(new FileNameExtensionFilter("PDF Document (*.pdf)", "pdf"));
        chooser.setCurrentDirectory(defaultDir);
        chooser.setSelectedFile(new File(defaultDir, baseName + ".pdf"));

        int result = chooser.showSaveDialog(parent);
        if (result != JFileChooser.APPROVE_OPTION) {
            status("PDF export cancelled for " + title);
            return;
        }

        File file = chooser.getSelectedFile();
        if (!file.getName().toLowerCase(Locale.ROOT).endsWith(".pdf"))
            file = new File(file.getParentFile(), file.getName() + ".pdf");

        if (file.exists()) {
            int overwrite = JOptionPane.showConfirmDialog(parent,
                    "Overwrite existing file?\n" + file.getAbsolutePath(),
                    "Confirm Save", JOptionPane.YES_NO_OPTION);
            if (overwrite != JOptionPane.YES_OPTION)
                return;
        }

        try {
            writeSimplePdf(file, bill);
            status("Saved PDF: " + file.getName());
            JOptionPane.showMessageDialog(parent, "Receipt saved as PDF:\n" + file.getAbsolutePath(),
                    "PDF Saved", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(parent, "Unable to save PDF:\n" + ex.getMessage(), "Save Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    void writeSimplePdf(File file, String text) throws Exception {
        java.util.List<String> normalizedLines = new ArrayList<>();
        String[] sourceLines = text.replace("\r\n", "\n").replace('\r', '\n').split("\n", -1);
        for (String line : sourceLines) {
            if (line.length() <= 90) {
                normalizedLines.add(line);
            } else {
                for (int i = 0; i < line.length(); i += 90)
                    normalizedLines.add(line.substring(i, Math.min(line.length(), i + 90)));
            }
        }

        int linesPerPage = 48;
        java.util.List<String> pages = new ArrayList<>();
        for (int i = 0; i < normalizedLines.size(); i += linesPerPage) {
            StringBuilder content = new StringBuilder();
            content.append("BT\n");
            content.append("/F1 11 Tf\n");
            content.append("50 790 Td\n");
            boolean firstLine = true;
            for (int j = i; j < Math.min(normalizedLines.size(), i + linesPerPage); j++) {
                if (!firstLine)
                    content.append("0 -14 Td\n");
                content.append("(").append(escapePdfText(normalizedLines.get(j))).append(") Tj\n");
                firstLine = false;
            }
            content.append("ET\n");
            pages.add(content.toString());
        }
        if (pages.isEmpty())
            pages.add("BT\n/F1 11 Tf\n50 790 Td\n() Tj\nET\n");

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        java.util.List<Integer> xref = new ArrayList<>();
        out.write("%PDF-1.4\n".getBytes(StandardCharsets.US_ASCII));

        int pageCount = pages.size();
        int fontObj = 3 + pageCount * 2;
        int objCount = fontObj;

        xref.add(0);
        writePdfObject(out, xref, 1, "<< /Type /Catalog /Pages 2 0 R >>");

        StringBuilder kids = new StringBuilder();
        for (int i = 0; i < pageCount; i++) {
            if (i > 0)
                kids.append(' ');
            kids.append(3 + i).append(" 0 R");
        }
        writePdfObject(out, xref, 2,
                "<< /Type /Pages /Count " + pageCount + " /Kids [" + kids + "] >>");

        for (int i = 0; i < pageCount; i++) {
            int pageObj = 3 + i;
            int contentObj = 3 + pageCount + i;
            String page = "<< /Type /Page /Parent 2 0 R /MediaBox [0 0 595 842] "
                    + "/Resources << /Font << /F1 " + fontObj + " 0 R >> >> "
                    + "/Contents " + contentObj + " 0 R >>";
            writePdfObject(out, xref, pageObj, page);
        }

        for (int i = 0; i < pageCount; i++) {
            String stream = pages.get(i);
            byte[] streamBytes = stream.getBytes(StandardCharsets.US_ASCII);
            writePdfStreamObject(out, xref, 3 + pageCount + i, streamBytes);
        }

        writePdfObject(out, xref, fontObj, "<< /Type /Font /Subtype /Type1 /BaseFont /Courier >>");

        int startXref = out.size();
        StringBuilder xrefTable = new StringBuilder();
        xrefTable.append("xref\n");
        xrefTable.append("0 ").append(objCount + 1).append("\n");
        xrefTable.append("0000000000 65535 f \n");
        for (int i = 1; i <= objCount; i++)
            xrefTable.append(String.format(Locale.ROOT, "%010d 00000 n %n", xref.get(i)));
        out.write(xrefTable.toString().getBytes(StandardCharsets.US_ASCII));

        String trailer = "trailer\n<< /Size " + (objCount + 1) + " /Root 1 0 R >>\n"
                + "startxref\n" + startXref + "\n%%EOF";
        out.write(trailer.getBytes(StandardCharsets.US_ASCII));

        try (FileOutputStream fos = new FileOutputStream(file)) {
            out.writeTo(fos);
        }
    }

    void writePdfObject(ByteArrayOutputStream out, java.util.List<Integer> xref, int objNum, String body)
            throws Exception {
        xref.add(out.size());
        String obj = objNum + " 0 obj\n" + body + "\nendobj\n";
        out.write(obj.getBytes(StandardCharsets.US_ASCII));
    }

    void writePdfStreamObject(ByteArrayOutputStream out, java.util.List<Integer> xref, int objNum, byte[] streamBytes)
            throws Exception {
        xref.add(out.size());
        String head = objNum + " 0 obj\n<< /Length " + streamBytes.length + " >>\nstream\n";
        out.write(head.getBytes(StandardCharsets.US_ASCII));
        out.write(streamBytes);
        out.write("\nendstream\nendobj\n".getBytes(StandardCharsets.US_ASCII));
    }

    String escapePdfText(String text) {
        StringBuilder sb = new StringBuilder();
        for (char ch : text.toCharArray()) {
            switch (ch) {
                case '\\' -> sb.append("\\\\");
                case '(' -> sb.append("\\(");
                case ')' -> sb.append("\\)");
                default -> {
                    if (ch >= 32 && ch <= 126)
                        sb.append(ch);
                    else
                        sb.append('?');
                }
            }
        }
        return sb.toString();
    }

    void msg(String m) {
        JOptionPane.showMessageDialog(this, m);
    }
}

// ================================================================
// LOGIN DIALOG
// ================================================================
class LoginDialog extends JDialog {

    private String grantedRole = null;
    private int grantedCashierId = -1;

    private static final String MGR_USER = getEnvOrProperty("FRESHMITRA_MANAGER_USER", "manager");
    private static final String MGR_PASS = getEnvOrProperty("FRESHMITRA_MANAGER_PASS", "admin123");

    LoginDialog() {
        super((java.awt.Frame) null, "Fresh Mitra — Login", true);
        setSize(440, 520);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setResizable(false);
        buildUI();
    }

    String getGrantedRole() {
        return grantedRole;
    }

    int getCashierId() {
        return grantedCashierId;
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(new Color(245, 247, 252));

        // ---- Top brand banner ----
        JPanel banner = new JPanel(new BorderLayout());
        banner.setBackground(new Color(22, 60, 140));
        banner.setPreferredSize(new Dimension(0, 110));
        JLabel brand = new JLabel("Fresh Mitra", SwingConstants.CENTER);
        brand.setFont(new Font("SansSerif", Font.BOLD, 30));
        brand.setForeground(Color.WHITE);
        JLabel sub = new JLabel("Store Management System", SwingConstants.CENTER);
        sub.setFont(new Font("SansSerif", Font.PLAIN, 13));
        sub.setForeground(new Color(180, 200, 255));
        JPanel brandBox = new JPanel();
        brandBox.setOpaque(false);
        brandBox.setLayout(new BoxLayout(brandBox, BoxLayout.Y_AXIS));
        brand.setAlignmentX(0.5f);
        sub.setAlignmentX(0.5f);
        brandBox.add(Box.createVerticalStrut(22));
        brandBox.add(brand);
        brandBox.add(sub);
        banner.add(brandBox, BorderLayout.CENTER);
        root.add(banner, BorderLayout.NORTH);

        // ---- Form panel ----
        JPanel form = new JPanel();
        form.setOpaque(false);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBorder(new javax.swing.border.EmptyBorder(30, 50, 30, 50));

        // Role toggle
        JPanel roleRow = new JPanel(new java.awt.GridLayout(1, 2, 10, 0));
        roleRow.setOpaque(false);
        roleRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        roleRow.setAlignmentX(0f);
        JToggleButton mgrBtn = new JToggleButton("Manager");
        JToggleButton cshBtn = new JToggleButton("Cashier");
        ButtonGroup bg = new ButtonGroup();
        bg.add(mgrBtn);
        bg.add(cshBtn);
        styleRoleBtn(mgrBtn, new Color(22, 60, 140));
        styleRoleBtn(cshBtn, new Color(0, 130, 90));
        mgrBtn.setSelected(true);
        roleRow.add(mgrBtn);
        roleRow.add(cshBtn);

        // Username
        JLabel userLbl = new JLabel("Username");
        userLbl.setFont(new Font("SansSerif", Font.BOLD, 12));
        userLbl.setAlignmentX(0f);
        JTextField userF = new JTextField();
        userF.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        userF.setAlignmentX(0f);
        userF.setFont(new Font("SansSerif", Font.PLAIN, 14));
        userF.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                javax.swing.BorderFactory.createLineBorder(new Color(180, 180, 210)),
                new javax.swing.border.EmptyBorder(4, 8, 4, 8)));

        // Password
        JLabel passLbl = new JLabel("Password");
        passLbl.setFont(new Font("SansSerif", Font.BOLD, 12));
        passLbl.setAlignmentX(0f);
        JPasswordField passF = new JPasswordField();
        passF.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        passF.setAlignmentX(0f);
        passF.setFont(new Font("SansSerif", Font.PLAIN, 14));
        passF.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                javax.swing.BorderFactory.createLineBorder(new Color(180, 180, 210)),
                new javax.swing.border.EmptyBorder(4, 8, 4, 8)));

        // Error label
        JLabel errLbl = new JLabel(" ");
        errLbl.setFont(new Font("SansSerif", Font.BOLD, 12));
        errLbl.setForeground(new Color(200, 40, 40));
        errLbl.setAlignmentX(0f);

        // Login button
        JButton loginBtn = new JButton("Login");
        loginBtn.setFont(new Font("SansSerif", Font.BOLD, 15));
        loginBtn.setBackground(new Color(22, 60, 140));
        loginBtn.setForeground(Color.WHITE);
        loginBtn.setOpaque(true);
        loginBtn.setBorderPainted(false);
        loginBtn.setFocusPainted(false);
        loginBtn.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
        loginBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        loginBtn.setAlignmentX(0f);

        // Hint label
        JLabel hintLbl = new JLabel();
        hintLbl.setFont(new Font("SansSerif", Font.ITALIC, 11));
        hintLbl.setForeground(new Color(130, 130, 160));
        hintLbl.setAlignmentX(0f);
        updateHint(hintLbl, true);
        mgrBtn.addActionListener(e -> {
            updateHint(hintLbl, true);
            loginBtn.setBackground(new Color(22, 60, 140));
        });
        cshBtn.addActionListener(e -> {
            updateHint(hintLbl, false);
            loginBtn.setBackground(new Color(0, 130, 90));
        });

        Runnable doLogin = () -> {
            String user = userF.getText().trim();
            String pass = new String(passF.getPassword());
            boolean mgr = mgrBtn.isSelected();
            if (mgr) {
                if (MGR_USER.equals(user) && MGR_PASS.equals(pass)) {
                    grantedRole = "MANAGER";
                    grantedCashierId = -1;
                    dispose();
                } else {
                    errLbl.setText("\u2716  Invalid manager credentials.");
                }
            } else {
                int cid = DatabaseManager.verifyCashierLogin(user, pass);
                if (cid > 0) {
                    grantedRole = "CASHIER";
                    grantedCashierId = cid;
                    dispose();
                } else if (cid == -2) {
                    errLbl.setText("\u2716  Account is inactive. Contact manager.");
                } else {
                    errLbl.setText("\u2716  Invalid cashier credentials.");
                }
            }
        };

        loginBtn.addActionListener(e -> doLogin.run());
        passF.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent e) {
                if (e.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER)
                    doLogin.run();
            }
        });
        userF.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent e) {
                if (e.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER)
                    passF.requestFocus();
            }
        });

        JLabel loginAsLbl = new JLabel("Login as");
        loginAsLbl.setAlignmentX(0f);

        form.add(loginAsLbl);
        form.add(Box.createVerticalStrut(6));
        form.add(roleRow);
        form.add(Box.createVerticalStrut(18));
        form.add(userLbl);
        form.add(Box.createVerticalStrut(4));
        form.add(userF);
        form.add(Box.createVerticalStrut(12));
        form.add(passLbl);
        form.add(Box.createVerticalStrut(4));
        form.add(passF);
        form.add(Box.createVerticalStrut(8));
        form.add(errLbl);
        form.add(Box.createVerticalStrut(12));
        form.add(loginBtn);
        form.add(Box.createVerticalStrut(12));
        form.add(hintLbl);

        root.add(form, BorderLayout.CENTER);
        setContentPane(root);
    }

    private void styleRoleBtn(JToggleButton b, Color accent) {
        b.setFont(new Font("SansSerif", Font.BOLD, 13));
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setBackground(accent);
        b.setForeground(Color.WHITE);
        b.setOpaque(true);
        b.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
        b.setSelectedIcon(null);
    }

    private static String getEnvOrProperty(String key, String defaultValue) {
        String value = System.getenv(key);
        if (value == null || value.isBlank())
            value = System.getProperty(key);
        return (value == null || value.isBlank()) ? defaultValue : value;
    }

    private void updateHint(JLabel h, boolean mgr) {
        h.setText(mgr ? "user: manager · pass: admin123" : "e.g.  rahul / rahul123");
    }
}
