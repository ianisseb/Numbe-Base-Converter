package converter;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.Scanner;

class NumberConverter {

    private static final int SCALE = 30;      // internal precision, far more than the 5 digits we output
    private static final int FRAC_DIGITS = 5; // digits in the fractional part of the result

    static boolean isValidBase(int base) {
        return base >= 2 && base <= 36;
    }

    // integer part: source string -> BigInteger -> target string
    static String convert(String number, int sourceBase, int targetBase) {
        BigInteger value = new BigInteger(number, sourceBase);
        return value.toString(targetBase);
    }

    // fractional input like "ff.8" or ".5"
    static String convertFractional(String input, int sourceBase, int targetBase) {
        String[] parts = input.split("\\.", -1);
        if (parts.length != 2) {
            throw new NumberFormatException();               // e.g. "1.2.3"
        }

        String intPart = parts[0].isEmpty() ? "0" : parts[0];
        String fracPart = parts[1];

        String intResult = convert(intPart, sourceBase, targetBase);
        if (fracPart.isEmpty()) {
            return intResult;                                // "5." -> just the integer
        }
        if (fracPart.startsWith("+") || fracPart.startsWith("-")) {
            throw new NumberFormatException();               // BigInteger would accept a sign
        }

        BigDecimal value = fractionToDecimal(fracPart, sourceBase);
        return intResult + "." + decimalToFraction(value, targetBase);
    }

    // digits after the dot -> decimal value between 0 and 1
    private static BigDecimal fractionToDecimal(String frac, int sourceBase) {
        BigDecimal numerator = new BigDecimal(new BigInteger(frac, sourceBase));
        BigDecimal denominator = new BigDecimal(BigInteger.valueOf(sourceBase).pow(frac.length()));
        return numerator.divide(denominator, SCALE, RoundingMode.HALF_UP);
    }

    // decimal fraction -> digits in the target base (multiply-by-base loop)
    private static String decimalToFraction(BigDecimal value, int targetBase) {
        BigDecimal base = BigDecimal.valueOf(targetBase);
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < FRAC_DIGITS; i++) {
            value = value.multiply(base);
            BigInteger digit = value.toBigInteger();         // integer part = next digit
            sb.append(Character.forDigit(digit.intValue(), targetBase));
            value = value.subtract(new BigDecimal(digit));   // keep only the fractional part
        }
        return sb.toString();
    }
}

public class Main {
    private static final Scanner sc = new Scanner(System.in);

    public static void main(String[] args) {
        while (true) {
            System.out.println("Enter two numbers in format: {source base} {target base} (To quit type /exit)");
            String line = sc.nextLine().trim();

            if (line.equals("/exit")) {
                return;
            }

            String[] parts = line.split("\\s+");
            if (parts.length != 2) {
                System.out.println("Invalid input");
                continue;
            }

            int sourceBase;
            int targetBase;
            try {
                sourceBase = Integer.parseInt(parts[0]);
                targetBase = Integer.parseInt(parts[1]);
            } catch (NumberFormatException e) {
                System.out.println("Invalid input");
                continue;
            }

            if (!NumberConverter.isValidBase(sourceBase) || !NumberConverter.isValidBase(targetBase)) {
                System.out.println("Invalid base");
                continue;
            }

            conversionLoop(sourceBase, targetBase);
        }
    }

    private static void conversionLoop(int sourceBase, int targetBase) {
        while (true) {
            System.out.printf("Enter number in base %d to convert to base %d (To go back type /back)%n",
                    sourceBase, targetBase);
            String input = sc.nextLine().trim();

            if (input.equals("/back")) {
                return; // back to the outer loop
            }

            try {
                String result = input.contains(".")
                        ? NumberConverter.convertFractional(input, sourceBase, targetBase)
                        : NumberConverter.convert(input, sourceBase, targetBase);
                System.out.println("Conversion result: " + result);
            } catch (NumberFormatException e) {
                System.out.println("Invalid number for base " + sourceBase);
            }
        }
    }
}