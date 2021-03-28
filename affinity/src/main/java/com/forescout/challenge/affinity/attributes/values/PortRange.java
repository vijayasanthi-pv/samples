package com.forescout.challenge.affinity.attributes.values;

import com.forescout.challenge.affinity.Validate;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PortRange implements Comparable<PortRange> {
    public static final String PORT_REGEX = "\\d{1,5}";
    public static final String SINGLE_PORT_REGEX = "^" + PORT_REGEX + "$";
    public static final String PORT_RANGE_REGEX = "^(" + PORT_REGEX + ")\\-(" + PORT_REGEX + ")$";

    public static final Pattern portRangePattern = Pattern.compile(PORT_RANGE_REGEX);

    private int begin   = 0;
    private int end     = 0;

    public PortRange() {
    }

    public PortRange(int port) {
        this.begin = port;
        this.end = port;
    }

    public PortRange(int begin, int end) {
        this.begin = begin;
        this.end   = end;
    }

    @SuppressWarnings("OverridableMethodCallInConstructor")
    public PortRange(PortRange that) {
        assign(that);
    }

    public void assign(PortRange that) {
        Validate.notNull(that, "Argument that cannot be null");
        this.begin = that.begin;
        this.end = that.end;
    }

    @Override
    @SuppressWarnings({"CloneDeclaresCloneNotSupported", "CloneDoesntCallSuperClone"})
    public PortRange clone() {
        return new PortRange(this);
    }

    @Override
    public boolean equals(Object obj) {
        //noinspection PointlessBooleanExpression
        if ( obj == null || obj.getClass().equals(this.getClass()) == false )
            return false;
        if ( this == obj )
            return true;
        PortRange portRange = (PortRange)obj;
        return
                this.begin == portRange.begin &&
                this.end == portRange.end;
    }

    @Override
    public int hashCode() {
        return
                Integer.valueOf(this.begin).hashCode() +
                Integer.valueOf(this.end).hashCode();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if ( this.begin == this.end ) {
            sb.append(this.begin);
        } else {
            sb.append(this.begin);
            sb.append("-");
            sb.append(this.end);
        }
        return sb.toString();
    }

    public static boolean isValidPort(String port) {
        if (port == null || port.isEmpty()) {
            return false;
        }
        try {
            int portVal = Integer.valueOf(port);
            return (portVal >= 0 && portVal <= 0xFFFF);
        } catch (Throwable e) {
            return false;
        }
    }

    public static boolean isValidPortRange(String begin, String end) {
        return isValidPortRange(begin, end, false);
    }

    public static boolean isValidPortRange(String begin, String end, boolean acceptSingleRangePort) {
        if (!isValidPort(begin)) {
            return false;
        }
        if (!isValidPort(end)) {
            return false;
        }
        try {
            int beginPort = Integer.valueOf(begin);
            int endPort = Integer.valueOf(end);
            return (acceptSingleRangePort ? endPort >= beginPort : endPort > beginPort);
        } catch (Throwable e) {
            return false;
        }
    }

    public static PortRange parse(String value) {
        return parse(value, false);
    }

    /**
     * Attempt to parse an port range from a string yielding the correspondent
     * object. Throws a runtime exception if the parsing fails.
     *
     * @param value The string to parse
     * @param acceptSingleRangePort When this flag is true the method does not
     * throw with a port range comprising only one port (e.g. 53-53).
     * These flag is used mainly for validating filters arguments.
     * In general it is not used to validate concrete port ranges.
     * @return The object representing the parsed value
     */
    public static PortRange parse(String value, boolean acceptSingleRangePort) {
        if (value == null || value.isEmpty()) {
            throw new IllegalArgumentException("Argument value cannot be null or empty");
        }
        if (value.matches(PortRange.SINGLE_PORT_REGEX)) {
            if (!isValidPort(value)) {
                throw new NumberFormatException("Invalid single port format");
            }
            return new PortRange(Integer.valueOf(value));
        } else if (value.matches(PortRange.PORT_RANGE_REGEX)) {
            Matcher m = PortRange.portRangePattern.matcher(value);
            if (m.matches()) {
                String begin = m.group(1);
                String end = m.group(2);
                if (!isValidPortRange(begin, end, acceptSingleRangePort)) {
                    throw new NumberFormatException("Invalid port range format");
                }
                return new PortRange(Integer.valueOf(begin), Integer.valueOf(end));
            } else {
                throw new NumberFormatException("Invalid port range format");
            }
        } else {
            throw new NumberFormatException("Invalid port range format");
        }
    }

    @Override
    public int compareTo(PortRange that) {
        Validate.notNull(that, "Argument that cannot be null");
        long rangeSize1 = this.getRangeSize();
        long rangeSize2 = that.getRangeSize();
        if ( rangeSize1 != rangeSize2 )
            return Long.valueOf(rangeSize1).compareTo(rangeSize2);
        if ( this.begin != that.begin )
            return Integer.valueOf(begin).compareTo(that.begin);
        return Integer.valueOf(end).compareTo(that.end);
    }

    public boolean matches(int port) {
        return port >= begin && port <= end;
    }

    public boolean overlaps(PortRange that) {
        Validate.notNull(that, "Argument that cannot be null");
        return !(that.begin > this.end || this.begin > that.end);
    }

    public boolean contains(PortRange that) {
        Validate.notNull(that, "Argument that cannot be null");
        return this.begin <= that.begin && this.end >= that.end;
    }

    public long getRangeSize() {
        return (long)(this.end - this.begin) + 1L;
    }

    /**
     * Return this subtracted by that. Convention used for return values:
     * (null, null) indicates this PortRange has been completely erased by subtracting PortRange that
     * (X, null) means a single PortRange X is left after the subtraction
     * (X, Y) means the subtraction has resulted in two PortRanges
     *
     * @param that
     * @return this minus that
     */
    public Pair<PortRange, PortRange> subtract(PortRange that) {
        if(that == null) return new Pair<>(this.clone(), null);
        PortRange left = this.clone();
        PortRange right = that.clone();

        // No overlapping
        if (left.end < right.begin || right.end < left.begin) {
            return new Pair<>(left, null);
        }

        // The intervals here overlap
        PortRange leftPart = null;
        if (left.begin < right.begin) {
            leftPart = new PortRange(left.begin, Math.min(left.end, right.begin - 1));
        }

        PortRange rightPart = null;
        if (left.end > right.end) {
            rightPart = new PortRange(Math.max(left.begin, right.end + 1), left.end);
        }

        if (leftPart != null) {
            return new Pair<>(leftPart, rightPart);
        }

        // If there is no left part the right part takes it over
        if (rightPart != null) {
            return new Pair<>(rightPart, null);
        }

        return new Pair<>(null, null);
    }

    @SuppressWarnings("unused")
    public int getBegin() {
        return this.begin;
    }

    @SuppressWarnings("unused")
    public void setBegin(int begin) {
        this.begin = begin;
    }

    public int getEnd() {
        return this.end;
    }

    public void setEnd(int end) {
        this.end = end;
    }
}
