package com.forescout.challenge.impl;

import java.util.Collection;

import com.forescout.challenge.affinity.IPacket;
import com.forescout.challenge.affinity.IRoutingRule.AttributeKey;
import com.forescout.challenge.affinity.attributes.Attribute;
import com.forescout.challenge.affinity.attributes.AttributeOperator;
import com.forescout.challenge.affinity.attributes.CollectionAttribute;

public class CollectionAttributeImpl extends CollectionAttribute<Collection<Attribute<Object>>, Object>{

	protected CollectionAttributeImpl(Collection<Attribute<Object>> argument, AttributeOperator operator) {
		super(argument, operator);
	}
	
	protected CollectionAttributeImpl(Collection<Attribute<Object>> argument) {
		super(argument);
	}


	@Override
	public AttributeKey getAttributeInstanceId() {
		return RoutingRule.currentAttributeKey;
	}

	@Override
	public long equalsMatchingScore(IPacket packet, Collection<Attribute<Object>> argument) {
		return 0;
	}

}
