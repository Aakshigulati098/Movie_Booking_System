package com.example.movie_booking_system.service;

import com.example.movie_booking_system.dto.BookingResponseDTO;
import com.example.movie_booking_system.exceptions.*;
import com.example.movie_booking_system.models.*;
import com.example.movie_booking_system.repository.*;
import jakarta.mail.MessagingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
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
    void bookingMovie_ShouldBookSuccessfully() throws MessagingException {
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
        boolean result = bookingService.bookingMovie(userId, seatIds, movieId);

        // Assert
        assertTrue(result);
        verify(seatsRepository, times(2)).save(any(Seats.class));
        verify(bookingRepository, times(1)).save(any(Booking.class));
        verify(emailSenderService).sendBookingConfirmationEmail(
                eq("test@example.com"),
                eq("Test User"),
                eq("Test Theatre"),
                eq("Test Movie"),
                eq(LocalDate.now().toString()),
                eq("19:00"),
                eq("Row: A, Number: 1; Row: A, Number: 2; ")
        );
    }

    @Test
    void bookingMovie_ShouldThrowExceptionWhenSeatNotAvailable() {
        // Arrange
        Long userId = 1L;
        Long movieId = 1L;
        List<Long> seatIds = List.of(1L);

        Users user = new Users();
        user.setId(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        Movie movie = new Movie();
        movie.setId(movieId);
        when(movieRepository.findById(movieId)).thenReturn(Optional.of(movie));

        Seats seat = new Seats();
        seat.setId(1L);
        seat.setSeatAvailable(false);
        when(seatsRepository.findById(1L)).thenReturn(Optional.of(seat));

        // Act & Assert
        SeatAlreadyBookedException exception = assertThrows(SeatAlreadyBookedException.class,
                () -> bookingService.bookingMovie(userId, seatIds, movieId));
        assertEquals("Seat is already booked!", exception.getMessage());
    }

    @Test
    void bookingMovie_ShouldThrowExceptionWhenUserNotFound() {
        // Arrange
        Long userId = 1L;
        Long movieId = 1L;
        List<Long> seatIds = List.of(1L);

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        UserNotFoundException exception = assertThrows(UserNotFoundException.class,
                () -> bookingService.bookingMovie(userId, seatIds, movieId));
        assertEquals("User not found with ID: " + userId, exception.getMessage());
    }

    @Test
    void bookingMovie_ShouldThrowExceptionWhenSeatNotFound() {
        // Arrange
        Long userId = 1L;
        Long movieId = 1L;
        List<Long> seatIds = List.of(1L);

        when(userRepository.findById(userId)).thenReturn(Optional.of(new Users()));
        when(seatsRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        SeatNotFoundException exception = assertThrows(SeatNotFoundException.class,
                () -> bookingService.bookingMovie(userId, seatIds, movieId));
        assertEquals("Seat not found with ID: 1", exception.getMessage());
    }

    @Test
    void bookingMovie_ShouldThrowExceptionWhenShowTimeNotFound() {
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
        ShowTimeNotFoundException exception = assertThrows(ShowTimeNotFoundException.class,
                () -> bookingService.bookingMovie(userId, seatIds, movieId));
        assertEquals("Showtime not found with ID: null", exception.getMessage());
    }

    @Test
    void bookingMovie_ShouldThrowExceptionWhenTheatreNotFound() {
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
        TheatreNotFoundException exception = assertThrows(TheatreNotFoundException.class,
                () -> bookingService.bookingMovie(userId, seatIds, movieId));
        assertEquals("Theatre not found", exception.getMessage());
    }

    @Test
    void bookingMovie_ShouldThrowExceptionWhenMovieNotFound() {
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
        MovieNotFoundException exception = assertThrows(MovieNotFoundException.class,
                () -> bookingService.bookingMovie(userId, seatIds, movieId));
        assertEquals("Movie not found with ID: " + movieId, exception.getMessage());
    }

    @Test
    void cancellingBookingMovie_ShouldCancelBookingSuccessfully() {
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
        Boolean result = bookingService.cancellingBookingMovie(userId, bookingId);

        // Assert
        assertTrue(result);
        verify(seatsRepository, times(1)).save(any(Seats.class));
        verify(bookingRepository, times(1)).deleteById(bookingId);
    }

    @Test
    void cancellingBookingMovie_ShouldThrowExceptionWhenUserNotAuthorized() {
        // Arrange
        Long userId = 1L;
        Long bookingId = 2L;

        Booking booking = new Booking();
        Users user = new Users();
        user.setId(3L); // Different user ID
        booking.setUser(user);

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));

        // Act & Assert
        UnauthorizedAccessException exception = assertThrows(UnauthorizedAccessException.class,
                () -> bookingService.cancellingBookingMovie(userId, bookingId));
        assertEquals("User is not authorized to cancel this booking.", exception.getMessage());
    }

    @Test
    void getBookingDetails_ShouldReturnBookingDetails() {
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
        BookingResponseDTO result = bookingService.getBookingDetails(userId, bookingId);

        // Assert
        assertNotNull(result);
        assertEquals("Test Movie", result.getMovieName());
        assertEquals("Test Theatre", result.getTheatreName());
    }

    @Test
    void getAllBookings_ShouldReturnAllBookings() {
        // Arrange
        Long userId = 1L;

        Booking booking = new Booking();
        booking.setId(1L);
        booking.setMovie(new Movie());
        booking.getMovie().setTitle("Test Movie");
        booking.setShowtime(new ShowTime());
        booking.getShowtime().setTheatre(new Theatre());
        booking.getShowtime().getTheatre().setName("Test Theatre");
        booking.setSeatIds("Row: A, Number: 1;");

        when(bookingRepository.findAllByUserId(userId)).thenReturn(List.of(booking));

        // Act
        List<BookingResponseDTO> result = bookingService.getAllBookings(userId);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Test Movie", result.get(0).getMovieName());
    }

    @Test
    void transferBooking_ShouldTransferBookingSuccessfully() {
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
        bookingService.transferBooking(bookingId, userId, finalAmount);

        // Assert
        verify(bookingRepository, times(1)).save(any(Booking.class));
        verify(webSocketService, times(1)).sendBookingTransferNotification(bookingId);
    }
}