package stellarburgers.generator;

import stellarburgers.model.User;

import java.util.UUID;

public class UserGenerator {

    public static User getRandomUser() {
        String uniquePart = UUID.randomUUID().toString().substring(0, 8);
        String email = "testuser-" + uniquePart + "@yandex.ru";
        String password = "Pass" + uniquePart;
        String name = "TestUser" + uniquePart;
        return new User(email, password, name);
    }
}
