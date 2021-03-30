package com.forescout.challenge.impl;

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

import static com.forescout.challenge.affinity.Utility.cidrStringToNetAndMask;

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

		Map<IRoutingRule, Object> srcAddressMap = rules.stream().collect(Collectors.toMap(Function.identity(),
				routingRule -> ((Collection<Attribute<String>>) routingRule.getSrcAddresses()).stream().filter(srcAddress->Utility.isIpInsideNet(packet.getSrcAddr(), srcAddress.getArgument())).findFirst()));

		srcAddressMap.entrySet().stream().forEach(entry -> {
			Collection<String> srcAddresses = (Collection) entry.getKey().getSrcAddresses().getArgument();
			srcAddresses.clear();
			srcAddresses.add((String) entry.getValue());
		});
		return getIPAddressCloseAffinity(srcAddressMap.keySet(), IRoutingRule.AttributeKey.srcAddresses);

		/*Optional<Entry<IRoutingRule, Object>> optionalRule = srcAddressMap.entrySet().stream().max(Comparator.comparing(rule -> cidrStringToNetAndMask((String) rule.getValue()).getFirst()));

		if (optionalRule.isPresent()) {
			Entry<IRoutingRule, Object> rule = optionalRule.get();

			Set<Entry<IRoutingRule, Object>> rulesSubnet = srcAddressMap.entrySet().stream().filter(item-> cidrStringToNetAndMask((String)item.getValue()).getFirst() ==
					cidrStringToNetAndMask((String)rule.getValue()).getFirst())
					.collect(Collectors.toSet());

			if (rulesSubnet.size()==1)
				return rulesSubnet.stream().findFirst().get().getKey();
			else {
				optionalRule = rulesSubnet.stream().max((rule1,rule2)-> cidrStringToNetAndMask((String)rule1.getValue()).getSecond()
						.compareTo(cidrStringToNetAndMask((String)rule2.getValue()).getSecond()));
				if (optionalRule.isPresent()) {
					Entry<IRoutingRule, Object> ruleMask = optionalRule.get();

					Set<Entry<IRoutingRule, Object>> rulesMask = rulesSubnet.stream().filter(item-> cidrStringToNetAndMask((String)item.getValue()).getSecond() ==
							cidrStringToNetAndMask((String)ruleMask.getValue()).getSecond())
							.collect(Collectors.toSet());

					if (rulesMask.size()==1)
						return rulesMask.stream().findFirst().get().getKey();
					else
						return rulesMask.stream().min((rule1,rule2)->((Integer)rule1.getKey().getId()).compareTo((Integer)(rule2.getKey().getId()))).get().getKey();
				}
			}
		}
		return null;*/
	}

		/**
         * Returns the {@RoutingRule} that has closest affinity for the IP attribute
         * @param rules The pool of rules where the affinity is computed on
         * @return
         */
	public static Map<IRoutingRule, Long> getIPAddressCloseAffinity(Collection<IRoutingRule> rules, IRoutingRule.AttributeKey attributeKey){

		//across all the rules
		Optional<IRoutingRule> optionalRule = rules.stream().max(Comparator.comparing(rule -> cidrStringToNetAndMask(getAttributeValue(rule, attributeKey)).getFirst()));

		if (optionalRule.isPresent()) {
			IRoutingRule rule = optionalRule.get();
			Long ruleIP = cidrStringToNetAndMask(getAttributeValue(rule, attributeKey)).getFirst();

			Set<IRoutingRule> rulesSubnet = rules.stream().filter(item -> cidrStringToNetAndMask(getAttributeValue(item, attributeKey)).getFirst() == ruleIP).collect(Collectors.toSet());

			if (rulesSubnet.size() == 1) {
				return Collections.singletonMap(rulesSubnet.stream().findFirst().get(), ruleIP);
			} else {
				// should this be Min / Max???
				optionalRule = rulesSubnet.stream().min(Comparator.comparing(rule2 -> cidrStringToNetAndMask(getAttributeValue(rule2, attributeKey)).getSecond()));
				if (optionalRule.isPresent()) {
					IRoutingRule ruleMask = optionalRule.get();
					Pair<Long, Integer> ruleIPMaskPair = cidrStringToNetAndMask(getAttributeValue(ruleMask, attributeKey));

					Set<IRoutingRule> rulesMask = rulesSubnet.stream().filter(item-> cidrStringToNetAndMask(getAttributeValue(item, attributeKey)).getSecond() == ruleIPMaskPair.getSecond()).collect(Collectors.toSet());

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
