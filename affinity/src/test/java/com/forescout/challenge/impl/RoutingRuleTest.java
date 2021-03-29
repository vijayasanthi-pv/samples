package com.forescout.challenge.impl;

import com.forescout.challenge.affinity.IPacket;
import com.forescout.challenge.affinity.IRoutingRule;
import com.forescout.challenge.affinity.attributes.AttributeOperator;
import java.util.Arrays;
import java.util.Collection;
import org.junit.Assert;
import org.junit.Test;

public class RoutingRuleTest {


    @Test
    public void testRoutingSingleRule() {

        IPacket packet;
        IRoutingRule alikeRule;

        IRoutingRule routingRule = Factory.createRoutingRule1();
        Collection<IRoutingRule> rules = Arrays.asList(routingRule);

        packet = Factory.createPacket("192.168.1.1", "10.2.1.1", 21, "UDP");
        alikeRule = packet.getClosestAffinityNetwork(rules, routingRule.getDefaultAttributesPriority());
        Assert.assertEquals(routingRule, alikeRule);

        packet = Factory.createPacket("192.168.1.1", "10.2.1.2", 21, "UDP");
        alikeRule = packet.getClosestAffinityNetwork(rules, routingRule.getDefaultAttributesPriority());
        Assert.assertNull("Destination address must not match", alikeRule);

        packet = Factory.createPacket("172.130.2.5", "10.2.1.1", 21, "UDP");
        alikeRule = packet.getClosestAffinityNetwork(rules, routingRule.getDefaultAttributesPriority());
        Assert.assertEquals(routingRule, alikeRule);

        packet = Factory.createPacket("172.130.2.5", "10.2.1.1", 2021, "UDP");
        alikeRule = packet.getClosestAffinityNetwork(rules, routingRule.getDefaultAttributesPriority());
        Assert.assertNull("Port must not match", alikeRule);

        packet = Factory.createPacket("8.8.8.8", "10.2.1.1", 21, "UDP");
        alikeRule = packet.getClosestAffinityNetwork(rules, routingRule.getDefaultAttributesPriority());
        Assert.assertEquals(routingRule, alikeRule);

        packet = Factory.createPacket("8.8.8.8", "10.2.1.1", 21, "TCP");
        alikeRule = packet.getClosestAffinityNetwork(rules, routingRule.getDefaultAttributesPriority());
        Assert.assertNull("Protocol must not match", alikeRule);

        packet = Factory.createPacket("1.1.1.1", "10.2.1.1", 21, "UDP");
        alikeRule = packet.getClosestAffinityNetwork(rules, routingRule.getDefaultAttributesPriority());
        Assert.assertNull("Source address must not match", alikeRule);

        packet = Factory.createPacket("192.168.2.1", "10.2.1.1", 21, "UDP");
        alikeRule = packet.getClosestAffinityNetwork(rules, routingRule.getDefaultAttributesPriority());
        Assert.assertNull("Source address must not match", alikeRule);

        // Relaxing some constraints now, must match anyway

        routingRule.getDstAddress().setOperator(AttributeOperator.Any);
        packet = Factory.createPacket("192.168.1.1", "10.2.1.2", 21, "UDP");
        alikeRule = packet.getClosestAffinityNetwork(rules, routingRule.getDefaultAttributesPriority());
        Assert.assertEquals(routingRule, alikeRule);

        routingRule.getDstPort().setOperator(AttributeOperator.Any);
        packet = Factory.createPacket("172.130.2.5", "10.2.1.1", 2021, "UDP");
        alikeRule = packet.getClosestAffinityNetwork(rules, routingRule.getDefaultAttributesPriority());
        Assert.assertEquals(routingRule, alikeRule);

        routingRule.getSrcAddresses().setOperator(AttributeOperator.Any);
        packet = Factory.createPacket("1.1.1.1", "10.2.1.1", 21, "UDP");
        alikeRule = packet.getClosestAffinityNetwork(rules, routingRule.getDefaultAttributesPriority());
        Assert.assertEquals(routingRule, alikeRule);

        packet = Factory.createPacket("192.168.2.1", "10.2.1.1", 21, "UDP");
        alikeRule = packet.getClosestAffinityNetwork(rules, routingRule.getDefaultAttributesPriority());
        Assert.assertEquals(routingRule, alikeRule);

        routingRule.getProtocol().setOperator(AttributeOperator.Any);
        packet = Factory.createPacket("8.8.8.8", "10.2.1.1", 21, "TCP");
        alikeRule = packet.getClosestAffinityNetwork(rules, routingRule.getDefaultAttributesPriority());
        Assert.assertEquals(routingRule, alikeRule);

        // All matches now

        packet = Factory.createPacket("1.1.1.1", "10.2.1.2", 2021, "TCP");
        alikeRule = packet.getClosestAffinityNetwork(rules, routingRule.getDefaultAttributesPriority());
        Assert.assertEquals(routingRule, alikeRule);
    }


