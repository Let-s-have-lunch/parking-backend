package com.lineacademy.parkingbackend.domain.entity;

import com.lineacademy.parkingbackend.domain.common.BaseTimeEntity;
import com.lineacademy.parkingbackend.dto.parking.PublicParkingDataDto;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "parking_lots")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ParkingLot extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // --- 기본 정보 ---
    @Column(name = "parking_lot_no", nullable = false, unique = true, length = 50)
    private String parkingLotNo; // 주차장관리번호 (prkplceNo)

    @Column(nullable = false, length = 100)
    private String name; // 주차장명 (prkplceNm)

    @Column(name = "parking_lot_se", length = 50)
    private String parkingLotSe; // 주차장구분 (prkplceSe) - 공영/민영 등

    @Column(name = "parking_lot_type", length = 50)
    private String parkingLotType; // 주차장유형 (prkplceType) - 노상/노외 등

    @Column(name = "road_address", length = 200)
    private String roadAddress; // 소재지도로명주소 (rdnmadr)

    @Column(name = "land_address", length = 200)
    private String landAddress; // 소재지지번주소 (lnmadr)

    @Column(name = "capacity")
    private Integer capacity; // 주차구획수 (prkcmprt)

    // --- 운영 정보 ---
    @Column(name = "feeding_se", length = 50)
    private String feedingSe; // 급지구분 (feedingSe)

    @Column(name = "enforce_se", length = 50)
    private String enforceSe; // 부제시행구분 (enforceSe)

    @Column(name = "oper_day", length = 50)
    private String operDay; // 운영요일 (operDay)

    @Column(name = "weekday_oper_open", length = 10)
    private String weekdayOperOpen; // 평일운영시작시각 (weekdayOperOpenHhmm)

    @Column(name = "weekday_oper_close", length = 10)
    private String weekdayOperClose; // 평일운영종료시각 (weekdayOperColseHhmm)

    @Column(name = "sat_oper_open", length = 10)
    private String satOperOpen; // 토요일운영시작시각 (satOperOperOpenHhmm)

    @Column(name = "sat_oper_close", length = 10)
    private String satOperClose; // 토요일운영종료시각 (satOperCloseHhmm)

    @Column(name = "holiday_oper_open", length = 10)
    private String holidayOperOpen; // 공휴일운영시작시각 (holidayOperOpenHhmm)

    @Column(name = "holiday_oper_close", length = 10)
    private String holidayOperClose; // 공휴일운영종료시각 (holidayCloseOpenHhmm)

    // --- 요금 정보 ---
    @Column(name = "parking_charge_info", length = 50)
    private String parkingChargeInfo; // 요금정보 (parkingchrgeInfo) - 유료/무료

    @Column(name = "basic_time")
    private Integer basicTime; // 주차기본시간 (basicTime) - 단위: 분

    @Column(name = "basic_charge")
    private Integer basicCharge; // 주차기본요금 (basicCharge) - 단위: 원

    @Column(name = "add_unit_time")
    private Integer addUnitTime; // 추가단위시간 (addUnitTime)

    @Column(name = "add_unit_charge")
    private Integer addUnitCharge; // 추가단위요금 (addUnitCharge)

    @Column(name = "day_ticket_adj_time")
    private String dayTicketAdjTime; // 1일주차권요금적용시간 (dayCmmtktAdjTime)

    @Column(name = "day_ticket_charge")
    private Integer dayTicketCharge; // 1일주차권요금 (dayCmmtkt)

    @Column(name = "month_ticket_charge")
    private Integer monthTicketCharge; // 월정기권요금 (monthCmmtkt)

    @Column(name = "payment_method", length = 100)
    private String paymentMethod; // 결제방법 (metpay)

    // --- 기타 부가 정보 ---
    @Column(length = 500)
    private String spcmnt; // 특기사항 (spcmnt)

    @Column(name = "institution_nm", length = 100)
    private String institutionNm; // 관리기관명 (institutionNm)

    @Column(name = "phone_number", length = 50)
    private String phoneNumber; // 전화번호 (phoneNumber)

    @Column(name = "pwdbs_ppk_zone_yn", length = 10)
    private String pwdbsPpkZoneYn; // 장애인전용주차구역보유여부 (pwdbsPpkZoneYn)

    // --- 좌표 및 관리 정보 ---
    @Column(nullable = false, precision = 10, scale = 7)
    private BigDecimal latitude; // 위도 (latitude)

    @Column(nullable = false, precision = 11, scale = 7)
    private BigDecimal longitude; // 경도 (longitude)

    @Column(name = "reference_date")
    private LocalDate referenceDate; // 데이터기준일자 (referenceDate)

    @Column(name = "instt_code", length = 50)
    private String insttCode; // 제공기관코드 (instt_code)

    @Column(name = "instt_nm", length = 100)
    private String insttNm; // 제공기관기관명 (instt_nm)

    // --- 실시간 연동 플래그 (기존 유지) ---
    @Column(name = "realtime_parking_code", length = 50)
    private String realtimeParkingCode;

    @Column(name = "is_realtime_supported", nullable = false)
    private boolean isRealtimeSupported;

    @Builder
    private ParkingLot(String parkingLotNo, String name, String parkingLotSe, String parkingLotType,
                       String roadAddress, String landAddress, Integer capacity, String feedingSe, String enforceSe,
                       String operDay, String weekdayOperOpen, String weekdayOperClose, String satOperOpen, String satOperClose,
                       String holidayOperOpen, String holidayOperClose, String parkingChargeInfo, Integer basicTime, Integer basicCharge,
                       Integer addUnitTime, Integer addUnitCharge, String dayTicketAdjTime, Integer dayTicketCharge,
                       Integer monthTicketCharge,
                       String paymentMethod, String spcmnt, String institutionNm, String phoneNumber, String pwdbsPpkZoneYn,
                       BigDecimal latitude, BigDecimal longitude, LocalDate referenceDate, String insttCode, String insttNm,
                       String realtimeParkingCode, boolean isRealtimeSupported) {
        this.parkingLotNo = parkingLotNo;
        this.name = name;
        this.parkingLotSe = parkingLotSe;
        this.parkingLotType = parkingLotType;
        this.roadAddress = roadAddress;
        this.landAddress = landAddress;
        this.capacity = capacity;
        this.feedingSe = feedingSe;
        this.enforceSe = enforceSe;
        this.operDay = operDay;
        this.weekdayOperOpen = weekdayOperOpen;
        this.weekdayOperClose = weekdayOperClose;
        this.satOperOpen = satOperOpen;
        this.satOperClose = satOperClose;
        this.holidayOperOpen = holidayOperOpen;
        this.holidayOperClose = holidayOperClose;
        this.parkingChargeInfo = parkingChargeInfo;
        this.basicTime = basicTime;
        this.basicCharge = basicCharge;
        this.addUnitTime = addUnitTime;
        this.addUnitCharge = addUnitCharge;
        this.dayTicketAdjTime = dayTicketAdjTime;
        this.dayTicketCharge = dayTicketCharge;
        this.monthTicketCharge = monthTicketCharge;
        this.paymentMethod = paymentMethod;
        this.spcmnt = spcmnt;
        this.institutionNm = institutionNm;
        this.phoneNumber = phoneNumber;
        this.pwdbsPpkZoneYn = pwdbsPpkZoneYn;
        this.latitude = latitude;
        this.longitude = longitude;
        this.referenceDate = referenceDate;
        this.insttCode = insttCode;
        this.insttNm = insttNm;
        this.realtimeParkingCode = realtimeParkingCode;
        this.isRealtimeSupported = isRealtimeSupported;
    }

    public void updateInfo(PublicParkingDataDto dto) {
        this.name = dto.getPrkplceNm();
        this.parkingLotSe = dto.getPrkplceSe();
        this.parkingLotType = dto.getPrkplceType();
        this.roadAddress = dto.getRdnmadr();
        this.landAddress = dto.getLnmadr();
        this.capacity = dto.getPrkcmprt();
        this.feedingSe = dto.getFeedingSe();
        this.enforceSe = dto.getEnforceSe();
        this.operDay = dto.getOperDay();
        this.weekdayOperOpen = dto.getWeekdayOperOpenHhmm();
        this.weekdayOperClose = dto.getWeekdayOperColseHhmm();
        this.satOperOpen = dto.getSatOperOperOpenHhmm();
        this.satOperClose = dto.getSatOperCloseHhmm();
        this.holidayOperOpen = dto.getHolidayOperOpenHhmm();
        this.holidayOperClose = dto.getHolidayCloseOpenHhmm();
        this.parkingChargeInfo = dto.getParkingchrgeInfo();
        this.basicTime = dto.getBasicTime();
        this.basicCharge = dto.getBasicCharge();
        this.addUnitTime = dto.getAddUnitTime();
        this.addUnitCharge = dto.getAddUnitCharge();
        this.dayTicketAdjTime = dto.getDayCmmtktAdjTime();
        this.dayTicketCharge = dto.getDayCmmtkt();
        this.monthTicketCharge = dto.getMonthCmmtkt();
        this.paymentMethod = dto.getMetpay();
        this.spcmnt = dto.getSpcmnt();
        this.institutionNm = dto.getInstitutionNm();
        this.phoneNumber = dto.getPhoneNumber();
        this.pwdbsPpkZoneYn = dto.getPwdbsPpkZoneYn();
        this.latitude = dto.getLatitude();
        this.longitude = dto.getLongitude();
        this.referenceDate = dto.getReferenceDate();
        this.insttCode = dto.getInsttCode();
        this.insttNm = dto.getInsttNm();
    }

    public void updateRealtimeParkingCode(String realtimeParkingCode) {
        this.realtimeParkingCode = realtimeParkingCode;
    }

    public void updateRealtimeSupported(boolean realtimeSupported) {
        this.isRealtimeSupported = realtimeSupported;
    }
}
