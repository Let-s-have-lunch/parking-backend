package com.lineacademy.parkingbackend.dto.parking.response;

import com.lineacademy.parkingbackend.domain.entity.ParkingLot;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class ParkingLotResponse {

    // --- 기본 정보 ---
    private Long id;
    private String parkingLotNo;
    private String name;
    private String parkingLotSe;
    private String parkingLotType;
    private String roadAddress;
    private String landAddress;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private Integer capacity; // 프론트엔드 스펙에 맞춤 (기존 totalCapacity -> capacity)

    // --- 운영 정보 ---
    private String feedingSe;
    private String enforceSe;
    private String operDay;
    private String weekdayOperOpen;
    private String weekdayOperClose;
    private String satOperOpen;
    private String satOperClose;
    private String holidayOperOpen;
    private String holidayOperClose;

    // --- 요금 정보 ---
    private String parkingChargeInfo;
    private Integer basicTime;       // 만약 엔티티에서 String으로 바꾸셨다면 String으로 맞춰주세요
    private Integer basicCharge;
    private Integer addUnitTime;
    private Integer addUnitCharge;
    private String dayTicketAdjTime;
    private Integer dayTicketCharge;
    private Integer monthTicketCharge;
    private String paymentMethod;

    // --- 부가 정보 ---
    private String spcmnt;
    private String institutionNm;
    private String phoneNumber;
    private String pwdbsPpkZoneYn;

    // --- 실시간 연동 정보 ---
    private boolean hasRealtimeData;
    private Integer currentAvailableSpots;
    private String congestionLevel;

    // --- 즐겨찾기 여부 ---
    private boolean isFavorite;

    public static ParkingLotResponse of(ParkingLot lot, Integer availableSpots) {
        return of(lot, availableSpots, false);
    }

    public static ParkingLotResponse of(ParkingLot lot, Integer availableSpots, boolean isFavorite) {
        return ParkingLotResponse.builder()
                .id(lot.getId())
                .parkingLotNo(lot.getParkingLotNo())
                .name(lot.getName())
                .parkingLotSe(lot.getParkingLotSe())
                .parkingLotType(lot.getParkingLotType())
                .roadAddress(lot.getRoadAddress())
                .landAddress(lot.getLandAddress())
                .latitude(lot.getLatitude())
                .longitude(lot.getLongitude())
                .capacity(lot.getCapacity())

                .feedingSe(lot.getFeedingSe())
                .enforceSe(lot.getEnforceSe())
                .operDay(lot.getOperDay())
                .weekdayOperOpen(lot.getWeekdayOperOpen())
                .weekdayOperClose(lot.getWeekdayOperClose())
                .satOperOpen(lot.getSatOperOpen())
                .satOperClose(lot.getSatOperClose())
                .holidayOperOpen(lot.getHolidayOperOpen())
                .holidayOperClose(lot.getHolidayOperClose())

                .parkingChargeInfo(lot.getParkingChargeInfo())
                .basicTime(lot.getBasicTime())
                .basicCharge(lot.getBasicCharge())
                .addUnitTime(lot.getAddUnitTime())
                .addUnitCharge(lot.getAddUnitCharge())
                .dayTicketAdjTime(lot.getDayTicketAdjTime())
                .dayTicketCharge(lot.getDayTicketCharge())
                .monthTicketCharge(lot.getMonthTicketCharge())
                .paymentMethod(lot.getPaymentMethod())

                .spcmnt(lot.getSpcmnt())
                .institutionNm(lot.getInstitutionNm())
                .phoneNumber(lot.getPhoneNumber())
                .pwdbsPpkZoneYn(lot.getPwdbsPpkZoneYn())
                .hasRealtimeData(lot.isRealtimeSupported())
                .currentAvailableSpots(availableSpots)
                .congestionLevel(calculateCongestion(lot.getCapacity(), availableSpots))
                .isFavorite(isFavorite)
                .build();
    }

    private static String calculateCongestion(Integer capacity, Integer availableSpots) {
        if (capacity == null || capacity <= 0 || availableSpots == null) {
            return "UNKNOWN";
        }
        double ratio = (double) availableSpots / capacity;
        if (ratio <= 0.2) return "CROWDED"; // 잔여 20% 이하
        if (ratio >= 0.7) return "SPACIOUS"; // 잔여 70% 이상
        return "NORMAL";
    }
}
