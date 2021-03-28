package com.forescout.challenge.impl;

import com.forescout.challenge.affinity.IPacket;
import com.forescout.challenge.affinity.IRoutingRule;
import com.forescout.challenge.affinity.IRoutingRule.AttributeKey;
import com.forescout.challenge.affinity.Utility;
import com.forescout.challenge.affinity.attributes.Attribute;
import com.forescout.challenge.affinity.attributes.AttributeOperator;
import com.forescout.challenge.affinity.attributes.values.PortRange;

public class AttributeImpl<T extends Object> extends Attribute<T>{

	protected AttributeImpl(T argument, AttributeOperator operator) {
		super(argument, operator);
	}
	
	protected AttributeImpl(T argument) {
		super(argument);
	}

	@Override
	public long equalsMatchingScore(IPacket packet, T argument) {
		
		if (getAttributeInstanceId().equals(IRoutingRule.AttributeKey.srcAddresses)) {
			if (Utility.isIpInsideNet(packet.getSrcAddr(),(String) argument)){
				return 2L;
			}
		}else if (getAttributeInstanceId().equals(IRoutingRule.AttributeKey.dstAddress)) {
			if (Utility.isIpInsideNet(packet.getDstAddr(),(String) argument)){
				return 2L;
			}
		}else if (getAttributeInstanceId().equals(IRoutingRule.AttributeKey.dstPort)) {
			if (((PortRange)argument).matches(packet.getDstPort())){
				return 2L;
			}
		}else if (getAttributeInstanceId().equals(IRoutingRule.AttributeKey.protocol)) {
			if (((String)argument).equalsIgnoreCase(packet.getProtocol())){
				return 2L;
			}
		}
		return 0;
	}

	@Override
	public AttributeKey getAttributeInstanceId() {
		return RoutingRule.currentAttributeKey;
	}

}
