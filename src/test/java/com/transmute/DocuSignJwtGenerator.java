package com.docker.example;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.entity.UrlEncodedFormEntity;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.message.BasicNameValuePair;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;

import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;
import java.security.Security;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

//If Our Main Focus Is to Send PDF in email for users to sign then best way is this.
public class DocuSignJwtGenerator {

    // DocuSign configuration values - replace with your actual values
    private static final String AUTH_SERVER = "account-d.docusign.com"; // Use account.docusign.com for production

    // JWT token expiration time in seconds (1 hour)
    private static final long EXPIRE_TIME = 3600;
    private static final String CLIENT_ID = "fac0771b-a879-4a59-aff7-b3c528fe0c87"; // Integration Key (Client ID)
    private static final String USER_ID = "8a91ac22-9be0-44d1-8d3b-5df09ce8e9ae"; // API Username (GUID)

    //TODO: Sort of bad habit practice to have RSA private key as a string.
    // But oh well this ain't going to prod. That's what the security "experts" say.
    private static final String PRIVATE_KEY_PEM =
            "-----BEGIN RSA PRIVATE KEY-----\n" +
                    "MIIEpQIBAAKCAQEAvJPkaG8aXxhb7u8eyvE8yvBgfBiTCJnOwEZN/q9e4yJX4xka\n" +
                    "k8Ea0Lih0cz8tGB3wKGjRqsBilPBX7HdszPwLXVI9weZyOwDo3gqADIfsuXb6/8j\n" +
                    "6OpAdacaSCX/HBnDFW7UTBa/FvNO1VM0OlGYKQ2sKrh0xVneXzT6VB5MaLwmW4wT\n" +
                    "tA2xlAJVyxNo1EJ/tdo1ae5+IaIXiqgNFeAMx73u45p/Res1cTYo8BHktBuWh5iH\n" +
                    "i2+9sb2tdDu7V8iCqzZKSov1HNcjXR4F77APhusOBjBQBbSXkirB2VHexpONgrCH\n" +
                    "YvgsUSEd8po2IQU0NKcmBtB0SjZUuLHM6xPLowIDAQABAoIBAA7VJ80/wUMYlJFD\n" +
                    "IANrcnzYkmZAZ+mbG3mL2MepvfIUpKAOvJnsQZ0y4JDdfu+lz4AzK+IQ7P1B4D8U\n" +
                    "K5wpqisRLheMHVWsnA6oEs79d6PSfk1ePfysgZHKKSrinjcfdEpDyOa2j/o7UgiS\n" +
                    "flRV+vzDqraY1/UhP/icyfyC05TAkSFGHKrM+W7OQqtOvqBZaFJQTq+HXSiBROzM\n" +
                    "L9BR6rfX+ItlQVa91mdWHOaY1I1K9x6FQfZ9PAyvW1hZ54E7F2J5MHshic+DjPM9\n" +
                    "uK9evmtW/DFIzV2QTtqo+Yl2kOknkkqENgHZ+XNdO4FUHN/5uPumcUZU3SEZulVJ\n" +
                    "H71K6CECgYEA8T+4YiJSZek2NIpVil1y0EjoC0YC6zIy4ylPFleiYMWnvKBDDNyL\n" +
                    "jG7lrBQCJDYVS09Csp1FXgQpCs2VO2IjsZErSTcZU8PVFmAD7Btq0XDkA3lUhP5o\n" +
                    "O6REtPR/DFiTnfd5fo4RVyLOzs1vQ2p+xMd+ardzukasTwUCbJc88mECgYEAyBu1\n" +
                    "ElLeb723oyHuPCOGtOCehF40XHAHoSMS7SqRQFpAnyMphcvrXn2brDXW1OwRFUmm\n" +
                    "DbCuqdgatH6C0QSamOOmp+ZlHeXQuLKTThPVUIXpqhnskkm0WO9Bm/k4pNZQ4SEP\n" +
                    "5CtsHGNNSp92ksti/KFkDG2CLp0vL2z6AknCRIMCgYEAoESmp3sJpm5h9lGYNk2S\n" +
                    "gtCMZ0Dhm4JD0CmXQs6BLyzgHC2Tna2+f1ME/WZlbc0IJHnJY4Obm7PD2EtvQn4W\n" +
                    "HFbGnOMfzkgg/bZXpLkqI22N7Mjj315rvIOKqXRsGrWVi7HmYq7a0jprjXNo4S6U\n" +
                    "fQHS9+5b0Xo16mD3lVB7ysECgYEAkE+jCv7mWZFpQOV0aLx6JjGjP/SsMEm+xs+D\n" +
                    "k7RSIiW0Ws/B3zLLoE5XmR8OumkwfLirX04e+G/X+nkOIG0AjOgIRSebnq8hEkH+\n" +
                    "h504BiU8+SZO/MTYohaRq0lVN1Mz6tesHTHPer2GfR15Jq40ydoeS/QhoTpMUBh4\n" +
                    "SHQm/L8CgYEAxXen3+ykaq7RZHrTQspOe7/yJMb6eDZsSjfFydeJiwScKo17b8iY\n" +
                    "s7wuI2Z8/uuKP7x85R0KdWIH9+V3e9mSfY+rbwCwkQ6cdLE8OVflMig7FbehsfSj\n" +
                    "5XFXsROgnhHqteElRBy8X4/p8ZrI+sJfFuzTKcFUKUcRIbyqyUo63LY=\n" +
                    "-----END RSA PRIVATE KEY-----";

