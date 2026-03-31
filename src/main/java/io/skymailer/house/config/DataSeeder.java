package io.skymailer.house.config;

import io.skymailer.house.model.*;
import io.skymailer.house.repository.LeadRepository;
import io.skymailer.house.repository.PropertyRepository;
import io.skymailer.house.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final PropertyRepository propertyRepository;
    private final LeadRepository leadRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) {
            log.info("Database already seeded. Skipping.");
            return;
        }

        log.info("Seeding database...");
        seedUsers();
        List<Property> properties = seedProperties();
        seedLeads(properties);
        log.info("Database seeding complete.");
    }

    private void seedUsers() {
        String hash = passwordEncoder.encode("password123");

        userRepository.saveAll(List.of(
                User.builder().name("Admin User").email("admin@house.com")
                        .passwordHash(hash).role(UserRole.ADMIN).build(),
                User.builder().name("Agent Alice").email("alice@house.com")
                        .passwordHash(hash).role(UserRole.AGENT).build(),
                User.builder().name("Agent Bob").email("bob@house.com")
                        .passwordHash(hash).role(UserRole.AGENT).build()
        ));
        log.info("Seeded 3 users (password: password123)");
    }

    private List<Property> seedProperties() {
        List<Property> properties = propertyRepository.saveAll(List.of(
                Property.builder().title("Luxury Villa in Bandra").address("123 Link Road, Bandra West")
                        .city("Mumbai").price(new BigDecimal("25000000")).bedrooms(4).status(PropertyStatus.AVAILABLE).build(),
                Property.builder().title("Sea-View Apartment").address("456 Marine Drive")
                        .city("Mumbai").price(new BigDecimal("18000000")).bedrooms(3).status(PropertyStatus.AVAILABLE).build(),
                Property.builder().title("Modern Flat in Andheri").address("789 MIDC Road, Andheri East")
                        .city("Mumbai").price(new BigDecimal("9500000")).bedrooms(2).status(PropertyStatus.AVAILABLE).build(),
                Property.builder().title("Penthouse in Juhu").address("101 Juhu Beach Road")
                        .city("Mumbai").price(new BigDecimal("45000000")).bedrooms(5).status(PropertyStatus.AVAILABLE).build(),
                Property.builder().title("Compact Studio Dadar").address("202 Dadar TT Circle")
                        .city("Mumbai").price(new BigDecimal("6500000")).bedrooms(1).status(PropertyStatus.AVAILABLE).build(),
                Property.builder().title("Garden Villa Koramangala").address("10 5th Block, Koramangala")
                        .city("Bangalore").price(new BigDecimal("22000000")).bedrooms(4).status(PropertyStatus.AVAILABLE).build(),
                Property.builder().title("Tech Park View Flat").address("55 Whitefield Main Road")
                        .city("Bangalore").price(new BigDecimal("12000000")).bedrooms(3).status(PropertyStatus.AVAILABLE).build(),
                Property.builder().title("Budget Home HSR Layout").address("88 HSR Sector 2")
                        .city("Bangalore").price(new BigDecimal("7500000")).bedrooms(2).status(PropertyStatus.AVAILABLE).build(),
                Property.builder().title("Duplex in Indiranagar").address("14 12th Main, Indiranagar")
                        .city("Bangalore").price(new BigDecimal("35000000")).bedrooms(4).status(PropertyStatus.AVAILABLE).build(),
                Property.builder().title("Starter Flat Electronic City").address("77 Phase 1, EC")
                        .city("Bangalore").price(new BigDecimal("5500000")).bedrooms(1).status(PropertyStatus.AVAILABLE).build()
        ));
        log.info("Seeded 10 properties across Mumbai and Bangalore");
        return properties;
    }

    private void seedLeads(List<Property> props) {
        leadRepository.saveAll(List.of(
                // NEW leads
                Lead.builder().buyerName("Rahul Sharma").phone("+919876543210").email("rahul@email.com")
                        .property(props.get(0)).status(LeadStatus.NEW).priority(LeadPriority.HOT).build(),
                Lead.builder().buyerName("Priya Patel").phone("+919876543211").email("priya@email.com")
                        .property(props.get(1)).status(LeadStatus.NEW).priority(LeadPriority.WARM).build(),
                Lead.builder().buyerName("Amit Kumar").phone("+919876543212").email("amit@email.com")
                        .property(props.get(5)).status(LeadStatus.NEW).priority(LeadPriority.COLD).build(),

                // CONTACTED leads
                Lead.builder().buyerName("Sneha Reddy").phone("+919876543213").email("sneha@email.com")
                        .property(props.get(2)).status(LeadStatus.CONTACTED).priority(LeadPriority.HOT).build(),
                Lead.builder().buyerName("Vikram Singh").phone("+919876543214").email("vikram@email.com")
                        .property(props.get(6)).status(LeadStatus.CONTACTED).priority(LeadPriority.WARM).build(),
                Lead.builder().buyerName("Neha Gupta").phone("+919876543215").email("neha@email.com")
                        .property(props.get(3)).status(LeadStatus.CONTACTED).priority(LeadPriority.HOT).build(),

                // VISITED leads
                Lead.builder().buyerName("Karan Mehta").phone("+919876543216").email("karan@email.com")
                        .property(props.get(0)).status(LeadStatus.VISITED).priority(LeadPriority.HOT).build(),
                Lead.builder().buyerName("Anita Desai").phone("+919876543217").email("anita@email.com")
                        .property(props.get(7)).status(LeadStatus.VISITED).priority(LeadPriority.WARM).build(),
                Lead.builder().buyerName("Rajesh Iyer").phone("+919876543218").email("rajesh@email.com")
                        .property(props.get(8)).status(LeadStatus.VISITED).priority(LeadPriority.HOT).build(),

                // BOOKED leads
                Lead.builder().buyerName("Deepa Nair").phone("+919876543219").email("deepa@email.com")
                        .property(props.get(4)).status(LeadStatus.BOOKED).priority(LeadPriority.HOT).build(),
                Lead.builder().buyerName("Suresh Joshi").phone("+919876543220").email("suresh@email.com")
                        .property(props.get(9)).status(LeadStatus.BOOKED).priority(LeadPriority.WARM).build(),

                // LOST leads
                Lead.builder().buyerName("Meera Kapoor").phone("+919876543221").email("meera@email.com")
                        .property(props.get(1)).status(LeadStatus.LOST).priority(LeadPriority.COLD).build(),
                Lead.builder().buyerName("Arjun Rao").phone("+919876543222").email("arjun@email.com")
                        .property(props.get(6)).status(LeadStatus.LOST).priority(LeadPriority.WARM).build(),
                Lead.builder().buyerName("Pooja Shah").phone("+919876543223").email("pooja@email.com")
                        .property(props.get(3)).status(LeadStatus.LOST).priority(LeadPriority.COLD).build(),
                Lead.builder().buyerName("Nikhil Verma").phone("+919876543224").email("nikhil@email.com")
                        .property(props.get(8)).status(LeadStatus.LOST).priority(LeadPriority.WARM).build()
        ));
        log.info("Seeded 15 leads across all statuses and priorities");
    }
}

