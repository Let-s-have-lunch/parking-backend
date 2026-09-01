package com.lineacademy.parkingbackend.dto.parking;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
public class SeoulParkingDto {

    @JsonProperty("GetParkingInfo")
    private GetParkingInfo getParkingInfo;

    @Getter
    @Setter
    @ToString
    public static class GetParkingInfo {
        @JsonProperty("list_total_count")
        private Integer listTotalCount;

        @JsonProperty("row")
        private List<SeoulParkingData> row;
    }

    @Getter
    @Setter
    @ToString
    public static class SeoulParkingData {
        @JsonProperty("PKLT_CD")
        private String pkltCd;

        @JsonProperty("PKLT_NM")
        private String pkltNm;

        @JsonProperty("ADDR")
        private String addr;

        // 💡 ".0"이 붙어오는 소수점 에러 방지를 위해 Double로 받습니다.
        @JsonProperty("TPKCT")
        private Double tpkct;

        @JsonProperty("NOW_PRK_VHCL_CNT")
        private Double nowPrkVhclCnt;
    }
}
