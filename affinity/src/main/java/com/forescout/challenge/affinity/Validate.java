package com.forescout.challenge.affinity;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Validators
 */
public class Validate {
    public static final String STRICT_CIDR_MATCH_GROUP_OCT_REGEX = "^(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})/(.+)$";

    public static <T> T notNull(final T object, final String message, final Object... values) {
        if (object == null) {
            throw new NullPointerException(String.format(message, values));
        }
        return object;
    }

    public static void validateCIDR(String cidr, boolean allowAny) throws RuntimeException {
        if (cidr == null || cidr.equals("") || (allowAny && cidr.equalsIgnoreCase("any"))) {
            return;
        }
        Pattern p = Pattern.compile(STRICT_CIDR_MATCH_GROUP_OCT_REGEX);
        Matcher m = p.matcher(cidr);
        if (!m.matches()) {
            throw new RuntimeException("Invalid IP format '" + cidr + "'");
        }

        for (int i = 1; i <= 4; i++) {
            int iIpSegment = 0;
            String ipSegment = m.group(i);
            if (ipSegment == null || ipSegment.length() <= 0) {
                throw new RuntimeException("Invalid IP format. Missing network mask");
            }
            try {
                iIpSegment = Integer.parseInt(ipSegment);
            } catch (NumberFormatException e) {
                throw new RuntimeException("Invalid IP octect " + ipSegment);
            }
            if (iIpSegment > 255) {
                throw new RuntimeException("Invalid IP octect value " + ipSegment);
            }
        }

        // Check the mask is ok
        if (m.group(5) != null) {
            int mask;
            try {
                mask = Integer.parseInt(m.group(5));
            } catch (NumberFormatException e) {
                RuntimeException ex = new RuntimeException("Invalid mask value " + m.group(5));
                throw ex;
            }
            if (mask < 0 || mask > 32) {
                RuntimeException ex = new RuntimeException("Invalid mask value " + mask);
                throw ex;
            }
        }
    }
}
