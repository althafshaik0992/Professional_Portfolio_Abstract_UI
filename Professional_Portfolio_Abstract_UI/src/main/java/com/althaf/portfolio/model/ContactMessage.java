package com.althaf.portfolio.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "contact_messages")
public class ContactMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(nullable = false, length = 160)
    private String email;

    @Column(nullable = false, length = 4000)
    private String message;

    @Column(nullable = false, length = 30)
    private String status; // OPEN / CLOSED

    @Column(nullable = false, length = 20, unique = true)
    private String ticketId;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public ContactMessage() {}

    // getters/setters
    public Long getId() { return id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getTicketId() { return ticketId; }
    public void setTicketId(String ticketId) { this.ticketId = ticketId; }

    public Instant getCreatedAt() { return createdAt; }
}
