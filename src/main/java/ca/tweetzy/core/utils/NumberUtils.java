package ca.tweetzy.core.utils;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

/**
 * The current file has been created by Kiran Hart
 * Date Created: 5/15/2020
 * Time Created: 11:11 AM
 * Usage of any code found within this class is prohibited unless given explicit permission otherwise.
 */
public class NumberUtils {

    public static String formatEconomy(char currencySymbol, double number) {
        return currencySymbol + formatNumber(number);
    }

    public static String formatNumber(double number) {
        DecimalFormat decimalFormatter = new DecimalFormat(number == Math.ceil(number) ? "#,###" : "#,###.00");

        // This is done to specifically prevent the NBSP character from printing in foreign languages.
        DecimalFormatSymbols symbols = decimalFormatter.getDecimalFormatSymbols();
        symbols.setGroupingSeparator(',');

        decimalFormatter.setDecimalFormatSymbols(symbols);
        return decimalFormatter.format(number);
    }

    public static String formatWithSuffix(long count) {
        if (count < 1000) return String.valueOf(count);
        int exp = (int) (Math.log(count) / Math.log(1000));
        return String.format("%.1f%c", count / Math.pow(1000, exp),
                "kMGTPE".charAt(exp - 1)).replace(".0", "");
    }

    public static boolean isInt(String number) {
        if (number == null || number.equals(""))
            return false;
        try {
            Integer.parseInt(number);
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
    }

    public static boolean isDouble(String number) {
        if (number == null || number.equals(""))
            return false;
        try {
            Double.parseDouble(number);
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
    }

    public static boolean isNumeric(String s) {
        if (s == null || s.equals(""))
            return false;
        return s.matches("[-+]?\\d*\\.?\\d+");
    }
}
