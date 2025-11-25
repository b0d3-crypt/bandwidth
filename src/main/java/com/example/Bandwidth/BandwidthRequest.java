package com.example.Bandwidth;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BandwidthRequest {

	private String ipAddress;
	private String interfaceName;
}