package uz.paylite.uzcard.util;

public class CardUtil {

    public static String maskPan(String pan) {
        if (pan == null || pan.length() < 4) {
            return "****";
        }
        return "**** **** **** " + pan.substring(pan.length() - 4);
    }
}
