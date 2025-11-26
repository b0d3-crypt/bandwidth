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
import java.util.HashMap;

@Service
public class BandwidthCalculator {

	private static final String COMMUNITY = "public";
	private static final String IF_IN_OCTETS = "1.3.6.1.2.1.2.2.1.10.";
	private static final String IF_OUT_OCTETS = "1.3.6.1.2.1.2.2.1.16.";
	private static HashMap<String, String> interfaceOctetsMap;

	public BandwidthResult calcular(BandwidthRequest request) throws Exception {
		boolean isConnected = SnmpConnectionTester.testConnection(request.getIpAddress(), COMMUNITY);

		if (!isConnected) {
			throw new RuntimeException("Não foi possível conectar ao host via SNMP: " + request.getIpAddress());
		}

		setOctets(request.getInterfaceName());

		long in1 = snmpGetCounter(interfaceOctetsMap.get("in"), request.getIpAddress());
		long out1 = snmpGetCounter(interfaceOctetsMap.get("out"), request.getIpAddress());

		Thread.sleep(1000);

		long in2 = snmpGetCounter(interfaceOctetsMap.get("in"), request.getIpAddress());
		long out2 = snmpGetCounter(interfaceOctetsMap.get("out"), request.getIpAddress());

		double rxMbps = calcularMbps(in1, in2, 1);
		double txMbps = calcularMbps(out1, out2, 1);

		return new BandwidthResult(
				rxMbps,
				txMbps,
				Instant.now());
	}

	private double calcularMbps(long antes, long depois, double segundos) {
		long diff = depois - antes;
		if (diff < 0)
			diff = 0;
		return (diff * 8.0) / segundos / 1_000_000.0;
	}

	private long snmpGetCounter(String oid, String host) throws Exception {
		TransportMapping<?> transport = null;
		Snmp snmp = null;

		try {
			Address targetAddress = GenericAddress.parse("udp:" + host + "/161");
			transport = new DefaultUdpTransportMapping();
			transport.listen();

			CommunityTarget target = new CommunityTarget();
			target.setCommunity(new OctetString(COMMUNITY));
			target.setAddress(targetAddress);
			target.setVersion(SnmpConstants.version2c);
			target.setTimeout(5000);
			target.setRetries(2);

			PDU pdu = new PDU();
			pdu.add(new VariableBinding(new OID(oid)));
			pdu.setType(PDU.GET);

			snmp = new Snmp(transport);
			ResponseEvent event = snmp.send(pdu, target);

			if (event == null) {
				throw new RuntimeException("SNMP ResponseEvent é null para OID: " + oid);
			}

			if (event.getResponse() == null) {
				throw new RuntimeException("SNMP timeout ao consultar OID: " + oid);
			}

			if (event.getResponse().getErrorStatus() != PDU.noError) {
				throw new RuntimeException("Erro SNMP ao consultar OID " + oid + ": " +
						event.getResponse().getErrorStatusText());
			}

			return event.getResponse().get(0).getVariable().toLong();

		} finally {
			try {
				if (snmp != null) {
					snmp.close();
				}
				if (transport != null) {
					transport.close();
				}
			} catch (Exception e) {
				// Ignorar erros ao fechar recursos
			}
		}
	}

	private void setOctets(String interfaceName) {
		interfaceOctetsMap = new HashMap<>();
		switch (interfaceName) {
			case "ether1":
				interfaceOctetsMap.put("in", IF_IN_OCTETS + 2);
				interfaceOctetsMap.put("out", IF_OUT_OCTETS + 2);
				break;
			case "ether2":
				interfaceOctetsMap.put("in", IF_IN_OCTETS + 3);
				interfaceOctetsMap.put("out", IF_OUT_OCTETS + 3);
				break;
			default:
				throw new IllegalArgumentException("Interface desconhecida: " + interfaceName +
						". Use 'ether1' ou 'ether2'");
		}
	}
}
