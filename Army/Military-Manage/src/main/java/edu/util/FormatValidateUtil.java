package edu.util;

import java.util.regex.Pattern;

public class FormatValidateUtil {

    private static final Pattern PHONE_CN = Pattern.compile("^1[3-9]\\d{9}$");
    private static final Pattern ID_CARD_15 = Pattern.compile("^\\d{15}$");
    private static final Pattern ID_CARD_18 = Pattern.compile("^\\d{17}[0-9Xx]$");

    public static boolean isValidPhone(String phone) {
        if (phone == null || phone.isBlank()) return false;
        return PHONE_CN.matcher(phone.trim()).matches();
    }

    public static boolean isValidIdCard(String idCard) {
        if (idCard == null || idCard.isBlank()) return false;
        String s = idCard.trim();
        return ID_CARD_15.matcher(s).matches() || ID_CARD_18.matcher(s).matches();
    }
}
