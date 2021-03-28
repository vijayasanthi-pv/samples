package com.forescout.challenge.affinity;

import com.forescout.challenge.affinity.attributes.values.Pair;

import java.util.Collection;
import java.util.StringTokenizer;

/**
 * Generic utility functions
 */
public class Utility {

    public static final String STRICT_IP_MATCH_REGEX = "^\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}$";

    /**
     * Clear dst and add all the elements in src to dest. At the end of the
     * operation dst and src have the same elements (if they implement the same
     * interface)
     *
     * @param <T>
     * @param dest The destination collection (must be mutable)
     * @param src The source collection
     */
    public static <T> void assignCollection(Collection<T> dest, Collection<T> src) {
        if (dest != src) { // same object guard
            dest.clear();
            if (src != null) {
                dest.addAll(src);
            }
        }
    }

    /**
     * Checks if the given ip is inside the given net.
     * @param ip   the ip.
     * @param net  the network address.
     * @param mask the network mask, in CIDR notation, i.e. number from 1 to 32.
     * @return true if ip is inside net, false otherwise.
     */
    public static boolean isIpInsideNet(long ip, long net, int mask) {
        return net >> (32 - mask) << (32 - mask) == ip >> (32 - mask) << (32 - mask);
    }

    /**
     * Checks if the given ip is inside the given net in CIDR format.
     * @param ip           the ip to check.
     * @param ipAndNetmask the network CIDR.
     * @return true if ip is inside net, false otherwise.
     */
    public static boolean isIpInsideNet(long ip, String ipAndNetmask) {
        StringTokenizer st = new StringTokenizer(ipAndNetmask, "/");
        if ( st.countTokens() == 2 ) { // found a '/'
            try {
                long net = Utility.string2Ip(st.nextToken());
                return isIpInsideNet(ip, net, Integer.parseInt(st.nextToken()));
            } catch ( NumberFormatException e ) {
                return false;
            }
        } else { // could be a simple IP address
            return ipAndNetmask.matches(STRICT_IP_MATCH_REGEX) && Utility.string2Ip(ipAndNetmask) == ip;
        }
    }

    /**
     * Converts an IP address from a string into a long
     * @param ip
     * @return long IP address
     */
    public static long string2Ip(String ip) {
        return string2Ip(ip, false);
    }

    /**
     * Converts an IP address from a string into a long
     * @param ip
     * @param anyIsMinus1 if true the symbol "any" is encoded as -1
     * @return long IP address
     */
    public static long string2Ip(String ip, boolean anyIsMinus1) {
        try {
            if ( ip == null || ip.equals("") || ip.equals("-") ) {
                return 0L;
            }

            if ( anyIsMinus1 && ip.equals("any") ) {
                return -1L;
            }

            StringTokenizer st = new StringTokenizer(ip, ".");
            long numParts = st.countTokens();
            if ( numParts == 0 ) {
                throw new IllegalArgumentException("Invalid IP address: empty argument");
            }
            if ( numParts > 4 ) {
                throw new IllegalArgumentException("Invalid IP address: too many tokens");
            }
            long ipInt = 0;
            while ( st.hasMoreTokens() ) {
                long tokenVal = Integer.parseInt(st.nextToken());
                ipInt = (ipInt << 8) + tokenVal;
            }
            return ipInt;
        } catch ( Exception e ) {
            return 0;
        }
    }

    /**
     * Takes a CIDR notation string (i.e. 10.0.0.0/8) and return a pair of the network as long, and the mask as integer.
     * @param cidrString the String to split
     * @return a Pair of Long and Integer representing the network and the mask respectively.
     */
    public static Pair<Long, Integer> cidrStringToNetAndMask(String cidrString) {
        String cidrBitsStr;
        String ip;
        try {
            ip = cidrString.substring(0, cidrString.indexOf('/'));
            cidrBitsStr = cidrString.substring(cidrString.indexOf('/') + 1);
        } catch ( StringIndexOutOfBoundsException e ) {
            throw new IllegalArgumentException("Supplied string is not a valid CIDR notation");
        }
        int cidrBits;
        try {
            cidrBits = Integer.parseInt(cidrBitsStr);
            if ( cidrBits < 0 ) {
                throw new IllegalArgumentException("Negative CIDR bits");
            }
            if ( cidrBits > 32 ) {
                throw new IllegalArgumentException("CIDR bits > 32");
            }
        } catch ( NumberFormatException e ) {
            throw new IllegalArgumentException("Argument address has invalid CIDR bits: " + e.getMessage());
        }
        return new Pair<>(Utility.string2Ip(ip), cidrBits);
    }

    /**
     * Creates a string from an IP address in long format.
     *
     * @param intIp The Integer representation of the IP address
     * @return a String representation of the given IP address
     */
    public static String ip2String(long intIp) {
        if (intIp > 0L) {
            return (intIp / 0x1000000) + "." + ((intIp / 0x10000) & 0xff) + "." + ((intIp / 0x100) & 0xff) + "." + (intIp & 0xff);
        }
        return "-";
    }

}