    @Test
    public void testRoutingRules() {
        IPacket packet;
        IRoutingRule alikeRule;

        IRoutingRule routingRule1 = Factory.createRoutingRule1();
        IRoutingRule routingRule2 = Factory.createRoutingRule2();

        Collection<IRoutingRule> rules = Arrays.asList(routingRule1, routingRule2);

        packet = Factory.createPacket("192.168.1.1", "10.2.1.1", 512, "UDP");
        alikeRule = packet.getClosestAffinityNetwork(rules, routingRule1.getDefaultAttributesPriority());
        Assert.assertEquals(routingRule1, alikeRule);

        // Dst addess weights more
        packet = Factory.createPacket("10.1.1.1", "10.2.1.1", 512, "UDP");
        alikeRule = packet.getClosestAffinityNetwork(rules, routingRule1.getDefaultAttributesPriority());
        Assert.assertEquals(routingRule1, alikeRule);

        packet = Factory.createPacket("10.1.1.1", "10.2.1.1", 512, "UDP");
        // Use different priority where src weights more
        alikeRule = packet.getClosestAffinityNetwork(rules, routingRule2.getDefaultAttributesPriority());
        Assert.assertEquals(routingRule2, alikeRule);

        packet = Factory.createPacket("10.1.1.1", "10.2.1.1", 512, "TCP");
        // Use different priority where src weights more, protocol is irrelevant
        alikeRule = packet.getClosestAffinityNetwork(rules, routingRule2.getDefaultAttributesPriority());
        Assert.assertEquals(routingRule2, alikeRule);

        packet = Factory.createPacket("10.1.1.1", "10.2.1.0", 512, "UDP");
        alikeRule = packet.getClosestAffinityNetwork(rules, routingRule1.getDefaultAttributesPriority());
        Assert.assertEquals(routingRule2, alikeRule);

        packet = Factory.createPacket("10.1.1.1", "10.2.1.0", 512, "UDP");
        // Use different priority where src weights more, but the dst never matches in rule1
        alikeRule = packet.getClosestAffinityNetwork(rules, routingRule2.getDefaultAttributesPriority());
        Assert.assertEquals(routingRule2, alikeRule);

        // No matches

        packet = Factory.createPacket("192.168.1.1", "10.2.1.2", 21, "UDP");
        alikeRule = packet.getClosestAffinityNetwork(rules, routingRule1.getDefaultAttributesPriority());
        Assert.assertNull("Destination address must not match", alikeRule);

        packet = Factory.createPacket("172.130.2.5", "10.2.1.1", 2021, "UDP");
        alikeRule = packet.getClosestAffinityNetwork(rules, routingRule1.getDefaultAttributesPriority());
        Assert.assertNull("Port must not match", alikeRule);

        packet = Factory.createPacket("8.8.8.8", "10.2.1.1", 21, "TCP");
        alikeRule = packet.getClosestAffinityNetwork(rules, routingRule1.getDefaultAttributesPriority());
        Assert.assertNull("Protocol must not match", alikeRule);

        packet = Factory.createPacket("1.1.1.1", "10.2.1.1", 21, "UDP");
        alikeRule = packet.getClosestAffinityNetwork(rules, routingRule1.getDefaultAttributesPriority());
        Assert.assertNull("Source address must not match", alikeRule);

        packet = Factory.createPacket("192.168.2.1", "10.2.1.1", 21, "UDP");
        alikeRule = packet.getClosestAffinityNetwork(rules, routingRule1.getDefaultAttributesPriority());
        Assert.assertNull("Source address must not match", alikeRule);

    }

