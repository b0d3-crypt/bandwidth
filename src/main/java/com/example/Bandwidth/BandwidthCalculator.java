package com.example.Bandwidth;


import org.snmp4j.CommunityTarget;
import org.snmp4j.PDU;
import org.snmp4j.Snmp;
import org.snmp4j.TransportMapping;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.*;
import org.snmp4j.transport.DefaultUdpTransportMapping;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class BandwidthCalculator {

	private static final int PORT = 161;
	private static final String COMMUNITY = "public";

	private static final String IF_IN_OCTETS = "1.3.6.1.2.1.2.2.1.10.2";
	private static final String IF_OUT_OCTETS = "1.3.6.1.2.1.2.2.1.16.2";

	public BandwidthResult calcular(String host) throws Exception {

		boolean isConnected = SnmpConnectionTester.testConnection(host, COMMUNITY);

		if (!isConnected) {
			throw new RuntimeException("Não foi possível conectar ao host via SNMP: " + host);
		}

		long in1 = snmpGetCounter(IF_IN_OCTETS, host);
		long out1 = snmpGetCounter(IF_OUT_OCTETS, host);

		Thread.sleep(1000);

		long in2 = snmpGetCounter(IF_IN_OCTETS, host);
		long out2 = snmpGetCounter(IF_OUT_OCTETS, host);

		double rxMbps = calcularMbps(in1, in2, 1);
		double txMbps = calcularMbps(out1, out2, 1);

		return new BandwidthResult(
				rxMbps,
				txMbps,
				Instant.now()
		);
	}

	private double calcularMbps(long antes, long depois, double segundos) {
		long diff = depois - antes;
		if (diff < 0) diff = 0;
		return (diff * 8.0) / segundos / 1_000_000.0;
	}

	private long snmpGetCounter(String oid, String host) throws Exception {
		Address targetAddress = GenericAddress.parse("udp:" + host + "/" + PORT);
		TransportMapping<?> transport = new DefaultUdpTransportMapping();
		transport.listen();

		CommunityTarget target = new CommunityTarget();
		target.setCommunity(new OctetString(COMMUNITY));
		target.setAddress(targetAddress);
		target.setVersion(SnmpConstants.version2c);
		target.setTimeout(2000);
		target.setRetries(1);

		PDU pdu = new PDU();
		pdu.add(new VariableBinding(new OID(oid)));
		pdu.setType(PDU.GET);

		Snmp snmp = new Snmp(transport);
		ResponseEvent event = snmp.send(pdu, target);
		snmp.close();

		if (event.getResponse() == null) {
			throw new RuntimeException("SNMP timeout ao consultar OID: " + oid);
		}

		return event.getResponse().get(0).getVariable().toLong();
	}
}
