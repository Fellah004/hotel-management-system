package com.hms.operationsservice.event;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE_NAME = "hms.direct.exchange";

    public static final String CHECKOUT_QUEUE = "hms.operations.checkout.queue";
    public static final String ROOM_DIRTY_QUEUE = "hms.operations.roomdirty.queue";
    public static final String SERVICE_REQUEST_QUEUE = "hms.operations.servicerequest.queue";

    public static final String CHECKOUT_ROUTING_KEY = "hms.reservation.checkedout";
    public static final String ROOM_DIRTY_ROUTING_KEY = "hms.room.dirty";
    public static final String SERVICE_REQUEST_ROUTING_KEY = "hms.guestexperience.servicerequest.created";

    @Bean
    public DirectExchange directExchange() {
        return new DirectExchange(EXCHANGE_NAME);
    }

    @Bean
    public Queue checkoutQueue() {
        return QueueBuilder.durable(CHECKOUT_QUEUE).build();
    }

    @Bean
    public Queue roomDirtyQueue() {
        return QueueBuilder.durable(ROOM_DIRTY_QUEUE).build();
    }

    @Bean
    public Queue serviceRequestQueue() {
        return QueueBuilder.durable(SERVICE_REQUEST_QUEUE).build();
    }

    @Bean
    public Binding checkoutBinding(Queue checkoutQueue, DirectExchange directExchange) {
        return BindingBuilder.bind(checkoutQueue).to(directExchange).with(CHECKOUT_ROUTING_KEY);
    }

    @Bean
    public Binding roomDirtyBinding(Queue roomDirtyQueue, DirectExchange directExchange) {
        return BindingBuilder.bind(roomDirtyQueue).to(directExchange).with(ROOM_DIRTY_ROUTING_KEY);
    }

    @Bean
    public Binding serviceRequestBinding(Queue serviceRequestQueue, DirectExchange directExchange) {
        return BindingBuilder.bind(serviceRequestQueue).to(directExchange).with(SERVICE_REQUEST_ROUTING_KEY);
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
