package com.micro.notificationservice.listener;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.time.Duration;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@EmbeddedKafka(partitions = 1, topics = "order-placed", bootstrapServersProperty = "spring.kafka.bootstrap-servers")
@ExtendWith(OutputCaptureExtension.class)
@ActiveProfiles("test")
class OrderPlacedListenerTest {

	@Autowired
	private KafkaTemplate<String, String> kafkaTemplate;

	@Test
	void sendsNotificationForOrderPlacedEvent(CapturedOutput output) {
		kafkaTemplate.send("order-placed", "order-123");

		await().atMost(Duration.ofSeconds(30))
				.untilAsserted(() -> assertThat(output).contains("sending notification for order order-123"));
	}
}
