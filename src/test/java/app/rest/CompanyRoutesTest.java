package app.rest;

import app.ApplicationConfig;
import app.config.HibernateTestConfig;
import io.javalin.Javalin;
import io.restassured.RestAssured;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.*;

public class CompanyRoutesTest
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
    @DisplayName("Return status 201: Create new company")
    void createCompany()
    {
        String requestBody = """
            {
              "name": "Test Company"
            }
            """;

        RestAssured
                .given()
                    .contentType("application/json")
                    .body(requestBody)
                .when()
                    .post("/companies")
                .then()
                    .statusCode(201)
                    .body("name", org.hamcrest.Matchers.equalTo("Test Company"))
                    .body("id", org.hamcrest.Matchers.notNullValue());
    }

    @Test
    @DisplayName("Return status 200: Get all companies")
    void getAllCompanies()
    {
        RestAssured
                .given()
                .when()
                    .get("/companies")
                .then()
                    .statusCode(200);
    }

    @Test
    @DisplayName("Return status 200: Get company by ID")
    void shouldGetCompanyById() {
        Long companyId = Integer.toUnsignedLong(
                RestAssured
                        .given()
                        .contentType("application/json")
                        .body("""
                              {
                                "name": "Company For GetById Test"
                              }
                              """)
                        .when()
                        .post("/companies")
                        .then()
                        .statusCode(201)
                        .extract()
                        .path("id"));

        RestAssured
                .given()
                .when()
                .get("/companies/" + companyId)
                .then()
                .statusCode(200)
                .body("id", org.hamcrest.Matchers.equalTo(companyId.intValue()))
                .body("name", org.hamcrest.Matchers.equalTo("Company For GetById Test"));
    }

    @Test
    @DisplayName("Return status 404: Company Does not exist")
    void shouldReturn404WhenCompanyDoesNotExist() {
        RestAssured
                .given()
                .when()
                .get("/companies/999999")
                .then()
                .statusCode(404)
                .body("status", org.hamcrest.Matchers.equalTo(404));
    }

}