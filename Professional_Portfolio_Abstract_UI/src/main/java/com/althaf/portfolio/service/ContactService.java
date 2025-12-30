package com.althaf.portfolio.service;

import com.althaf.portfolio.model.ContactForm;
import com.althaf.portfolio.model.ContactMessage;
import com.althaf.portfolio.repository.ContactMessageRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import jakarta.mail.Address;
import jakarta.mail.Message;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.nio.charset.StandardCharsets;
import java.time.Year;
import java.util.Date;
import java.util.Properties;
import java.util.UUID;

@Service
public class ContactService {

    @Value("${mail.username}")
    private String username;

    @Value("${mail.password}")
    private String password;

    @Value("${mail.supportTo}")
    private String supportTo;

    @Value("${mail.fromName:Althaf Portfolio}")
    private String fromName;

    @Value("${mail.smtp.host:smtp.gmail.com}")
    private String host;

    @Value("${mail.smtp.port:587}")
    private int port;

    @Value("${mail.smtp.starttls:true}")
    private boolean starttls;

    @Value("${mail.smtp.auth:true}")
    private boolean auth;

    private final SpringTemplateEngine thymeleaf;
    private final ContactMessageRepository contactRepo;

    public ContactService(SpringTemplateEngine thymeleaf,
                          ContactMessageRepository contactRepo) {
        this.thymeleaf = thymeleaf;
        this.contactRepo = contactRepo;
    }

    /**
     * Saves contact to DB and sends branded HTML ticket email.
     * Template: templates/email/contact_ticket.html
     */
    public String sendContact(ContactForm form) throws Exception {

        String ticketId = "AL-" + UUID.randomUUID().toString()
                .substring(0, 8).toUpperCase();

        // === Save to DB ===
        ContactMessage msg = new ContactMessage();
        msg.setName(form.getName().trim());
        msg.setEmail(form.getEmail().trim());
        msg.setMessage(form.getMessage().trim());
        msg.setTicketId(ticketId);
        msg.setStatus("OPEN");

        contactRepo.save(msg);

        // === Prepare Thymeleaf context ===
        Context ctx = new Context();
        ctx.setVariable("form", form);
        ctx.setVariable("ticketId", ticketId);
        ctx.setVariable("year", Year.now().getValue());

        // === SMTP session ===
        Session session = Session.getInstance(smtpProps(), new jakarta.mail.Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

    /* ======================================================
       1️⃣ EMAIL TO YOU (SUPPORT / ADMIN)
       ====================================================== */
        String adminHtml = thymeleaf.process("email/contact_ticket", ctx);

        MimeMessage adminMail = new MimeMessage(session);
        adminMail.setSentDate(new Date());
        adminMail.setFrom(new InternetAddress(username, fromName, StandardCharsets.UTF_8.name()));
        adminMail.setRecipient(Message.RecipientType.TO, new InternetAddress(supportTo));
        adminMail.setReplyTo(new Address[]{ new InternetAddress(form.getEmail(), true) });
        adminMail.setSubject("[Ticket " + ticketId + "] New Contact Message",
                StandardCharsets.UTF_8.name());
        adminMail.setContent(adminHtml, "text/html; charset=UTF-8");

        Transport.send(adminMail);

    /* ======================================================
       2️⃣ AUTO-REPLY EMAIL TO SENDER ✅
       ====================================================== */
        if (notBlank(form.getEmail())) {

            String userHtml = thymeleaf.process("email/contact_confirmation", ctx);

            MimeMessage userMail = new MimeMessage(session);
            userMail.setSentDate(new Date());
            userMail.setFrom(new InternetAddress(username, fromName, StandardCharsets.UTF_8.name()));
            userMail.setRecipient(Message.RecipientType.TO, new InternetAddress(form.getEmail()));
            userMail.setSubject("We received your message — Ticket " + ticketId,
                    StandardCharsets.UTF_8.name());
            userMail.setContent(userHtml, "text/html; charset=UTF-8");

            Transport.send(userMail);
        }

        return ticketId;
    }


    // -------- helpers ----------
    private Properties smtpProps() {
        Properties p = new Properties();
        p.put("mail.transport.protocol", "smtp");
        p.put("mail.smtp.host", host);
        p.put("mail.smtp.port", String.valueOf(port));
        p.put("mail.smtp.auth", String.valueOf(auth));
        p.put("mail.smtp.starttls.enable", String.valueOf(starttls));
        p.put("mail.mime.contenthandler", "com.sun.mail.handlers.text_html"); // avoids ClassCastException
        return p;
    }

    private static boolean notBlank(String s) {
        return s != null && !s.isBlank();
    }

    private static String safe(String s) {
        return s == null ? "" : s;
    }
}
