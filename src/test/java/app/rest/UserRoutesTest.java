package app.rest;

import app.ApplicationConfig;
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

public class UserRoutesTest
{
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

    private Long createCompany(String name, String token)
    {
        return Integer.toUnsignedLong(
                RestAssured
                        .given()
                        .contentType("application/json")
                        .body("""
                                  {
                                    "name": "%s"
                                }
                                """.formatted(name))
                        .header("Authorization", "Bearer " + token)
                        .when()
                        .post("/companies")
                        .then()
                        .statusCode(201)
                        .extract()
                        .path("id"));
    }

    private Long createUser(Long companyId, String email, String password, String token)
    {
        return Integer.toUnsignedLong(
                RestAssured
                        .given()
                        .contentType("application/json")
                        .body("""
                                {
                                  "companyId": %d,
                                  "email": "%s",
                                  "firstname": "Test",
                                  "lastname": "User",
                                  "dob": "1996-05-24",
                                  "role": "MEMBER",
                                  "password": "%s"
                                }
                                """.formatted(companyId, email, password))
                        .header("Authorization", "Bearer " + token)
                        .when()
                        .post("/users")
                        .then()
                        .statusCode(201)
                        .extract()
                        .path("id"));
    }

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
                .post("/login")
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
                .body(org.hamcrest.Matchers.equalTo("MemberSystem API is running"));
    }

    @Test
    @DisplayName("POST - Return status 201: Create new user")
    void createUser()
    {
        //Create company
        Long companyId = createCompany("CreateUser Test Company");

        //Create user
        String requestBody = """
                {
                  "companyId": %d,
                  "email": "test@test.dk",
                  "firstname": "Tester",
                  "lastname": "Testersen",
                  "dob": "1996-05-24",
                  "role": "MEMBER"
                }
                """.formatted(companyId);

        //Verify
        RestAssured
                .given()
                .contentType("application/json")
                .body(requestBody)
                .when()
                .post("/users")
                .then()
                .statusCode(201)
                .body("email", org.hamcrest.Matchers.equalTo("test@test.dk"))
                .body("companyId", org.hamcrest.Matchers.equalTo(companyId.intValue()));

    }

    @Test
    @DisplayName("PUT - Return status 200: Update user")
    void updateUser()
    {
        //Create company
        Long companyId = createCompany("UpdateUser Test Company");

        //Create user
        String requestBody = """
                {
                  "companyId": %d,
                  "email": "test@test.dk",
                  "firstname": "Tester",
                  "lastname": "Testersen",
                  "dob": "1996-05-24",
                  "role": "MEMBER"
                }
                """.formatted(companyId);

        Long userId = Integer.toUnsignedLong(
                RestAssured
                        .given()
                        .contentType("application/json")
                        .body(requestBody)
                        .when()
                        .post("/users")
                        .then()
                        .statusCode(201)
                        .extract()
                        .path("id"));

        //Update user
        String updateBody = """
                {
                  "firstname": "Updated",
                  "lastname": "alsoUpdated",
                  "dob": "1996-05-25",
                  "role": "MEMBER"
                }
                """;

        RestAssured
                .given().contentType("application/json").body(updateBody)
                .when().put("/users/" + userId)
                .then().statusCode(200)
                .body("id", org.hamcrest.Matchers.equalTo(userId.intValue()))
                .body("firstname", org.hamcrest.Matchers.equalTo("Updated"))
                .body("lastname", org.hamcrest.Matchers.equalTo("alsoUpdated"))
                .body("email", org.hamcrest.Matchers.equalTo("test@test.dk"))
                .body("role", org.hamcrest.Matchers.equalTo("MEMBER"));


    }

    @Test
    @DisplayName("GET - Return status 200: Get all users")
    void getAllUsers()
    {
        //Create company
        Long companyId = createCompany("GetAllUsers Test Company");

        //Create user
        String requestBody = """
                {
                  "companyId": %d,
                  "email": "test@test.dk",
                  "firstname": "Tester",
                  "lastname": "Testersen",
                  "dob": "1996-05-24",
                  "role": "MEMBER"
                }
                """.formatted(companyId);
        String requestBody2 = """
                {
                  "companyId": %d,
                  "email": "test2@test.dk",
                  "firstname": "Tester2",
                  "lastname": "Testersen",
                  "dob": "1996-05-24",
                  "role": "MEMBER"
                }
                """.formatted(companyId);
        RestAssured
                .given()
                .contentType("application/json")
                .body(requestBody)
                .body(requestBody2)
                .when()
                .post("/users")
                .then()
                .statusCode(201);

        RestAssured
                .given()
                .when()
                .get("/users")
                .then()
                .statusCode(200);
    }

    @Test
    @DisplayName("GET - Return status 200: Get user by ID")
    void getUserById()
    {
        //Create company
        Long companyId = createCompany("GetAllUsers Test Company");

        //Create request bodies for users
        String requestBody = """
                {
                  "companyId": %d,
                  "email": "test@test.dk",
                  "firstname": "Tester",
                  "lastname": "Testersen",
                  "dob": "1996-05-24",
                  "role": "MEMBER"
                }
                """.formatted(companyId);
        String requestBody2 = """
                {
                  "companyId": %d,
                  "email": "test2@test.dk",
                  "firstname": "Tester2",
                  "lastname": "Testersen",
                  "dob": "1996-05-24",
                  "role": "MEMBER"
                }
                """.formatted(companyId);

        //Post both request bodies
        //Gets userId 1
        RestAssured
                .given()
                .contentType("application/json")
                .body(requestBody)
                .when()
                .post("/users")
                .then()
                .statusCode(201);

        // Gets userId 2
        Long userId = Integer.toUnsignedLong(
                RestAssured
                        .given()
                        .contentType("application/json")
                        .body(requestBody2)
                        .when()
                        .post("/users")
                        .then()
                        .statusCode(201)
                        .extract()
                        .path("id"));

        //Get user with user id 2
        RestAssured
                .given()
                .when()
                .get("/users/" + userId)
                .then()
                .statusCode(200)
                .body("id", org.hamcrest.Matchers.equalTo(userId.intValue()))
                .body("email", org.hamcrest.Matchers.equalTo("test2@test.dk"));
    }

    @Test
    @DisplayName("DELETE - Return status 201/204/404: Delete user")
    void deleteUser()
    {
        //Create company
        Long companyId = createCompany("DeleteUser Test Company");

        //Create user
        String requestBody = """
                {
                  "companyId": %d,
                  "email": "test@test.dk",
                  "firstname": "Tester",
                  "lastname": "Testersen",
                  "dob": "1996-05-24",
                  "role": "MEMBER"
                }
                """.formatted(companyId);

        Long userId = Integer.toUnsignedLong(
                RestAssured
                        .given()
                        .contentType("application/json")
                        .body(requestBody)
                        .when()
                        .post("/users")
                        .then()
                        .statusCode(201)
                        .extract()
                        .path("id"));

        //Delete user
        RestAssured
                .given()
                .when()
                .delete("/users/" + userId)
                .then()
                .statusCode(204);

        //Verify
        RestAssured
                .given()
                .when()
                .get("/users/" + userId)
                .then()
                .statusCode(404);
    }

}