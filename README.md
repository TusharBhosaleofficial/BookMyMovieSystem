# 🎬 BookMyMovie System

A Java console-based Movie Ticket Booking System that allows users to search theaters by city, view available movies and shows, book tickets with seat selection and dynamic pricing, and cancel existing bookings.

---

## 🧰 Technologies Used

- Java (Core)
- JDBC (MySQL integration)
- MySQL Database
- SQL Stored Procedures
- Git & GitHub

---

## 🚀 Features

✅ User input validation (name, email, phone)  
✅ View theaters by city  
✅ View movies and shows by theater  
✅ Seat layout with types (SILVER, GOLD, VIP)  
✅ Dynamic seat pricing  
✅ Prevent double booking  
✅ Retry booking logic if seats already booked  
✅ Ticket cancellation with seat availability restoration  
✅ Console-friendly UI & Error handling

---

## 🗃️ Database Schema

### `users`
- `user_id`, `name`, `email`, `phone`

### `movies`
- `movie_id`, `title`, `genre`, `lang`, `duration`

### `theaters`
- `theater_id`, `name`, `city`

### `shows`
- `show_id`, `movie_id`, `theater_id`, `timing`, `available_seats`

### `seat`
- `seat_id`, `show_id`, `seat_number`, `is_booked`, `seat_type`, `price`

### `bookings`
- `booking_id`, `user_id`, `show_id`, `seats_booked`, `total_price`

---

## 🧑‍💻 How to Run

1. Clone the repo  
   ```bash
   git clone https://github.com/your-username/BookMyMovieSystem.git
   cd BookMyMovieSystem
