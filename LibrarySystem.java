import java.io.*;
import java.time.LocalDate;
import java.util.*;

public class LibrarySystem {
    private static final String USERS_FILE = "users.txt";
    private static final String BOOKS_FILE = "books.txt";
    private static final String TRANSACTIONS_FILE = "transactions.txt";

    static class Book {
        String id, title, author;
        boolean available;

        Book(String id, String title, String author, boolean available) {
            this.id = id;
            this.title = title;
            this.author = author;
            this.available = available;
        }
    }

    static class User {
        String username, password, role;

        User(String username, String password, String role) {
            this.username = username;
            this.password = password;
            this.role = role;
        }
    }

    static class Transaction {
        String transactionId, username, bookId, dateBorrowed, dateReturned;

        Transaction(String transactionId, String username, String bookId, String dateBorrowed, String dateReturned) {
            this.transactionId = transactionId;
            this.username = username;
            this.bookId = bookId;
            this.dateBorrowed = dateBorrowed;
            this.dateReturned = dateReturned;
        }
    }

    private static List<Book> books = new ArrayList<>();
    private static List<User> users = new ArrayList<>();
    private static List<Transaction> transactions = new ArrayList<>();

    public static void main(String[] args) {
        loadUsers();
        loadBooks();
        loadTransactions();
        System.out.println("----------------------------------------");
        System.out.println("Please log in to continue.\n");

        Scanner sc = new Scanner(System.in);
        User loggedInUser = login(sc);

        if (loggedInUser != null) {
            System.out.println("\nLogin successful! Welcome, " + loggedInUser.username + ".");
            mainMenu(sc, loggedInUser);
           } else {
            System.out.println("Too many failed attempts. Exiting system...");
        }
    }

    private static void loadUsers() {
        try (BufferedReader br = new BufferedReader(new FileReader(USERS_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] data = line.split(",");
                if (data.length >= 3) {
                    users.add(new User(data[0].trim(), data[1].trim(), data[2].trim()));
                }
            }
        } catch (IOException e) {
            System.out.println(" users.txt not found. Creating default user...");
            createDefaultUser();
        }
    }

