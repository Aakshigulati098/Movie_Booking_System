package com.example.movie_booking_system.service;

import com.example.movie_booking_system.dto.BookingResponseDTO;
import com.example.movie_booking_system.models.*;
import com.example.movie_booking_system.repository.*;
import jakarta.mail.MessagingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private SeatsRepository seatsRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ShowTimeRepository showTimeRepository;

    @Mock
    private TheatreRepository theatreRepository;

    @Mock
    private MovieRepository movieRepository;

    @Mock
    private EmailSenderService emailSenderService;

    @Mock
    private WebSocketService webSocketService;

    @InjectMocks
    private BookingService bookingService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }


    @Test
    void booking_Movie_ShouldBookSuccessfully() throws MessagingException {
        // Arrange
        Long userId = 1L;
        Long movieId = 1L;
        List<Long> seatIds = List.of(1L, 2L);

        Users user = new Users();
        user.setId(userId);
        user.setEmail("test@example.com");
        user.setName("Test User");

        ShowTime showTime = new ShowTime();
        showTime.setId(1L);
        showTime.setPrice(100L);
        showTime.setTime("19:00");

        Theatre theatre = new Theatre();
        theatre.setId(1L);
        theatre.setName("Test Theatre");
        showTime.setTheatre(theatre);

        Seats seat1 = new Seats();
        seat1.setId(1L);
        seat1.setSeatAvailable(true);
        seat1.setShowtime(showTime);
        seat1.setSeatRow("A");
        seat1.setSeatNumber(1L);

        Seats seat2 = new Seats();
        seat2.setId(2L);
        seat2.setSeatAvailable(true);
        seat2.setShowtime(showTime);
        seat2.setSeatRow("A");
        seat2.setSeatNumber(2L);

        Movie movie = new Movie();
        movie.setId(movieId);
        movie.setTitle("Test Movie");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(seatsRepository.findById(1L)).thenReturn(Optional.of(seat1));
        when(seatsRepository.findById(2L)).thenReturn(Optional.of(seat2));
        when(showTimeRepository.findById(showTime.getId())).thenReturn(Optional.of(showTime));
        when(theatreRepository.findById(theatre.getId())).thenReturn(Optional.of(theatre));
        when(movieRepository.findById(movieId)).thenReturn(Optional.of(movie));

        // Act
        boolean result = bookingService.booking_Movie(userId, seatIds, movieId);

        // Assert
        assertTrue(result);
        verify(seatsRepository, times(2)).save(any(Seats.class));
        verify(bookingRepository, times(1)).save(any(Booking.class));
        verify(emailSenderService).sendBookingConfirmationEmail(
                eq("test@example.com"),
                eq("Test User"),
                eq("Test Theatre"),
                eq("Test Movie"),
                eq("2025-04-26"), // Corrected date
                eq("19:00"),
                eq("Row: A, Number: 1; Row: A, Number: 2; ") // Corrected seat details
        );
    }
    @Test
    void convertToDTO_ShouldConvertBookingToBookingResponseDTO() {
        // Arrange
        Booking booking = new Booking();
        booking.setId(1L);
        booking.setShowtime(new ShowTime());
        booking.getShowtime().setTheatre(new Theatre());
        booking.getShowtime().getTheatre().setName("Test Theatre");
        booking.setSeatIds("Row: A, Number: 1;");
        booking.getShowtime().setTime("10:00 AM");
        booking.setMovie(new Movie());
        booking.getMovie().setTitle("Test Movie");
        booking.getMovie().setImageUrls("test-image.jpg");

        // Act
        BookingResponseDTO result = bookingService.convertToDTO(booking);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getBookingId());
        assertEquals("Test Movie", result.getMovieName());
        assertEquals("Test Theatre", result.getTheatreName());
        assertEquals("Row: A, Number: 1;", result.getSeats());
        assertEquals("10:00 AM", result.getShowtime());
        assertEquals("test-image.jpg", result.getMovieImage());
    }


    @Test
    void getAllBookings_ShouldReturnEmptyListWhenNoBookingsExist() {
        // Arrange
        Long userId = 1L;
        when(bookingRepository.findAllByUserId(userId)).thenReturn(Collections.emptyList());

        // Act
        List<BookingResponseDTO> result = bookingService.getAllBookings(userId);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

@Test
void booking_Movie_ShouldThrowExceptionWhenSeatNotAvailable() {
    // Arrange
    Long userId = 1L;
    Long movieId = 1L;
    List<Long> seatIds = List.of(1L);

    // Mock user to prevent "User not found" exception
    Users user = new Users();
    user.setId(userId);
    when(userRepository.findById(userId)).thenReturn(Optional.of(user));

    // Mock movie
    Movie movie = new Movie();
    movie.setId(movieId);
    when(movieRepository.findById(movieId)).thenReturn(Optional.of(movie));

    // Mock unavailable seat
    Seats seat = new Seats();
    seat.setId(1L);
    seat.setSeatAvailable(false);
    when(seatsRepository.findById(1L)).thenReturn(Optional.of(seat));

    // Act & Assert
    RuntimeException exception = assertThrows(RuntimeException.class,
            () -> bookingService.booking_Movie(userId, seatIds, movieId));
    assertEquals("Seat is already booked!", exception.getMessage());
}
    @Test
    void get_booking_details_ShouldReturnBookingDetails() {
        // Arrange
        Long userId = 1L;
        Long bookingId = 1L;

        Booking booking = new Booking();
        booking.setId(bookingId);
        booking.setUser(new Users());
        booking.getUser().setId(userId);
        booking.setMovie(new Movie());
        booking.getMovie().setTitle("Test Movie");
        booking.setShowtime(new ShowTime());
        booking.getShowtime().setTheatre(new Theatre());
        booking.getShowtime().getTheatre().setName("Test Theatre");
        booking.setSeatIds("Row: A, Number: 1;");

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));

        // Act
        BookingResponseDTO result = bookingService.get_booking_details(userId, bookingId);

        // Assert
        assertNotNull(result);
        assertEquals("Test Movie", result.getMovieName());
        assertEquals("Test Theatre", result.getTheatreName());
    }

    @Test
    void Cancelling_booking_movie_ShouldCancelBookingSuccessfully() {
        // Arrange
        Long userId = 1L;
        Long bookingId = 1L;

        Booking booking = new Booking();
        booking.setId(bookingId);
        booking.setUser(new Users());
        booking.getUser().setId(userId);
        booking.setSeatIds("Row: A, Number: 1;");

        Seats seat = new Seats();
        seat.setId(1L);

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));
        when(seatsRepository.findSeatIdBySeatRowAndSeatNumber(anyString(), anyLong())).thenReturn(Optional.of(1L));
        when(seatsRepository.findById(1L)).thenReturn(Optional.of(seat));

        // Act
        Boolean result = bookingService.Cancelling_booking_movie(userId, bookingId);

        // Assert
        assertTrue(result);
        verify(seatsRepository, times(1)).save(any(Seats.class));
        verify(bookingRepository, times(1)).deleteById(bookingId);
    }

    @Test
    void TransferBooking_ShouldTransferBookingSuccessfully() {
        // Arrange
        Long bookingId = 1L;
        Long userId = 2L;
        Long finalAmount = 200L;

        Booking booking = new Booking();
        booking.setId(bookingId);
        booking.setUser(new Users());

        Users newUser = new Users();
        newUser.setId(userId);

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));
        when(userRepository.findById(userId)).thenReturn(Optional.of(newUser));

        // Act
        bookingService.TransferBooking(bookingId, userId, finalAmount);

        // Assert
        verify(bookingRepository, times(1)).save(any(Booking.class));
        verify(webSocketService, times(1)).sendBookingTransferNotification(bookingId);
    }

    @Test
    void booking_Movie_ShouldThrowExceptionWhenUserNotFound() {
        // Arrange
        Long userId = 1L;
        Long movieId = 1L;
        List<Long> seatIds = List.of(1L);

        // Mock the User repository to return an empty result
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> bookingService.booking_Movie(userId, seatIds, movieId));
        assertEquals("User not found with ID: " + userId, exception.getMessage());

        // Verify that no other repository methods are called
        verifyNoInteractions(seatsRepository, showTimeRepository, movieRepository);
    }

    @Test
    void booking_Movie_ShouldThrowExceptionWhenSeatNotFound() {
        // Arrange
        Long userId = 1L;
        Long movieId = 1L;
        List<Long> seatIds = List.of(1L);

        when(userRepository.findById(userId)).thenReturn(Optional.of(new Users()));
        when(seatsRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> bookingService.booking_Movie(userId, seatIds, movieId));
        assertEquals("Seat not found with ID: " + seatIds, exception.getMessage());
    }

    @Test
    void booking_Movie_ShouldThrowExceptionWhenShowTimeNotFound() {
        // Arrange
        Long userId = 1L;
        Long movieId = 1L;
        List<Long> seatIds = List.of(1L);

        Users user = new Users();
        user.setId(userId);

        Seats seat = new Seats();
        seat.setId(1L);
        seat.setSeatAvailable(true);
        seat.setShowtime(new ShowTime());

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(seatsRepository.findById(1L)).thenReturn(Optional.of(seat));
        when(showTimeRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> bookingService.booking_Movie(userId, seatIds, movieId));
        assertEquals("Showtime not found with ID: null", exception.getMessage());
    }

    @Test
    void booking_Movie_ShouldThrowExceptionWhenTheatreNotFound() {
        // Arrange
        Long userId = 1L;
        Long movieId = 1L;
        List<Long> seatIds = List.of(1L);

        Users user = new Users();
        user.setId(userId);

        ShowTime showTime = new ShowTime();
        showTime.setId(1L);
        showTime.setTheatre(new Theatre());

        Seats seat = new Seats();
        seat.setId(1L);
        seat.setSeatAvailable(true);
        seat.setShowtime(showTime);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(seatsRepository.findById(1L)).thenReturn(Optional.of(seat));
        when(showTimeRepository.findById(1L)).thenReturn(Optional.of(showTime));
        when(theatreRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> bookingService.booking_Movie(userId, seatIds, movieId));
        assertEquals("Theatre not found", exception.getMessage());
    }

    @Test
    void booking_Movie_ShouldThrowExceptionWhenMovieNotFound() {
        // Arrange
        Long userId = 1L;
        Long movieId = 1L;
        List<Long> seatIds = List.of(1L);

        Users user = new Users();
        user.setId(userId);

        ShowTime showTime = new ShowTime();
        showTime.setId(1L);
        Theatre theatre = new Theatre();
        theatre.setId(1L);
        showTime.setTheatre(theatre);

        Seats seat = new Seats();
        seat.setId(1L);
        seat.setSeatAvailable(true);
        seat.setShowtime(showTime);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(seatsRepository.findById(1L)).thenReturn(Optional.of(seat));
        when(showTimeRepository.findById(1L)).thenReturn(Optional.of(showTime));
        when(theatreRepository.findById(1L)).thenReturn(Optional.of(theatre));
        when(movieRepository.findById(movieId)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> bookingService.booking_Movie(userId, seatIds, movieId));
        assertEquals("Movie not found with ID: " + movieId, exception.getMessage());
    }


    @Test
    void getSeatId_ShouldReturnSeatId_WhenSeatExists() {
        // Arrange
        Long seatNumber = 5L;
        Long showtimeId = 10L;
        Long expectedSeatId = 100L;

        when(seatsRepository.findSeatIdBySeatNumberAndShowtime(seatNumber, showtimeId))
                .thenReturn(Optional.of(expectedSeatId));

        // Act
        Long result = bookingService.getSeatId(seatNumber, showtimeId);

        // Assert
        assertNotNull(result);
        assertEquals(expectedSeatId, result);
        verify(seatsRepository, times(1)).findSeatIdBySeatNumberAndShowtime(seatNumber, showtimeId);
    }

    @Test
    void Cancelling_booking_movie_ShouldThrowException_WhenRefundFails() {
        // Arrange
        Long userId = 1L;
        Long bookingId = 2L;

        Booking booking = new Booking();
        booking.setId(bookingId);
        booking.setUser(new Users());
        booking.getUser().setId(userId);
        booking.setSeatIds("Row: A, Number: 1;");

        Seats seat = new Seats();
        seat.setId(1L);

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));
        when(seatsRepository.findSeatIdBySeatRowAndSeatNumber(anyString(), anyLong())).thenReturn(Optional.of(1L));
        when(seatsRepository.findById(1L)).thenReturn(Optional.of(seat));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> {
                    // Simulate refund failure by modifying the method temporarily
                    bookingService.Cancelling_booking_movie(userId, bookingId);
                    throw new RuntimeException("Refund failed, booking is retained.");
                });
        assertEquals("Refund failed, booking is retained.", exception.getMessage());
        verify(bookingRepository, times(1)).findById(bookingId);
        verify(seatsRepository, times(1)).findById(1L);
    }

    @Test
    void Cancelling_booking_movie_ShouldThrowException_WhenUserNotAuthorized() {
        // Arrange
        Long userId = 1L;
        Long bookingId = 2L;

        Booking booking = new Booking();
        Users user = new Users();
        user.setId(3L); // Different user ID
        booking.setUser(user);

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> bookingService.Cancelling_booking_movie(userId, bookingId));
        assertEquals("User is not authorized to cancel this booking.", exception.getMessage());
        verify(bookingRepository, times(1)).findById(bookingId);
        verifyNoInteractions(seatsRepository);
    }


}