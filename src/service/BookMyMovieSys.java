package service;

import config.DataBaseConfig;

import java.sql.*;
import java.util.List;
import java.util.Scanner;

public class BookMyMovieSys {
    Scanner sc = new Scanner(System.in);
    //display movies
    public boolean displayMoviesByTheater(int theaterId){
        try{
            Connection con = DataBaseConfig.getConnection();
            PreparedStatement stmt = con.prepareStatement("SELECT DISTINCT m.movie_id, m.title, m.genre, m.lang " +
                    "FROM movies m JOIN shows s ON m.movie_id = s.movie_id " +
                    "WHERE s.theater_id = ?");
            stmt.setInt(1,theaterId);
            ResultSet rs = stmt.executeQuery();
            boolean hasMovies = false;
            System.out.println("-------- Movies Playing at Selected Theater --------");
            while(rs.next()){
                hasMovies = true;
                System.out.println(rs.getInt("movie_id")
                        +". "+rs.getString("title")
                        +" ("+rs.getString("genre")+", "+rs.getString("lang") +")");
            }
            if(!hasMovies){
                System.out.println("No movies available for this theater.");
            }
            return hasMovies;
        }catch (SQLException e){
            e.printStackTrace();
            return false;
        }
    }

    public boolean displayTheaters(String city) {
        try {
            Connection con = DataBaseConfig.getConnection();
            PreparedStatement stmt = con.prepareStatement(
                    "SELECT * FROM theaters WHERE city = ?"
            );
            stmt.setString(1, city);
            ResultSet rs = stmt.executeQuery();

            boolean hasTheaters = false;
            System.out.println("\nTheaters in " + city + ":");
            while (rs.next()) {
                hasTheaters = true;
                System.out.println(rs.getInt("theater_id") + ". " + rs.getString("name"));
            }

            if (!hasTheaters) {
                System.out.println("No theaters found in this city.");
            }

            return hasTheaters;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }


    public boolean displayShows(int movieId, int theaterId) {
        try (Connection con = DataBaseConfig.getConnection();
             PreparedStatement stmt = con.prepareStatement("SELECT * FROM shows WHERE movie_id=? AND theater_id=?")) {

            stmt.setInt(1, movieId);
            stmt.setInt(2, theaterId);

            try (ResultSet rs = stmt.executeQuery()) {
                boolean showsAvailable = false;
                System.out.println("Available shows for Movie ID " + movieId + " at Theater ID " + theaterId + ":");

                // Check if any shows are available
                while (rs.next()) {
                    System.out.println(rs.getInt("show_id") + ". " + rs.getString("timing") +
                            " - Seats Available: " + rs.getString("available_seats"));
                    showsAvailable = true;
                }

                if (!showsAvailable) {
                    System.out.println("No shows available for this movie at this theater.");
                }

                return showsAvailable;

            }
        } catch (SQLException e) {
            System.out.println("Error displaying shows. Please try again.");
            e.printStackTrace();
            return false;
        }
    }

    public void displaySeatLayout(int showId) {
        try (Connection con = DataBaseConfig.getConnection()) {
            PreparedStatement stmt = con.prepareStatement(
                    "SELECT seat_number, is_booked, seat_type, price FROM seat WHERE show_id = ? ORDER BY seat_number"
            );
            stmt.setInt(1, showId);
            ResultSet rs = stmt.executeQuery();

            System.out.println("\n-------- Seat Layout --------");
            int count = 0;
            while (rs.next()) {
                String seat = rs.getString("seat_number");
                boolean booked = rs.getBoolean("is_booked");
                String type = rs.getString("seat_type");
                int price = rs.getInt("price");

                String status = booked ? "[X]" : "[ ]"; // Booked = X
                System.out.printf("%s %-3s (%s ₹%d)  ", status, seat, type, price);

                count++;
                if (count % 6 == 0) System.out.println(); // new line after 6 seats
            }
            System.out.println("\n[X] = Booked  |  [ ] = Available");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    // CUSTOMER DETAILS
    public int customerDetails(String name, String phone, String email){
        int userId=0;
        try{
            Connection con = DataBaseConfig.getConnection();
            PreparedStatement stmt = con.prepareStatement("insert into users (name,email,phone) values (?,?,?)");
            stmt.setString(1,name);
            stmt.setString(2,email);
            stmt.setString(3,phone);
            stmt.executeUpdate();

            Statement  st = con.createStatement();
            ResultSet set=st.executeQuery("select user_id from users ORDER BY user_id DESC limit 1");
            while (set.next()){
                userId = set.getInt("user_id");
                System.out.println("Your user_id is "+set.getInt("user_id"));
            }
        }catch(SQLException e){
            e.printStackTrace();
        }
        return userId;
    }

    // Book Ticket
    public boolean bookTicket(int userId, int showId, List<String> selectedSeats) {
        try {
            Connection con = DataBaseConfig.getConnection();
            con.setAutoCommit(false);
            boolean alreadyBookedSeat = false;

            // Check if seats are already booked
            for (String seat : selectedSeats) {
                PreparedStatement stmt = con.prepareStatement(
                        "SELECT is_booked FROM seat WHERE seat_number = ? AND show_id = ?"
                );
                stmt.setString(1, seat);
                stmt.setInt(2, showId);
                ResultSet rs = stmt.executeQuery();

                if (rs.next() && rs.getBoolean("is_booked")) {
                    alreadyBookedSeat = true;
                    System.out.println("Seat " + seat + " is already booked. Choose another seat.");
                }
            }

            if (alreadyBookedSeat) {
                System.out.println("Booking failed! Some seats are already booked.");
                con.rollback();
                return false; //  Return failure
            }

            // Booking logic...
            // (Update seats, calculate price, insert booking)
            double totalPrice = 0;
            for (String seat : selectedSeats) {
                PreparedStatement stmt = con.prepareStatement(
                        "SELECT price FROM seat WHERE seat_number = ? AND show_id = ?"
                );
                stmt.setString(1, seat);
                stmt.setInt(2, showId);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    totalPrice += rs.getDouble("price");
                }
            }

            con.commit();
            System.out.println("Booking Successful, Seats " + selectedSeats + " | Total Price ₹" +  totalPrice );
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }


    public boolean cancelBooking(int bookingId, int userId) {
        Connection con = null;
        PreparedStatement stmt = null;

        try {
            con = DataBaseConfig.getConnection();
            con.setAutoCommit(false);

            // Step 1: Get seat list and show ID
            PreparedStatement getBooking = con.prepareStatement(
                    "SELECT seats_booked, show_id FROM bookings WHERE booking_id = ? AND user_id = ?"
            );
            getBooking.setInt(1, bookingId);
            getBooking.setInt(2, userId);
            ResultSet rs = getBooking.executeQuery();

            if (!rs.next()) {
                System.out.println("No booking found with the given details.");
                con.rollback();
                return false;
            }

            String seatListStr = rs.getString("seats_booked");
            int showId = rs.getInt("show_id");
            String[] seatList = seatListStr.split(",");

            // Step 2: Unbook each seat
            for (String seat : seatList) {
                PreparedStatement updateSeat = con.prepareStatement(
                        "UPDATE seat SET is_booked = FALSE WHERE seat_number = ? AND show_id = ?"
                );
                updateSeat.setString(1, seat.trim());
                updateSeat.setInt(2, showId);
                updateSeat.executeUpdate();
            }

            // Step 3: Increase available seats in shows table
            PreparedStatement updateShow = con.prepareStatement(
                    "UPDATE shows SET available_seats = available_seats + ? WHERE show_id = ?"
            );
            updateShow.setInt(1, seatList.length);
            updateShow.setInt(2, showId);
            updateShow.executeUpdate();

            // Step 4: Delete the booking
            stmt = con.prepareStatement("DELETE FROM bookings WHERE booking_id = ? AND user_id = ?");
            stmt.setInt(1, bookingId);
            stmt.setInt(2, userId);
            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected == 0) {
                con.rollback();
                return false;
            }

            con.commit();
            System.out.println("Booking cancelled. Seats restored: " + String.join(", ", seatList));
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            try {
                if (con != null) con.rollback();
            } catch (SQLException rollbackEx) {
                rollbackEx.printStackTrace();
            }
            return false;

        } finally {
            try {
                if (stmt != null) stmt.close();
                if (con != null) con.setAutoCommit(true);
                if (con != null) con.close();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }
}