    @Test
    public void testRouting3Rules() {
        IPacket packet;
        IRoutingRule alikeRule;

        IRoutingRule routingRule1 = Factory.createRoutingRule1();
        IRoutingRule routingRule2 = Factory.createRoutingRule2();
        IRoutingRule routingRule3 = Factory.createRoutingRule3();
        Collection<IRoutingRule> rules = Arrays.asList(routingRule1, routingRule2, routingRule3);

        packet = Factory.createPacket("192.168.1.1", "10.2.1.1", 512, "UDP");
        alikeRule = packet.getClosestAffinityNetwork(rules, routingRule1.getDefaultAttributesPriority());
        Assert.assertEquals(routingRule1, alikeRule);

        packet = Factory.createPacket("10.1.1.1", "10.2.1.1", 512, "UDP");
        alikeRule = packet.getClosestAffinityNetwork(rules, routingRule1.getDefaultAttributesPriority());
        Assert.assertEquals(routingRule1, alikeRule);

        packet = Factory.createPacket("10.1.1.1", "10.2.1.1", 512, "UDP");
        // Use different priority where src weights more
        alikeRule = packet.getClosestAffinityNetwork(rules, routingRule2.getDefaultAttributesPriority());
        Assert.assertEquals(routingRule2, alikeRule);

        packet = Factory.createPacket("10.1.1.1", "10.2.1.1", 512, "TCP");
        // Use different priority where src weights more, protocol is irrelevant
        alikeRule = packet.getClosestAffinityNetwork(rules, routingRule2.getDefaultAttributesPriority());
        Assert.assertEquals(routingRule2, alikeRule);

        packet = Factory.createPacket("10.1.1.1", "10.2.1.0", 512, "UDP");
        alikeRule = packet.getClosestAffinityNetwork(rules, routingRule1.getDefaultAttributesPriority());
        Assert.assertEquals(routingRule2, alikeRule);

        packet = Factory.createPacket("10.1.1.1", "10.2.1.0", 512, "UDP");
        // Use different priority where src weights more, but the dst never matches in rule1
        alikeRule = packet.getClosestAffinityNetwork(rules, routingRule2.getDefaultAttributesPriority());
        Assert.assertEquals(routingRule2, alikeRule);

        packet = Factory.createPacket("192.168.1.1", "10.2.1.1", 21, "UDP");
        alikeRule = packet.getClosestAffinityNetwork(rules, routingRule1.getDefaultAttributesPriority());
        Assert.assertEquals(routingRule1, alikeRule);

        packet = Factory.createPacket("172.130.2.5", "10.2.1.1", 2021, "UDP");
        alikeRule = packet.getClosestAffinityNetwork(rules, routingRule1.getDefaultAttributesPriority());
        Assert.assertNull("Port must not match", alikeRule);

        packet = Factory.createPacket("8.8.8.8", "10.2.1.1", 21, "TCP");
        alikeRule = packet.getClosestAffinityNetwork(rules, routingRule1.getDefaultAttributesPriority());
        Assert.assertEquals(routingRule3, alikeRule);

        packet = Factory.createPacket("1.1.1.1", "10.2.1.1", 21, "UDP");
        alikeRule = packet.getClosestAffinityNetwork(rules, routingRule1.getDefaultAttributesPriority());
        Assert.assertNull("Source address must not match", alikeRule);

        packet = Factory.createPacket("192.168.2.1", "10.2.1.1", 21, "UDP");
        alikeRule = packet.getClosestAffinityNetwork(rules, routingRule1.getDefaultAttributesPriority());
        Assert.assertNull("Source address must not match", alikeRule);
    }

