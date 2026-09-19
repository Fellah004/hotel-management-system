package com.hms.reservationservice.event;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE_NAME = "hms.events.exchange";

    public static final String RESERVATION_QUEUE = "hms.reservation.queue";
    public static final String RESERVATION_PAYMENT_QUEUE = "hms.reservation.payment.queue";
    public static final String RESERVATION_ROOM_QUEUE = "hms.reservation.room.queue";

    public static final String RESERVATION_ROUTING_KEY = "hms.reservation.#";
    public static final String PAYMENT_ROUTING_KEY = "hms.payment.#";
    public static final String ROOM_AVAILABLE_ROUTING_KEY = "hms.room.available";

    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(EXCHANGE_NAME);
    }

    @Bean
    public Queue reservationQueue() {
        return new Queue(RESERVATION_QUEUE, true);
    }

    @Bean
    public Queue reservationPaymentQueue() {
        return new Queue(RESERVATION_PAYMENT_QUEUE, true);
    }

    @Bean
    public Queue reservationRoomQueue() {
        return new Queue(RESERVATION_ROOM_QUEUE, true);
    }

    @Bean
    public Binding reservationBinding(Queue reservationQueue, TopicExchange exchange) {
        return BindingBuilder.bind(reservationQueue).to(exchange).with(RESERVATION_ROUTING_KEY);
    }

    @Bean
    public Binding reservationPaymentBinding(Queue reservationPaymentQueue, TopicExchange exchange) {
        return BindingBuilder.bind(reservationPaymentQueue).to(exchange).with(PAYMENT_ROUTING_KEY);
    }

    @Bean
    public Binding reservationRoomBinding(Queue reservationRoomQueue, TopicExchange exchange) {
        return BindingBuilder.bind(reservationRoomQueue).to(exchange).with(ROOM_AVAILABLE_ROUTING_KEY);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }
}
