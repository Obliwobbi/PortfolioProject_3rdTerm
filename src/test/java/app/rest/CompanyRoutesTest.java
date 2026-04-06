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
    @DisplayName("POST - Return status 201: Create new company")
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
    @DisplayName("PUT - Return status 200: Update company")
    void updateCompany()
    {
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
                        .when()
                        .post("/companies")
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
                .when().put("/companies/" + companyId)
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
                .get("/companies")
                .then()
                .statusCode(200);
    }

    @Test
    @DisplayName("Return status 200: Get company by ID")
    void getCompanyById()
    {
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
    @DisplayName("Return status 201/204/404: Create, Delete, Verify (no) company")
    void deleteCompany()
    {
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
                        .when()
                        .post("/companies")
                        .then()
                        .statusCode(201)
                        .extract()
                        .path("id"));

        //Delete company
        RestAssured
                .given()
                .when()
                .delete("/companies/" + companyId)
                .then()
                .statusCode(204);

        //Verify
        RestAssured
                .given()
                .when()
                .get("/companies/" + companyId)
                .then()
                .statusCode(404);
    }

    @Test
    @DisplayName("Return status 404: Company Does not exist")
    void return404WhenCompanyDoesNotExist()
    {
        RestAssured
                .given()
                .when()
                .get("/companies/999999")
                .then()
                .statusCode(404)
                .body("status", org.hamcrest.Matchers.equalTo(404));
    }

}