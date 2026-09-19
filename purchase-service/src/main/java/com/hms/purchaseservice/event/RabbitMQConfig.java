package com.hms.purchaseservice.event;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE_NAME = "hms.events.exchange";

    public static final String LOW_STOCK_QUEUE = "hms.purchase.lowstock.queue";
    public static final String LOW_STOCK_ROUTING_KEY = "hms.inventory.low*";

    public static final String GOODS_RECEIVED_ROUTING_KEY = "hms.purchase.goods.received";
    public static final String PO_APPROVED_ROUTING_KEY = "hms.purchase.order.approved";

    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(EXCHANGE_NAME);
    }

    @Bean
    public Queue lowStockQueue() {
        return QueueBuilder.durable(LOW_STOCK_QUEUE).build();
    }

    @Bean
    public Binding lowStockBinding(Queue lowStockQueue, TopicExchange exchange) {
        return BindingBuilder.bind(lowStockQueue).to(exchange).with(LOW_STOCK_ROUTING_KEY);
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
