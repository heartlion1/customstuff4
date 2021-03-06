package cubex2.cs4.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ArrayDeserializerTest
{
    private static Gson gson;

    @BeforeAll
    public static void setUp()
    {
        gson = new GsonBuilder()
                .registerTypeAdapter(String[].class, new ArrayDeserializer<>(String[]::new, String.class))
                .create();
    }

    @Test
    public void test_singleValue()
    {
        Map<String, String[]> map = gson.fromJson("{ \"array\": \"value\" }", new TypeToken<Map<String, String[]>>() {}.getType());

        assertEquals(1, map.size());
        String[] array = map.get("array");
        assertArrayEquals(new String[] {"value"}, array);
    }

    @Test
    public void test_array()
    {
        String[] array = gson.fromJson("[\"a\", \"b\"]", String[].class);

        assertArrayEquals(new String[] {"a", "b"}, array);
    }
}