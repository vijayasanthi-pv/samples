package com.forescout.challenge.impl;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;

import com.forescout.challenge.affinity.IPacket;
import com.forescout.challenge.affinity.IRoutingRule;
import com.forescout.challenge.affinity.IRoutingRule.AttributeKey;
import com.forescout.challenge.affinity.Utility;
import com.forescout.challenge.affinity.attributes.values.Pair;

public class Packet implements IPacket, Serializable {
	
	private static final long serialVersionUID = -2226687685731525241L;
	
	private Long dstAddr;
	private Long srcAddr;
	private Integer dstPort;
	private String protocol;
	
	public Packet(long srcAddr, long dstAddr, int dstPort, String protocol) {
		this.srcAddr = srcAddr;
		this.dstAddr = dstAddr;
		this.dstPort = dstPort;
		this.protocol = protocol;
	}

	@Override
	public long getDstAddr() {
		return this.dstAddr;
	}

	@Override
	public long getSrcAddr() {
		return this.srcAddr;
	}

	@Override
	public int getDstPort() {
		return this.dstPort;
	}

	@Override
	public String getProtocol() {
		return this.protocol;
	}

	@Override
	public IRoutingRule getClosestAffinityNetwork(Collection<IRoutingRule> rules,
			LinkedHashSet<AttributeKey> attributesPriority) {
		
		if (rules==null || attributesPriority==null)
			throw new IllegalArgumentException();
		
		Iterator<AttributeKey> attributeIterator = attributesPriority.iterator();
		Iterator<IRoutingRule> rulesIterator = rules.iterator();
		
		if (!attributeIterator.hasNext() || !rulesIterator.hasNext())
			throw new AssertionError("Collection is empty");
		
		AttributeKey attributeKey = attributeIterator.next();
		
		return getClosestNetwork(attributeKey,attributesPriority, rules);
			
	}

	private Collection<IRoutingRule> getClosestNetwork(AttributeKey attributeKey, Collection<IRoutingRule> rules) {
		return Collections.emptySet();
	}
	
	private IRoutingRule getClosestNetwork(AttributeKey attributeKey, LinkedHashSet<AttributeKey> attributesPriority, Collection<IRoutingRule> rules) {
		
		System.out.println("attributeKey :: "+attributeKey);
		System.out.println("attributesPriority :: "+attributesPriority);
		Iterator<IRoutingRule> rulesIterator = rules.iterator();

		IRoutingRule iRoutingRule = rulesIterator.next();
		Long maxScore = iRoutingRule.getMatchingScore(attributeKey, this);
		
		if (maxScore==0 && !rulesIterator.hasNext())
			return null;
		while(rulesIterator.hasNext()) {
			
			IRoutingRule rule = rulesIterator.next();
			
			long itemScore = rule.getMatchingScore(attributeKey, this);
			
			if (itemScore == maxScore) {
				if (itemScore == 0) {
					iRoutingRule = null;
				}else {
					
					Pair<Long, Integer> rulePair;
					Pair<Long, Integer> iRulePair;
					
					if (attributeKey.equals(IRoutingRule.AttributeKey.dstAddress)) {
						
						rulePair = Utility.cidrStringToNetAndMask((String)rule.getDstAddress().getArgument());
						iRulePair = Utility.cidrStringToNetAndMask((String)iRoutingRule.getDstAddress().getArgument());
						
						if (rulePair.getFirst() > iRulePair.getFirst()) {
							iRoutingRule = rule;
						}else if (rulePair.getFirst() == iRulePair.getFirst()) { //First is subnet
							if (rulePair.getSecond() > iRulePair.getSecond()) {
								iRoutingRule = rule;
							}else if (rulePair.getSecond() == iRulePair.getSecond()) { //Second is mask
								attributesPriority.remove(attributeKey);
								Iterator<AttributeKey> attributeIterator = attributesPriority.iterator();
								iRoutingRule = attributeIterator.hasNext()?getClosestNetwork(attributeIterator.next(),attributesPriority,rules)
										: rule.getId()<iRoutingRule.getId()?rule:iRoutingRule;
								//iRoutingRule = rule.getId()<iRoutingRule.getId()?rule:iRoutingRule;
							}
						}
					}		
					else {
						attributesPriority.remove(attributeKey);
						Iterator<AttributeKey> attributeIterator = attributesPriority.iterator();
						iRoutingRule = attributeIterator.hasNext()?getClosestNetwork(attributeIterator.next(),attributesPriority,rules)
								: rule.getId()<iRoutingRule.getId()?rule:iRoutingRule;
						//iRoutingRule = rule.getId()<iRoutingRule.getId()?rule:iRoutingRule;
					}
				}
            } else if (itemScore > maxScore) {
                maxScore = itemScore;
                iRoutingRule = rule;
            } else {
            	System.out.println("GGGGGGGGGGGGGG");
            }
		}
		
		return iRoutingRule;

	}

}
