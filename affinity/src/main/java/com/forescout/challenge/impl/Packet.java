package com.forescout.challenge.impl;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.forescout.challenge.affinity.IPacket;
import com.forescout.challenge.affinity.IRoutingRule;
import com.forescout.challenge.affinity.IRoutingRule.AttributeKey;

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

		//No rule matches with the given attribute across all rules, return null
		//scoreMap = scoreMap.entrySet().stream().filter(entry -> entry.getValue() > 0L).collect(Collectors.toMap(routingRule -> routingRule.getKey(), score -> score.getValue()));
		
		//if (scoreMap.isEmpty())
			//return null;

		if (attributeIterator.hasNext()) {
			//Discard the rules that does not match the attribute
			//and move to the next attribute rule matching across all attributes
			rules = scoreMap.entrySet().stream()
					.filter(entry->entry.getValue()>0L)
					.map(Map.Entry::getKey)
					.collect(Collectors.toSet());

			//Also remove the current attributeKey as its already done 
			LinkedHashSet<AttributeKey> updateAttributesPriority = new LinkedHashSet<>();
			attributeIterator.forEachRemaining(item->updateAttributesPriority.add(item));
			rule = processRoutingRules(rules,updateAttributesPriority);			
		} else {

			//Only if 1 element exists
			if (scoreMap.entrySet().stream().count()==1L)
				rule = scoreMap.keySet().stream().findFirst().get();

			//if more than one rule satisfies all the attributes
			//return the rule that has least id i.e which is created first
			if (scoreMap.values().stream().filter(value->value>=1L).count()>1L)	{
				rule = rules.stream().min((rule1,rule2)->((Integer)rule1.getId()).compareTo((Integer)(rule2.getId()))).get();
			}
		}
		return rule;
		
		//NOTE: This method is in enhancement for the comparing attributes across multiple rules
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
     * Returns the {@Map <RoutingRule,Long} routing rules with their scores for a 
     * given attribute
     * @param rules The pool of rules where the matching score is computed on
     * @param attributesKey against which the routing rules are scored
     * @return
     */
	private Map<IRoutingRule,Long> getAttributeRulesScore(AttributeKey attributeKey, Collection<IRoutingRule> rules) {
		return rules.stream().collect(Collectors.toMap(Function.identity(), routingRule -> routingRule.getMatchingScore(attributeKey, this)));
	}
}
