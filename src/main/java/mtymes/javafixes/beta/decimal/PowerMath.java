package mtymes.javafixes.beta.decimal;

import java.math.BigInteger;

import static java.lang.Math.min;

public class PowerMath {

    private static final long[] powersOf10L = {
            1L,                  // 10 ^ 0
            10L,                 // 10 ^ 1
            100L,                // 10 ^ 2
            1000L,               // 10 ^ 3
            10000L,              // 10 ^ 4
            100000L,             // 10 ^ 5
            1000000L,            // 10 ^ 6
            10000000L,           // 10 ^ 7
            100000000L,          // 10 ^ 8
            1000000000L,         // 10 ^ 9
            10000000000L,        // 10 ^ 10
            100000000000L,       // 10 ^ 11
            1000000000000L,      // 10 ^ 12
            10000000000000L,     // 10 ^ 13
            100000000000000L,    // 10 ^ 14
            1000000000000000L,   // 10 ^ 15
            10000000000000000L,  // 10 ^ 16
            100000000000000000L, // 10 ^ 17
            1000000000000000000L // 10 ^ 18
    };
    private static final BigInteger[] powersOf10B = {
            BigInteger.ONE,                          // 10 ^ 0
            BigInteger.TEN,                          // 10 ^ 1
            BigInteger.valueOf(100L),                // 10 ^ 2
            BigInteger.valueOf(1000L),               // 10 ^ 3
            BigInteger.valueOf(10000L),              // 10 ^ 4
            BigInteger.valueOf(100000L),             // 10 ^ 5
            BigInteger.valueOf(1000000L),            // 10 ^ 6
            BigInteger.valueOf(10000000L),           // 10 ^ 7
            BigInteger.valueOf(100000000L),          // 10 ^ 8
            BigInteger.valueOf(1000000000L),         // 10 ^ 9
            BigInteger.valueOf(10000000000L),        // 10 ^ 10
            BigInteger.valueOf(100000000000L),       // 10 ^ 11
            BigInteger.valueOf(1000000000000L),      // 10 ^ 12
            BigInteger.valueOf(10000000000000L),     // 10 ^ 13
            BigInteger.valueOf(100000000000000L),    // 10 ^ 14
            BigInteger.valueOf(1000000000000000L),   // 10 ^ 15
            BigInteger.valueOf(10000000000000000L),  // 10 ^ 16
            BigInteger.valueOf(100000000000000000L), // 10 ^ 17
            BigInteger.valueOf(1000000000000000000L) // 10 ^ 18
    };
    private static final long[] minUpscaleLimitForPowOf10 = {
            Long.MIN_VALUE / 1L,
            Long.MIN_VALUE / 10L,
            Long.MIN_VALUE / 100L,
            Long.MIN_VALUE / 1000L,
            Long.MIN_VALUE / 10000L,
            Long.MIN_VALUE / 100000L,
            Long.MIN_VALUE / 1000000L,
            Long.MIN_VALUE / 10000000L,
            Long.MIN_VALUE / 100000000L,
            Long.MIN_VALUE / 1000000000L,
            Long.MIN_VALUE / 10000000000L,
            Long.MIN_VALUE / 100000000000L,
            Long.MIN_VALUE / 1000000000000L,
            Long.MIN_VALUE / 10000000000000L,
            Long.MIN_VALUE / 100000000000000L,
            Long.MIN_VALUE / 1000000000000000L,
            Long.MIN_VALUE / 10000000000000000L,
            Long.MIN_VALUE / 100000000000000000L,
            Long.MIN_VALUE / 1000000000000000000L
    };
    private static final long[] maxUpscaleLimitForPowOf10= {
            Long.MAX_VALUE / 1L,
            Long.MAX_VALUE / 10L,
            Long.MAX_VALUE / 100L,
            Long.MAX_VALUE / 1000L,
            Long.MAX_VALUE / 10000L,
            Long.MAX_VALUE / 100000L,
            Long.MAX_VALUE / 1000000L,
            Long.MAX_VALUE / 10000000L,
            Long.MAX_VALUE / 100000000L,
            Long.MAX_VALUE / 1000000000L,
            Long.MAX_VALUE / 10000000000L,
            Long.MAX_VALUE / 100000000000L,
            Long.MAX_VALUE / 1000000000000L,
            Long.MAX_VALUE / 10000000000000L,
            Long.MAX_VALUE / 100000000000000L,
            Long.MAX_VALUE / 1000000000000000L,
            Long.MAX_VALUE / 10000000000000000L,
            Long.MAX_VALUE / 100000000000000000L,
            Long.MAX_VALUE / 1000000000000000000L
    };

    public static int maxLongPowerOf10() {
        return powersOf10L.length - 1;
    }

    public static int maxBigPowerOf10() {
        return powersOf10B.length - 1;
    }

    public static long powerOf10Long(int n) {
        return powersOf10L[n];
    }

    public static BigInteger powerOf10Big(int n) {
        return powersOf10B[n];
    }

    public static boolean canUpscaleLongByPowerOf10(long value, int n) {
        if (n >= 19) {
            return false;
        }
        return minUpscaleLimitForPowOf10[n] <= value && value <= maxUpscaleLimitForPowOf10[n];
    }

    public static long upscaleByPowerOf10(long value, int n) {
        long newValue = value;
        if (value != 0) {
            int newN = n;
            while (newN > 0 && newValue != 0) {
                int scale = min(maxLongPowerOf10(), newN);
                if (!canUpscaleLongByPowerOf10(newValue, scale)) {
                    throw new ArithmeticException("can't upscale long " + value + " by " + n + ". power of 10");
                }
                newValue *= powerOf10Long(scale);
                newN -= scale;
            }
        }
        return newValue;
    }

    public static long downscaleByPowerOf10(long value, int n) {
        while (n > 0 && value != 0) {
            int scale = min(maxLongPowerOf10(), n);
            value /= powerOf10Long(scale);
            n -= scale;
        }
        return value;
    }

    public static BigInteger upscaleByPowerOf10(BigInteger value, int n) {
        if (value.signum() != 0) {
            while (n > 0) {
                int scale = min(maxBigPowerOf10(), n);
                value = value.multiply(powerOf10Big(scale));
                n -= scale;
            }
        }
        return value;
    }

    public static BigInteger downscaleByPowerOf10(BigInteger value, int n) {
        while (n > 0 && value.signum() != 0) {
            int scale = min(maxBigPowerOf10(), n);
            value = value.divide(powerOf10Big(scale));
            n -= scale;
        }
        return value;
    }
}