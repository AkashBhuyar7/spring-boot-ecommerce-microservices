package com.micro.notificationservice.listener;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class OrderPlacedListener {

	@KafkaListener(topics = "${app.kafka.order-placed-topic}")
	public void handleOrderPlaced(String orderNumber) {
		log.info("Received order-placed event, sending notification for order {}", orderNumber);
	}
}
