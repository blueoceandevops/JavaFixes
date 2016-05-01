package co.uk.matejtymes.tdp;

import java.math.RoundingMode;

import static co.uk.matejtymes.tdp.DecimalCreator.createDecimal;
import static co.uk.matejtymes.tdp.LongUtil.*;
import static java.lang.Math.max;

// todo: test it
class DecimalAdder {

    // todo: HUGEDECIMAL

    // todo: add support for HugeDecimal
    static Decimal add(Decimal x, Decimal y, int scaleToUse, RoundingMode roundingMode) {

        int scaleX = x.scale();
        int scaleY = y.scale();
        // todo: this is easy to read, but maybe
        // todo: use rather min(scaleToUse, scaleX, scaleY) to actually prevent unnecessary overflow
        int maxScale = max(scaleX, scaleY);

        long maxScaledValueX = multiplyExact(x.unscaledValue(), pow10(maxScale - scaleX));
        long maxScaledValueY = multiplyExact(y.unscaledValue(), pow10(maxScale - scaleY));

        return createDecimal(
                addExact(maxScaledValueX, maxScaledValueY),
                maxScale
        ).descaleTo(scaleToUse, roundingMode);
    }
}