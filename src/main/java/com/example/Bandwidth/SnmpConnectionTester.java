package com.example.Bandwidth;

import org.snmp4j.CommunityTarget;
import org.snmp4j.PDU;
import org.snmp4j.Snmp;
import org.snmp4j.TransportMapping;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.GenericAddress;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.VariableBinding;
import org.snmp4j.transport.DefaultUdpTransportMapping;

public class SnmpConnectionTester {

	public static boolean testConnection(String ip, String community) {
		String oid = "1.3.6.1.2.1.1.1.0";

		try {
			TransportMapping<?> transport = new DefaultUdpTransportMapping();
			transport.listen();

			CommunityTarget target = new CommunityTarget();
			target.setCommunity(new OctetString(community));
			target.setAddress(GenericAddress.parse("udp:" + ip + "/161"));
			target.setVersion(SnmpConstants.version2c);
			target.setTimeout(2000);
			target.setRetries(1);

			PDU pdu = new PDU();
			pdu.add(new VariableBinding(new OID(oid)));
			pdu.setType(PDU.GET);

			Snmp snmp = new Snmp(transport);
			ResponseEvent response = snmp.send(pdu, target);
			snmp.close();

			return response.getResponse() != null;

		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
}
