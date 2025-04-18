package com.example.movie_booking_system.service;

import com.example.movie_booking_system.KafkaConsumer.ActionResultConsumer;
import com.example.movie_booking_system.dto.BookingResponseDTO;
import com.example.movie_booking_system.models.Users;
import com.example.movie_booking_system.models.*;
import com.example.movie_booking_system.repository.*;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Service
public class BookingService {

    private static final Logger logger = Logger.getLogger(BookingService.class.getName());


    @Autowired
    private AuctionWinnerRepository auctionWinnerRepository;
    @Autowired
    private BookingRepository bookingRepository;
    @Autowired
    private TheatreRepository theatreRepository;

    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    EmailSenderService emailSenderService;
    @Autowired
    private AuctionRepository auctionRepository;

    @Autowired
    private SeatsRepository seatsRepository;
//    seat mai bss avialable hai ke nahi woh dikhani hai
//    and the fact that woh showtime specific hai so no worries woh either available hogi ya fir nahi

    @Autowired
    private UserRepository userRepository;
//    everything related to user is being handled by om

    @Autowired
    private ShowTimeRepository showTimeRepository;
    @Autowired
    private WebSocketService webSocketService;

//    @Autowired
//    private PaymentService paymentService; // Inject Payment Service

    public Long getSeatId(Long seatNumber, Long showtimeId) {
        return seatsRepository.findSeatIdBySeatNumberAndShowtime(seatNumber, showtimeId)
                .orElseThrow(() -> new RuntimeException("Seat not found for seatNumber: "
                        + seatNumber + " and showtimeId: " + showtimeId));
    }


    //    need to handle the runtime exception here
    @Transactional
    public boolean booking_Movie(Long user_id, List<Long> seatId,Long movie_id) throws MessagingException {

        for(Long seat: seatId){
            Seats seats = seatsRepository.findById(seat)
                    .orElseThrow(() -> new RuntimeException("Seat not found with ID: " + seatId));
            if (!seats.getSeatAvailable()) {
                throw new RuntimeException("Seat is already booked!");
            }
            ShowTime showtime = showTimeRepository.findById(seats.getShowtime().getId()).orElseThrow(() -> new RuntimeException("Showtime not found with ID: " + seats.getShowtime().getId()));


            // Mark seat as booked
            seats.setSeatAvailable(false);
            seatsRepository.save(seats);

//            idhar tak ho gaya hai jo hona hai sare unavailable

        }

//        Users user = userRepository.findById(user_id)
//                .orElseThrow(() -> new RuntimeException("User not found with ID: " + user_id));
//



        Users user = userRepository.findById(user_id)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + user_id));

        String email = user.getEmail();
        String name = user.getName();

        Seats seat = seatsRepository.findById(seatId.get(0)).orElseThrow(() -> new RuntimeException("Seat not found with ID: " + seatId.get(0)));
        ShowTime showtime = showTimeRepository.findById(seat.getShowtime().getId()).orElseThrow(() -> new RuntimeException("Showtime not found with ID: " + seat.getShowtime().getId()));
        String showTime = showtime.getTime();
        Theatre theatre = theatreRepository.findById(showtime.getTheatre().getId()).orElseThrow(() -> new RuntimeException("Theatre not found"));
        String theatre_name = theatre.getName();

        Movie movie = movieRepository.findById(movie_id).orElseThrow(() -> new RuntimeException("Movie not found with ID: " + movie_id));
        String movie_name = movie.getTitle();
        LocalDate currentDate = LocalDate.now();

        // Convert the date to a string using the default format (ISO-8601)
        String dateString = currentDate.toString();

        StringBuilder seatDetails = new StringBuilder();
        for (Long seatIds : seatId) {
            Seats s = seatsRepository.findById(seatIds)
                    .orElseThrow(() -> new RuntimeException("Seat not found with ID: " + seatIds));
            seatDetails.append("Row: ").append(s.getSeatRow()).append(", Number: ").append(s.getSeatNumber()).append("; ");
        }

