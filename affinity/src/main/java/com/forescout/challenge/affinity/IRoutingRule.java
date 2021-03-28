package com.forescout.challenge.affinity;

import com.forescout.challenge.affinity.attributes.Attribute;
import java.util.Arrays;
import java.util.LinkedHashSet;

/**
 * Represents a rule for routing packet instances of type {@link IPacket}
 * through networks
 */
public interface IRoutingRule {

    /**
     * This is the default priority
     */
    public static final LinkedHashSet<AttributeKey> defaultAttributesPriority = new LinkedHashSet<>(
            Arrays.asList(
                    AttributeKey.dstAddress,
                    AttributeKey.dstPort,
                    AttributeKey.srcAddresses,
                    AttributeKey.protocol
            )
    );

    /**
     * This enumeration is needed to map attributes instances of this class to
     * field values from the {@link IPacket} class
     */
    public enum AttributeKey {
        srcAddresses,
        dstAddress,
        protocol,
        dstPort
    }

    /**
     * Returns the match against the given attribute instance
     *
     * @param attributeKey The enum value identifying the attribute instance
     * @param packet The packet which the attribute is matched
     * @return The score of the matched attribute in this packet
     */
    Long getMatchingScore(AttributeKey attributeKey, IPacket packet);

    /**
     *
     * @return An ordered set of attribute keys representing the priority in
     * which attributes will be evaluated (topmost highest priority)
     */
    default LinkedHashSet<AttributeKey> getDefaultAttributesPriority() {
        return defaultAttributesPriority;
    };

    // Accessors

    /**
     * The unique id assigned to this rule
     */
    int getId();

    /**
     * A pool of addresses identifying subnetworks from packets come from
     */
    Attribute<?> getSrcAddresses();

    /**
     * The address of the destination network packets must be routed to
     */
    Attribute<?> getDstAddress();

    /**
     * The destination ports range packets must be routed to
     */
    Attribute<?> getDstPort();

    /**
     * The protocol of routed packets
     */
    Attribute<?> getProtocol();

}
