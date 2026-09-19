package com.hms.reportingservice.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String HOTEL_EXCHANGE = "hms.events.exchange";
    public static final String REPORTING_QUEUE = "hms.reporting.queue";

    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(HOTEL_EXCHANGE);
    }

    @Bean
    public Queue reportingQueue() {
        return QueueBuilder.durable(REPORTING_QUEUE).build();
    }

    @Bean
    public Binding reportingBinding(Queue reportingQueue, TopicExchange exchange) {
        return BindingBuilder.bind(reportingQueue).to(exchange).with("hms.#");
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
