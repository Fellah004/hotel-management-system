package com.hms.guestexperienceservice.event;

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
    public static final String HK_TASK_COMPLETED_QUEUE = "hms.guestexperience.hktask.completed.queue";
    public static final String PAYMENT_SUCCEEDED_QUEUE = "hms.guestexperience.payment.succeeded.queue";

    public static final String HK_TASK_COMPLETED_ROUTING_KEY = "hms.operations.housekeeping.completed";
    public static final String PAYMENT_SUCCEEDED_ROUTING_KEY = "hms.payment.succeeded";

    @Bean
    public DirectExchange directExchange() {
        return new DirectExchange(EXCHANGE_NAME);
    }

    @Bean
    public Queue hkTaskCompletedQueue() {
        return QueueBuilder.durable(HK_TASK_COMPLETED_QUEUE).build();
    }

    @Bean
    public Queue paymentSucceededQueue() {
        return QueueBuilder.durable(PAYMENT_SUCCEEDED_QUEUE).build();
    }

    @Bean
    public Binding hkTaskCompletedBinding(Queue hkTaskCompletedQueue, DirectExchange directExchange) {
        return BindingBuilder.bind(hkTaskCompletedQueue).to(directExchange).with(HK_TASK_COMPLETED_ROUTING_KEY);
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
