
package com.althaf.portfolio.controller;

import com.althaf.portfolio.model.ContactForm;
import com.althaf.portfolio.service.ContactService;
import com.openhtmltopdf.resource.Resource;
import jakarta.validation.Valid;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class PortfolioController {


    private final ContactService  contactService;

    public PortfolioController(ContactService contactService) {
        this.contactService = contactService;
    }

    @GetMapping("/")
    public String home() { return "index"; }

    @GetMapping("/about")
    public String about() { return "about"; }

    @GetMapping("/projects")
    public String projects() { return "projects"; }

    @GetMapping("/contact")
    public String contact(Model model) {
        model.addAttribute("form", new ContactForm());
        return "contact";
    }

    @PostMapping("/contact")
    public String submit(
            @Valid @ModelAttribute("form") ContactForm form,
            BindingResult result,
            Model model
    ) {
        if (result.hasErrors()) {
            model.addAttribute("error", "Please fill all fields correctly.");
            return "contact";
        }

        try {
            String ticketId = contactService.sendContact(form);
            model.addAttribute("success", "Message sent! Ticket: " + ticketId);
            model.addAttribute("form", new ContactForm());
            return "contact";
        } catch (Exception e) {
            model.addAttribute("error", "Unable to send message right now. Please try again later.");
            return "contact";
        }
    }




}
