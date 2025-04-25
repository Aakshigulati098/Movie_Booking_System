package com.example.movie_booking_system.service;

import com.example.movie_booking_system.dto.BookingResponseDTO;
import com.example.movie_booking_system.exceptions.*;
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

@Service
public class BookingService {

    private static final Logger logger = Logger.getLogger(BookingService.class.getName());
    private static final String BOOKING_NOT_FOUND = "Booking not found with ID: ";
    private static final String SEAT_NOT_FOUND = "Seat not found with ID: ";



    private BookingRepository bookingRepository;
    private TheatreRepository theatreRepository;
    private MovieRepository movieRepository;
    private EmailSenderService emailSenderService;

    private SeatsRepository seatsRepository;
    private UserRepository userRepository;
    private ShowTimeRepository showTimeRepository;
    private WebSocketService webSocketService;

    @Autowired
    public BookingService(BookingRepository bookingRepository,
                          TheatreRepository theatreRepository,
                          MovieRepository movieRepository,
                          EmailSenderService emailSenderService,

                          SeatsRepository seatsRepository,
                          UserRepository userRepository,
                          ShowTimeRepository showTimeRepository,
                          WebSocketService webSocketService) {
        this.bookingRepository = bookingRepository;
        this.theatreRepository = theatreRepository;
        this.movieRepository = movieRepository;
        this.emailSenderService = emailSenderService;

        this.seatsRepository = seatsRepository;
        this.userRepository = userRepository;
        this.showTimeRepository = showTimeRepository;
        this.webSocketService = webSocketService;
    }

//    @Autowired
//    private PaymentService paymentService; // Inject Payment Service

    public Long getSeatId(Long seatNumber, Long showtimeId) {
        return seatsRepository.findSeatIdBySeatNumberAndShowtime(seatNumber, showtimeId)
                .orElseThrow(() -> new RuntimeException("Seat not found for seatNumber: "
                        + seatNumber + " and showtimeId: " + showtimeId));
    }


    //    need to handle the runtime exception here
    @Transactional
    public boolean bookingMovie(Long userId, List<Long> seatId, Long movieId) throws MessagingException {

        for(Long seat: seatId){
            Seats seats = seatsRepository.findById(seat)
                    .orElseThrow(() -> new SeatNotFoundException(SEAT_NOT_FOUND + seat));
            if (Boolean.FALSE.equals(seats.getSeatAvailable())) {
                throw new SeatAlreadyBookedException("Seat is already booked!");
            }

            // Mark seat as booked
            seats.setSeatAvailable(false);
            seatsRepository.save(seats);
        }

        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));

        String email = user.getEmail();
        String name = user.getName();

        Seats seat = seatsRepository.findById(seatId.get(0))
                .orElseThrow(() -> new SeatNotFoundException(SEAT_NOT_FOUND + seatId.get(0)));

        ShowTime showtime = showTimeRepository.findById(seat.getShowtime().getId())
                .orElseThrow(() -> new ShowTimeNotFoundException("Showtime not found with ID: " + seat.getShowtime().getId()));

        String showTime = showtime.getTime();

        Theatre theatre = theatreRepository.findById(showtime.getTheatre().getId())
                .orElseThrow(() -> new TheatreNotFoundException("Theatre not found"));

        String theatreName = theatre.getName();

        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new MovieNotFoundException("Movie not found with ID: " + movieId));

        String movieName = movie.getTitle();
        LocalDate currentDate = LocalDate.now();

        // Convert the date to a string using the default format (ISO-8601)
        String dateString = currentDate.toString();

        StringBuilder seatDetails = new StringBuilder();
        for (Long seatIds : seatId) {
            Seats s = seatsRepository.findById(seatIds)
                    .orElseThrow(() -> new SeatNotFoundException(SEAT_NOT_FOUND+ seatIds));
            seatDetails.append("Row: ").append(s.getSeatRow()).append(", Number: ").append(s.getSeatNumber()).append("; ");
        }

        // booking ki entry banani hai
        Booking booking = new Booking();
        booking.setUser(user);
        booking.setAmount(showtime.getPrice());
        booking.setBookingDate(LocalDateTime.now());
        booking.setSeatIds(seatDetails.toString());
        booking.setShowtime(showtime);
        booking.setUser(user);
        booking.setMovie(movie);

        bookingRepository.save(booking);

        // send the mail only it is done !!!
        triggerMail(email, name, theatreName, movieName, dateString, showTime, seatDetails.toString());

        return true;
    }

    public BookingResponseDTO getBookingDetails(Long userId, Long bookingId) {
        // Use specific BookingNotFoundException
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException(BOOKING_NOT_FOUND+ bookingId));

        // Use specific UnauthorizedAccessException
        if (!Objects.equals(booking.getUser().getId(), userId)) {
            throw new UnauthorizedAccessException("User is not authorized to access this booking.");
        }

        // Convert to BookingResponseDTO (unchanged)
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
    @Transactional
    public Boolean cancellingBookingMovie(Long userId, Long bookingId) {
        // Fetch the booking, throw specific exception if not found
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException(BOOKING_NOT_FOUND + bookingId));

        // Check if the booking belongs to the user
        if (!Objects.equals(booking.getUser().getId(), userId)) {
            throw new UnauthorizedAccessException("User is not authorized to cancel this booking.");
        }

        // Parse seat details string to get seat information
        String seatDetailsString = booking.getSeatIds();
        List<Long> seatIds = parseSeatDetails(seatDetailsString);

        // Mark seats as available
        for (Long seatId : seatIds) {
            Seats seat = seatsRepository.findById(seatId)
                    .orElseThrow(() -> new SeatNotFoundException(SEAT_NOT_FOUND + seatId));
            seat.setSeatAvailable(true);
            seatsRepository.save(seat);
        }

        // Mocking refund true here can be extended to be dynamic

        // Remove the booking from the database
        bookingRepository.deleteById(bookingId);
        return true;
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

    public void triggerMail(String email, String name, String theatreName, String movieName, String date, String showtime, String seats)  {

        emailSenderService.sendBookingConfirmationEmail(
                email,   // Recipient's email
                name,           // UserName
                theatreName,               // TheaterName
                movieName,         // MovieName
                date,                // Date
                showtime,                   // ShowTime
                seats                   // SeatNumber
        );
    }


    public List<BookingResponseDTO> getAllBookings(Long userId) {
        List<Booking> bookings = bookingRepository.findAllByUserId(userId);
        return bookings.stream().map(this::convertToDTO).toList();
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

    public void transferBooking(Long id, Long userId, Long finalAmount) {

        logger.info("hey i am in transfer booking method");

        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(BOOKING_NOT_FOUND + id));
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
