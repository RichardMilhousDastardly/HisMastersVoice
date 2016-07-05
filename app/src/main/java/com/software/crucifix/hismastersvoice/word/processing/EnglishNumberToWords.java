package com.software.crucifix.hismastersvoice.word.processing;

/**
 * Created by MUTTLEY on 27/05/2016.
 */
import java.text.DecimalFormat;

public class EnglishNumberToWords {

    private static final String[] tensNames = { "", " ten", " twenty", " thirty", " forty", " fifty", " sixty", " seventy", " eighty", " ninety" };
    private static final String[] numberNames = { "", " one", " two", " three", " four", " five", " six", " seven", " eight", " nine", " ten", " eleven", " twelve", " thirteen", " fourteen", " fifteen", " sixteen", " seventeen", " eighteen", " nineteen" };

    private static final String HUNDRED = " hundred";
    private static final String THOUSAND = " thousand ";
    private static final String ONE_THOUSAND = "one thousand ";
    private static final String MILLION = " million ";
    private static final String BILLION = " billion ";

    private EnglishNumberToWords() {
    }

    /**
     *
     * @param _number
     * @return
     */
    private static String convertLessThanOneThousand(final int _number) {
        String temporarily;
        int number = _number;

        if ((number % 100) < 20) {
            temporarily = numberNames[number % 100];
            number /= 100;
        } else {
            temporarily = numberNames[number % 10];
            number /= 10;

            temporarily = tensNames[number % 10] + temporarily;
            number /= 10;
        }
        if (number == 0) {
            return temporarily;
        }
        return numberNames[number] +  HUNDRED + temporarily;
    }

    /**
     *
     * @param number
     * @return
     */
    public static String convert(final long number) {
        // 0 to 999 999 999 999
        if (number == 0) {
            return "zero";
        }

        String snumber = Long.toString(number);

        // pad with "0"
        final String mask = "000000000000";
        final DecimalFormat decimalFormat = new DecimalFormat(mask);
        snumber = decimalFormat.format(number);

        // XXXnnnnnnnnn
        final int billions = Integer.parseInt(snumber.substring(0, 3));
        // nnnXXXnnnnnn
        final int millions = Integer.parseInt(snumber.substring(3, 6));
        // nnnnnnXXXnnn
        final int hundredThousands = Integer.parseInt(snumber.substring(6, 9));
        // nnnnnnnnnXXX
        final int thousands = Integer.parseInt(snumber.substring(9, 12));

        String tradBillions;
        switch (billions) {
            case 0:
                tradBillions = "";
                break;
            case 1:
                tradBillions = convertLessThanOneThousand(billions) + BILLION;
                break;
            default:
                tradBillions = convertLessThanOneThousand(billions) + BILLION;
        }
        String result = tradBillions;

        String tradMillions;
        switch (millions) {
            case 0:
                tradMillions = "";
                break;
            case 1:
                tradMillions = convertLessThanOneThousand(millions) + MILLION;
                break;
            default:
                tradMillions = convertLessThanOneThousand(millions) + MILLION;
        }
        result = result + tradMillions;

        String tradHundredThousands;
        switch (hundredThousands) {
            case 0:
                tradHundredThousands = "";
                break;
            case 1:
                tradHundredThousands = ONE_THOUSAND;
                break;
            default:
                tradHundredThousands = convertLessThanOneThousand(hundredThousands) + THOUSAND;
        }
        result = result + tradHundredThousands;

        String tradThousand;
        tradThousand = convertLessThanOneThousand(thousands);
        result = result + tradThousand;

        // remove extra spaces!
        return result.replaceAll("^\\s+", "").replaceAll("\\b\\s{2,}\\b", " ");
    }
}