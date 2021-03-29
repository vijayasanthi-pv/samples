package com.forescout.challenge.impl;

import java.util.Collection;

import com.forescout.challenge.affinity.IPacket;
import com.forescout.challenge.affinity.IRoutingRule.AttributeKey;
import com.forescout.challenge.affinity.attributes.Attribute;
import com.forescout.challenge.affinity.attributes.AttributeOperator;
import com.forescout.challenge.affinity.attributes.CollectionAttribute;

/**
 * Represents an attribute of type Collection&lt;T&gt; and
 * @param <S> The type of the collection
 * @param <T> The type held by the attribute in the collection
 */
public class CollectionAttributeImpl extends CollectionAttribute<Collection<Attribute<Object>>, Object>{

	protected CollectionAttributeImpl(Collection<Attribute<Object>> argument, AttributeOperator operator) {
		super(argument, operator);
	}
	
	protected CollectionAttributeImpl(Collection<Attribute<Object>> argument) {
		super(argument);
	}

	/**
     * Used as tracker for current AttributeKey
     * @return The current AttributeKey used
     */
	@Override
	public AttributeKey getAttributeInstanceId() {
		return RoutingRule.currentAttributeKey;
	}

	/**
     * Computes the matching score of a field in IPacket and the given argument using the equals operator
     * @param packet The packet that given item is matched against
     * @param argument The argument to match
     * @return The item matching score
     */
	@Override
	public long equalsMatchingScore(IPacket packet, Collection<Attribute<Object>> argument) {
		//Note : This method can be implemented when multiple source pool address
		//of a route are in matching with packet source address 
		return 0;
	}

}
