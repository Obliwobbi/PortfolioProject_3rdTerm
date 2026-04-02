package app;

import app.config.HibernateConfig;
import app.daos.CompanyDAO;
import app.daos.UserDAO;
import app.entities.Company;
import app.entities.Role;
import app.entities.User;
import jakarta.persistence.EntityManagerFactory;

import java.time.LocalDate;

public class MainSeeder {

    private static final EntityManagerFactory emf = HibernateConfig.getEntityManagerFactory();

    public static void main(String[] args) {

        CompanyDAO companyDAO = new CompanyDAO(emf);
        UserDAO userDAO = new UserDAO(emf);

        // Create company
        Company silverbackGym = companyDAO.create(
                Company.builder()
                        .name("Silverback Sportsgym")
                        .build()
        );

        // Create system admin
        User systemAdmin = userDAO.create(
                User.builder()
                        .company(silverbackGym)
                        .email("admin@silverback.dk")
                        .firstname("Toby")
                        .lastname("Hartzberg")
                        .dob(LocalDate.of(1996, 5, 24))
                        .role(Role.SYSTEM_ADMIN)
                        .passwordHash("dummy-hash-admin")
                        .build()
        );

        // Create member
        User member = userDAO.create(
                User.builder()
                        .company(silverbackGym)
                        .email("member@silverback.dk")
                        .firstname("Julie-Thilde")
                        .lastname("Hartzberg")
                        .dob(LocalDate.of(1996, 6, 28))
                        .role(Role.MEMBER)
                        .passwordHash("dummy-hash-member")
                        .build()
        );

        System.out.println("--- SEEDED DATA ---");
        System.out.println("Company id: " + silverbackGym.getId());
        System.out.println("System admin id: " + systemAdmin.getId());
        System.out.println("Member id: " + member.getId());

        emf.close();
    }
}