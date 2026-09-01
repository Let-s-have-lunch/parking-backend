package com.lineacademy.parkingbackend.repository;

import org.springframework.stereotype.Repository;

// 임포트 오류 방지를 위해서 만든 파일입니다!!
// Favorite 담당 팀원분. 이 코드 지우고 덮어 씌우면 됩니다.


@Repository
public interface FavoriteParkingLotRepository {
    // 임시 목업(가짜) 메서드
    default boolean existsByUserEmailAndParkingLotId(String email, Long parkingLotId) {
        return false;
    }
}
