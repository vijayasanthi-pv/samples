package com.forescout.challenge.affinity.attributes;

import com.forescout.challenge.affinity.Utility;

import java.util.Collection;
import com.forescout.challenge.affinity.IPacket;

/**
 * Represents an attribute of type Collection&lt;T&gt; and
 * @param <S> The type of the collection
 * @param <T> The type held by the attribute in the collection
 */
public abstract class CollectionAttribute<S extends Collection<Attribute<T>>, T> extends Attribute<S> {

    protected CollectionAttribute(S argument, AttributeOperator operator) {
        super(argument, operator);
    }

    protected CollectionAttribute(S argument) {
        this(argument, AttributeOperator.Any);
    }

    /**
     * Compute the matching score against a packet, with special treatment of Contains with collections
     *
     * @param packet The packet to match against
     * @return The matching score
     */
    @Override
    public long matchingScore(IPacket packet) {
        switch (operator) {
            case Any:
                return BARELY_ABOVE_ANY_MATCHING_SCORE;
            case Matches:
                return equalsMatchingScore(packet, argument);
            case Contains:
                long maxScore = 0L;
                for (Attribute<T> item: argument) {
                    long itemScore = item.equalsMatchingScore(packet, item.getArgument());
                    if (itemScore < 0) {
                        throw new AssertionError("Score must be >= 0");
                    }
                    if (itemScore > maxScore) {
                        maxScore = itemScore;
                    }
                }
                return maxScore;
        }
        return 0;
    }

    // Accessors

    @Override
    public void setArgument(S argument) {
        Utility.assignCollection(this.argument, argument);
    }

}
