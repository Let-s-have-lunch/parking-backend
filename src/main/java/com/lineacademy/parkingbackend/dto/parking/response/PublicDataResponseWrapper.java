package com.lineacademy.parkingbackend.dto.parking.response;

import com.lineacademy.parkingbackend.dto.parking.PublicParkingDataDto;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
public class PublicDataResponseWrapper {
    private Response response;

    @Getter
    @Setter
    public static class Response {
        private Header header;
        private Body body;
    }

    @Getter
    @Setter
    public static class Header {
        private String resultCode;
        private String resultMsg;
    }

    @Getter
    @Setter
    public static class Body {
        // 우리가 실제로 필요한 주차장 배열 데이터
        private List<PublicParkingDataDto> items;
        private int numOfRows;
        private int pageNo;
        private int totalCount;
    }
}