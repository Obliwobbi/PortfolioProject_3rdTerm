package app;

import app.config.HibernateConfig;
import app.daos.CompanyDAO;
import app.daos.UserDAO;
import app.entities.Company;
import app.entities.Role;
import app.entities.User;
import app.services.PasswordService;
import app.utils.Utils;
import jakarta.persistence.EntityManagerFactory;

import java.time.LocalDate;

public class Main {

    public static void main(String[] args) {
        EntityManagerFactory emf = HibernateConfig.getEntityManagerFactory();

        seedBootstrapAdmin(emf);

        int port = getPort();
        ApplicationConfig.startApp(port, emf);
    }

    private static void seedBootstrapAdmin(EntityManagerFactory emf) {
        UserDAO userDAO = new UserDAO(emf);
        CompanyDAO companyDAO = new CompanyDAO(emf);
        PasswordService passwordService = new PasswordService();

        String adminEmail = "admin@obli.dk";

        if (userDAO.findByEmail(adminEmail).isPresent()) {
            System.out.println("Bootstrap admin already exists.");
            return;
        }

        Company company = companyDAO.create(
                Company.builder()
                        .name("Membersystem Bootstrap Company")
                        .build()
        );

//        String password = "Test1234!";
        String password = System.getenv("BOOTSTRAP_ADMIN_PASSWORD");
        if (password == null || password.isBlank()) {
            throw new IllegalStateException("BOOTSTRAP_ADMIN_PASSWORD is not set");
        }
        String hashedPassword = passwordService.hashPassword(password);

        User admin = User.builder()
                .company(company)
                .email(adminEmail)
                .firstname("Admin")
                .lastname("User")
                .dob(LocalDate.of(1990, 1, 1))
                .role(Role.SYSTEM_ADMIN)
                .passwordHash(hashedPassword)
                .build();

        userDAO.create(admin);

        System.out.println("Bootstrap admin created: " + adminEmail);
    }

    private static int getPort() {
        String portEnv = System.getenv("PORT");
        if (portEnv != null && !portEnv.isBlank()) {
            return Integer.parseInt(portEnv);
        }
        return 7000;
    }
}