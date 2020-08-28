package no.met.forecasts;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Date;
import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class LocationForecast {
    private String type;
    private Geometry geometry;
    private Properties properties;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Geometry {
        private String type;
        private List<Number> coordinates;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Properties {
        private Meta meta;
        @JsonProperty(value = "timeseries")
        private List<TimeSeries> timeSeries;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Meta {
        @JsonProperty(value = "updated_at")
        private String updatedAt;
        private Map<String, String> units;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class TimeSeries {
        private Date time;
        private TSData data;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class TSData {
        private Instant instant;

        @JsonProperty(value = "next_1_hours")
        private NextXHours next1Hours;

        @JsonProperty(value = "next_6_hours")
        private NextXHours next6Hours;

        @JsonProperty(value = "next_12_hours")
        private NextXHours next12Hours;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Instant {
        private Map<String, Number> details;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class NextXHours {
        private Summary summary;
        private Map<String, Number> details;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Summary {
        @JsonProperty(value = "symbol_code")
        private String symbolCode;
    }
}