package co.uk.matejtymes.tdp;

import co.uk.matejtymes.tdp.Decimal.HugeDecimal;
import co.uk.matejtymes.tdp.Decimal.LongDecimal;

import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.Arrays;

import static co.uk.matejtymes.tdp.DecimalCreator.createDecimal;
import static co.uk.matejtymes.tdp.LongUtil.*;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.lang.String.format;

// todo: handle scale overflow and underflow
// todo: test this
// todo: speed up multiply by ten
class DecimalsIntern {

    // todo: add support for HugeDecimal
    static Decimal subtract(Decimal x, Decimal y, int scaleToUse, RoundingMode roundingMode) {
        return DecimalAdder.add(x, DecimalNegator.negate(y), scaleToUse, roundingMode);
    }

    // todo: add support for HugeDecimal
    static Decimal multiply(Decimal x, Decimal y, int scaleToUse, RoundingMode roundingMode) {

        // todo: extremely simplistic algorithm - please improve
        // todo: - this is extremely prone to overflow

        // todo: use BigInteger in case of overflow
        long unscaledValue = multiplyExact(x.unscaledValue(), y.unscaledValue());
//        long unscaledValue = BigInteger.valueOf(strippedX.unscaledValue())
//                .multiply(BigInteger.valueOf(strippedY.unscaledValue()))
//                .longValueExact();

        return createDecimal(
                unscaledValue,
                x.scale() + y.scale()
        ).descaleTo(scaleToUse, roundingMode);
    }

    // todo: handle zero divisor exception
    static Decimal divide(Decimal x, Decimal y, int scaleToUse, RoundingMode roundingMode) {

        long remainder = x.unscaledValue();
        long divisor = y.unscaledValue();

        long result = remainder / divisor;
        remainder = multiplyBy10Exact(remainder % divisor);
        int newScale = x.scale() - y.scale();

        if (newScale > scaleToUse) {
            return createDecimal(result, newScale)
                    .descaleTo(scaleToUse, roundingMode);
        } else {
            while (newScale < scaleToUse) {
                result = addExact(multiplyBy10Exact(result), (remainder / divisor));
                remainder = multiplyBy10Exact(remainder % divisor);
                newScale++;
            }
            long remainingDigit = remainder / divisor;
            result = roundBasedOnRemainder(result, remainingDigit, roundingMode);


            return createDecimal(result, newScale)
                    .descaleTo(scaleToUse, roundingMode);
        }
    }

    // todo: add support for HugeDecimal
    static int compare(Decimal x, Decimal y) {
        int scaleX = x.scale();
        int scaleY = y.scale();

        long unscaledX = x.unscaledValue();
        long unscaledY = y.unscaledValue();

        if (scaleX == scaleY) {
            return Long.compare(unscaledX, unscaledY);
        }

        int minScale = min(scaleX, scaleY);

        long scalerX = pow10(scaleX - minScale);
        long scalerY = pow10(scaleY - minScale);

        // by doing division instead of multiplication we prevent overflow
        long xScaledDownValue = unscaledX / scalerX;
        long yScaledDownValue = unscaledY / scalerY;

        int comparison = Long.compare(xScaledDownValue, yScaledDownValue);
        if (comparison != 0) {
            return comparison;
        } else {
            long xRemainder = subtractExact(unscaledX, multiplyExact(xScaledDownValue, scalerX));
            long yRemainder = subtractExact(unscaledY, multiplyExact(yScaledDownValue, scalerY));

            return Long.compare(xRemainder, yRemainder);
        }
    }

    static boolean areEqual(Decimal x, Decimal y) {
        return x.compareTo(y) == 0;
    }

    // todo: add support for HugeDecimal
    static int hashCode(Decimal d) {
        // its important that this starts as zero - this way we'll ignore trailing zeros
        int hashCode = 0;

        long remainder = d.unscaledValue();
        while (remainder > 0) {
            hashCode = (hashCode * 5) + (int) (remainder % 10);
            remainder /= 10;
        }

        return hashCode;
    }

