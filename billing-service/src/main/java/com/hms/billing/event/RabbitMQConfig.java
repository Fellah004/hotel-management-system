package com.hms.billingservice.event;

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
    public static final String BREAKAGE_QUEUE = "hms.billing.breakage.queue";
    public static final String BREAKAGE_ROUTING_KEY = "hms.operations.breakage.approved";
    public static final String PAYMENT_SUCCEEDED_QUEUE = "hms.billing.payment.succeeded.queue";
    public static final String PAYMENT_SUCCEEDED_ROUTING_KEY = "hms.payment.succeeded";

    @Bean
    public DirectExchange directExchange() {
        return new DirectExchange(EXCHANGE_NAME);
    }

    @Bean
    public Queue breakageQueue() {
        return QueueBuilder.durable(BREAKAGE_QUEUE).build();
    }

    @Bean
    public Binding breakageBinding(Queue breakageQueue, DirectExchange directExchange) {
        return BindingBuilder.bind(breakageQueue).to(directExchange).with(BREAKAGE_ROUTING_KEY);
    }

    @Bean
    public Queue paymentSucceededQueue() {
        return QueueBuilder.durable(PAYMENT_SUCCEEDED_QUEUE).build();
    }

    @Bean
    public Binding paymentSucceededBinding(Queue paymentSucceededQueue, DirectExchange directExchange) {
        return BindingBuilder.bind(paymentSucceededQueue).to(directExchange).with(PAYMENT_SUCCEEDED_ROUTING_KEY);
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
