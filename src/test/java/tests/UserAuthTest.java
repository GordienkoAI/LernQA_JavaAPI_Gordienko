package tests;

import io.restassured.RestAssured;
import io.restassured.http.Headers;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import lib.BaseTestCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import lib.Assertions;
import lib.ApiCoreRequests;
import java.nio.channels.FileChannel;
import java.util.HashMap;
import java.util.Map;

import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import org.junit.jupiter.api.DisplayName;

@Epic("Authorisation cases")
@Feature("Authorization")
public class UserAuthTest extends BaseTestCase {

    private final ApiCoreRequests apiCoreRequests = new ApiCoreRequests();
    String cookie;
    String header;
    int userIdOnAuth;

    @BeforeEach
    public void loginUser(){
        Map<String, String> authData = new HashMap<>();
        authData.put("email", "vinkotov@example.com");
        authData.put("password", "1234");


        Response responseGetAuth = apiCoreRequests.
                makePostRequest("https://playground.learnqa.ru/api/user/login", authData);

        this.cookie = this.getCookie(responseGetAuth,"auth_sid");
        this.header = this.getHeader(responseGetAuth,"x-csrf-token");
        this.userIdOnAuth = this.getIntFromJson(responseGetAuth, "user_id");
    }

    @Test
    @Description("This test successfully authorize user by email and password")
    @DisplayName("Test positive auth user")
    public void testAuthUser(){
        Response responseCheckAuth = apiCoreRequests
                .makeGetRequest("https://playground.learnqa.ru/api/user/auth",this.header, this.cookie );
        Assertions.assertJsonByName(responseCheckAuth, "user_id", this.userIdOnAuth);
    }

    @ParameterizedTest
    @ValueSource(strings = {"cookie", "header"})
    @DisplayName("Test negative auth user")
    public void testNegativeAuthUser(String condition){

        RequestSpecification spec = RestAssured.given();
        spec.baseUri("https://playground.learnqa.ru/api/user/auth");

        if(condition.equals("cookie")){
            Response responseForCheck = apiCoreRequests
                    .makeGetRequestWithCookie("https://playground.learnqa.ru/api/user/auth", this.cookie);
            Assertions.assertJsonByName(responseForCheck, "user_id", 0);
        } else if(condition.equals("header")){
            Response responseForeCheck = apiCoreRequests
                    .makeGetRequestWithToken("https://playground.learnqa.ru/api/user/auth", this.header);
            Assertions.assertJsonByName(responseForeCheck, "user_id", 0);
        }else{
            throw new IllegalArgumentException("Condition value is not know: " + condition);
        }
    }
}
