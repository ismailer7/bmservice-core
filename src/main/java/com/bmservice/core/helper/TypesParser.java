package com.bmservice.core.helper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TypesParser {

    public static int safeParseInt(final Object numericValue) {
        try {
            return Integer.parseInt(String.valueOf(numericValue));
        }
        catch (NumberFormatException ex) {
            return 0;
        }
    }
}
