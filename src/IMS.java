import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Scanner;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.io.IOException;

/**
 * Represents a product in the inventory management system.
 * Contains information about the product's name, description, price, and quantity.
 */
class Product { // Changed from class to static class to prevent unnecessary instance creation
    private String name;
    private String description;
    private double price;
    private int quantity;

    public Product(String name, String description, double price, int quantity) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.quantity = quantity;
    }

    // Getters and setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }
}

/**
 * Main inventory management system class that handles all inventory operations.
 * Provides functionality for adding, removing, updating, and viewing products.
 */
class InventoryManagementSystem { // Changed from class to static class
    private ArrayList<Product> products;

    public InventoryManagementSystem() {
        products = new ArrayList<>();
        Logger.info("Inventory Management System initialized");
    }

    /**
     * Adds a new product to the inventory.
     * @param name Product name
     * @param description Product description
     * @param price Product price
     * @param quantity Product quantity
     */
    public void addProduct(String name, String description, double price, int quantity) {
        if (name == null || name.isEmpty()) {
            Logger.error("Attempted to add product with empty name");
            return;
        }
        if (price <= 0) {
            Logger.error("Attempted to add product with invalid price: " + price);
            return;
        }
        if (quantity < 0) {
            Logger.error("Attempted to add product with negative quantity: " + quantity);
            return;
        }

        try (Connection connection = DatabaseConnector.connect();
                PreparedStatement statement = connection.prepareStatement("SELECT * FROM products WHERE name = ?")) {
            statement.setString(1, name);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                Logger.error("Product already exists: " + name);
                return;
            }
        } catch (SQLException e) {
            Logger.error("Database error while checking for duplicate product: " + e.getMessage());
            return;
        }

        try (Connection connection = DatabaseConnector.connect();
                PreparedStatement statement = connection.prepareStatement(
                        "INSERT INTO products (name, description, price, quantity) VALUES (?, ?, ?, ?)")) {
            statement.setString(1, name);
            statement.setString(2, description);
            statement.setDouble(3, price);
            statement.setInt(4, quantity);
            statement.executeUpdate();
            Logger.info("Product added successfully: " + name);
        } catch (SQLException e) {
            Logger.error("Database error while adding product: " + e.getMessage());
        }
    }

    public boolean removeProduct(String productName) {
        if (productName == null || productName.isEmpty()) {
            Logger.error("Product name cannot be empty");
            return false;
        }

        try (Connection connection = DatabaseConnector.connect();
                PreparedStatement statement = connection.prepareStatement("DELETE FROM products WHERE name = ?")) {
            statement.setString(1, productName);
            int rowsAffected = statement.executeUpdate();
            if (rowsAffected > 0) {
                Logger.info("Product removed successfully: " + productName);
                return true;
            } else {
                Logger.error("Product not found: " + productName);
                return false;
            }
        } catch (SQLException e) {
            Logger.error("Database error while removing product: " + e.getMessage());
            return false;
        }
    }

    public void updateProductDetails(String currentName) {
        if (currentName == null || currentName.isEmpty()) {
            Logger.error("Current product name cannot be empty");
            return;
        }
    
        Product foundProduct = null;
        try (Connection connection = DatabaseConnector.connect();
             PreparedStatement statement = connection.prepareStatement("SELECT * FROM products WHERE name = ?")) {
            statement.setString(1, currentName);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                foundProduct = new Product(
                        resultSet.getString("name"),
                        resultSet.getString("description"),
                        resultSet.getDouble("price"),
                        resultSet.getInt("quantity")
                );
            }
        } catch (SQLException e) {
            Logger.error("Database error while fetching product: " + e.getMessage());
            return;
        }
    
        if (foundProduct != null) {
            Scanner sc = new Scanner(System.in);
            System.out.print("Do you want to change the product name? (yes/no): ");
            if (sc.nextLine().equalsIgnoreCase("yes")) {
                System.out.print("Enter new product name: ");
                foundProduct.setName(sc.nextLine());
            }
    
            System.out.print("Do you want to change the product description? (yes/no): ");
            if (sc.nextLine().equalsIgnoreCase("yes")) {
                System.out.print("Enter new product description: ");
                foundProduct.setDescription(sc.nextLine());
            }
    
            System.out.print("Do you want to change the product price? (yes/no): ");
            if (sc.nextLine().equalsIgnoreCase("yes")) {
                System.out.print("Enter new product price: ");
                try {
                    foundProduct.setPrice(Double.parseDouble(sc.nextLine()));
                } catch (NumberFormatException e) {
                    Logger.error("Invalid price input");
                    return;
                }
            }
    
            System.out.print("Do you want to change the product quantity? (yes/no): ");
            if (sc.nextLine().equalsIgnoreCase("yes")) {
                System.out.print("Enter new quantity: ");
                try {
                    foundProduct.setQuantity(Integer.parseInt(sc.nextLine()));
                } catch (NumberFormatException e) {
                    Logger.error("Invalid quantity input");
                    return;
                }
            }
    
            try (Connection connection = DatabaseConnector.connect();
                 PreparedStatement statement = connection.prepareStatement(
                     "UPDATE products SET name = ?, description = ?, price = ?, quantity = ? WHERE name = ?")) {
                statement.setString(1, foundProduct.getName());
                statement.setString(2, foundProduct.getDescription());
                statement.setDouble(3, foundProduct.getPrice());
                statement.setInt(4, foundProduct.getQuantity());
                statement.setString(5, currentName);
                statement.executeUpdate();
                Logger.info("Product updated successfully: " + foundProduct.getName());
            } catch (SQLException e) {
                Logger.error("Database error while updating product: " + e.getMessage());
            }
        } else {
            Logger.error("Product not found: " + currentName);
        }
    }
    
    

    public void viewInventory() {
        try (Connection connection = DatabaseConnector.connect();
             PreparedStatement statement = connection.prepareStatement("SELECT * FROM products");
             ResultSet resultSet = statement.executeQuery()) {
            if (!resultSet.isBeforeFirst()) {
                Logger.info("The inventory is currently empty");
                return;
            }
    
            System.out.printf("%-20s %-30s %-10s %-10s%n", "Product Name", "Description", "Price", "Quantity");
            System.out.println("---------------------------------------------------------------------");
            while (resultSet.next()) {
                System.out.printf("%-20s %-30s $%-9.2f %-10d%n",
                        resultSet.getString("name"),
                        resultSet.getString("description"),
                        resultSet.getDouble("price"),
                        resultSet.getInt("quantity"));
            }
        } catch (SQLException e) {
            Logger.error("Database error while viewing inventory: " + e.getMessage());
        }
    }
    

    public void saveInventoryToFile(String filename) {
        try (Connection connection = DatabaseConnector.connect();
             PreparedStatement statement = connection.prepareStatement("SELECT * FROM products");
             ResultSet resultSet = statement.executeQuery();
             PrintWriter writer = new PrintWriter(filename)) {
            
            while (resultSet.next()) {
                writer.printf("%s,%s,%.2f,%d%n",
                    resultSet.getString("name"),
                    resultSet.getString("description"),
                    resultSet.getDouble("price"),
                    resultSet.getInt("quantity"));
            }
            Logger.info("Inventory data saved to file: " + filename);
        } catch (SQLException | IOException e) {
            Logger.error("Error saving inventory to file: " + e.getMessage());
        }
    }

    public void loadInventoryFromFile(String filename) {
        try (Scanner fileScanner = new Scanner(new File(filename))) {
            while (fileScanner.hasNextLine()) {
                String line = fileScanner.nextLine();
                String[] parts = line.split(",");
                if (parts.length == 4) {
                    try (Connection connection = DatabaseConnector.connect();
                         PreparedStatement statement = connection.prepareStatement(
                             "INSERT INTO products (name, description, price, quantity) VALUES (?, ?, ?, ?)")) {
                        statement.setString(1, parts[0]);
                        statement.setString(2, parts[1]);
                        statement.setDouble(3, Double.parseDouble(parts[2]));
                        statement.setInt(4, Integer.parseInt(parts[3]));
                        statement.executeUpdate();
                    } catch (SQLException e) {
                        Logger.error("Error loading product from file: " + e.getMessage());
                    }
                }
            }
            Logger.info("Inventory data loaded from file: " + filename);
        } catch (FileNotFoundException e) {
            Logger.error("File not found: " + filename);
        } catch (NumberFormatException e) {
            Logger.error("Error in number format in the file");
        }
    }
}

