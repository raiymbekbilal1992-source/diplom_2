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

import java.util.UUID;

import static org.hamcrest.Matchers.equalTo;

public class ChangeUserTest {

    private UserClient userClient;
    private User user;
    private String accessToken;

    @Before
    public void setUp() {
        userClient = new UserClient();
        user = UserGenerator.getRandomUser();

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
    @DisplayName("Изменение имени пользователя с авторизацией")
    @Description("PATCH /auth/user с валидным токеном меняет имя пользователя")
    public void updateNameWithAuthShouldSucceed() {
        String newName = "Updated" + UUID.randomUUID().toString().substring(0, 6);
        User update = new User();
        update.setName(newName);

        Response response = userClient.updateUser(accessToken, update);

        response.then()
                .statusCode(200)
                .body("success", equalTo(true))
                .body("user.name", equalTo(newName))
                .body("user.email", equalTo(user.getEmail().toLowerCase()));
    }

    @Test
    @DisplayName("Изменение email пользователя с авторизацией")
    @Description("PATCH /auth/user с валидным токеном меняет email пользователя")
    public void updateEmailWithAuthShouldSucceed() {
        String newEmail = "updated-" + UUID.randomUUID().toString().substring(0, 8) + "@yandex.ru";
        User update = new User();
        update.setEmail(newEmail);

        Response response = userClient.updateUser(accessToken, update);

        response.then()
                .statusCode(200)
                .body("success", equalTo(true))
                .body("user.email", equalTo(newEmail.toLowerCase()));
    }

    @Test
    @DisplayName("Изменение пароля пользователя с авторизацией")
    @Description("PATCH /auth/user с валидным токеном меняет пароль; проверяем логином с новым паролем")
    public void updatePasswordWithAuthShouldSucceed() {
        String newPassword = "NewPass" + UUID.randomUUID().toString().substring(0, 6);
        User update = new User();
        update.setPassword(newPassword);

        Response response = userClient.updateUser(accessToken, update);
        response.then().statusCode(200).body("success", equalTo(true));

        // проверяем, что новый пароль реально работает
        User newCredentials = User.credentials(user.getEmail(), newPassword);
        Response loginResponse = userClient.login(newCredentials);
        loginResponse.then().statusCode(200).body("success", equalTo(true));
    }

    @Test
    @DisplayName("Изменение данных пользователя без авторизации")
    @Description("PATCH /auth/user без токена возвращает 401 и ошибку")
    public void updateUserWithoutAuthShouldReturnError() {
        User update = new User();
        update.setName("ShouldNotBeApplied");

        Response response = userClient.updateUserWithoutAuth(update);

        response.then()
                .statusCode(401)
                .body("success", equalTo(false))
                .body("message", equalTo("You should be authorised"));
    }
}
