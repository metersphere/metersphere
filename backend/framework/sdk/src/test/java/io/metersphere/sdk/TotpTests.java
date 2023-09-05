package io.metersphere.sdk;

import com.bastiaanjansen.otp.HMACAlgorithm;
import com.bastiaanjansen.otp.HOTPGenerator;
import com.bastiaanjansen.otp.TOTPGenerator;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.time.Duration;


@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TotpTests {
    public static String code;
    // Generate a secret (or use your own secret)
//    private static final byte[] secret = SecretGenerator.generate(256);
    private static final byte[] secret = "3c4a9b79-c45d-4d64-b5f4-ec7620ca3108".getBytes();

    @Test
    @Order(1)
    public void testGenerateTotp() {


        TOTPGenerator totpGenerator = new TOTPGenerator.Builder(secret)
                .withHOTPGenerator(builder -> {
                    builder.withPasswordLength(6);
                    builder.withAlgorithm(HMACAlgorithm.SHA256); // SHA256 and SHA512 are also supported
                })
                .withPeriod(Duration.ofSeconds(60))
                .build();


        try {
            code = totpGenerator.now();
            System.out.println(code);

        } catch (IllegalStateException e) {
            // Handle error
        }
    }

    @Test
    @Order(2)
    public void testVerifyTotp() {

        TOTPGenerator totpGenerator = new TOTPGenerator.Builder(secret)
                .withHOTPGenerator(builder -> {
                    builder.withPasswordLength(6);
                    builder.withAlgorithm(HMACAlgorithm.SHA256); // SHA256 and SHA512 are also supported
                })
                .withPeriod(Duration.ofSeconds(60))
                .build();


        try {

            // To verify a token:
            boolean isValid = totpGenerator.verify(code);
            System.out.println(isValid);
        } catch (IllegalStateException e) {
            // Handle error
        }
    }

    @Test
    @Order(3)
    public void testGenerateHotp() {

        HOTPGenerator hotp = new HOTPGenerator.Builder(secret)
                .withAlgorithm(HMACAlgorithm.SHA256) // SHA256 and SHA512 are also supported
                .withPasswordLength(6)
                .build();
        code = hotp.generate(1);// 1 is the counter value
        System.out.println(code);
    }

    @Test
    @Order(4)
    public void testVerifyHotp() {

        HOTPGenerator hotp = new HOTPGenerator.Builder(secret)
                .withAlgorithm(HMACAlgorithm.SHA256) // SHA256 and SHA512 are also supported
                .withPasswordLength(6)
                .build();

        boolean verify = hotp.verify(code, 1);
        System.out.println(verify);
    }
}
