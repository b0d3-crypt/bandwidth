package com.example.Bandwidth;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class BandwidthController {
	private final BandwidthCalculator bandwidthCalculator;

	public BandwidthController(BandwidthCalculator bandwidthCalculator) {
		this.bandwidthCalculator = bandwidthCalculator;
	}

	@PostMapping()
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Taxas buscadas com sucesso."),
			@ApiResponse(responseCode = "500", description = "Erro interno do servidor.")
	})
	@Operation(description = "tráfego de banda para um endereço IP.")
	public ResponseEntity<BandwidthResult> getBandwidth(@RequestBody BandwidthRequest request) {
		try {
			BandwidthResult result = bandwidthCalculator.calcular(request);
			return ResponseEntity.ok(result);
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}
}
