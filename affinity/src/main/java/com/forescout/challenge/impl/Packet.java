package com.forescout.challenge.impl;

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.forescout.challenge.affinity.IPacket;
import com.forescout.challenge.affinity.IRoutingRule;
import com.forescout.challenge.affinity.IRoutingRule.AttributeKey;
import com.forescout.challenge.affinity.Utility;
import com.forescout.challenge.affinity.attributes.Attribute;

/**
 * Represents a packet routed from a source network to a destination network
 */
public class Packet implements IPacket, Serializable {

	private static final long serialVersionUID = -2226687685731525241L;

	/**
	 * Attributes of a packet
	 */

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

	/**
	 * Returns the {@link RoutingRule} with the highest matching score. The
	 * destination network address can be obtained from the dstAddress property
	 * in the rule.
	 * @param rules The pool of rules where the matching score is computed on
	 * @param attributesPriority A ordered set of attributes representing the
	 * priority order in which attributes are evaluated
	 * @return
	 */
	@Override
	public IRoutingRule getClosestAffinityNetwork(Collection<IRoutingRule> rules,
			LinkedHashSet<AttributeKey> attributesPriority) {

		if (rules==null || attributesPriority==null)
			throw new IllegalArgumentException();

		if (attributesPriority.isEmpty() || rules.isEmpty())
			throw new AssertionError("Priority or Rules is empty");

		rules = discardMismatchRules(rules);

		//If no rule matches with the given attribute across all rules, return null
		return rules.isEmpty()?null:processRoutingRules(rules, attributesPriority);
	}


	/**
	 * Returns the {@link RoutingRule} with the highest matching score.
	 * @param rules The pool of rules where the matching score is computed on
	 * @param attributesPriority A ordered set of attributes representing the
	 * priority order in which attributes are evaluated
	 * @return
	 */
	private IRoutingRule processRoutingRules(Collection<IRoutingRule> rules,
			LinkedHashSet<AttributeKey> attributesPriority) {

		Iterator<AttributeKey> attributeIterator = attributesPriority.iterator();
		AttributeKey attributeKey = attributeIterator.next();

		IRoutingRule rule = null;

		Map<IRoutingRule, Long> scoreMap = getAttributeRulesScore(attributeKey, rules);

		if (attributeIterator.hasNext()) {
			//Remove the current attributeKey as its already done 
			LinkedHashSet<AttributeKey> updateAttributesPriority = new LinkedHashSet<>();
			attributeIterator.forEachRemaining(item->updateAttributesPriority.add(item));
			rule = processRoutingRules(rules,updateAttributesPriority);			
		} else {
			rule = scoreMap.keySet().stream().min((rule1,rule2)->((Integer)rule1.getId()).compareTo((Integer)(rule2.getId()))).get();
		}
		return rule;
	}
	
	
	/**
	 * Returns the {@Map <RoutingRule,Long} routing rules with their scores for a 
	 * given attribute
	 * @param rules The pool of rules where the matching score is computed on
	 * @param attributesKey against which the routing rules are scored
	 * @return
	 */
	private Map<IRoutingRule,Long> getAttributeRulesScore(AttributeKey attributeKey, Collection<IRoutingRule> rules) {
		return rules.stream().collect(Collectors.toMap(Function.identity(), routingRule -> routingRule.getMatchingScore(attributeKey, this)));
	}
	

	/**
	 * Returns the {@Collection <IRoutingRule} routing rules with their scores for a 
	 * given attribute
	 * @param rules The pool of rules where the matching score is computed on
	 * @param rules The pool of rules after discarding the mismatched rules
	 * @return
	 */
	private Collection<IRoutingRule> discardMismatchRules(Collection<IRoutingRule> rules) {

		return rules.stream().filter(iRoutingRule->{return !(iRoutingRule.getMatchingScore(AttributeKey.dstAddress,this)==0
				|| iRoutingRule.getMatchingScore(AttributeKey.srcAddresses,this)==0
				|| iRoutingRule.getMatchingScore(AttributeKey.dstPort,this)==0
				|| iRoutingRule.getMatchingScore(AttributeKey.protocol,this)==0);})
				.collect(Collectors.toSet());

	}
	

