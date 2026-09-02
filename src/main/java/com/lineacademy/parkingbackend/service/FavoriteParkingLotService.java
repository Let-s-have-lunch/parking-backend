package com.lineacademy.parkingbackend.service;

import com.lineacademy.parkingbackend.domain.entity.FavoriteParkingLot;
import com.lineacademy.parkingbackend.domain.entity.ParkingLot;
import com.lineacademy.parkingbackend.domain.entity.User;
import com.lineacademy.parkingbackend.dto.parking.response.ParkingLotResponse;
import com.lineacademy.parkingbackend.repository.FavoriteParkingLotRepository;
import com.lineacademy.parkingbackend.repository.ParkingLotRepository;
import com.lineacademy.parkingbackend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FavoriteParkingLotService {

    private final FavoriteParkingLotRepository favoriteRepository;
    private final ParkingLotRepository parkingLotRepository;
    private final UserRepository userRepository;
    private final RealtimeParkingService realtimeParkingService;


    @Transactional
    public Mono<Boolean> toggleFavorite(String email, Long parkingLotId) {
        return Mono.fromCallable(() -> {
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("유저를 찾을 수 없습니다."));

            ParkingLot lot = parkingLotRepository.findById(parkingLotId)
                    .orElseThrow(() -> new RuntimeException("주차장을 찾을 수 없습니다."));

            return favoriteRepository.findByUserIdAndParkingLotId(user.getId(), lot.getId())
                    .map(favorite -> {
                        favoriteRepository.delete(favorite);
                        return false;
                    })
                    .orElseGet(() -> {
                        favoriteRepository.save(FavoriteParkingLot.builder().user(user).parkingLot(lot).build());
                        return true;
                    });
        }).subscribeOn(Schedulers.boundedElastic());
    }


    @Transactional(readOnly = true)
    public Flux<ParkingLotResponse> getMyFavorites(String email) {
        Mono<List<ParkingLot>> myFavoritesMono = Mono.fromCallable(() -> {
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("유저를 찾을 수 없습니다."));

            return favoriteRepository.findAllByUserId(user.getId()).stream()
                    .map(FavoriteParkingLot::getParkingLot)
                    .toList();
        }).subscribeOn(Schedulers.boundedElastic());

        return myFavoritesMono.flatMapMany(Flux::fromIterable)
                .flatMap(lot -> {
                    if (lot.isRealtimeSupported() && lot.getRealtimeParkingCode() != null) {
                        return realtimeParkingService.getAvailableSpots(lot.getRealtimeParkingCode())
                                .map(spots -> ParkingLotResponse.of(lot, spots))
                                .defaultIfEmpty(ParkingLotResponse.of(lot, null));
                    }
                    return Mono.just(ParkingLotResponse.of(lot, null));
                });
    }
}