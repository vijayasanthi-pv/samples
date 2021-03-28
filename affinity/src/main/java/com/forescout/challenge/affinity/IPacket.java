
package com.forescout.challenge.affinity;

import java.util.Collection;
import java.util.LinkedHashSet;

/**
 * Represents a packet routed from a source network to a destination network
 */
public interface IPacket {

    /**
     * The destination network address encoded in a long value. Less significant
     * address octet occupies the less significant byte.
     * @return
     */
    long getDstAddr();

    /**
     * The source network address encoded in a long value. Less significant
     * address octet occupies the less significant byte.
     *
     * @return
     */
    long getSrcAddr();

    /**
     * The port in the destination address this packet is targeted to
     * @return
     */
    int getDstPort();

    /**
     * The communication protocol this packet is of. Can be an L3 protocol like
     * "UDP", "ICMP", or "TCP"
     * @return
     */
    String getProtocol();

    /**
     * Returns the {@link RoutingRule} with the highest matching score. The
     * destination network address can be obtained from the dstAddress property
     * in the rule.
     * @param rules The pool of rules where the matching score is computed on
     * @param attributesPriority A ordered set of attributes representing the
     * priority order in which attributes are evaluated
     * @return
     */
    IRoutingRule getClosestAffinityNetwork(Collection<IRoutingRule> rules, LinkedHashSet<IRoutingRule.AttributeKey> attributesPriority);

}
