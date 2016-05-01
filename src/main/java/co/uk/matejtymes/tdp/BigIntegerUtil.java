package co.uk.matejtymes.tdp;

import java.math.BigInteger;

// todo: test
public class BigIntegerUtil {

    private static BigInteger BIG_INTEGER_MAX_LONG = BigInteger.valueOf(Long.MAX_VALUE);
    private static BigInteger BIG_INTEGER_MIN_LONG = BigInteger.valueOf(Long.MIN_VALUE);

    public static boolean canConvertToLong(BigInteger unscaledValue) {
        return unscaledValue.compareTo(BIG_INTEGER_MAX_LONG) <= 0 && unscaledValue.compareTo(BIG_INTEGER_MIN_LONG) >= 0;
    }
}