    public static void main(String[] args) {
        try {
            // Generate the JWT token
            String token = generateJWTToken();

            System.out.println("Generated DocuSign JWT Token:");
            System.out.println(token);

            System.out.println("\nTo exchange this token for an access token, make a POST request to:");
            System.out.println("https://" + AUTH_SERVER + "/oauth/token");
            System.out.println("With form parameters:");
            System.out.println("grant_type=urn:ietf:params:oauth:grant-type:jwt-bearer");
            System.out.println("assertion=" + token);

            String tokenEndpoint = "https://account-d.docusign.com/oauth/token";

            try (CloseableHttpClient client = HttpClients.createDefault()) {
                HttpPost post = new HttpPost(tokenEndpoint);
                post.setHeader("Content-Type", "application/x-www-form-urlencoded");

                // Set form parameters
                List<NameValuePair> formParams = new ArrayList<>();
                formParams.add(new BasicNameValuePair("grant_type", "urn:ietf:params:oauth:grant-type:jwt-bearer"));
                formParams.add(new BasicNameValuePair("assertion", token));

                post.setEntity(new UrlEncodedFormEntity(formParams, StandardCharsets.UTF_8));

                try (CloseableHttpResponse response = client.execute(post)) {
                    ObjectMapper mapper = new ObjectMapper();
                    JsonNode jsonResponse = mapper.readTree(response.getEntity().getContent());

                    if (response.getCode() == 200) {
                        String accessToken = jsonResponse.get("access_token").asText();
                        System.out.println("Access Token: " + accessToken);
                    } else {
                        System.out.println("Error: " + jsonResponse.toPrettyString());
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String generateJWTToken() throws Exception {
        // Add BouncyCastle as a Security Provider
        Security.addProvider(new BouncyCastleProvider());

        // Get the private key from the PEM string
        PrivateKey privateKey = getPrivateKeyFromString(PRIVATE_KEY_PEM);

        // Current time and expiration time
        long now = System.currentTimeMillis();
        long expiry = now + (EXPIRE_TIME * 1000);

        // Build the JWT claims
        return Jwts.builder()
                .setIssuer(CLIENT_ID)
                .setSubject(USER_ID)
                .setAudience(AUTH_SERVER)
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(expiry))
                .setId(UUID.randomUUID().toString())
                .claim("scope", "signature impersonation")  // Standard DocuSign scopes
                .signWith(privateKey, SignatureAlgorithm.RS256)
                .compact();
    }

    private static PrivateKey getPrivateKeyFromString(String pemKey) throws Exception {
        try (PEMParser pemParser = new PEMParser(new StringReader(pemKey))) {
            Object parsedObject = pemParser.readObject();
            JcaPEMKeyConverter converter = new JcaPEMKeyConverter().setProvider("BC");

            if (parsedObject instanceof PEMKeyPair) {
                return converter.getPrivateKey(((PEMKeyPair) parsedObject).getPrivateKeyInfo());
            } else if (parsedObject instanceof PrivateKeyInfo) {
                return converter.getPrivateKey((PrivateKeyInfo) parsedObject);
            } else {
                throw new IllegalArgumentException("Invalid private key format");
            }
        }
    }
}