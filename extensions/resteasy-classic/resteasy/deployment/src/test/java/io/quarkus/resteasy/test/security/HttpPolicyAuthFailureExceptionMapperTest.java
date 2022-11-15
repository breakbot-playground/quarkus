package io.quarkus.resteasy.test.security;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import org.hamcrest.Matchers;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.quarkus.security.AuthenticationFailedException;
import io.quarkus.security.test.utils.TestIdentityController;
import io.quarkus.security.test.utils.TestIdentityProvider;
import io.quarkus.test.QuarkusUnitTest;
import io.restassured.RestAssured;

public class HttpPolicyAuthFailureExceptionMapperTest {

    private static final String EXPECTED_RESPONSE = "expect response";

    @RegisterExtension
    static QuarkusUnitTest runner = new QuarkusUnitTest()
            .withApplicationRoot((jar) -> jar
                    .addClasses(TestIdentityProvider.class, TestIdentityController.class)
                    .addAsResource(
                            new StringAsset("quarkus.http.auth.proactive=false\n" +
                                    "quarkus.http.auth.permission.basic.paths=/*\n" +
                                    "quarkus.http.auth.permission.basic.policy=authenticated\n"),
                            "application.properties"));

    @BeforeAll
    public static void setupUsers() {
        TestIdentityController.resetRoles()
                .add("user", "user", "user");
    }

    @Test
    public void testAuthFailedExceptionMapper() {
        RestAssured
                .given()
                .auth().basic("user", "unknown-pwd")
                .get("/")
                .then()
                .statusCode(401)
                .body(Matchers.equalTo(EXPECTED_RESPONSE));
    }

    @Provider
    public static class AuthFailedExceptionMapper implements ExceptionMapper<AuthenticationFailedException> {

        @Override
        public Response toResponse(AuthenticationFailedException exception) {
            return Response.status(401).entity(EXPECTED_RESPONSE).build();
        }
    }

    @Path("hello")
    public static final class HelloResource {

        @GET
        public String hello() {
            return "hello world";
        }
    }

}
