package stellarburgers.client;

import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.response.Response;

import java.util.List;

import static io.restassured.RestAssured.given;
import static stellarburgers.config.Config.BASE_URI;
import static stellarburgers.config.Config.INGREDIENTS;

public class IngredientClient {

    public Response getIngredients() {
        return given()
                .filter(new AllureRestAssured())
                .baseUri(BASE_URI)
                .header("Content-Type", "application/json")
                .when()
                .get(INGREDIENTS);
    }

    // Возвращает id первых count ингредиентов — удобно для тела заказа
    public List<String> getIngredientIds(int count) {
        return getIngredients()
                .then()
                .extract()
                .jsonPath()
                .getList("data._id", String.class)
                .subList(0, count);
    }
}
