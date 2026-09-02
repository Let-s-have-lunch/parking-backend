package com.lineacademy.parkingbackend.domain.entity;

import com.lineacademy.parkingbackend.domain.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "favorite_parking_lots", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "parking_lot_id"})
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FavoriteParkingLot extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parking_lot_id", nullable = false)
    private ParkingLot parkingLot;

    @Builder
    public FavoriteParkingLot(User user, ParkingLot parkingLot) {
        this.user = user;
        this.parkingLot = parkingLot;
    }
}