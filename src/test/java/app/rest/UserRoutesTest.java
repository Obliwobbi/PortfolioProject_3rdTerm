package app.rest;

import app.ApplicationConfig;
import app.config.HibernateTestConfig;
import io.javalin.Javalin;
import io.restassured.RestAssured;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.*;

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
    @DisplayName("Test user creation")
    void CreateUser()
    {
        String requestBody = """
            {
              "email: "test@test.dk
              "firstname": "Tester"
              "lastname": "Testersen"
              "dob":
              {
                "0":
            }
            """;
    }
}