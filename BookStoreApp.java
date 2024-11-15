package com.company;

import java.awt.*;
import java.io.*;
import java.util.Scanner;
import java.util.ArrayList;

abstract class Item {
    protected String name;
    protected String author;
    protected int availablequantity;
    protected int price;

    public Item(String name, String author, int availablequantity, int price) {
        this.name = name;
        this.author = author;
        this.availablequantity = availablequantity;
        this.price = price;
    }

    public abstract void displayItemDetails();

    public abstract int calculatePrice(int quantity);
}

class Book extends Item {
    public Book(String name, String author, int availablequantity, int price) {
        super(name, author, availablequantity, price);
    }

    @Override
    public void displayItemDetails() {
        System.out.println("Book Name: " + name);
        System.out.println("Author: " + author);
        System.out.println("Quantity Available: " + availablequantity);
        System.out.println("Price per Book: " + price);
    }

    @Override
    public int calculatePrice(int quantity) {
        return this.price * quantity;
    }
}
class InventoryManager {
    public static ArrayList<Book> loadInventory() throws FileNotFoundException {
        ArrayList<Book> books = new ArrayList<>();

        try {
            File bookFile = new File("BooksName.txt");
            File authorFile = new File("AuthorsName.txt");
            File quantityFile = new File("Quantity.txt");
            File priceFile = new File("Price.txt");

            // Check if the files exist before attempting to read
            if (!bookFile.exists() || !authorFile.exists() || !quantityFile.exists() || !priceFile.exists()) {
                throw new FileNotFoundException("One or more required inventory files are missing.");
            }

            Scanner bookScanner = new Scanner(bookFile);
            Scanner authorScanner = new Scanner(authorFile);
            Scanner quantityScanner = new Scanner(quantityFile);
            Scanner priceScanner = new Scanner(priceFile);

            while (bookScanner.hasNextLine() && authorScanner.hasNextLine() &&
                    quantityScanner.hasNextLine() && priceScanner.hasNextLine()) {

                String name = bookScanner.nextLine();
                String author = authorScanner.nextLine();
                int availablequantity = Integer.parseInt(quantityScanner.nextLine());
                int price = Integer.parseInt(priceScanner.nextLine());

                // Ensure quantity and price are positive numbers
                if (availablequantity < 0 || price < 0) {
                    throw new IllegalArgumentException("Quantity and price must be non-negative.");
                }

                Book book = new Book(name, author, availablequantity, price);
                books.add(book);
            }

            bookScanner.close();
            authorScanner.close();
            quantityScanner.close();
            priceScanner.close();

        } catch (IOException | IllegalArgumentException e) {
            System.out.println("Error loading inventory: " + e.getMessage());
            throw new RuntimeException("Failed to load inventory", e);
        }

        return books;
    }
}

class Transaction {
    private static final Scanner in = new Scanner(System.in);

    public static void initiatePurchase(ArrayList<Book> Books){
        System.out.print("Enter the Name of the Book you want to buy: ");
        String selectedBookName = in.nextLine();

        // Find the book by name
        Book selectedBook = null;
        for (Book book : Books) {
            if (book.name.equalsIgnoreCase(selectedBookName)) {
                selectedBook = book;
                break;
            }
        }

        if (selectedBook != null) {
            // Process the purchase if book found
            Transaction.processPurchase(selectedBook);
        } else {
            System.out.println("Sorry, the book you requested is not available.");
        }
    }
    public static void processPurchase(Book book) {
        System.out.print("Enter the Number of Copies you want to buy: ");
        int quantity = in.nextInt();

        if (quantity > book.availablequantity) {
            System.out.println("Sorry, only " + book.availablequantity + " copies are available.");
        } else {
            int totalPrice = book.calculatePrice(quantity);
            System.out.println("Total Amount to be paid: " + totalPrice);
//            completeTransaction(book, quantity, totalPrice);
            System.out.println("Would you like to proceed with the transaction? (yes/no): ");
            String answer = in.next();

            if ("yes".equalsIgnoreCase(answer)) {
                System.out.print("Enter your name: ");
                String customerName = in.next();
                System.out.print("Enter your mobile number: ");
                long phone = in.nextLong();

                System.out.println("Transaction Successful. Generating Receipt...");
                generateReceipt(customerName, phone, book, quantity, totalPrice);
                updateInventory(book, quantity);
            }
        }
    }

    private static void generateReceipt(String customerName, long phone, Book book, int quantity, int totalPrice) {
        try {
            File receipt = new File("Receipt.txt");
            if (receipt.createNewFile()) {
                FileWriter writer = new FileWriter(receipt);
                writer.write("----- Book Store Receipt -----\n");
                writer.write("Name: " + customerName + "\n");
                writer.write("Mobile Number: " + phone + "\n");
                writer.write("Book: " + book.name + "\n");
                writer.write("Author: " + book.author + "\n");
                writer.write("Quantity: " + quantity + "\n");
                writer.write("Total Price: " + totalPrice + "\n");
                writer.close();
                Desktop.getDesktop().open(receipt);

                receipt.deleteOnExit();

            }
        } catch (IOException e) {
            System.out.println("Error generating receipt: " + e.getMessage());
        }
    }

    private static void updateInventory(Book book, int quantitySold) {
        // Update the inventory and write back to the text files
        book.availablequantity -= quantitySold; // Update book's quantity

        try {
            // Read the existing inventory data
            ArrayList<String> books = readFile("BooksName.txt");
            ArrayList<String> authors = readFile("AuthorsName.txt");
            ArrayList<String> quantities = readFile("Quantity.txt");
            ArrayList<String> prices = readFile("Price.txt");

            // Find the index of the book that was purchased
            int index = books.indexOf(book.name);
            if (index != -1) {
                quantities.set(index, String.valueOf(book.availablequantity));
                writeToFile("Quantity.txt", quantities);

                // Additional handling if you want to update other files (like prices, etc.)
            }

        } catch (IOException e) {
            System.out.println("Error updating inventory: " + e.getMessage());
        }
    }

    private static ArrayList<String> readFile(String fileName) throws IOException {
        ArrayList<String> lines = new ArrayList<>();
        try (Scanner scanner = new Scanner(new File(fileName))) {
            while (scanner.hasNextLine()) {
                lines.add(scanner.nextLine());
            }
        }
        return lines;
    }

    private static void writeToFile(String fileName, ArrayList<String> data) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
            for (String line : data) {
                writer.write(line);
                writer.newLine();
            }
        }
    }
}


public class BookStoreApp {
    public static void main(String[] args) {
        try {
            Scanner scanner = new Scanner(System.in);

            ArrayList<Book> books = InventoryManager.loadInventory();

            System.out.println("Welcome to the Book Store");
            System.out.println("Do you want to see the catalogue:");
            String ans = scanner.nextLine();
            if( ans.equalsIgnoreCase("yes")) {
                System.out.println("Available Books:");

                // Display all books in inventory
                for (Book book : books) {
                    book.displayItemDetails();
                    System.out.println("--------------------------");
                }
            }

            System.out.println("Do you want to buy any booke: ");
            ans = scanner.nextLine();

            if( ans.equalsIgnoreCase("yes")) {
                // Ask user to select a book to purchase
                Transaction.initiatePurchase(books);
            }
            else{
                System.out.println("Thankyou for visiting");
            }

        } catch (Exception e) {
            System.out.println("An error occurred: " + e.getMessage());
        }
    }
}