//        booking ki entry banani hai
        Booking booking =new Booking();
        booking.setUser(user);
        booking.setAmount(showtime.getPrice());
        booking.setBooking_date(LocalDateTime.now());
        booking.setSeatIds(seatDetails.toString());
        booking.setShowtime(showtime);
        booking.setUser(user);
        booking.setMovie(movie);

        bookingRepository.save(booking);


//        send the mail only it is done !!!
        triggerMail(email, name, theatre_name, movie_name, dateString, showTime, seatDetails.toString());


//        ShowTime showtime = showTimeRepository.findById(seats.getShowtime().getId()).orElseThrow(() -> new RuntimeException("Showtime not found with ID: " + seat.getShowtime().getId()));


        // Mark seat as booked


//        idhar se hi cheezo ko call kar dena hai

        // Create and save booking
//        Booking booking = new Booking();
//        booking.setBooking_date(LocalDateTime.now());
//        booking.setUser(user);
//        booking.setSeat(seat);
//        booking.setAmount(showtime.getPrice());
//        booking.setShowtime(showtime);

//        return bookingRepository.save(booking);

        return true;
    }

    public BookingResponseDTO get_booking_details(Long user_id, Long booking_id) {

        Booking booking = bookingRepository.findById(booking_id).orElseThrow(() -> new RuntimeException("Booking not found with ID: " + booking_id));//custom exception handling
        if (!Objects.equals(booking.getUser().getId(), user_id)) {
            throw new RuntimeException("User is not authorized to cancel this booking.");
        }

//        isko booking responseDTO mai convert karo chup chap
        BookingResponseDTO bookingResponseDTO = new BookingResponseDTO();
        bookingResponseDTO.setBookingId(booking.getId());
        bookingResponseDTO.setMovieName(booking.getMovie().getTitle());
        bookingResponseDTO.setTheatreName(booking.getShowtime().getTheatre().getName());
        bookingResponseDTO.setSeats(booking.getSeatIds());
        bookingResponseDTO.setShowtime(booking.getShowtime().getTime());
        bookingResponseDTO.setMovieImage(booking.getMovie().getImage());


        return bookingResponseDTO;

    }

    //    here i am using transactional not for concurrency but if for any reason the refund is failed
//    i should keep the booking and not betray the user
//    !TODO-> i need to provide useful responses to frontend such that proper experience is there for the user
    @Transactional
    public Boolean Cancelling_booking_movie(Long user_id, Long booking_id) {
        // Fetch the booking, throw exception if not found
        Booking booking = bookingRepository.findById(booking_id)
                .orElseThrow(() -> new RuntimeException("Booking not found with ID: " + booking_id));

        // Check if the booking belongs to the user
        if (!Objects.equals(booking.getUser().getId(), user_id)) {
            throw new RuntimeException("User is not authorized to cancel this booking.");
        }

        // Parse seat details string to get seat information
        String seatDetailsString = booking.getSeatIds();
        List<Long> seatIds = parseSeatDetails(seatDetailsString);

        // Mark seats as available
        for (Long seatId : seatIds) {
            Seats seat = seatsRepository.findById(seatId)
                    .orElseThrow(() -> new RuntimeException("Seat not found with ID: " + seatId));
            seat.setSeatAvailable(true);
            seatsRepository.save(seat);
        }

        // TODO: Call the payment service for refund process
        boolean refundSuccessful = true;

        if (refundSuccessful) {
            // Remove the booking from the database
            bookingRepository.deleteById(booking_id);
            return true;
        } else {
            throw new RuntimeException("Refund failed, booking is retained.");
        }
    }

    // Helper method to parse seat details string and extract seat IDs
    private List<Long> parseSeatDetails(String seatDetailsString) {
        List<Long> seatIds = new ArrayList<>();

        // Split the seat details string into individual seat entries
        String[] seatEntries = seatDetailsString.split(";");

        for (String seatEntry : seatEntries) {
            // Trim and split each seat entry to extract row and number
            String[] parts = seatEntry.trim().split(",");

            if (parts.length == 2) {
                // Extract row and number (assuming format: "Row: A, Number: 5")
                String rowPart = parts[0].trim();
                String numberPart = parts[1].trim();

                String row = rowPart.split(":")[1].trim();
                String number = numberPart.split(":")[1].trim();

                // Find the seat ID using row and number
                Long seatId = seatsRepository.findSeatIdBySeatRowAndSeatNumber(row, Long.parseLong(number))
                        .orElseThrow(() -> new RuntimeException("Seat not found with Row: " + row + ", Number: " + number));

                seatIds.add(seatId);
            }
        }

        return seatIds;
    }
