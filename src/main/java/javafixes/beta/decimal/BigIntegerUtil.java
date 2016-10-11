package javafixes.beta.decimal;

import java.math.BigInteger;

public class BigIntegerUtil {

    public static final BigInteger BIG_INTEGER_MAX_LONG = BigInteger.valueOf(Long.MAX_VALUE);
    public static final BigInteger BIG_INTEGER_MIN_LONG = BigInteger.valueOf(Long.MIN_VALUE);

    public static boolean canConvertToLong(BigInteger unscaledValue) {
        return unscaledValue.compareTo(BIG_INTEGER_MAX_LONG) <= 0 && unscaledValue.compareTo(BIG_INTEGER_MIN_LONG) >= 0;
    }
}