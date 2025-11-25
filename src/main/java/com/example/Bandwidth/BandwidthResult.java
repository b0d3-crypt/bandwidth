package com.example.Bandwidth;

import lombok.*;

import java.time.Instant;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BandwidthResult {
	private double rxMbps;
	private double txMbps;
	private Instant hora;
}
