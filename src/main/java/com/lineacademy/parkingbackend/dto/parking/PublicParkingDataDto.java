package com.lineacademy.parkingbackend.dto.parking;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class PublicParkingDataDto {

    @JsonProperty("prkplceNo")
    private String prkplceNo; // 주차장관리번호

    @JsonProperty("prkplceNm")
    private String prkplceNm; // 주차장명

    @JsonProperty("prkplceSe")
    private String prkplceSe; // 주차장구분

    @JsonProperty("prkplceType")
    private String prkplceType; // 주차장유형

    @JsonProperty("rdnmadr")
    private String rdnmadr; // 소재지도로명주소

    @JsonProperty("lnmadr")
    private String lnmadr; // 소재지지번주소

    @JsonProperty("prkcmprt")
    private Integer prkcmprt; // 주차구획수 (기존 prkcmptnc -> prkcmprt)

    @JsonProperty("feedingSe")
    private String feedingSe; // 급지구분

    @JsonProperty("enforceSe")
    private String enforceSe; // 부제시행구분

    @JsonProperty("operDay")
    private String operDay; // 운영요일

    @JsonProperty("weekdayOperOpenHhmm")
    private String weekdayOperOpenHhmm; // 평일운영시작시각

    @JsonProperty("weekdayOperColseHhmm")
    private String weekdayOperColseHhmm; // 평일운영종료시각 (오타 그대로)

    @JsonProperty("satOperOperOpenHhmm")
    private String satOperOperOpenHhmm; // 토요일운영시작시각 (오타 그대로)

    @JsonProperty("satOperCloseHhmm")
    private String satOperCloseHhmm; // 토요일운영종료시각

    @JsonProperty("holidayOperOpenHhmm")
    private String holidayOperOpenHhmm; // 공휴일운영시작시각

    @JsonProperty("holidayCloseOpenHhmm")
    private String holidayCloseOpenHhmm; // 공휴일운영종료시각 (오타 그대로)

    @JsonProperty("parkingchrgeInfo")
    private String parkingchrgeInfo; // 요금정보

    @JsonProperty("basicTime")
    private Integer basicTime; // 주차기본시간

    @JsonProperty("basicCharge")
    private Integer basicCharge; // 주차기본요금

    @JsonProperty("addUnitTime")
    private Integer addUnitTime; // 추가단위시간

    @JsonProperty("addUnitCharge")
    private Integer addUnitCharge; // 추가단위요금

    @JsonProperty("dayCmmtktAdjTime")
    private String dayCmmtktAdjTime; // 1일주차권요금적용시간

    @JsonProperty("dayCmmtkt")
    private Integer dayCmmtkt; // 1일주차권요금

    @JsonProperty("monthCmmtkt")
    private Integer monthCmmtkt; // 월정기권요금

    @JsonProperty("metpay")
    private String metpay; // 결제방법

    @JsonProperty("spcmnt")
    private String spcmnt; // 특기사항

    @JsonProperty("institutionNm")
    private String institutionNm; // 관리기관명

    @JsonProperty("phoneNumber")
    private String phoneNumber; // 전화번호

    @JsonProperty("latitude")
    private BigDecimal latitude; // 위도

    @JsonProperty("longitude")
    private BigDecimal longitude; // 경도

    @JsonProperty("pwdbsPpkZoneYn")
    private String pwdbsPpkZoneYn; // 장애인전용주차구역보유여부

    @JsonProperty("referenceDate")
    private LocalDate referenceDate; // 데이터기준일자

    @JsonProperty("insttCode")
    private String insttCode;

    @JsonProperty("insttNm")
    private String insttNm;
}
