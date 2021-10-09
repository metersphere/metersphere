package io.metersphere.log.utils.json.diff;

public class DiffTest {
    public static void main(String[] args) {
        GsonDiff diff = new GsonDiff();

        String newValue= "{\n" +
                "    \"username\": \"zyy\",\n" +
                "    \"username2\": \"zyy\",\n" +
                "    \"password\": \"Calong@2015\"\n" +
                "}";
        String oldValue = "{\n" +
                "    \"username\": \"zyy\",\n" +
                "    \"username1\": \"zyy\",\n" +
                "    \"password\": \"Calong@201512\"\n" +
                "}";
        String d = diff.diff(oldValue,newValue);

        System.out.println(d);

        System.out.println(diff.apply(newValue,d));
    }
}
