package app.rest;

import app.config.ApplicationConfig;
import app.config.HibernateTestConfig;
import app.daos.CompanyDAO;
import app.daos.UserDAO;
import app.entities.Company;
import app.entities.Role;
import app.entities.User;
import app.services.PasswordService;
import io.javalin.Javalin;
import io.restassured.RestAssured;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.*;

import java.time.LocalDate;

public class CompanyRoutesTest
{
    private static final String API = "/api/v1";
    private static Javalin app;
    private static EntityManagerFactory emf;

    @BeforeAll
    static void setUp()
    {
        emf = HibernateTestConfig.getEntityManagerFactory();
        app = ApplicationConfig.startApp(7001, emf);
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = 7001;
    }

    @BeforeEach
    void cleanUpDatabase()
    {
        var em = emf.createEntityManager();
        try
        {
            em.getTransaction().begin();

            em.createNativeQuery("DELETE FROM users").executeUpdate();
            em.createNativeQuery("DELETE FROM companies").executeUpdate();

            em.getTransaction().commit();
        } finally
        {
            em.close();
        }
    }

    @AfterAll
    static void tearDown()
    {
        if (app != null)
        {
            app.stop();
        }
        if (emf != null && emf.isOpen())
        {
            emf.close();
        }
    }

    // --------------
    // Helper methods
    // --------------

    private String loginAsSeededAdmin()
    {
        CompanyDAO companyDAO = new CompanyDAO(emf);
        UserDAO userDAO = new UserDAO(emf);
        PasswordService passwordService = new PasswordService();

        Company company = companyDAO.create(
                Company.builder()
                        .name("Seeded Admin Company")
                        .build()
        );

        userDAO.create(
                User.builder()
                        .company(company)
                        .email("admin@silverback.dk")
                        .firstname("Toby")
                        .lastname("Hartzberg")
                        .dob(LocalDate.of(1996, 5, 24))
                        .role(Role.SYSTEM_ADMIN)
                        .passwordHash(passwordService.hashPassword("secret123"))
                        .build()
        );

        return RestAssured
                .given()
                .contentType("application/json")
                .body("""
                        {
                          "email": "admin@silverback.dk",
                          "password": "secret123"
                        }
                        """)
                .when()
                .post(API+"/login")
                .then()
                .statusCode(200)
                .extract()
                .path("token");
    }

    @Test
    @DisplayName("Return status 200: API is running")
    void rootEndpointShouldReturnApiRunningMessage()
    {
        RestAssured
                .given()
                .when()
                .get("/")
                .then()
                .statusCode(200)
                .body("message", org.hamcrest.Matchers.equalTo("MemberSystem API is running"));
    }

    @Test
    @DisplayName("POST - Return status 201: Create new company")
    void createCompany()
    {
        String token = loginAsSeededAdmin();
        String requestBody = """
                {
                  "name": "Test Company"
                }
                """;

        RestAssured
                .given()
                .contentType("application/json")
                .body(requestBody)
                .header("Authorization", "Bearer " + token)
                .when()
                .post(API+"/companies")
                .then()
                .statusCode(201)
                .body("name", org.hamcrest.Matchers.equalTo("Test Company"))
                .body("id", org.hamcrest.Matchers.notNullValue());
    }

    @Test
    @DisplayName("PUT - Return status 200: Update company")
    void updateCompany()
    {
        String token = loginAsSeededAdmin();
        //Create company
        String requestBody = """
                {
                  "name": "Test Company"
                }
                """;

        Long companyId = Integer.toUnsignedLong(
                RestAssured
                        .given()
                        .contentType("application/json")
                        .body(requestBody)
                        .header("Authorization", "Bearer " + token)
                        .when()
                        .post(API+"/companies")
                        .then()
                        .statusCode(201)
                        .extract()
                        .path("id"));

        //Update company
        String updateBody = """
                {
                  "name": "Update Test Company"
                }
                """;

        //Verify
        RestAssured
                .given().contentType("application/json").body(updateBody)
                .header("Authorization", "Bearer " + token)
                .when().put(API+"/companies/" + companyId)
                .then().statusCode(200)
                .body("id", org.hamcrest.Matchers.equalTo(companyId.intValue()))
                .body("name", org.hamcrest.Matchers.equalTo("Update Test Company"));

    }

    @Test
    @DisplayName("Return status 200: Get all companies")
    void getAllCompanies()
    {
        RestAssured
                .given()
                .when()
                .get(API+"/companies/public")
                .then()
                .statusCode(200);
    }

    @Test
    @DisplayName("Return status 200: Get company by ID")
    void getCompanyById()
    {
        String token = loginAsSeededAdmin();
        String requestBody = """
                {
                  "name": "Company For GetById Test"
                }
                """;

        Long companyId = Integer.toUnsignedLong(
                RestAssured
                        .given()
                        .contentType("application/json")
                        .body(requestBody)
                        .header("Authorization", "Bearer " + token)
                        .when()
                        .post(API+"/companies")
                        .then()
                        .statusCode(201)
                        .extract()
                        .path("id"));

        RestAssured
                .given()
                .header("Authorization", "Bearer " + token)
                .when()
                .get(API+"/companies/" + companyId)
                .then()
                .statusCode(200)
                .body("id", org.hamcrest.Matchers.equalTo(companyId.intValue()))
                .body("name", org.hamcrest.Matchers.equalTo("Company For GetById Test"));
    }

    @Test
    @DisplayName("Return status 201/204/404: Create, Delete, Verify (no) company")
    void deleteCompany()
    {
        String token = loginAsSeededAdmin();
        //Create company to test delete
        String requestBody = """
                {
                  "name": "Company To Delete"
                }
                """;

        Long companyId = Integer.toUnsignedLong(
                RestAssured
                        .given()
                        .contentType("application/json")
                        .body(requestBody)
                        .header("Authorization", "Bearer " + token)
                        .when()
                        .post(API+"/companies")
                        .then()
                        .statusCode(201)
                        .extract()
                        .path("id"));

        //Delete company
        RestAssured
                .given()
                .header("Authorization", "Bearer " + token)
                .when()
                .delete(API+"/companies/" + companyId)
                .then()
                .statusCode(204);

        //Verify
        RestAssured
                .given()
                .header("Authorization", "Bearer " + token)
                .when()
                .get(API+"/companies/" + companyId)
                .then()
                .statusCode(404);
    }

    @Test
    @DisplayName("Return status 404: Company Does not exist")
    void return404WhenCompanyDoesNotExist()
    {
        String token = loginAsSeededAdmin();

        RestAssured
                .given()
                .header("Authorization", "Bearer " + token)
                .when()
                .get(API+"/companies/999999")
                .then()
                .statusCode(404)
                .body("status", org.hamcrest.Matchers.equalTo(404));
    }

}