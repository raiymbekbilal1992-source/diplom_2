    package stellarburgers.tests;

import io.qameta.allure.Description;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import stellarburgers.client.UserClient;
import stellarburgers.generator.UserGenerator;
import stellarburgers.model.User;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

public class CreateUserTest {

    private UserClient userClient;
    private User user;
    private String accessToken;

    @Before
    public void setUp() {
        RestAssured.baseURI = "https://qa-stellarburgers.education-services.ru";
        userClient = new UserClient();
        user = UserGenerator.getRandomUser();
        accessToken = null;
    }

    @After
    public void tearDown() {
        // Если пользователь был создан — удаляем его, чтобы не засорять базу
        if (accessToken != null) {
            userClient.delete(accessToken);
        }
    }

    @Test
    @DisplayName("Создание уникального пользователя")
    @Description("POST /auth/register с уникальными email/password/name возвращает 200 и success:true")
    public void createUniqueUserSuccessfully() {
        Response response = userClient.create(user);

        response.then()
                .statusCode(200)
                .body("success", equalTo(true))
                .body("user.email", equalTo(user.getEmail().toLowerCase()))
                .body("user.name", equalTo(user.getName()))
                .body("accessToken", notNullValue())
                .body("refreshToken", notNullValue());

        accessToken = response.then().extract().path("accessToken");
    }

    @Test
    @DisplayName("Повторное создание уже зарегистрированного пользователя")
    @Description("POST /auth/register с email уже существующего пользователя возвращает 403 и понятную ошибку")
    public void createUserThatAlreadyExistsShouldReturnError() {
        // сначала успешно создаём пользователя
        Response firstResponse = userClient.create(user);
        accessToken = firstResponse.then().extract().path("accessToken");

        // пробуем создать такого же второй раз
        Response secondResponse = userClient.create(user);

        secondResponse.then()
                .statusCode(403)
                .body("success", equalTo(false))
                .body("message", equalTo("User already exists"));
    }

    @Test
    @DisplayName("Создание пользователя без обязательного поля password")
    @Description("POST /auth/register без одного из обязательных полей возвращает 403 и ошибку валидации")
    public void createUserWithoutPasswordShouldReturnError() {
        User invalidUser = new User(user.getEmail(), null, user.getName());

        Response response = userClient.create(invalidUser);

        response.then()
                .statusCode(403)
                .body("success", equalTo(false))
                .body("message", equalTo("Email, password and name are required fields"));
    }
}
