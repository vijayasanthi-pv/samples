package com.forescout.challenge.impl;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

import com.forescout.challenge.affinity.IPacket;
import com.forescout.challenge.affinity.IRoutingRule;
import com.forescout.challenge.affinity.attributes.Attribute;
import com.forescout.challenge.affinity.attributes.CollectionAttribute;
import com.forescout.challenge.affinity.attributes.values.PortRange;

/**
 * Represents a rule for routing packet instances of type {@link IPacket}
 * through networks
 */
public class RoutingRule implements IRoutingRule{

    /**
     * Routing rule attributes
     */
	
	private CollectionAttribute<Collection<Attribute<Object>>, Object> srcAddresses;
	private Attribute<String> dstAddress;
	private Attribute<String> protocol;
	private Attribute<PortRange> portRange;
	
    /**
     * id of the rule
     */
	private int id;
	private static int idInitializer;
	
    /**
     * Key used to keep track of current Attribute
     */
	public static AttributeKey currentAttributeKey;
	
	public RoutingRule(Set<String> srcAddresses, String dstAddress, String protocol,
			PortRange portRange) {
		
		Collection<Attribute<Object>> srcAddressesPool = srcAddresses.stream().map(source->new AttributeImpl<Object>(source)).collect(Collectors.toSet());
		this.srcAddresses = new CollectionAttributeImpl(srcAddressesPool);
		this.dstAddress = new AttributeImpl<String>(dstAddress);
		this.protocol = new AttributeImpl<String>(protocol);
		this.portRange = new AttributeImpl<PortRange>(portRange);
		
		id = ++idInitializer;
	}

	/**
     * Returns the match against the given attribute instance
     *
     * @param attributeKey The enum value identifying the attribute instance
     * @param packet The packet which the attribute is matched
     * @return The score of the matched attribute in this packet
     */
	@Override
	public Long getMatchingScore(AttributeKey attributeKey, IPacket packet) {
		
		if (attributeKey.equals(AttributeKey.srcAddresses)) {
			currentAttributeKey = AttributeKey.srcAddresses;
			Long score = this.srcAddresses.matchingScore(packet);
			return score;
		}else if (attributeKey.equals(AttributeKey.dstAddress)) {
			currentAttributeKey = AttributeKey.dstAddress;
			Long score = this.dstAddress.matchingScore(packet);
			return score;
		}else if (attributeKey.equals(AttributeKey.dstPort)) {
			currentAttributeKey = AttributeKey.dstPort;
			return this.portRange.matchingScore(packet);
		}else if (attributeKey.equals(AttributeKey.protocol)) {
			currentAttributeKey = AttributeKey.protocol;
			return this.protocol.matchingScore(packet);
		}
		return null;
	}

	@Override
	public int getId() {
		return this.id;
	}

	@Override
	public Attribute<?> getSrcAddresses() {
		return this.srcAddresses;
	}

	@Override
	public Attribute<?> getDstAddress() {
		return this.dstAddress;
	}

	@Override
	public Attribute<?> getDstPort() {
		return this.portRange;
	}

	@Override
	public Attribute<?> getProtocol() {
		return this.protocol;
	}
	
	@Override
	public String toString() {
		return "RoutingRule [srcAddresses=" + srcAddresses + ", dstAddress=" + dstAddress + ", protocol=" + protocol
				+ ", portRange=" + portRange + ", id=" + id + "]";
	}

}
