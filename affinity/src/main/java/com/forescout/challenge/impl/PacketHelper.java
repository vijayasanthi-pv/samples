package com.forescout.challenge.impl;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.forescout.challenge.affinity.IPacket;
import com.forescout.challenge.affinity.IRoutingRule;
import com.forescout.challenge.affinity.Utility;
import com.forescout.challenge.affinity.attributes.Attribute;

/**
 * Packet utility functions
 */
public class PacketHelper {

	/**
	 * Returns the {@RoutingRule} that has closest affinity for the destination
	 * @param rules The pool of rules where the affinity is computed on
	 * @return
	 */
	public static IRoutingRule getDstAddressCloseAffinity(IPacket packet, Collection<IRoutingRule> rules){

		//across all the rules
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
	public static IRoutingRule getSrcAddressCloseAffinity(IPacket packet, Collection<IRoutingRule> rules){

		Map<IRoutingRule, Object> srcAddressMap = rules.stream().collect(Collectors.toMap(Function.identity(), 
				routingRule -> ((Collection<Attribute<String>>) routingRule.getSrcAddresses()).stream().filter(srcAddress->Utility.isIpInsideNet(packet.getSrcAddr(), srcAddress.getArgument())).findFirst()));

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
