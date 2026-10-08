package com.micro.orderservice.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderLineItemsDto {

	@NotBlank
	private String skuCode;

	@NotNull
	@Positive
	private BigDecimal price;

	@NotNull
	@Positive
	private Integer quantity;
}
