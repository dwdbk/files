package com.futurmaster.demandplanning.cucumber.utils;

import com.futurmaster.demandplanning.model.ChronoType;
import com.futurmaster.demandplanning.model.Perimeter;
import com.futurmaster.demandplanning.model.PerimeterDto;
import com.google.protobuf.Timestamp;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class FormatUtils {

    private static final String DATE_FORMAT = "dd/MM/yyyy";
    private static final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat(DATE_FORMAT);

    /**
     * Transform String in Double. If the value is NULL return NULL and not an Exception.
     *
     * @param value, The value to transform
     * @return null, if the input is null or a Double
     */
    public static Double getDoubleValueOrNull(final String value) {
        if (value == null || value.equals("NULL")) {
            return null;
        } else {
            return Double.parseDouble(value);
        }
    }

    /**
     * Transform String in Long. If the value is NULL return NULL and not an Exception.
     *
     * @param value, The value to transform
     * @return null, if the input is null or a Long
     */
    public static Long getLongValueOrNull(final String value) {
        if (value == null || value.equals("NULL")) {
            return null;
        } else {
            return Long.parseLong(value);
        }
    }

    public static Date getDateValueOrNull(final String value) throws ParseException {
        if (value == null || value.equals("NULL")) {
            return null;
        } else if (value.equals("TODAY")) {
            return new Date();
        } else {
            //SimpleDateFormat format = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy", Locale.ENGLISH);
            SimpleDateFormat format = new SimpleDateFormat(DATE_FORMAT);
            return format.parse(value);
        }
    }

    public static Timestamp getTimestampValueOrNull(final String value) throws ParseException {
        if (value == null || value.equals("NULL")) {
            return null;
        } else {
            Date date = SIMPLE_DATE_FORMAT.parse(value);
            return Timestamp.newBuilder().setSeconds(date.toInstant().getEpochSecond()).build();
        }
    }

    private static Timestamp getTimestamp(String dateString) throws ParseException {
        Date date = SIMPLE_DATE_FORMAT.parse(dateString);
        return Timestamp.newBuilder().setSeconds(date.toInstant().getEpochSecond()).build();
    }

    public static String getStringValueOrNull(final String value) {
        if (value == null || value.equals("NULL")) {
            return null;
        } else if ("\"\"".equals(value)) {
            return "";
        } else {
            return value;
        }
    }


    /**
     * Transform String in Boolean. If the value is NULL return NULL and not an Exception.
     *
     * @param value, The value to transform
     * @return null, if the input is null or a Boolean
     */
    public static Boolean getBooleanValueOrFalse(final String value) {
        if (value == null) {
            return false;
        } else {
            return Boolean.parseBoolean(value);
        }
    }

    public static List<String> getListStringValueOrNull(final String value) {
        if (value == null) {
            return new ArrayList<>();
        } else if (value.equals("NULL")) {
            return null;
        } else {
            return Arrays.stream(value.split(",")).map(String::trim).toList();
        }
    }

    public static List<ChronoType> getListChronoTypeValueOrNull(final String value) {
        if (value == null) {
            return new ArrayList<>();
        } else if (value.equals("NULL")) {
            return null;
        } else {
            return Arrays.stream(value.split(",")).map(ChronoType::valueOf).toList();
        }
    }

    public static List<Long> getListLongValueOrNull(final String value) {
        if (value == null) {
            return new ArrayList<>();
        } else if (value.equals("NULL")) {
            return null;
        } else {
            return Arrays.stream(value.split(",")).map(s -> Long.parseLong(s.trim())).toList();
        }
    }

    public static List<Double> getListDoubleValueOrNull(final String value) {
        if (value == null) {
            return new ArrayList<>();
        } else if (value.equals("NULL")) {
            return null;
        } else {
            return Arrays.stream(value.split(",")).map(s -> (!s.equals("NULL")) ? Double.parseDouble(s.trim()) : null).toList();
        }
    }

    public static List<Integer> getListIntegerValueOrNull(final String value) {
        if (value == null) {
            return new ArrayList<>();
        } else if (value.equals("NULL")) {
            return null;
        } else {
            return Arrays.stream(value.split(",")).map(s -> Integer.parseInt(s.trim())).toList();
        }
    }

    /**
     * Transform String in Map<String, List<String>>. If the value is NULL return NULL and not an Exception.
     *
     * @param value, The value to transform form like : 1:2$3,5;2:2$3,5;3:2
     *               ; => separate the different row of a map
     *               : => separate the key and value of the map
     *               , => separate the different row of a list
     * @return null, if the input is null or a Map<String, List<String>>
     */
    public static Map<String, List<String>> getMapOfList(final String value) {
        Map<String, List<String>> result = new HashMap<>();
        if (value == null) {
            return null;
        } else {
            String[] rows = value.split(";");
            for (String row : rows) {
                List<String> keyValue = Arrays.asList(row.split(":"));
                String key = keyValue.get(0);
                String val = keyValue.get(1);
                List<String> list = Arrays.asList(val.split(","));
                result.put(key, list);
            }
        }
        return result;
    }

    /**
     * Transform String in Integer. If the value is NULL return NULL and not an Exception.
     *
     * @param value, The value to transform
     * @return null, if the input is null or a Integer
     */
    public static Integer getIntegerValueOrNull(final String value) {
        if (value == null || "NULL".equals(value)) {
            return null;
        } else {
            return Integer.parseInt(value);
        }
    }

    public static Map<ChronoType, String> getModelByChronoValueOrNull(final String value) {
        Map<ChronoType, String> mapResult = new HashMap<>();
        if (value == null) {
            return new HashMap<>();
        } else if (value.equals("NULL")) {
            return null;
        } else {
            Arrays.stream(value.split(",")).forEach(s -> mapResult.put(ChronoType.valueOf(s.split(":")[0]), s.split(":")[1]));
            return mapResult;
        }
    }
        public static PerimeterDto.Status getPerimeterDtoStatusValueOrNull(final String value) {
            if (value == null) {
                return null;
            } else if (value.equals("NULL")) {
                return null;
            } else {
                return PerimeterDto.Status.valueOf(value);
            }
        }
    public static Perimeter.Status getPerimeterStatusValueOrNull(final String value) {
        if (value == null) {
            return null;
        } else if (value.equals("NULL")) {
            return null;
        } else {
            return Perimeter.Status.valueOf(value);
        }
    }

}