    @Test
    public void testRouting4Rules() {
        IPacket packet;
        IRoutingRule alikeRule;

        IRoutingRule routingRule1 = Factory.createRoutingRule1();
        IRoutingRule routingRule2 = Factory.createRoutingRule2();
        IRoutingRule routingRule3 = Factory.createRoutingRule3();
        IRoutingRule routingRule4 = Factory.createRoutingRule4();
        Collection<IRoutingRule> rules = Arrays.asList(routingRule1, routingRule2, routingRule3, routingRule4);

        packet = Factory.createPacket("192.168.1.1", "10.2.1.1", 512, "UDP");
        alikeRule = packet.getClosestAffinityNetwork(rules, routingRule1.getDefaultAttributesPriority());
        Assert.assertEquals(routingRule1, alikeRule);

        packet = Factory.createPacket("10.1.1.1", "10.2.1.1", 512, "UDP");
        alikeRule = packet.getClosestAffinityNetwork(rules, routingRule1.getDefaultAttributesPriority());
        Assert.assertEquals(routingRule1, alikeRule);

        packet = Factory.createPacket("10.1.1.1", "10.2.1.1", 512, "UDP");
        // Use different priority where src weights more
        alikeRule = packet.getClosestAffinityNetwork(rules, routingRule2.getDefaultAttributesPriority());
        Assert.assertEquals(routingRule2, alikeRule);

        packet = Factory.createPacket("10.1.1.1", "10.2.1.1", 512, "TCP");
        // Use different priority where src weights more, protocol is irrelevant
        alikeRule = packet.getClosestAffinityNetwork(rules, routingRule2.getDefaultAttributesPriority());
        Assert.assertEquals(routingRule2, alikeRule);

        packet = Factory.createPacket("10.1.1.1", "10.2.1.0", 512, "UDP");
        alikeRule = packet.getClosestAffinityNetwork(rules, routingRule1.getDefaultAttributesPriority());
        Assert.assertEquals(routingRule2, alikeRule);

        packet = Factory.createPacket("10.1.1.1", "10.2.1.0", 512, "UDP");
        // Use different priority where src weights more, but the dst never matches in rule1
        alikeRule = packet.getClosestAffinityNetwork(rules, routingRule2.getDefaultAttributesPriority());
        Assert.assertEquals(routingRule2, alikeRule);

        packet = Factory.createPacket("192.168.1.1", "10.2.1.1", 21, "UDP");
        alikeRule = packet.getClosestAffinityNetwork(rules, routingRule1.getDefaultAttributesPriority());
        Assert.assertEquals(routingRule1, alikeRule);

        packet = Factory.createPacket("172.130.2.5", "10.2.1.1", 2021, "UDP");
        alikeRule = packet.getClosestAffinityNetwork(rules, routingRule1.getDefaultAttributesPriority());
        Assert.assertNull("Protocol must not match", alikeRule);

        packet = Factory.createPacket("8.8.8.8", "10.2.1.1", 21, "TCP");
        alikeRule = packet.getClosestAffinityNetwork(rules, routingRule1.getDefaultAttributesPriority());
        Assert.assertEquals(routingRule4, alikeRule);

        packet = Factory.createPacket("1.1.1.1", "10.2.1.1", 21, "UDP");
        alikeRule = packet.getClosestAffinityNetwork(rules, routingRule1.getDefaultAttributesPriority());
        Assert.assertNull("Source address must not match", alikeRule);

        packet = Factory.createPacket("192.168.2.1", "10.2.1.1", 21, "UDP");
        alikeRule = packet.getClosestAffinityNetwork(rules, routingRule1.getDefaultAttributesPriority());
        Assert.assertNull("Protocol must not match", alikeRule);
    }
}
