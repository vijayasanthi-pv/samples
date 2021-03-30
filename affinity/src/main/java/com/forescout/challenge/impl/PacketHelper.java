package com.forescout.challenge.impl;

import static com.forescout.challenge.affinity.Utility.cidrStringToNetAndMask;
import static java.util.stream.Collectors.toMap;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.forescout.challenge.affinity.IPacket;
import com.forescout.challenge.affinity.IRoutingRule;
import com.forescout.challenge.affinity.Utility;
import com.forescout.challenge.affinity.attributes.Attribute;
import com.forescout.challenge.affinity.attributes.values.Pair;

/**
 * Packet utility functions
 */
public class PacketHelper {

	/**
	 * Returns the {@RoutingRule} that has closest affinity for the destination
	 * @param rules The pool of rules where the affinity is computed on
	 * @return
	 */
	public static Map<IRoutingRule, Long> getSrcAddressCloseAffinity(IPacket packet, Collection<IRoutingRule> rules) {

		Map<IRoutingRule, String> srcAddressMap = rules.stream().collect(toMap(Function.identity(),
				routingRule -> ((Collection<Attribute<String>>) routingRule.getSrcAddresses().getArgument()).stream().filter(srcAddress->Utility.isIpInsideNet(packet.getSrcAddr(), srcAddress.getArgument())).findFirst().get().getArgument()));
		return getIPAddressCloseAffinity(srcAddressMap);

	}

	public static Map<IRoutingRule, Long> getIPAddressCloseAffinity(Map<IRoutingRule, String> rules) {

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

	/**
	 * Returns the {@RoutingRule} that has closest affinity for the IP attribute
	 * @param rules The pool of rules where the affinity is computed on
	 * @return
	 */
	public static Map<IRoutingRule, Long> getIPAddressCloseAffinity(Collection<IRoutingRule> rules, IRoutingRule.AttributeKey attributeKey) {

		//across all the rules
		Optional<IRoutingRule> optionalRule = rules.stream().max(Comparator.comparing(rule -> cidrStringToNetAndMask(getAttributeValue(rule, attributeKey)).getFirst()));

		if (optionalRule.isPresent()) {
			IRoutingRule rule = optionalRule.get();
			Long ruleIP = cidrStringToNetAndMask(getAttributeValue(rule, attributeKey)).getFirst();

			Set<IRoutingRule> rulesSubnet = rules.stream().filter(item -> cidrStringToNetAndMask(getAttributeValue(item, attributeKey)).getFirst().equals(ruleIP)).collect(Collectors.toSet());

			if (rulesSubnet.size() == 1) {
				return Collections.singletonMap(rulesSubnet.stream().findFirst().get(), ruleIP);
			} else {
				optionalRule = rulesSubnet.stream().min(Comparator.comparing(rule2 -> cidrStringToNetAndMask(getAttributeValue(rule2, attributeKey)).getSecond()));
				if (optionalRule.isPresent()) {
					IRoutingRule ruleMask = optionalRule.get();
					Pair<Long, Integer> ruleIPMaskPair = cidrStringToNetAndMask(getAttributeValue(ruleMask, attributeKey));

					Set<IRoutingRule> rulesMask = rulesSubnet.stream().filter(item-> cidrStringToNetAndMask(getAttributeValue(item, attributeKey)).getSecond().equals(ruleIPMaskPair.getSecond())).collect(Collectors.toSet());

					if (rulesMask.size() == 1) {
						return Collections.singletonMap(rulesMask.stream().findFirst().get(), ruleIPMaskPair.getFirst());
					} else {
						IRoutingRule iRoutingRule = rulesMask.stream().min(Comparator.comparing(rule2 -> (rule2.getId()))).get();
						return Collections.singletonMap(iRoutingRule, ruleIPMaskPair.getFirst());
					}
				}
			}
		}

		return null;
	}

	private static String getAttributeValue(IRoutingRule rule, IRoutingRule.AttributeKey attributeKey) {
		if(attributeKey == IRoutingRule.AttributeKey.dstAddress) {
			return (String) rule.getDstAddress().getArgument();
		} else if (attributeKey == IRoutingRule.AttributeKey.srcAddresses) {
			return ((Collection<String>)rule.getSrcAddresses().getArgument()).stream().findFirst().get();
		}

		return null;
	}

}
