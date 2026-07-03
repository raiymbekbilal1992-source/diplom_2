package stellarburgers.client;

import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.response.Response;
import stellarburgers.model.OrderRequest;

import static io.restassured.RestAssured.given;
import static stellarburgers.config.Config.*;

public class OrderClient {

    private io.restassured.specification.RequestSpecification spec() {
        return given()
                .filter(new AllureRestAssured())
                .baseUri(BASE_URI)
                .header("Content-Type", "application/json");
    }

    // Создание заказа с авторизацией
    public Response create(OrderRequest order, String accessToken) {
        return spec()
                .header("Authorization", accessToken)
                .body(order)
                .when()
                .post(ORDERS);
    }

    // Создание заказа без авторизации
    public Response createWithoutAuth(OrderRequest order) {
        return spec()
                .body(order)
                .when()
                .post(ORDERS);
    }

    // Получение заказов авторизованного пользователя
    public Response getUserOrders(String accessToken) {
        return spec()
                .header("Authorization", accessToken)
                .when()
                .get(ORDERS);
    }

    // Получение заказов без авторизации
    public Response getUserOrdersWithoutAuth() {
        return spec()
                .when()
                .get(ORDERS);
    }
}
