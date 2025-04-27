package com.example.movie_booking_system.service;

import com.example.movie_booking_system.dto.BookingResponseDTO;
import com.example.movie_booking_system.exceptions.*;
import com.example.movie_booking_system.models.*;
import com.example.movie_booking_system.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private TheatreRepository theatreRepository;

    @Mock
    private MovieRepository movieRepository;

    @Mock
    private EmailSenderService emailSenderService;

    @Mock
    private SeatsRepository seatsRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ShowTimeRepository showTimeRepository;

    @Mock
    private WebSocketService webSocketService;

    @InjectMocks
    private BookingService bookingService;

    private Users user;
    private Movie movie;
    private Theatre theatre;
    private ShowTime showTime;
    private Seats seat1;
    private Seats seat2;
    private Booking booking;

    @BeforeEach
    void setUp() {
        // Initialize test data
        user = new Users();
        user.setId(1L);
        user.setName("Test User");
        user.setEmail("test@example.com");

        movie = new Movie();
        movie.setId(1L);
        movie.setTitle("Test Movie");
        movie.setImageUrls("test-image.jpg");

        theatre = new Theatre();
        theatre.setId(1L);
        theatre.setName("Test Theatre");

        showTime = new ShowTime();
        showTime.setId(1L);
        showTime.setTime("18:00");
        showTime.setTheatre(theatre);
        showTime.setPrice(200L);

        seat1 = new Seats();
        seat1.setId(1L);
        seat1.setSeatRow("A");
        seat1.setSeatNumber(1L);
        seat1.setSeatAvailable(true);
        seat1.setShowtime(showTime);

        seat2 = new Seats();
        seat2.setId(2L);
        seat2.setSeatRow("A");
        seat2.setSeatNumber(2L);
        seat2.setSeatAvailable(true);
        seat2.setShowtime(showTime);

        booking = new Booking();
        booking.setId(1L);
        booking.setUser(user);
        booking.setMovie(movie);
        booking.setShowtime(showTime);
        booking.setAmount(200L);
        booking.setBookingDate(LocalDateTime.now());
        booking.setSeatIds("Row: A, Number: 1; Row: A, Number: 2;");
        booking.setBookingStatus(BookingEnum.OWNED);
    }

    @Test
    void testGetSeatId_Success() {
        // Arrange
        Long seatNumber = 1L;
        Long showtimeId = 1L;
        when(seatsRepository.findSeatIdBySeatNumberAndShowtime(seatNumber, showtimeId))
                .thenReturn(Optional.of(1L));

        // Act
        Long result = bookingService.getSeatId(seatNumber, showtimeId);

        // Assert
        assertEquals(1L, result);
        verify(seatsRepository).findSeatIdBySeatNumberAndShowtime(seatNumber, showtimeId);
    }

    @Test
    void testGetSeatId_NotFound() {
        // Arrange
        Long seatNumber = 1L;
        Long showtimeId = 1L;
        when(seatsRepository.findSeatIdBySeatNumberAndShowtime(seatNumber, showtimeId))
                .thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> bookingService.getSeatId(seatNumber, showtimeId));

        assertEquals("Seat not found for seatNumber: " + seatNumber + " and showtimeId: " + showtimeId,
                exception.getMessage());
    }

    @Test
    void testBookingMovie_Success() {
        // Arrange
        Long userId = 1L;
        List<Long> seatIds = Arrays.asList(1L, 2L);
        Long movieId = 1L;

        when(seatsRepository.findById(1L)).thenReturn(Optional.of(seat1));
        when(seatsRepository.findById(2L)).thenReturn(Optional.of(seat2));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(showTimeRepository.findById(1L)).thenReturn(Optional.of(showTime));
        when(theatreRepository.findById(1L)).thenReturn(Optional.of(theatre));
        when(movieRepository.findById(movieId)).thenReturn(Optional.of(movie));

        doNothing().when(emailSenderService).sendBookingConfirmationEmail(
                anyString(), anyString(), anyString(), anyString(), anyString(), anyString(), anyString());

        // Act
        boolean result = bookingService.bookingMovie(userId, seatIds, movieId);

        // Assert
        assertTrue(result);
        verify(seatsRepository, times(2)).save(any(Seats.class));
        verify(bookingRepository).save(any(Booking.class));
        verify(emailSenderService).sendBookingConfirmationEmail(
                anyString(), anyString(), anyString(), anyString(), anyString(), anyString(), anyString());

        // Verify seats are marked as unavailable
        assertFalse(seat1.getSeatAvailable());
        assertFalse(seat2.getSeatAvailable());
    }

    @Test
    void testBookingMovie_SeatNotFound() {
        // Arrange
        Long userId = 1L;
        List<Long> seatIds = Arrays.asList(1L, 100L); // 100L is not found
        Long movieId = 1L;

        when(seatsRepository.findById(1L)).thenReturn(Optional.of(seat1));
        when(seatsRepository.findById(100L)).thenReturn(Optional.empty());

        // Act & Assert
        SeatNotFoundException exception = assertThrows(SeatNotFoundException.class,
                () -> bookingService.bookingMovie(userId, seatIds, movieId));

        assertEquals("Seat not found with ID: 100", exception.getMessage());
    }

    @Test
    void testBookingMovie_SeatAlreadyBooked() {
        // Arrange
        Long userId = 1L;
        List<Long> seatIds = Arrays.asList(1L, 2L);
        Long movieId = 1L;

        seat1.setSeatAvailable(false); // Seat is already booked

        when(seatsRepository.findById(1L)).thenReturn(Optional.of(seat1));

        // Act & Assert
        SeatAlreadyBookedException exception = assertThrows(SeatAlreadyBookedException.class,
                () -> bookingService.bookingMovie(userId, seatIds, movieId));

        assertEquals("Seat is already booked!", exception.getMessage());
    }

    @Test
    void testBookingMovie_UserNotFound() {
        // Arrange
        Long userId = 100L; // User does not exist
        List<Long> seatIds = Arrays.asList(1L, 2L);
        Long movieId = 1L;

        when(seatsRepository.findById(1L)).thenReturn(Optional.of(seat1));
        when(seatsRepository.findById(2L)).thenReturn(Optional.of(seat2));
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        UserNotFoundException exception = assertThrows(UserNotFoundException.class,
                () -> bookingService.bookingMovie(userId, seatIds, movieId));

        assertEquals("User not found with ID: 100", exception.getMessage());
    }

    @Test
    void testBookingMovie_ShowtimeNotFound() {
        // Arrange
        Long userId = 1L;
        List<Long> seatIds = Arrays.asList(1L, 2L);
        Long movieId = 1L;

        when(seatsRepository.findById(1L)).thenReturn(Optional.of(seat1));
        when(seatsRepository.findById(2L)).thenReturn(Optional.of(seat2));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(showTimeRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        ShowTimeNotFoundException exception = assertThrows(ShowTimeNotFoundException.class,
                () -> bookingService.bookingMovie(userId, seatIds, movieId));

        assertEquals("Showtime not found with ID: 1", exception.getMessage());
    }

    @Test
    void testBookingMovie_TheatreNotFound() {
        // Arrange
        Long userId = 1L;
        List<Long> seatIds = Arrays.asList(1L, 2L);
        Long movieId = 1L;

        when(seatsRepository.findById(1L)).thenReturn(Optional.of(seat1));
        when(seatsRepository.findById(2L)).thenReturn(Optional.of(seat2));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(showTimeRepository.findById(1L)).thenReturn(Optional.of(showTime));
        when(theatreRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        TheatreNotFoundException exception = assertThrows(TheatreNotFoundException.class,
                () -> bookingService.bookingMovie(userId, seatIds, movieId));

        assertEquals("Theatre not found", exception.getMessage());
    }

    @Test
    void testBookingMovie_MovieNotFound() {
        // Arrange
        Long userId = 1L;
        List<Long> seatIds = Arrays.asList(1L, 2L);
        Long movieId = 100L; // Movie does not exist

        when(seatsRepository.findById(1L)).thenReturn(Optional.of(seat1));
        when(seatsRepository.findById(2L)).thenReturn(Optional.of(seat2));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(showTimeRepository.findById(1L)).thenReturn(Optional.of(showTime));
        when(theatreRepository.findById(1L)).thenReturn(Optional.of(theatre));
        when(movieRepository.findById(movieId)).thenReturn(Optional.empty());

        // Act & Assert
        MovieNotFoundException exception = assertThrows(MovieNotFoundException.class,
                () -> bookingService.bookingMovie(userId, seatIds, movieId));

        assertEquals("Movie not found with ID: 100", exception.getMessage());
    }

    @Test
    void testGetBookingDetails_Success() {
        // Arrange
        Long userId = 1L;
        Long bookingId = 1L;

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));

        // Act
        BookingResponseDTO result = bookingService.getBookingDetails(userId, bookingId);

        // Assert
        assertNotNull(result);
        assertEquals(bookingId, result.getBookingId());
        assertEquals("Test Movie", result.getMovieName());
        assertEquals("Test Theatre", result.getTheatreName());
        assertEquals("Row: A, Number: 1; Row: A, Number: 2;", result.getSeats());
        assertEquals("18:00", result.getShowtime());
        assertEquals("test-image.jpg", result.getMovieImage());
        assertEquals(BookingEnum.OWNED, result.getBookingStatus());
    }

    @Test
    void testGetBookingDetails_BookingNotFound() {
        // Arrange
        Long userId = 1L;
        Long bookingId = 100L; // Booking does not exist

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.empty());

        // Act & Assert
        BookingNotFoundException exception = assertThrows(BookingNotFoundException.class,
                () -> bookingService.getBookingDetails(userId, bookingId));

        assertEquals("Booking not found with ID: 100", exception.getMessage());
    }

    @Test
    void testGetBookingDetails_UnauthorizedAccess() {
        // Arrange
        Long userId = 2L; // Different user than booking owner
        Long bookingId = 1L;

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));

        // Act & Assert
        UnauthorizedAccessException exception = assertThrows(UnauthorizedAccessException.class,
                () -> bookingService.getBookingDetails(userId, bookingId));

        assertEquals("User is not authorized to access this booking.", exception.getMessage());
    }

    @Test
    void testCancellingBookingMovie_Success() {
        // Arrange
        Long userId = 1L;
        Long bookingId = 1L;

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));
        when(seatsRepository.findSeatIdBySeatRowAndSeatNumber("A", 1L)).thenReturn(Optional.of(1L));
        when(seatsRepository.findSeatIdBySeatRowAndSeatNumber("A", 2L)).thenReturn(Optional.of(2L));
        when(seatsRepository.findById(1L)).thenReturn(Optional.of(seat1));
        when(seatsRepository.findById(2L)).thenReturn(Optional.of(seat2));

        doNothing().when(bookingRepository).deleteById(bookingId);

        // Act
        Boolean result = bookingService.cancellingBookingMovie(userId, bookingId);

        // Assert
        assertTrue(result);
        verify(seatsRepository, times(2)).save(any(Seats.class));
        verify(bookingRepository).deleteById(bookingId);

        // Verify seats are marked as available
        assertTrue(seat1.getSeatAvailable());
        assertTrue(seat2.getSeatAvailable());
    }

    @Test
    void testCancellingBookingMovie_BookingNotFound() {
        // Arrange
        Long userId = 1L;
        Long bookingId = 100L; // Booking does not exist

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.empty());

        // Act & Assert
        BookingNotFoundException exception = assertThrows(BookingNotFoundException.class,
                () -> bookingService.cancellingBookingMovie(userId, bookingId));

        assertEquals("Booking not found with ID: 100", exception.getMessage());
    }

    @Test
    void testCancellingBookingMovie_UnauthorizedAccess() {
        // Arrange
        Long userId = 2L; // Different user than booking owner
        Long bookingId = 1L;

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));

        // Act & Assert
        UnauthorizedAccessException exception = assertThrows(UnauthorizedAccessException.class,
                () -> bookingService.cancellingBookingMovie(userId, bookingId));

        assertEquals("User is not authorized to cancel this booking.", exception.getMessage());
    }

    @Test
    void testCancellingBookingMovie_SeatNotFound() {
        // Arrange
        Long userId = 1L;
        Long bookingId = 1L;

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));
        when(seatsRepository.findSeatIdBySeatRowAndSeatNumber("A", 1L)).thenReturn(Optional.of(1L));
        when(seatsRepository.findSeatIdBySeatRowAndSeatNumber("A", 2L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> bookingService.cancellingBookingMovie(userId, bookingId));

        assertEquals("Seat not found with Row: A, Number: 2", exception.getMessage());
    }

    @Test
    void testParseSeatDetails() {
        // This is a private method, so we test it through cancelling a booking
        // The test for cancellingBookingMovie_Success already covers this
        // But we can add an additional test with malformed seat string

        // Arrange
        Long userId = 1L;
        Long bookingId = 1L;

        // Create a booking with malformed seat details
        Booking malformedBooking = new Booking();
        malformedBooking.setId(bookingId);
        malformedBooking.setUser(user);
        malformedBooking.setSeatIds("Invalid seat format");

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(malformedBooking));

        // Act & Assert - should handle the malformed seat string and not fail
        assertDoesNotThrow(() -> bookingService.cancellingBookingMovie(userId, bookingId));
    }

    @Test
    void testTriggerMail() {
        // Arrange
        String email = "test@example.com";
        String name = "Test User";
        String theatreName = "Test Theatre";
        String movieName = "Test Movie";
        String date = LocalDate.now().toString();
        String showtime = "18:00";
        String seats = "Row: A, Number: 1; Row: A, Number: 2;";

        doNothing().when(emailSenderService).sendBookingConfirmationEmail(
                email, name, theatreName, movieName, date, showtime, seats);

        // Act
        bookingService.triggerMail(email, name, theatreName, movieName, date, showtime, seats);

        // Assert
        verify(emailSenderService).sendBookingConfirmationEmail(
                email, name, theatreName, movieName, date, showtime, seats);
    }

    @Test
    void testGetAllBookings_Success() {
        // Arrange
        Long userId = 1L;
        List<Booking> bookings = new ArrayList<>();
        bookings.add(booking);

        when(bookingRepository.findAllByUserId(userId)).thenReturn(bookings);

        // Act
        List<BookingResponseDTO> result = bookingService.getAllBookings(userId);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(booking.getId(), result.get(0).getBookingId());
        assertEquals(booking.getMovie().getTitle(), result.get(0).getMovieName());
        assertEquals(booking.getShowtime().getTheatre().getName(), result.get(0).getTheatreName());
    }

    @Test
    void testGetAllBookings_EmptyList() {
        // Arrange
        Long userId = 1L;

        when(bookingRepository.findAllByUserId(userId)).thenReturn(new ArrayList<>());

        // Act
        List<BookingResponseDTO> result = bookingService.getAllBookings(userId);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testTransferBooking_Success() {
        // Arrange
        Long bookingId = 1L;
        Long newUserId = 2L;
        Long finalAmount = 300L;

        Users newUser = new Users();
        newUser.setId(newUserId);

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));
        when(userRepository.findById(newUserId)).thenReturn(Optional.of(newUser));
        doNothing().when(webSocketService).sendBookingTransferNotification(bookingId);

        // Act
        bookingService.transferBooking(bookingId, newUserId, finalAmount);

        // Assert
        verify(bookingRepository).save(booking);
        verify(webSocketService).sendBookingTransferNotification(bookingId);

        assertEquals(newUser, booking.getUser());
        assertEquals(finalAmount, booking.getAmount());
        assertEquals(BookingEnum.AUCTIONED, booking.getBookingStatus());
    }

    @Test
    void testTransferBooking_BookingNotFound() {
        // Arrange
        Long bookingId = 100L;
        Long newUserId = 2L;
        Long finalAmount = 300L;

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> bookingService.transferBooking(bookingId, newUserId, finalAmount));

        assertEquals("Booking not found with ID: 100", exception.getMessage());
    }

    @Test
    void testTransferBooking_UserNotFound() {
        // Arrange
        Long bookingId = 1L;
        Long newUserId = 100L; // User does not exist
        Long finalAmount = 300L;

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));
        when(userRepository.findById(newUserId)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> bookingService.transferBooking(bookingId, newUserId, finalAmount));

        assertEquals("User not found with ID: 100", exception.getMessage());
    }
}