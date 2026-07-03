package stellarburgers.tests;

import io.qameta.allure.Description;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.response.Response;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import stellarburgers.client.IngredientClient;
import stellarburgers.client.OrderClient;
import stellarburgers.client.UserClient;
import stellarburgers.generator.UserGenerator;
import stellarburgers.model.OrderRequest;
import stellarburgers.model.User;

import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

public class CreateOrderTest {

    private UserClient userClient;
    private OrderClient orderClient;
    private IngredientClient ingredientClient;
    private User user;
    private String accessToken;

    @Before
    public void setUp() {
        userClient = new UserClient();
        orderClient = new OrderClient();
        ingredientClient = new IngredientClient();

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
    @DisplayName("Создание заказа с авторизацией и валидными ингредиентами")
    @Description("POST /orders с токеном и списком ингредиентов возвращает 200 и номер заказа")
    public void createOrderWithAuthAndIngredientsShouldSucceed() {
        List<String> ingredientIds = ingredientClient.getIngredientIds(2);
        OrderRequest order = new OrderRequest(ingredientIds);

        Response response = orderClient.create(order, accessToken);

        response.then()
                .statusCode(200)
                .body("success", equalTo(true))
                .body("order.number", notNullValue());
    }

    @Test
    @DisplayName("Создание заказа без авторизации")
    @Description("POST /orders без токена всё равно позволяет создать заказ (по документации доступно неавторизованным)")
    public void createOrderWithoutAuthShouldSucceed() {
        List<String> ingredientIds = ingredientClient.getIngredientIds(2);
        OrderRequest order = new OrderRequest(ingredientIds);

        Response response = orderClient.createWithoutAuth(order);

        response.then()
                .statusCode(200)
                .body("success", equalTo(true))
                .body("order.number", notNullValue());
    }

    @Test
    @DisplayName("Создание заказа без ингредиентов")
    @Description("POST /orders с пустым списком ингредиентов возвращает 400 Bad Request")
    public void createOrderWithoutIngredientsShouldReturnError() {
        OrderRequest order = new OrderRequest(Collections.emptyList());

        Response response = orderClient.create(order, accessToken);

        response.then()
                .statusCode(400)
                .body("success", equalTo(false))
                .body("message", equalTo("Ingredient ids must be provided"));
    }

    @Test
    @DisplayName("Создание заказа с неверным хешем ингредиентов")
    @Description("POST /orders с невалидным id ингредиента возвращает 500 Internal Server Error")
    public void createOrderWithInvalidIngredientHashShouldReturnError() {
        OrderRequest order = new OrderRequest(Collections.singletonList("invalidHash12345"));

        Response response = orderClient.create(order, accessToken);

        response.then()
                .statusCode(500);
    }
}
