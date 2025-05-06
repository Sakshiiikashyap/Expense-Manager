import java.io.*;
import java.util.*;

class Expense {
    String description;
    double amount;

    Expense(String description, double amount) {
        this.description = description;
        this.amount = amount;
    }

    public String toString() {
        return description + " - ₹" + amount;
    }
}

class User {
    String username;
    String password;
    List<Expense> expenses = new ArrayList<>();

    User(String username, String password) {
        this.username = username;
        this.password = password;
    }

    void loadExpensesFromFile() {
        expenses.clear();
        File file = new File("expenses_" + username + ".txt");
        if (!file.exists()) return;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",", 2);
                if (parts.length == 2) {
                    expenses.add(new Expense(parts[0], Double.parseDouble(parts[1])));
                }
            }
        } catch (IOException e) {
            System.out.println("Error reading expenses file.");
        }
    }

    void saveExpensesToFile() {
        File file = new File("expenses_" + username + ".txt");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            for (Expense e : expenses) {
                writer.write(e.description + "," + e.amount);
                writer.newLine();
            }
        } catch (IOException e) {
            System.out.println("Error writing expenses file.");
        }
    }
}

public class ExpenseManager {
    static Scanner scanner = new Scanner(System.in);
    static Map<String, User> users = new HashMap<>();
    static User currentUser = null;
    static final String USERS_FILE = "users.txt";

    public static void main(String[] args) {
        loadUsers();

        while (true) {
            System.out.println("\n=== EXPENSE MANAGER ===");
            if (currentUser == null) {
                System.out.println("1. Register");
                System.out.println("2. Login");
                System.out.println("3. Exit");
                System.out.print("Select option: ");
                int choice = scanner.nextInt();
                scanner.nextLine(); // consume newline

                switch (choice) {
                    case 1 -> register();
                    case 2 -> login();
                    case 3 -> {
                        saveUsers();
                        System.out.println("Goodbye!");
                        return;
                    }
                    default -> System.out.println("Invalid choice.");
                }
            } else {
                System.out.println("Welcome, " + currentUser.username + "!");
                System.out.println("1. Add Expense");
                System.out.println("2. View Expenses");
                System.out.println("3. Delete Expense");
                System.out.println("4. Logout");
                System.out.print("Select option: ");
                int choice = scanner.nextInt();
                scanner.nextLine();

                switch (choice) {
                    case 1 -> addExpense();
                    case 2 -> viewExpenses();
                    case 3 -> deleteExpense();
                    case 4 -> {
                        currentUser.saveExpensesToFile();
                        currentUser = null;
                        System.out.println("Logged out.");
                    }
                    default -> System.out.println("Invalid choice.");
                }
            }
        }
    }

    static void loadUsers() {
        File file = new File(USERS_FILE);
        if (!file.exists()) return;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",", 2);
                if (parts.length == 2) {
                    users.put(parts[0], new User(parts[0], parts[1]));
                }
            }
        } catch (IOException e) {
            System.out.println("Error loading users.");
        }
    }

    static void saveUsers() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(USERS_FILE))) {
            for (User user : users.values()) {
                writer.write(user.username + "," + user.password);
                writer.newLine();
            }
        } catch (IOException e) {
            System.out.println("Error saving users.");
        }
    }

    static void register() {
        System.out.print("Enter new username: ");
        String username = scanner.nextLine();
        if (users.containsKey(username)) {
            System.out.println("Username already exists.");
            return;
        }

        System.out.print("Enter password: ");
        String password = scanner.nextLine();
        User newUser = new User(username, password);
        users.put(username, newUser);
        saveUsers();
        System.out.println("Registration successful!");
    }

    static void login() {
        System.out.print("Username: ");
        String username = scanner.nextLine();
        System.out.print("Password: ");
        String password = scanner.nextLine();

        User user = users.get(username);
        if (user != null && user.password.equals(password)) {
            currentUser = user;
            currentUser.loadExpensesFromFile();
            System.out.println("Login successful!");
        } else {
            System.out.println("Invalid username or password.");
        }
    }

    static void addExpense() {
        System.out.print("Description: ");
        String desc = scanner.nextLine();
        System.out.print("Amount (in ₹): ");
        double amt = scanner.nextDouble();
        scanner.nextLine();

        currentUser.expenses.add(new Expense(desc, amt));
        currentUser.saveExpensesToFile();
        System.out.println("Expense added and saved.");
    }

    static void viewExpenses() {
        if (currentUser.expenses.isEmpty()) {
            System.out.println("No expenses recorded.");
        } else {
            System.out.println("Your Expenses:");
            for (int i = 0; i < currentUser.expenses.size(); i++) {
                System.out.println((i + 1) + ". " + currentUser.expenses.get(i));
            }
        }
    }

    static void deleteExpense() {
        viewExpenses();
        if (currentUser.expenses.isEmpty()) return;

        System.out.print("Enter expense number to delete: ");
        int index = scanner.nextInt();
        scanner.nextLine();

        if (index < 1 || index > currentUser.expenses.size()) {
            System.out.println("Invalid selection.");
        } else {
            currentUser.expenses.remove(index - 1);
            currentUser.saveExpensesToFile();
            System.out.println("Expense deleted and saved.");
        }
    }
}
