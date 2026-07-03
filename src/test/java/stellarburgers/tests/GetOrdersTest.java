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

import java.util.List;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

public class GetOrdersTest {

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

        // создаём заказ, чтобы было что получать
        List<String> ingredientIds = ingredientClient.getIngredientIds(2);
        orderClient.create(new OrderRequest(ingredientIds), accessToken);
    }

    @After
    public void tearDown() {
        if (accessToken != null) {
            userClient.delete(accessToken);
        }
    }

    @Test
    @DisplayName("Получение заказов авторизованным пользователем")
    @Description("GET /orders с валидным токеном возвращает 200 и список заказов пользователя")
    public void getOrdersWithAuthShouldSucceed() {
        Response response = orderClient.getUserOrders(accessToken);

        response.then()
                .statusCode(200)
                .body("success", equalTo(true))
                .body("orders", notNullValue());
    }

    @Test
    @DisplayName("Получение заказов неавторизованным пользователем")
    @Description("GET /orders без токена возвращает 401 Unauthorized")
    public void getOrdersWithoutAuthShouldReturnError() {
        Response response = orderClient.getUserOrdersWithoutAuth();

        response.then()
                .statusCode(401)
                .body("success", equalTo(false))
                .body("message", equalTo("You should be authorised"));
    }
}