class Login {
    private String username;
    private String password;

    public Login(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public boolean verifyCredentials() {
        try (Connection connection = DatabaseConnector.connect();
             PreparedStatement statement = connection.prepareStatement("SELECT * FROM users WHERE username = ? AND password = ?")) {
            statement.setString(1, username);
            statement.setString(2, password);
            ResultSet resultSet = statement.executeQuery();
            return resultSet.next();
        } catch (SQLException e) {
            Logger.error("Database error during login: " + e.getMessage());
            return false;
        }
    }
}

public class IMS {
    public static void main(String[] args) {
        InventoryManagementSystem ims = new InventoryManagementSystem();
        Scanner sc = new Scanner(System.in);
        System.out.print("Enter username: ");
        String username = sc.nextLine();

        System.out.print("Enter password: ");
        String password = sc.nextLine();

        Login login = new Login(username, password);
        boolean isAuthenticated = login.verifyCredentials();

        if (isAuthenticated) {
            boolean exit = false;
            while (!exit) {
                System.out.println("\n--- Inventory Management System ---");
                System.out.println("1. Add Product");
                System.out.println("2. Remove Product");
                System.out.println("3. Update Inventory");
                System.out.println("4. View Inventory");
                System.out.println("5. Save Inventory to File");
                System.out.println("6. Load Inventory from File");
                System.out.println("7. Exit");
                System.out.print("Choose an option: ");
                int choice = sc.nextInt();
                sc.nextLine(); // Consume the newline character

                switch (choice) {
                    case 1:
                        System.out.print("Enter product name: ");
                        String name = sc.nextLine();
                        System.out.print("Enter product description: ");
                        String description = sc.nextLine();
                        System.out.print("Enter product price: ");
                        double price = sc.nextDouble();
                        System.out.print("Enter product quantity: ");
                        int quantity = sc.nextInt();
                        ims.addProduct(name, description, price, quantity);
                        break;
                    case 2:
                        System.out.print("Enter product name to remove: ");
                        String productNameToRemove = sc.nextLine();
                        ims.removeProduct(productNameToRemove);
                        break;
                    case 3:
                        System.out.print("Enter product name to update: ");
                        String productNameToUpdate = sc.nextLine();
                        ims.updateProductDetails(productNameToUpdate);
                        break;
                    case 4:
                        ims.viewInventory();
                        break;
                    case 5:
                        System.out.print("Enter filename to save inventory: ");
                        String saveFilename = sc.nextLine();
                        ims.saveInventoryToFile(saveFilename);
                        break;
                    case 6:
                        System.out.print("Enter filename to load inventory: ");
                        String loadFilename = sc.nextLine();
                        ims.loadInventoryFromFile(loadFilename);
                        break;
                    case 7:
                        exit = true;
                        System.out.println("Exiting the system. Goodbye!");
                        break;
                    default:
                        System.out.println("Invalid choice. Please try again.");
                }
            }
        } else {
            System.out.println("Invalid username or password. Access denied.");
        }
    }
}
