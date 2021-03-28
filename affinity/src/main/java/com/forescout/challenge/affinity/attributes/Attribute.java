package com.forescout.challenge.affinity.attributes;

import com.forescout.challenge.affinity.Validate;
import com.forescout.challenge.affinity.IPacket;
import com.forescout.challenge.affinity.IRoutingRule;

/**
 * Represents an attribute of type T and
 * @param <T>
 */
public abstract class Attribute<T> {

    // This is the default matching score returned by a Any operator.
    // It is set on purpose to the lowest value representing the weakest matching
    public static final long BARELY_ABOVE_ANY_MATCHING_SCORE = 1L;

    protected T argument;
    protected AttributeOperator operator;

    protected Attribute(T argument, AttributeOperator operator) {
        this.operator = Validate.notNull(operator, "Argument operator cannot be null");
        this.argument = Validate.notNull(argument, "Argument arguments cannot be null");
    }

    protected Attribute(T arguments) {
        this(arguments, AttributeOperator.Any);
    }

    /**
     * Computes the matching score of a field in IPacket and the given argument using the equals operator
     * @param packet The packet that given item is matched against
     * @param argument The argument to match
     * @return The item matching score
     */
    public abstract long equalsMatchingScore(IPacket packet, T argument);

    /**
     * Compute the matching score against a packet. The score is always &gt;= 0.
     * Zero means no score at all.
     *
     * @param packet The packet to match against
     * @return The matching score
     */
    public long matchingScore(IPacket packet) {
        switch (operator) {
            case Any:
                return BARELY_ABOVE_ANY_MATCHING_SCORE;
            case Matches:
                return equalsMatchingScore(packet, argument);
            case Contains:
                throw new AssertionError("Contains operator only allowed with CollectionsAttribute");
        }
        return 0;
    }

    public abstract IRoutingRule.AttributeKey getAttributeInstanceId();

    // Accessors

    public T getArgument() {
        return this.argument;
    }

    public void setArgument(T argument) {
        this.argument = argument;
    }

    public AttributeOperator getOperator() {
        return operator;
    }

    public final void setOperator(AttributeOperator operator) {
        this.operator = Validate.notNull(operator, "Argument operator cannot be null");
    }
}
