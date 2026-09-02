package com.lineacademy.parkingbackend.repository;

import com.lineacademy.parkingbackend.domain.entity.FavoriteParkingLot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FavoriteParkingLotRepository extends JpaRepository<FavoriteParkingLot, Long> {
    Optional<FavoriteParkingLot> findByUserIdAndParkingLotId(Long userId, Long parkingLotId);

    List<FavoriteParkingLot> findAllByUserId(Long userId);

    boolean existsByUserEmailAndParkingLotId(String email, Long parkingLotId);
}