	/**
	 * Returns the {@RoutingRule} that has closest affinity for the destination
	 * @param rules The pool of rules where the affinity is computed on
	 * @return
	 */
	private IRoutingRule getDstAddressCloseAffinity(Collection<IRoutingRule> rules){

		Optional<IRoutingRule> optionalRule = rules.stream().max((rule1,rule2)->Utility.cidrStringToNetAndMask((String)rule1.getDstAddress().getArgument()).getFirst()
				.compareTo(Utility.cidrStringToNetAndMask((String)rule2.getDstAddress().getArgument()).getFirst()));

		if (optionalRule.isPresent()) {
			IRoutingRule rule = optionalRule.get();

			Set<IRoutingRule> rulesSubnet = rules.stream().filter(item->Utility.cidrStringToNetAndMask((String)item.getDstAddress().getArgument()).getFirst() ==
					Utility.cidrStringToNetAndMask((String)rule.getDstAddress().getArgument()).getFirst())
					.collect(Collectors.toSet());

			if (rulesSubnet.size()==1)
				return rulesSubnet.stream().findFirst().get();
			else {
				optionalRule = rulesSubnet.stream().max((rule1,rule2)->Utility.cidrStringToNetAndMask((String)rule1.getDstAddress().getArgument()).getSecond()
						.compareTo(Utility.cidrStringToNetAndMask((String)rule2.getDstAddress().getArgument()).getSecond()));
				if (optionalRule.isPresent()) {
					IRoutingRule ruleMask = optionalRule.get();

					Set<IRoutingRule> rulesMask = rulesSubnet.stream().filter(item->Utility.cidrStringToNetAndMask((String)item.getDstAddress().getArgument()).getSecond() ==
							Utility.cidrStringToNetAndMask((String)ruleMask.getDstAddress().getArgument()).getSecond())
							.collect(Collectors.toSet());

					if (rulesMask.size()==1)
						return rulesMask.stream().findFirst().get();
					else
						return rulesMask.stream().min((rule1,rule2)->((Integer)rule1.getId()).compareTo((Integer)(rule2.getId()))).get();
				}
			}
		}

		return null;
	}

	
	/**
	 * Returns the {@RoutingRule} that has closest affinity for the destination
	 * @param rules The pool of rules where the affinity is computed on
	 * @return
	 */
	private IRoutingRule getSrcAddressCloseAffinity(Collection<IRoutingRule> rules){
		
		Map<IRoutingRule, Object> srcAddressMap = rules.stream().collect(Collectors.toMap(Function.identity(), 
				routingRule -> ((Collection<Attribute<String>>) routingRule.getSrcAddresses()).stream().filter(srcAddress->Utility.isIpInsideNet(this.srcAddr, srcAddress.getArgument())).findFirst()));

		Optional<Entry<IRoutingRule, Object>> optionalRule = srcAddressMap.entrySet().stream().max((rule1,rule2)->Utility.cidrStringToNetAndMask((String)rule1.getValue()).getFirst()
				.compareTo(Utility.cidrStringToNetAndMask((String) rule2.getValue()).getFirst()));

		if (optionalRule.isPresent()) {
			Entry<IRoutingRule, Object> rule = optionalRule.get();

			Set<Entry<IRoutingRule, Object>> rulesSubnet = srcAddressMap.entrySet().stream().filter(item->Utility.cidrStringToNetAndMask((String)item.getValue()).getFirst() ==
					Utility.cidrStringToNetAndMask((String)rule.getValue()).getFirst())
					.collect(Collectors.toSet());

			if (rulesSubnet.size()==1)
				return rulesSubnet.stream().findFirst().get().getKey();
			else {
				optionalRule = rulesSubnet.stream().max((rule1,rule2)->Utility.cidrStringToNetAndMask((String)rule1.getValue()).getSecond()
						.compareTo(Utility.cidrStringToNetAndMask((String)rule2.getValue()).getSecond()));
				if (optionalRule.isPresent()) {
					Entry<IRoutingRule, Object> ruleMask = optionalRule.get();

					Set<Entry<IRoutingRule, Object>> rulesMask = rulesSubnet.stream().filter(item->Utility.cidrStringToNetAndMask((String)item.getValue()).getSecond() ==
							Utility.cidrStringToNetAndMask((String)ruleMask.getValue()).getSecond())
							.collect(Collectors.toSet());

					if (rulesMask.size()==1)
						return rulesMask.stream().findFirst().get().getKey();
					else
						return rulesMask.stream().min((rule1,rule2)->((Integer)rule1.getKey().getId()).compareTo((Integer)(rule2.getKey().getId()))).get().getKey();
				}
			}
		}
		return null;	
	}

}
