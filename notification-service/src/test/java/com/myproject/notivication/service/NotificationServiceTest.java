package com.myproject.notivication.service;

import com.myproject.notivication.NotificationService;
import com.myproject.notivication.order.event.OrderPlacedEvent;
import jakarta.mail.Message;
import jakarta.mail.Session;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessagePreparator;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private JavaMailSender javaMailSender;

    @InjectMocks
    private NotificationService notificationService;

    private OrderPlacedEvent orderPlacedEvent;

    @BeforeEach
    void setUp() {
        orderPlacedEvent = OrderPlacedEvent.newBuilder()
                .setOrderNumber("ORD-123")
                .setEmail("john.doe@example.com")
                .setFirstName("John")
                .setLastName("Doe")
                .build();
    }

    @Test
    void shouldSendNotificationEmailWhenOrderPlacedEventIsReceived() throws Exception {
        ArgumentCaptor<MimeMessagePreparator> preparatorCaptor =
                ArgumentCaptor.forClass(MimeMessagePreparator.class);

        notificationService.listen(orderPlacedEvent);

        verify(javaMailSender).send(preparatorCaptor.capture());

        MimeMessage mimeMessage = new MimeMessage(Session.getInstance(new Properties()));
        preparatorCaptor.getValue().prepare(mimeMessage);

        assertEquals("springshop@email.com",
                ((InternetAddress) mimeMessage.getFrom()[0]).getAddress());

        assertEquals("john.doe@example.com",
                ((InternetAddress) mimeMessage.getRecipients(Message.RecipientType.TO)[0]).getAddress());

        assertEquals("Your Order with OrderNumber ORD-123 is placed successfully",
                mimeMessage.getSubject());

        String content = mimeMessage.getContent().toString();
        assertTrue(content.contains("Hi John,Doe"));
        assertTrue(content.contains("Your order with order number ORD-123 is now placed successfully."));
        assertTrue(content.contains("Best Regards"));
        assertTrue(content.contains("Spring Shop"));
    }

    @Test
    void shouldThrowRuntimeExceptionWhenMailSenderFails() {
        doThrow(new MailSendException("Mail sending failed"))
                .when(javaMailSender)
                .send(any(MimeMessagePreparator.class));

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> notificationService.listen(orderPlacedEvent));

        assertEquals("Exception occurred when sending mail to springshop@email.com", exception.getMessage());
        assertInstanceOf(MailSendException.class, exception.getCause());

        verify(javaMailSender).send(any(MimeMessagePreparator.class));
    }
}