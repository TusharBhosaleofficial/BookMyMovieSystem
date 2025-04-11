package service;

import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class BookMyMovieApp {

    public static void main(String[] args) {
        BookMyMovieSys mbs = new BookMyMovieSys(); // Backend system
        Scanner sc = new Scanner(System.in);
        boolean exit = false;

        System.out.println("=====================================");
        System.out.println("      Welcome to BookMyMovie         ");
        System.out.println("=====================================");

        while (!exit) {
            printMainMenu();
            System.out.print("Enter your choice: ");
            String choice = sc.nextLine();

            switch (choice) {
                case "1":
                    bookMovie(sc, mbs);
                    break;

                case "2":
                    cancelTicket(sc, mbs);
                    break;

                case "3":
                    viewShows(sc, mbs);
                    break;

                case "4":
                    System.out.println("Thank you for using BookMyMovieApp. Goodbye!");
                    exit = true;
                    break;

                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }

        sc.close();
    }


    private static void printMainMenu() {
        System.out.println("\n========= Main Menu =========");
        System.out.println("1. Book a Movie");
        System.out.println("2. Cancel a Ticket");
        System.out.println("3. View Shows");
        System.out.println("4. Exit");
        System.out.println("=============================");
    }

    private static void bookMovie(Scanner sc, BookMyMovieSys mbs) {
        try {
            String name, email, phone;

            while (true) {
                System.out.print("Enter name: ");
                name = sc.nextLine();
                if (isValidName(name)) break;
                System.out.println("Invalid name. Only letters and spaces allowed (min 2 chars).");
            }

            while (true) {
                System.out.print("Enter phone: ");
                phone = sc.nextLine();
                if (isValidPhone(phone)) break;
                System.out.println("Invalid phone. Must be a 10-digit number starting with 6–9.");
            }

            while (true) {
                System.out.print("Enter email: ");
                email = sc.nextLine();
                if (isValidEmail(email)) break;
                System.out.println("Invalid email format. Try again (e.g., abc@example.com).");
            }

            int userId=mbs.customerDetails(name,phone,email);

            String city;
            boolean validCity = true;
            do {
                System.out.print("\nEnter City: ");
                city = sc.nextLine();
                validCity = mbs.displayTheaters(city);
                if(!validCity){
                    System.out.print("No theaters found. Would you like to enter another city? (yes/no): ");
                    String choice=sc.nextLine();
                    if(choice.equalsIgnoreCase("no")){
                        System.out.println("Returning to main menu.");
                        return;
                    }
                }
            }while (!validCity);

            int theaterId;
            boolean hasMovies = false;

            do {
                System.out.print("Enter Theater ID: ");
                theaterId = sc.nextInt();
                sc.nextLine(); // clear buffer

                hasMovies = mbs.displayMoviesByTheater(theaterId);

                if (!hasMovies) {
                    System.out.print("Would you like to choose another theater? (yes/no): ");
                    String retry = sc.nextLine();
                    if (retry.equalsIgnoreCase("no")) {
                        System.out.println("Returning to main menu.");
                        return;
                    }
                }
            } while (!hasMovies);


            System.out.print("Enter Movie ID: ");
            int movieId = sc.nextInt();

            boolean showsAvailable = mbs.displayShows(movieId, theaterId);
            sc.nextLine(); // consume newline

            if (!showsAvailable) {
                System.out.println("No shows available. Returning to menu.");
                return;
            }

            System.out.print("Enter Show ID: ");
            int showId = sc.nextInt();


            // Show layout before seat selection
            mbs.displaySeatLayout(showId);

            handleSeatSelectionAndBooking(sc, mbs, userId, showId);



        } catch (Exception e) {
            System.out.println("Error during booking: " + e.getMessage());
            sc.nextLine(); // clear buffer
        }
    }
    private static void handleSeatSelectionAndBooking(Scanner sc, BookMyMovieSys mbs, int userId, int showId) {
        sc.nextLine(); // Clean up newline left by previous nextInt()
        while (true) {
            System.out.print("Enter number of seats: ");
            int numSeats = 0;
            try {
                numSeats = Integer.parseInt(sc.nextLine());
                if (numSeats <= 0) {
                    System.out.println("Number of seats must be greater than 0.");
                    continue;
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number (e.g., 1, 2, 3).");
                continue;
            }

            System.out.println("Enter seat numbers (e.g., A1, B2):");
            List<String> seats = getSeatNumbers(sc, numSeats);

            boolean success = mbs.bookTicket(userId, showId, seats);
            if (success) {
                System.out.println("Booking confirmed.");
                break;
            } else {
                System.out.print("Would you like to try selecting seats again? (yes/no): ");
                String retry = sc.nextLine();
                if (!retry.equalsIgnoreCase("yes")) {
                    System.out.println("Returning to main menu.");
                    break;
                }
            }
        }
    }

    private static boolean isValidName(String name) {
        return name.trim().matches("^[a-zA-Z\\s]{2,}$");
    }

    private static boolean isValidEmail(String email) {
        return email.matches("^[\\w.-]+@[\\w.-]+\\.\\w{2,}$");
    }

    private static boolean isValidPhone(String phone) {
        return phone.matches("^[6-9]\\d{9}$");
    }


    private static void cancelTicket(Scanner sc, BookMyMovieSys mbs) {
        try {
            System.out.print("\nEnter Booking ID to cancel: ");
            int BookingId = sc.nextInt();
            System.out.print("\nEnter user ID to cancel: ");
            int userId = sc.nextInt();
            sc.nextLine(); // consume newline

            boolean cancelled = mbs.cancelBooking(BookingId,userId);

            if (cancelled) {
                System.out.println("Ticket cancelled successfully.");
            } else {
                System.out.println("Could not cancel the ticket. Check the ID.");
            }

        } catch (Exception e) {
            System.out.println("Error while cancelling ticket: " + e.getMessage());
            sc.nextLine(); // clear buffer
        }
    }

    private static void viewShows(Scanner sc, BookMyMovieSys mbs) {
        try {
            System.out.print("\nEnter City: ");
            String city = sc.nextLine();
            mbs.displayTheaters(city);

            System.out.print("Enter Theater ID: ");
            int theaterId = sc.nextInt();

            System.out.print("Enter Movie ID: ");
            int movieId = sc.nextInt();
            sc.nextLine(); // consume newline

            mbs.displayShows(movieId, theaterId);

        } catch (Exception e) {
            System.out.println("Error while viewing shows: " + e.getMessage());
            sc.nextLine(); // clear buffer
        }
    }

    private static List<String> getSeatNumbers(Scanner sc, int numSeats) {
        String[] seatArray = new String[numSeats];
        for (int i = 0; i < numSeats; i++) {
            System.out.print("Enter seat " + (i + 1) + ": ");
            seatArray[i] = sc.nextLine();
        }
        return Arrays.asList(seatArray);
    }
}
