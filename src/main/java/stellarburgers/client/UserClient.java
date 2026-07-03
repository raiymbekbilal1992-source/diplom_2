package stellarburgers.client;

import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.response.Response;
import stellarburgers.model.User;

import static io.restassured.RestAssured.given;
import static stellarburgers.config.Config.*;

public class UserClient {

    private io.restassured.specification.RequestSpecification spec() {
        return given()
                .filter(new AllureRestAssured())
                .baseUri(BASE_URI)
                .header("Content-Type", "application/json");
    }

    // Регистрация нового пользователя
    public Response create(User user) {
        return spec()
                .body(user)
                .when()
                .post(REGISTER);
    }

    // Логин существующего пользователя (нужны только email + password)
    public Response login(User credentials) {
        return spec()
                .body(credentials)
                .when()
                .post(LOGIN);
    }

    // Удаление пользователя — используется в @After для очистки данных
    public Response delete(String accessToken) {
        return spec()
                .header("Authorization", accessToken)
                .when()
                .delete(USER);
    }

    // Получение данных пользователя
    public Response getUser(String accessToken) {
        return spec()
                .header("Authorization", accessToken)
                .when()
                .get(USER);
    }

    // Изменение данных пользователя с авторизацией
    public Response updateUser(String accessToken, User updatedFields) {
        return spec()
                .header("Authorization", accessToken)
                .body(updatedFields)
                .when()
                .patch(USER);
    }

    // Изменение данных пользователя без авторизации
    public Response updateUserWithoutAuth(User updatedFields) {
        return spec()
                .body(updatedFields)
                .when()
                .patch(USER);
    }
}
