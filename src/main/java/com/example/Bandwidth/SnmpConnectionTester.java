package com.example.Bandwidth;

import org.snmp4j.CommunityTarget;
import org.snmp4j.PDU;
import org.snmp4j.Snmp;
import org.snmp4j.TransportMapping;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.Address;
import org.snmp4j.smi.GenericAddress;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.VariableBinding;
import org.snmp4j.transport.DefaultUdpTransportMapping;

public class SnmpConnectionTester {

	public static boolean testConnection(String ip, String community) {
		String oid = "1.3.6.1.2.1.1.1.0";

		TransportMapping<?> transport = null;
		Snmp snmp = null;

		try {
			transport = new DefaultUdpTransportMapping();
			transport.listen();

			CommunityTarget target = new CommunityTarget();
			target.setCommunity(new OctetString(community));
			Address address = GenericAddress.parse("udp:" + ip + "/161");
			target.setAddress(address);
			target.setVersion(SnmpConstants.version2c);
			target.setTimeout(5000);
			target.setRetries(2);

			PDU pdu = new PDU();
			pdu.add(new VariableBinding(new OID(oid)));
			pdu.setType(PDU.GET);

			snmp = new Snmp(transport);
			ResponseEvent response = snmp.send(pdu, target);

			if (response == null) {
				return false;
			}

			if (response.getResponse() == null) {
				return false;
			}

			if (response.getResponse().getErrorStatus() != PDU.noError) {
				return false;
			}

			return true;

		} catch (Exception e) {
			return false;
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
}
