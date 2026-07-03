package stellarburgers.tests;

import io.qameta.allure.Description;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.response.Response;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import stellarburgers.client.UserClient;
import stellarburgers.generator.UserGenerator;
import stellarburgers.model.User;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

public class LoginUserTest {

    private UserClient userClient;
    private User user;
    private String accessToken;

    @Before
    public void setUp() {
        userClient = new UserClient();
        user = UserGenerator.getRandomUser();

        // регистрируем пользователя, под которым будем логиниться
        Response createResponse = userClient.create(user);
        accessToken = createResponse.then().extract().path("accessToken");
    }

    @After
    public void tearDown() {
        if (accessToken != null) {
            userClient.delete(accessToken);
        }
    }

    @Test
    @DisplayName("Логин под существующим пользователем")
    @Description("POST /auth/login с верными email/password возвращает 200 и токены")
    public void loginWithExistingUserSuccessfully() {
        User credentials = User.credentials(user.getEmail(), user.getPassword());

        Response response = userClient.login(credentials);

        response.then()
                .statusCode(200)
                .body("success", equalTo(true))
                .body("accessToken", notNullValue())
                .body("refreshToken", notNullValue())
                .body("user.email", equalTo(user.getEmail().toLowerCase()));
    }

    @Test
    @DisplayName("Логин с неверным логином и паролем")
    @Description("POST /auth/login с неверными данными возвращает 401 и сообщение об ошибке")
    public void loginWithWrongCredentialsShouldReturnError() {
        User wrongCredentials = User.credentials(user.getEmail(), "wrongPassword123");

        Response response = userClient.login(wrongCredentials);

        response.then()
                .statusCode(401)
                .body("success", equalTo(false))
                .body("message", equalTo("email or password are incorrect"));
    }
}
