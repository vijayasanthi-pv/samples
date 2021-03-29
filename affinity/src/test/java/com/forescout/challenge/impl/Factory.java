package com.forescout.challenge.impl;

import com.forescout.challenge.affinity.IPacket;
import com.forescout.challenge.affinity.IRoutingRule;
import com.forescout.challenge.affinity.Utility;
import com.forescout.challenge.affinity.attributes.AttributeOperator;
import com.forescout.challenge.affinity.attributes.values.PortRange;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

public class Factory {

    public static Set<String> createSrcAddresses1() {
        return new HashSet<>(Arrays.asList(
                "192.168.1.0/24",
                "10.1.0.0/16",
                "172.0.0.0/8",
                "8.8.8.8/32"
        ));
    }

    public static Set<String> createSrcAddresses2() {
        return new HashSet<>(Arrays.asList(
                "192.168.0.0/16", // Wider than the subnet in srcAddresses1
                "10.1.1.0/24",    // Narrower than the subnet in srcAddresses1
                "172.0.0.0/8",    // Same than the subnet in srcAddresses1
                "8.8.8.8/32"      //
        ));
    }

    public static String createDstAddress1() {
        return "10.2.1.1/32";
    }

    public static String createDstAddress2() {
        return "10.2.1.0/31"; // Wider than the subnet in dstAddress1
    }

    public static PortRange createPortRange1() {
        return PortRange.parse("1-1024");
    }

    public static PortRange createPortRange2() {
        return PortRange.parse("512-1024"); // Narrower than the port in portRange1
    }

    public static PortRange createPortRange3() {
        return PortRange.parse("21"); // Narrower than the port in portRange2
    }

    public static PortRange createPortRange4() {
        return PortRange.parse("1-60000"); // Widest
    }

    public static String createProtocol1() {
        return "UDP";
    }

    public static String createProtocol2() {
        return "TCP";
    }

    public static IRoutingRule createRoutingRule1() {
        RoutingRule routingRule = new RoutingRule(
                Factory.createSrcAddresses1(),
                Factory.createDstAddress1(),
                Factory.createProtocol1(),
                Factory.createPortRange1()
        );

        routingRule.getSrcAddresses().setOperator(AttributeOperator.Contains);
        routingRule.getDstAddress().setOperator(AttributeOperator.Matches);
        routingRule.getDstPort().setOperator(AttributeOperator.Matches);
        routingRule.getProtocol().setOperator(AttributeOperator.Matches);

        return routingRule;
    }

    public static IRoutingRule createRoutingRule2() {
        RoutingRule routingRule = new RoutingRule(
                Factory.createSrcAddresses2(),
                Factory.createDstAddress2(),
                Factory.createProtocol2(),
                Factory.createPortRange2()
        ) {
            // Change the default attribute priority, put src addresses on top
            private final LinkedHashSet<IRoutingRule.AttributeKey> customAttributesPriority = new LinkedHashSet<>(
                    Arrays.asList(
                            IRoutingRule.AttributeKey.srcAddresses,
                            IRoutingRule.AttributeKey.dstAddress,
                            IRoutingRule.AttributeKey.dstPort,
                            IRoutingRule.AttributeKey.protocol
                    )
            );

            // Override default priority in this rule
            @Override
            public LinkedHashSet<IRoutingRule.AttributeKey> getDefaultAttributesPriority() {
                return customAttributesPriority;
            }
        };

        routingRule.getSrcAddresses().setOperator(AttributeOperator.Contains);
        routingRule.getDstAddress().setOperator(AttributeOperator.Matches);
        routingRule.getDstPort().setOperator(AttributeOperator.Matches);
        // Don't care about protocol in this rule
        routingRule.getProtocol().setOperator(AttributeOperator.Any);

        return routingRule;
    }

    public static IRoutingRule createRoutingRule3() {
        RoutingRule routingRule = new RoutingRule(
                Factory.createSrcAddresses1(),
                Factory.createDstAddress2(),
                Factory.createProtocol2(),
                Factory.createPortRange3()
        );

        routingRule.getSrcAddresses().setOperator(AttributeOperator.Contains);
        routingRule.getDstAddress().setOperator(AttributeOperator.Matches);
        routingRule.getDstPort().setOperator(AttributeOperator.Matches);
        routingRule.getProtocol().setOperator(AttributeOperator.Matches);

        return routingRule;
    }

    public static IPacket createPacket(String srcAddr, String dstAddr, int port, String protocol) {
        return new Packet(Utility.string2Ip(srcAddr), Utility.string2Ip(dstAddr), port, protocol);
    }

    public static IRoutingRule createRoutingRule4() {
        RoutingRule routingRule = new RoutingRule(
                Factory.createSrcAddresses1(),
                Factory.createDstAddress1(),
                Factory.createProtocol2(),
                Factory.createPortRange4()
        );

        routingRule.getSrcAddresses().setOperator(AttributeOperator.Contains);
        routingRule.getDstAddress().setOperator(AttributeOperator.Matches);
        routingRule.getDstPort().setOperator(AttributeOperator.Matches);
        routingRule.getProtocol().setOperator(AttributeOperator.Matches);

        return routingRule;
    }
}
