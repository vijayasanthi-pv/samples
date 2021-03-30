package com.forescout.challenge.impl;

import com.forescout.challenge.affinity.IPacket;
import com.forescout.challenge.affinity.IRoutingRule;
import com.forescout.challenge.affinity.IRoutingRule.AttributeKey;
import com.forescout.challenge.affinity.Utility;
import com.forescout.challenge.affinity.attributes.Attribute;
import com.forescout.challenge.affinity.attributes.values.Pair;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.forescout.challenge.affinity.Utility.cidrStringToNetAndMask;
import static java.util.stream.Collectors.toMap;

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
		return rules.isEmpty()?null:processRoutingRules(rules, attributesPriority.iterator());
	}


	/**
	 * Returns the {@link RoutingRule} with the highest matching score.
	 * @param rules The pool of rules where the matching score is computed on
	 * @param attributePriorityIterator A ordered set of attributes representing the
	 * priority order in which attributes are evaluated
	 * @return
	 */
	private IRoutingRule processRoutingRules(Collection<IRoutingRule> rules,
			Iterator<AttributeKey> attributePriorityIterator) {

		if (!attributePriorityIterator.hasNext())
			return null;
		
		AttributeKey attributeKey = attributePriorityIterator.next();
		IRoutingRule rule = null;
		Map<IRoutingRule, Long> scoreMap = getAttributeRulesScore(attributeKey, rules);

		//find maxScore 
		Long maxScore = scoreMap.values().stream().max(Comparator.comparing(Long::longValue)).get();

		//collect the rules that has high score and return the rule if there is
		//only 1 element with highest score
		Set<IRoutingRule> scoreMapSet = scoreMap.entrySet().stream().filter(entry->entry.getValue()==maxScore)
				.map(entry->entry.getKey())
				.collect(Collectors.toSet());
		if(scoreMapSet.size()==1)
			return scoreMapSet.stream().findFirst().get();

		//else proceed with next attributes
		if (attributePriorityIterator.hasNext()) 
			rule = processRoutingRules(scoreMapSet,attributePriorityIterator);			
		else 
			rule = scoreMapSet.stream().min(Comparator.comparing(rule2 -> rule2.getId())).get();
		
		return rule;
	}


	/**
	 * Returns the {@Map <RoutingRule,Long} routing rules with their scores for a 
	 * given attribute
	 * @param rules The pool of rules where the matching score is computed on
	 * @param attributeKey against which the routing rules are scored
	 * @return
	 */
	private Map<IRoutingRule,Long> getAttributeRulesScore(AttributeKey attributeKey, Collection<IRoutingRule> rules) {
		if(attributeKey.equals(IRoutingRule.AttributeKey.srcAddresses)) {
			Map<IRoutingRule, String> srcAddressMap = rules.stream().collect(toMap(Function.identity(),
					routingRule -> ((Collection<Attribute<String>>) routingRule.getSrcAddresses().getArgument()).stream().filter(srcAddress->Utility.isIpInsideNet(this.getSrcAddr(), srcAddress.getArgument())).findFirst().get().getArgument()));
			return getIPAddressCloseAffinity(srcAddressMap);
		} else if (attributeKey.equals(IRoutingRule.AttributeKey.dstAddress)) {
			Map<IRoutingRule, String> rulesMap = rules.stream().collect(toMap(Function.identity(), rule -> (String) rule.getDstAddress().getArgument()));
			return getIPAddressCloseAffinity(rulesMap);
		}
		return rules.stream().collect(toMap(Function.identity(), routingRule -> routingRule.getMatchingScore(attributeKey, this)));
	}


	/**
	 * Returns the {@Collection <IRoutingRule} routing rules with their scores for a 
	 * given attribute
	 * @param rules The pool of rules where the matching score is computed on
	 * @param rules The pool of rules after discarding the mismatched rules
	 * @return
	 */
	private Collection<IRoutingRule> discardMismatchRules(Collection<IRoutingRule> rules) {

		return rules.stream().filter(iRoutingRule-> !(iRoutingRule.getMatchingScore(AttributeKey.dstAddress,this)==0
				|| iRoutingRule.getMatchingScore(AttributeKey.srcAddresses,this)==0
				|| iRoutingRule.getMatchingScore(AttributeKey.dstPort,this)==0
				|| iRoutingRule.getMatchingScore(AttributeKey.protocol,this)==0))
				.collect(Collectors.toSet());
	}



	private static Map<IRoutingRule, Long> getIPAddressCloseAffinity(Map<IRoutingRule, String> rules) {
		Map<IRoutingRule, Pair<Long, Integer>> iRoutingRulePairMap = rules.entrySet().stream().collect(toMap(Map.Entry::getKey, entry -> cidrStringToNetAndMask(entry.getValue())));
		Optional<Map.Entry<IRoutingRule, Pair<Long, Integer>>> maxRuleEntryPair = iRoutingRulePairMap.entrySet().stream().max(Comparator.comparing(entry -> entry.getValue().getFirst()));
		if (maxRuleEntryPair.isPresent()) {
			Long ruleIP = maxRuleEntryPair.get().getValue().getFirst();
			Map<IRoutingRule, Pair<Long, Integer>> rulesSubnet = iRoutingRulePairMap.entrySet().stream().filter(entry -> ruleIP.equals(entry.getValue().getFirst())).collect(toMap(Map.Entry::getKey, Map.Entry::getValue));
			if (rulesSubnet.size() == 1) {
				return Collections.singletonMap(rulesSubnet.keySet().stream().findFirst().get(), ruleIP);
			} else {
				Optional<Map.Entry<IRoutingRule, Pair<Long, Integer>>> minRuleEntryPair = rulesSubnet.entrySet().stream().min(Comparator.comparing(entry -> entry.getValue().getSecond()));
				if(minRuleEntryPair.isPresent()) {
					Pair<Long, Integer> minMaskPair = minRuleEntryPair.get().getValue();
					Map<IRoutingRule, Pair<Long, Integer>> rulesWithMask = rulesSubnet.entrySet().stream().filter(entry -> minMaskPair.getSecond().equals(entry.getValue().getSecond())).collect(toMap(Map.Entry::getKey, Map.Entry::getValue));
					if(rulesWithMask.size() == 1) {
						return Collections.singletonMap(rulesWithMask.entrySet().stream().findFirst().get().getKey(), minMaskPair.getFirst());
					} else {
						IRoutingRule iRoutingRule = rulesWithMask.entrySet().stream().min(Comparator.comparing(entry -> (entry.getKey().getId()))).get().getKey();
						return Collections.singletonMap(iRoutingRule, minMaskPair.getFirst());
					}
				}
			}
		}
		return null;
	}
}