    // todo: add support for HugeDecimal
    static String toPlainString(Decimal d, int minScaleToUse) {
        StringBuilder sb = new StringBuilder();

        boolean isNegative;
        if (d instanceof LongDecimal) {
            long unscaledValue = ((LongDecimal) d).unscaledValue;
            isNegative = unscaledValue < 0;
            sb.append(Long.toString(unscaledValue));
        } else if (d instanceof HugeDecimal) {
            BigInteger unscaledValue = ((HugeDecimal) d).unscaledValue;
            isNegative = unscaledValue.signum() < 0;
            sb.append(unscaledValue.toString());
        } else {
            throw new UnsupportedDecimalTypeException(d);
        }

        int currentScale = d.scale();
        int scaleToUse = max(currentScale, max(minScaleToUse, 0));
        if (currentScale < scaleToUse) {
            sb.append(arrayOfZeroChars(scaleToUse - currentScale));
        }

        if (scaleToUse > 0) {
            int prefixZerosOffset = isNegative ? 1 : 0;
            int firstDigitScale = scaleToUse - (sb.length() - prefixZerosOffset) + 1;
            if (firstDigitScale > 0) {
                sb.insert(prefixZerosOffset, arrayOfZeroChars(firstDigitScale));
            }
            int index = sb.length() - scaleToUse;
            sb.insert(index, '.');
        }

        return sb.toString();
    }

    // todo: in the future make sure the digit is only from 0 to 9 (currently the sign of the digit makes it a little bit awkward)
    // todo: turn remainingDigit into byte
    static long roundBasedOnRemainder(long valueBeforeRounding, long remainingDigit, RoundingMode roundingMode) {
        if (remainingDigit < -9 || remainingDigit > 9) {
            throw new IllegalArgumentException(format("Invalid remaining digit (%d). Should be only -9 to 9", remainingDigit));
        }

        long valueAfterRounding = valueBeforeRounding;

        if (remainingDigit != 0) {
            if (roundingMode == RoundingMode.UP) {
                if (remainingDigit > 0 && valueBeforeRounding >= 0) {
                    valueAfterRounding = incrementExact(valueBeforeRounding);
                } else if (remainingDigit < 0 && valueBeforeRounding < 0) {
                    valueAfterRounding = decrementExact(valueBeforeRounding);
                }
            } else if (roundingMode == RoundingMode.DOWN) {
                if (remainingDigit < 0 && valueBeforeRounding >= 0) {
                    valueAfterRounding = decrementExact(valueBeforeRounding);
                } else if (remainingDigit > 0 && valueBeforeRounding < 0) {
                    valueAfterRounding = incrementExact(valueBeforeRounding);
                }
            } else if (roundingMode == RoundingMode.CEILING) {
                if (remainingDigit > 0 && valueBeforeRounding >= 0) {
                    valueAfterRounding = incrementExact(valueBeforeRounding);
                }
            } else if (roundingMode == RoundingMode.FLOOR) {
                if (remainingDigit < 0 && valueBeforeRounding < 0) {
                    valueAfterRounding = decrementExact(valueBeforeRounding);
                }
            } else if (roundingMode == RoundingMode.HALF_UP) {
                if (remainingDigit >= 5 && valueBeforeRounding >= 0) {
                    valueAfterRounding = incrementExact(valueBeforeRounding);
                } else if (remainingDigit <= -5 && valueBeforeRounding < 0) {
                    valueAfterRounding = decrementExact(valueBeforeRounding);
                }
            } else if (roundingMode == RoundingMode.HALF_DOWN) {
                if (remainingDigit > 5 && valueBeforeRounding >= 0) {
                    valueAfterRounding = incrementExact(valueBeforeRounding);
                } else if (remainingDigit < -5 && valueBeforeRounding < 0) {
                    valueAfterRounding = decrementExact(valueBeforeRounding);
                }
            } else if (roundingMode == RoundingMode.HALF_EVEN) {
                if (valueBeforeRounding >= 0) {
                    if (remainingDigit > 5 || (remainingDigit == 5 && valueBeforeRounding % 2 != 0)) {
                        valueAfterRounding = incrementExact(valueBeforeRounding);
                    }
                } else {
                    if (remainingDigit < -5 || (remainingDigit == -5 && valueBeforeRounding % 2 != 0)) {
                        valueAfterRounding = decrementExact(valueBeforeRounding);
                    }
                }
            } else if (roundingMode == RoundingMode.UNNECESSARY) {
                throw new ArithmeticException("Rounding necessary");
            }
        }

        return valueAfterRounding;
    }

    private static final long TEN_MULTIPLICATION_UPPER_BOUND = Long.MAX_VALUE / 10;
    private static final long TEN_MULTIPLICATION_LOWER_BOUND = Long.MIN_VALUE / 10;

    // todo: add support for HugeDecimal
    private static long multiplyBy10Exact(long value) {
        if (value <= TEN_MULTIPLICATION_UPPER_BOUND && value >= TEN_MULTIPLICATION_LOWER_BOUND) {
            return value * 10;
        } else {
            return multiplyExact(value, 10L);
        }
    }

    private static char[] arrayOfZeroChars(int size) {
        char[] zeros = new char[size];
        Arrays.fill(zeros, '0');
        return zeros;
    }
}
