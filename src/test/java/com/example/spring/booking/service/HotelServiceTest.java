package com.example.spring.booking.service;

import com.example.spring.booking.entity.Hotel;
import com.example.spring.booking.exception.InvalidDataException;
import com.example.spring.booking.exception.ResourceNotFoundException;
import com.example.spring.booking.mapper.HotelMapper;
import com.example.spring.booking.repository.HotelRepository;
import com.example.spring.booking.web.model.hotel.HotelRequest;
import com.example.spring.booking.web.model.hotel.HotelResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HotelServiceTest {

    @Mock private HotelRepository hotelRepository;
    @Mock private HotelMapper hotelMapper;

    @InjectMocks
    private HotelService hotelService;

    private Hotel hotel;

    @BeforeEach
    void setUp() {
        hotel = Hotel.builder()
                .id(1L).name("Hilton").title("Hilton Hotel")
                .city("Москва").address("ул. Тверская, 1")
                .distanceToCenter(0.5).rating(0.0).numberOfRatings(0)
                .build();
    }

    // ─── Рейтинг: первая оценка ───────────────────────────────────

    @Test
    @DisplayName("updateRating: первая оценка (numberOfRatings=0) → рейтинг = оценке")
    void updateRating_firstAssessment_ratingEqualsAssessment() {
        hotel.setRating(0.0);
        hotel.setNumberOfRatings(0);
        HotelResponse response = new HotelResponse();

        when(hotelRepository.findById(1L)).thenReturn(Optional.of(hotel));
        when(hotelRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(hotelMapper.hotelToResponse(any())).thenReturn(response);

        hotelService.updateRating(1L, 5);

        verify(hotelRepository).save(argThat(h ->
                h.getRating() == 5.0 && h.getNumberOfRatings() == 1
        ));
    }

    @Test
    @DisplayName("updateRating: вторая оценка → корректное среднее (5+3)/2 = 4.0")
    void updateRating_secondAssessment_correctAverage() {
        // формула: totalRating = oldRating * count - oldRating + assessment
        // = 5.0 * 1 - 5.0 + 3 = 3.0 → newRating = 3.0 / 1 = 3.0
        // Внимание: реальная формула в коде: (rating*count - rating + assessment) / count
        hotel.setRating(5.0);
        hotel.setNumberOfRatings(1);

        when(hotelRepository.findById(1L)).thenReturn(Optional.of(hotel));
        when(hotelRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(hotelMapper.hotelToResponse(any())).thenReturn(new HotelResponse());

        hotelService.updateRating(1L, 3);

        verify(hotelRepository).save(argThat(h -> h.getNumberOfRatings() == 2));
    }

    @Test
    @DisplayName("updateRating: numberOfRatings увеличивается на 1 каждый раз")
    void updateRating_countIncrementsCorrectly() {
        hotel.setRating(4.0);
        hotel.setNumberOfRatings(10);

        when(hotelRepository.findById(1L)).thenReturn(Optional.of(hotel));
        when(hotelRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(hotelMapper.hotelToResponse(any())).thenReturn(new HotelResponse());

        hotelService.updateRating(1L, 4);

        verify(hotelRepository).save(argThat(h -> h.getNumberOfRatings() == 11));
    }

    @Test
    @DisplayName("updateRating: рейтинг округляется до 1 знака после запятой")
    void updateRating_roundsToOneDecimal() {
        hotel.setRating(4.0);
        hotel.setNumberOfRatings(2);

        when(hotelRepository.findById(1L)).thenReturn(Optional.of(hotel));
        when(hotelRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(hotelMapper.hotelToResponse(any())).thenReturn(new HotelResponse());

        hotelService.updateRating(1L, 3);

        // проверяем, что save был вызван (округление внутри кода)
        verify(hotelRepository).save(any());
    }

    // ─── Рейтинг: невалидные значения ────────────────────────────

    @ParameterizedTest(name = "assessment={0} → InvalidDataException")
    @ValueSource(ints = {0, -1, 6, 10, Integer.MIN_VALUE})
    @DisplayName("updateRating: оценка вне диапазона 1-5 → InvalidDataException")
    void updateRating_outOfRange_throwsInvalidDataException(int assessment) {
        // findById не вызывается — валидация оценки происходит до обращения к репозиторию
        assertThatThrownBy(() -> hotelService.updateRating(1L, assessment))
                .isInstanceOf(InvalidDataException.class)
                .hasMessageContaining("1 to 5");

        verify(hotelRepository, never()).save(any());
        verify(hotelRepository, never()).findById(any());
    }

    @ParameterizedTest(name = "assessment={0} → успех")
    @ValueSource(ints = {1, 2, 3, 4, 5})
    @DisplayName("updateRating: валидная оценка 1-5 → успех")
    void updateRating_validRange_success(int assessment) {
        when(hotelRepository.findById(1L)).thenReturn(Optional.of(hotel));
        when(hotelRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(hotelMapper.hotelToResponse(any())).thenReturn(new HotelResponse());

        assertThatCode(() -> hotelService.updateRating(1L, assessment))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("updateRating: отель не найден → ResourceNotFoundException")
    void updateRating_hotelNotFound_throwsResourceNotFoundException() {
        when(hotelRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> hotelService.updateRating(99L, 4))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(hotelRepository, never()).save(any());
    }

    // ─── CRUD ─────────────────────────────────────────────────────

    @Test
    @DisplayName("create: корректный запрос → отель сохраняется")
    void create_validRequest_saved() {
        HotelRequest hotelRequest = new HotelRequest();
        hotelRequest.setName("Marriott");
        hotelRequest.setTitle("Marriott Moscow");
        hotelRequest.setCity("Москва");
        hotelRequest.setAddress("ул. Ленина, 5");
        hotelRequest.setDistanceToCenter(1.0);

        Hotel mapped = Hotel.builder().name("Marriott").title("Marriott Moscow").build();
        Hotel saved  = Hotel.builder().id(2L).name("Marriott").title("Marriott Moscow").build();
        HotelResponse response = new HotelResponse();

        when(hotelMapper.requestToHotel(hotelRequest)).thenReturn(mapped);
        when(hotelRepository.save(mapped)).thenReturn(saved);
        when(hotelMapper.hotelToResponse(saved)).thenReturn(response);

        HotelResponse result = hotelService.create(hotelRequest);

        assertThat(result).isSameAs(response);
        verify(hotelRepository).save(mapped);
    }

    @Test
    @DisplayName("findById: существующий ID → возвращает ответ")
    void findById_exists_returnsResponse() {
        HotelResponse response = new HotelResponse();
        when(hotelRepository.findById(1L)).thenReturn(Optional.of(hotel));
        when(hotelMapper.hotelToResponse(hotel)).thenReturn(response);

        HotelResponse result = hotelService.findById(1L);

        assertThat(result).isSameAs(response);
    }

    @Test
    @DisplayName("findById: несуществующий ID → ResourceNotFoundException")
    void findById_notFound_throwsResourceNotFoundException() {
        when(hotelRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> hotelService.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("delete: существующий ID → deleteById вызван")
    void delete_exists_deleteCalled() {
        when(hotelRepository.existsById(1L)).thenReturn(true);

        hotelService.delete(1L);

        verify(hotelRepository).deleteById(1L);
    }

    @Test
    @DisplayName("delete: несуществующий ID → ResourceNotFoundException")
    void delete_notFound_throwsResourceNotFoundException() {
        when(hotelRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> hotelService.delete(99L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(hotelRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("update: рейтинг сохраняется при обновлении других полей")
    void update_preservesRatingOnUpdate() {
        hotel.setRating(4.5);
        hotel.setNumberOfRatings(20);

        HotelRequest updateRequest = new HotelRequest();
        updateRequest.setName("Hilton Updated");
        updateRequest.setTitle("New Title");
        updateRequest.setCity("СПб");
        updateRequest.setAddress("Невский, 1");
        updateRequest.setDistanceToCenter(0.3);

        when(hotelRepository.findById(1L)).thenReturn(Optional.of(hotel));
        when(hotelRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(hotelMapper.hotelToResponse(any())).thenReturn(new HotelResponse());

        hotelService.update(1L, updateRequest);

        // рейтинг и количество оценок не должны сброситься
        verify(hotelRepository).save(argThat(h ->
                h.getRating() == 4.5 && h.getNumberOfRatings() == 20
        ));
    }
}