//    public boolean SuccessfulEmailsending(Long userId, Long movieId, List<Long> seatIds) {
//
//        try {
//            Users user = userRepository.findById(userId)
//                    .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));
//
//            String email = user.getEmail();
//            String name = user.getName();
//
//            Seats seat = seatsRepository.findById(seatIds.get(0)).orElseThrow(() -> new RuntimeException("Seat not found with ID: " + seatIds.get(0)));
//            ShowTime showtime = showTimeRepository.findById(seat.getShowtime().getId()).orElseThrow(() -> new RuntimeException("Showtime not found with ID: " + seat.getShowtime().getId()));
//            String showTime = showtime.getTime();
//            Theatre theatre = theatreRepository.findById(showtime.getTheatre().getId()).orElseThrow(() -> new RuntimeException("Theatre not found"));
//            String theatre_name = theatre.getName();
//
//            Movie movie = movieRepository.findById(movieId).orElseThrow(() -> new RuntimeException("Movie not found with ID: " + movieId));
//            String movie_name = movie.getTitle();
//            LocalDate currentDate = LocalDate.now();
//
//            // Convert the date to a string using the default format (ISO-8601)
//            String dateString = currentDate.toString();
//
//            StringBuilder seatDetails = new StringBuilder();
//            for (Long seatId : seatIds) {
//                Seats s = seatsRepository.findById(seatId)
//                        .orElseThrow(() -> new RuntimeException("Seat not found with ID: " + seatId));
//                seatDetails.append("Row: ").append(s.getSeatRow()).append(", Number: ").append(s.getSeatNumber()).append("; ");
//            }
//
//            triggerMail(email, name, theatre_name, movie_name, dateString, showTime, seatDetails.toString());
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//
//        }
//
//        return true;
//    }

    public void triggerMail(String email, String name, String theatre_name, String movie_name, String Date, String showtime, String Seats) throws MessagingException {
//
//

        emailSenderService.sendBookingConfirmationEmail(
                email,   // Recipient's email
                name,           // UserName
                theatre_name,               // TheaterName
                movie_name,         // MovieName
                Date,                // Date
                showtime,                   // ShowTime
                Seats                   // SeatNumber
        );
    }


    public List<BookingResponseDTO> getAllBookings(Long userId) {
        List<Booking> bookings = bookingRepository.findAllByUserId(userId);
        return bookings.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    private BookingResponseDTO convertToDTO(Booking booking) {
        return new BookingResponseDTO(
                booking.getId(),
                booking.getShowtime().getTheatre().getName(),
                booking.getSeatIds(),
                booking.getShowtime().getTime(),
                booking.getMovie().getTitle(),
                booking.getMovie().getImage());
    }

    public void TransferBooking(Long id, Long userId, Long finalAmount) {

        logger.info("hey i am in transfer booking method");

        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found with ID: " + id));
        // Update the booking details

        booking.setUser(userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found with ID: " + userId)));
        booking.setAmount(finalAmount);
        bookingRepository.save(booking);
        logger.info("Booking transferred successfully to user with ID: " + userId);
//        abhi websocket topic broadcast karna hai jo ki frontend pe bhi reflect hoga
        webSocketService.sendBookingTransferNotification(id);
        logger.info("WebSocket notification sent for booking transfer with ID: " + id);


    }
}
