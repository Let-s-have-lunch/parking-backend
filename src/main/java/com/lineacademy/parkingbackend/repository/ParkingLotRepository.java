package com.lineacademy.parkingbackend.repository;

import com.lineacademy.parkingbackend.domain.entity.ParkingLot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface ParkingLotRepository extends JpaRepository<ParkingLot, Long> {
    // 관리번호로 기존 주차장 존재 여부 확인
    Optional<ParkingLot> findByParkingLotNo(String parkingLotNo);

    List<ParkingLot> findByLatitudeBetweenAndLongitudeBetween(
            BigDecimal minLat, BigDecimal maxLat,
            BigDecimal minLng, BigDecimal maxLng
    );
}