    private static void createDefaultUser() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(USERS_FILE))) {
            pw.println("ajcarsula,12345,user");
            users.add(new User("ajcarsula", "12345", "user"));
        } catch (IOException e) {
            System.out.println(" Error creating users.txt: " + e.getMessage());
        }
    }

    private static void loadBooks() {
        try (BufferedReader br = new BufferedReader(new FileReader(BOOKS_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] data = line.split(",");
                if (data.length >= 4) {
                    books.add(new Book(data[0].trim(), data[1].trim(), data[2].trim(),
                            Boolean.parseBoolean(data[3].trim())));
                }
            }
        } catch (IOException e) {
            System.out.println(" books.txt not found. Creating default books...");
            createDefaultBooks();
        }
    }

    private static void createDefaultBooks() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(BOOKS_FILE))) {
            pw.println("B001,The Great Gatsby,F. Scott Fitzgerald,true");
            pw.println("B002,To Kill a Mockingbird,Harper Lee,true");
            pw.println("B003,1984,George Orwell,false");
            books.add(new Book("B001", "The Great Gatsby", "F. Scott Fitzgerald", true));
            books.add(new Book("B002", "To Kill a Mockingbird", "Harper Lee", true));
            books.add(new Book("B003", "1984", "George Orwell", false));
        } catch (IOException e) {
            System.out.println(" Error creating books.txt: " + e.getMessage());
        }
    }

    private static void loadTransactions() {
        try (BufferedReader br = new BufferedReader(new FileReader(TRANSACTIONS_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] data = line.split(",");
                if (data.length >= 5) {
                    transactions.add(new Transaction(
                            data[0].trim(), data[1].trim(), data[2].trim(),
                            data[3].trim(), data[4].trim().equals("null") ? null : data[4].trim()));
                }
            }
        } catch (IOException e) {
            System.out.println(" transactions.txt not found. Creating new file...");
            createTransactionsFile();
        }
    }

    private static void createTransactionsFile() {
        try {
            new File(TRANSACTIONS_FILE).createNewFile();
        } catch (IOException e) {
            System.out.println(" Error creating transactions.txt: " + e.getMessage());
        }
    }


    private static User login(Scanner sc) {
        int attempts = 3;
        while (attempts > 0) {
            System.out.print("Username: ");
            String username = sc.nextLine();
            System.out.print("Password: ");
            String password = sc.nextLine();

            for (User u : users) {
                if (u.username.equalsIgnoreCase(username) && u.password.equals(password)) {
                    return u;
                }
            }

            attempts--;
            System.out.println("Invalid username or password. Try again.");
            if (attempts > 0) {
                System.out.println("(Attempts left: " + attempts + ")");
            }
            System.out.println();
        }
        return null;
    }


    private static void mainMenu(Scanner sc, User user) {
        int choice;
        do {
            System.out.println("\n1. View All Books");
            System.out.println("2. Borrow Book");
            System.out.println("3. Return Book");
            System.out.println("4. Search Book");
            System.out.println("5. Exit");
            System.out.print("Enter choice: ");
            choice = sc.nextInt();
            sc.nextLine(); // clear input

            switch (choice) {
                case 1 -> viewBooks();
                case 2 -> borrowBook(sc, user);
                case 3 -> returnBook(sc, user);
                case 4 -> searchBook(sc);
                case 5 -> System.out.println("\n----------------------------------------\nExiting system...");
                default -> System.out.println("Invalid choice!");
            }
        } while (choice != 5);
    }

    private static void viewBooks() {
        System.out.println("\n----------------------------------------");
        System.out.println("Available Books:\n");
        for (Book b : books) {
            System.out.printf("%s - %s by %s (%s)%n",
                    b.id, b.title, b.author, b.available ? "Available" : "Borrowed");
        }
        System.out.println("----------------------------------------");
    }

    private static void borrowBook(Scanner sc, User user) {
        long borrowedCount = transactions.stream()
                .filter(t -> t.username.equals(user.username) && t.dateReturned == null)
                .count();

        if (borrowedCount >= 3) {
            System.out.println(" You cannot borrow more than 3 books at once!");
            return;
        }

        System.out.print("\nEnter Book ID: ");
        String id = sc.nextLine();

        for (Book b : books) {
            if (b.id.equalsIgnoreCase(id)) {
                if (b.available) {
                    b.available = false;
                    saveBooks();
                    recordTransaction(user.username, b.id, "borrow");
                    System.out.println("Book borrowed successfully!");
                    System.out.println("\n----------------------------------------");
                    System.out.println("----------------- MAIN MENU -----------------");
                    return;
                } else {
                    System.out.println("Sorry, this book is currently unavailable.");
                    return;
                }
            }
        }
        System.out.println("Book not found!");
    }

    private static void returnBook(Scanner sc, User user) {
        System.out.print("\nEnter Book ID to return: ");
        String id = sc.nextLine();

        for (Book b : books) {
            if (b.id.equalsIgnoreCase(id)) {
                if (!b.available) {
                    b.available = true;
                    saveBooks();
                    recordTransaction(user.username, b.id, "return");
                    System.out.println("Book returned successfully!");
                    System.out.println("\n----------------------------------------");
                    System.out.println("----------------- MAIN MENU -----------------");
                    return;
                } else {
                    System.out.println("This book is already available.");
                    return;
                }
            }
        }
        System.out.println("Book not found!");
    }

    private static void searchBook(Scanner sc) {
        System.out.print("\nSearch by (title/author): ");
        String keyword = sc.nextLine().toLowerCase();
        boolean found = false;

        System.out.println("\nSearch Results:");
        for (Book b : books) {
            if (b.title.toLowerCase().contains(keyword) || b.author.toLowerCase().contains(keyword)) {
                System.out.printf("%s - %s by %s (%s)%n",
                        b.id, b.title, b.author, b.available ? "Available" : "Borrowed");
                found = true;
            }
        }

        if (!found) {
            System.out.println("No books found with that title or author.");
        }
    }

    private static void saveBooks() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(BOOKS_FILE))) {
            for (Book b : books) {
                pw.println(b.id + "," + b.title + "," + b.author + "," + b.available);
            }
        } catch (IOException e) {
            System.out.println(" Error saving books: " + e.getMessage());
        }
    }

    private static void recordTransaction(String username, String bookId, String action) {
        String transactionId = generateTransactionId();
        String date = LocalDate.now().toString();
        Transaction t;

        if (action.equals("borrow")) {
            t = new Transaction(transactionId, username, bookId, date, null);
        } else {
            t = new Transaction(transactionId, username, bookId, null, date);
        }

        transactions.add(t);

        try (PrintWriter pw = new PrintWriter(new FileWriter(TRANSACTIONS_FILE, true))) {
            pw.println(t.transactionId + "," + t.username + "," + t.bookId + "," +
                    (t.dateBorrowed == null ? "null" : t.dateBorrowed) + "," +
                    (t.dateReturned == null ? "null" : t.dateReturned));
        } catch (IOException e) {
            System.out.println(" Error recording transaction: " + e.getMessage());
        }
    }

    private static String generateTransactionId() {
        int nextId = transactions.size() + 1;
        return String.format("T%03d", nextId);
    }
}
