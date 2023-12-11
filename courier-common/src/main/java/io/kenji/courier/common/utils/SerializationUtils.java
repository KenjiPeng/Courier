package io.kenji.courier.common.utils;

import io.kenji.courier.common.exception.SerializerException;

import java.util.stream.IntStream;

/**
 * @Author Kenji Peng
 * @Description It is mainly for handling serializationType in protocol header,
 * serialization type 16 length. Less than 16 length followed by a 0, the convention is that the length of the serialized type cannot exceed 16
 * @Date 2023/4/25
 **/
public class SerializationUtils {
    private static final String PADDING_STRING = "0";
    /**
     * The convention serialization type has a maximum length of 16
     */
    public static final int MAX_SERIALIZATION_TYPE_LEN = 16;

    /**
     * Add 0 to a string of less than 16 length
     *
     * @param str
     * @return
     */
    public static String paddingString(String str) {
        str = transNullToEmpty(str);
        if (str.length() > MAX_SERIALIZATION_TYPE_LEN)
            throw new SerializerException("The length of SerializationType is more than the convention which is 16! Can not serialize " + str);
        int paddingLength = MAX_SERIALIZATION_TYPE_LEN - str.length();
        StringBuilder paddingString = new StringBuilder(str);
        IntStream.range(0, paddingLength).forEach(i -> paddingString.append(PADDING_STRING));
        return paddingString.toString();
    }

    /**
     * The string is removed by 0
     *
     * @param str
     * @return
     */
    public static String subString(String str) {
        str = transNullToEmpty(str);
        return str.replace(PADDING_STRING, "");
    }

    private static String transNullToEmpty(String str) {
        return str == null ? "" : str;
    }
}
