package com.micro.orderservice.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderRequest {

	@NotEmpty(message = "Order must contain at least one line item")
	private List<@Valid OrderLineItemsDto> orderLineItemsDtoList;
}
