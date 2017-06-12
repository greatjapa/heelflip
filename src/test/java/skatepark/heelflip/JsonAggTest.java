package skatepark.heelflip;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipInputStream;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.exceptions.JedisConnectionException;

public class JsonAggTest {

    private static final String SAMPLE_00 = "sample00.json";
    private static final String SAMPLE_01 = "sample01.json";
    private static final String SAMPLE_02 = "sample02.json";
    private static final String SAMPLE_03 = "sample03.json";

    //http://jsonstudio.com/resources/
    private static final String STOCKS_FILE_PATH = "stocks.zip";
    private static final String ZIPS_FILE_PATH = "zips.zip";

    @Before
    public void cleanRedis() {
        try {
            JedisPool pool = new JedisPool(new JedisPoolConfig(), "localhost");
            pool.getResource().flushAll();
        } catch (JedisConnectionException e) {
            //executing unit tests without redis
        }
    }

    @Test
    public void testFieldNamesOnPlainJson() throws IOException {
        for (JsonAgg jsonAgg : getJsonAggList()) {
            InputStream stream = getClass().getClassLoader().getResourceAsStream(SAMPLE_00);
            jsonAgg.loadNDJSON(stream);

            Assert.assertEquals(10, jsonAgg.getFieldAggCount());
            Assert.assertEquals(90, jsonAgg.getGroupByAggCount());

            Set<String> names = jsonAgg.fieldNames();
            Assert.assertTrue(names.contains("a"));
            Assert.assertTrue(names.contains("b"));
            Assert.assertTrue(names.contains("c"));
            Assert.assertTrue(names.contains("d"));
            Assert.assertTrue(names.contains("e"));
            Assert.assertTrue(names.contains("f"));
            Assert.assertTrue(names.contains("g"));
            Assert.assertTrue(names.contains("h"));
            Assert.assertTrue(names.contains("i"));
            Assert.assertTrue(names.contains("j"));
            Assert.assertFalse(names.contains("l"));
        }
    }

    @Test
    public void testGetFieldAggOnPlainJson() throws IOException {
        for (JsonAgg jsonAgg : getJsonAggList()) {
            InputStream stream = getClass().getClassLoader().getResourceAsStream(SAMPLE_00);
            jsonAgg.loadNDJSON(stream);

            Assert.assertEquals(10, jsonAgg.getFieldAggCount());
            Assert.assertEquals(90, jsonAgg.getGroupByAggCount());

            Assert.assertEquals(0, jsonAgg.getFieldAgg("a").getMin().intValue());
            Assert.assertEquals(0, jsonAgg.getFieldAgg("a").getMax().intValue());
            Assert.assertEquals(0, jsonAgg.getFieldAgg("a").getSum().intValue());
            Assert.assertEquals(20, jsonAgg.getFieldAgg("a").count("0"));

            Assert.assertEquals(1, jsonAgg.getFieldAgg("b").getMin().intValue());
            Assert.assertEquals(1, jsonAgg.getFieldAgg("b").getMax().intValue());
            Assert.assertEquals(20, jsonAgg.getFieldAgg("b").getSum().intValue());
            Assert.assertEquals(20, jsonAgg.getFieldAgg("b").count("1"));

            Assert.assertEquals(2, jsonAgg.getFieldAgg("c").getMin().intValue());
            Assert.assertEquals(2, jsonAgg.getFieldAgg("c").getMax().intValue());
            Assert.assertEquals(40, jsonAgg.getFieldAgg("c").getSum().intValue());
            Assert.assertEquals(20, jsonAgg.getFieldAgg("c").count("2"));

            Assert.assertEquals(3, jsonAgg.getFieldAgg("d").getMin().intValue());
            Assert.assertEquals(3, jsonAgg.getFieldAgg("d").getMax().intValue());
            Assert.assertEquals(60, jsonAgg.getFieldAgg("d").getSum().intValue());
            Assert.assertEquals(20, jsonAgg.getFieldAgg("d").count("3"));

            Assert.assertEquals(4, jsonAgg.getFieldAgg("e").getMin().intValue());
            Assert.assertEquals(4, jsonAgg.getFieldAgg("e").getMax().intValue());
            Assert.assertEquals(80, jsonAgg.getFieldAgg("e").getSum().intValue());
            Assert.assertEquals(20, jsonAgg.getFieldAgg("e").count("4"));

            Assert.assertEquals(5, jsonAgg.getFieldAgg("f").getMin().intValue());
            Assert.assertEquals(5, jsonAgg.getFieldAgg("f").getMax().intValue());
            Assert.assertEquals(100, jsonAgg.getFieldAgg("f").getSum().intValue());
            Assert.assertEquals(20, jsonAgg.getFieldAgg("f").count("5"));

            Assert.assertEquals(6, jsonAgg.getFieldAgg("g").getMin().intValue());
            Assert.assertEquals(6, jsonAgg.getFieldAgg("g").getMax().intValue());
            Assert.assertEquals(120, jsonAgg.getFieldAgg("g").getSum().intValue());
            Assert.assertEquals(20, jsonAgg.getFieldAgg("g").count("6"));

            Assert.assertEquals(7, jsonAgg.getFieldAgg("h").getMin().intValue());
            Assert.assertEquals(7, jsonAgg.getFieldAgg("h").getMax().intValue());
            Assert.assertEquals(140, jsonAgg.getFieldAgg("h").getSum().intValue());
            Assert.assertEquals(20, jsonAgg.getFieldAgg("h").count("7"));

            Assert.assertEquals(8, jsonAgg.getFieldAgg("i").getMin().intValue());
            Assert.assertEquals(8, jsonAgg.getFieldAgg("i").getMax().intValue());
            Assert.assertEquals(160, jsonAgg.getFieldAgg("i").getSum().intValue());
            Assert.assertEquals(20, jsonAgg.getFieldAgg("i").count("8"));

            Assert.assertEquals(9, jsonAgg.getFieldAgg("j").getMin().intValue());
            Assert.assertEquals(9, jsonAgg.getFieldAgg("j").getMax().intValue());
            Assert.assertEquals(180, jsonAgg.getFieldAgg("j").getSum().intValue());
            Assert.assertEquals(20, jsonAgg.getFieldAgg("j").count("9"));
        }

    }

    @Test
    public void testGetGroupByOnPlainJson() throws IOException {
        for (JsonAgg jsonAgg : getJsonAggList()) {
            InputStream stream = getClass().getClassLoader().getResourceAsStream(SAMPLE_00);

            jsonAgg.loadNDJSON(stream);

            Assert.assertEquals(10, jsonAgg.getFieldAggCount());
            Assert.assertEquals(90, jsonAgg.getGroupByAggCount());

            Map<String, String> expected = new HashMap<>();
            expected.put("a", "0");
            expected.put("b", "1");
            expected.put("c", "2");
            expected.put("d", "3");
            expected.put("e", "4");
            expected.put("f", "5");
            expected.put("g", "6");
            expected.put("h", "7");
            expected.put("i", "8");
            expected.put("j", "9");

            for (Map.Entry<String, String> fieldEntry : expected.entrySet()) {
                for (Map.Entry<String, String> groupByEntry : expected.entrySet()) {
                    String fieldName = fieldEntry.getKey();
                    String groupBy = groupByEntry.getKey();
                    if (fieldName.equals(groupBy)) {
                        continue;
                    }

                    String value = fieldEntry.getValue();
                    String groupByValue = groupByEntry.getValue();

                    IGroupByAgg groupByAgg = jsonAgg.getGroupBy(fieldName, groupBy);
                    Assert.assertEquals(1, groupByAgg.values().size());
                    Assert.assertEquals(1, groupByAgg.groupByValues().size());

                    Set<String> groupBySet = groupByAgg.groupBy(groupByValue).distinctValues();
                    Assert.assertEquals(1, groupBySet.size());
                    Assert.assertEquals(value, groupBySet.iterator().next());
                }
            }
        }
    }

    @Test
    public void testFieldNamesOnJsonArrayWithObject() throws IOException {
        for (JsonAgg jsonAgg : getJsonAggList()) {
            InputStream stream = getClass().getClassLoader().getResourceAsStream(SAMPLE_01);

            jsonAgg.loadNDJSON(stream);

            Assert.assertEquals(3, jsonAgg.getFieldAggCount());
            Assert.assertEquals(6, jsonAgg.getGroupByAggCount());

            Set<String> names = jsonAgg.fieldNames();
            Assert.assertTrue(names.contains("a"));
            Assert.assertTrue(names.contains("b.x"));
            Assert.assertTrue(names.contains("b.y"));
            Assert.assertFalse(names.contains("c"));
        }
    }

    @Test
    public void testGetFieldAggOnJsonArrayWithObject() throws IOException {
        for (JsonAgg jsonAgg : getJsonAggList()) {
            InputStream stream = getClass().getClassLoader().getResourceAsStream(SAMPLE_01);

            jsonAgg.loadNDJSON(stream);

            Assert.assertEquals(3, jsonAgg.getFieldAggCount());
            Assert.assertEquals(6, jsonAgg.getGroupByAggCount());

            Assert.assertNull(jsonAgg.getFieldAgg("a").getMin());
            Assert.assertNull(jsonAgg.getFieldAgg("a").getMax());
            Assert.assertNull(jsonAgg.getFieldAgg("a").getSum());

            Assert.assertEquals(9, jsonAgg.getFieldAgg("b.x").getMin().intValue());
            Assert.assertEquals(21, jsonAgg.getFieldAgg("b.x").getMax().intValue());
            Assert.assertEquals(97, jsonAgg.getFieldAgg("b.x").getSum().intValue());

            Assert.assertEquals(-20, jsonAgg.getFieldAgg("b.y").getMin().intValue());
            Assert.assertEquals(-1, jsonAgg.getFieldAgg("b.y").getMax().intValue());
            Assert.assertEquals(-83, jsonAgg.getFieldAgg("b.y").getSum().intValue());
        }
    }

    @Test
    public void testGetGroupByOnJsonArrayWithObject() throws IOException {
        for (JsonAgg jsonAgg : getJsonAggList()) {
            InputStream stream = getClass().getClassLoader().getResourceAsStream(SAMPLE_01);

            jsonAgg.loadNDJSON(stream);

            Assert.assertEquals(3, jsonAgg.getFieldAggCount());
            Assert.assertEquals(6, jsonAgg.getGroupByAggCount());

            IGroupByAgg groupByAgg = jsonAgg.getGroupBy("b.x", "a");

            IFieldAgg fieldAgg = groupByAgg.groupBy("true");
            Set<String> values = fieldAgg.distinctValues();
            Assert.assertEquals(3, values.size());
            Assert.assertTrue(values.contains("9"));
            Assert.assertTrue(values.contains("10"));
            Assert.assertTrue(values.contains("12"));

            groupByAgg = jsonAgg.getGroupBy("b.y", "a");
            fieldAgg = groupByAgg.groupBy("false");
            values = fieldAgg.distinctValues();
            Assert.assertEquals(4, values.size());
            Assert.assertTrue(values.contains("-12"));
            Assert.assertTrue(values.contains("-13"));
            Assert.assertTrue(values.contains("-2"));
            Assert.assertTrue(values.contains("-1"));

            groupByAgg = jsonAgg.getGroupBy("b.y", "b.x");
            fieldAgg = groupByAgg.groupBy("10");
            values = fieldAgg.distinctValues();
            Assert.assertEquals(4, values.size());
            Assert.assertTrue(values.contains("-20"));
            Assert.assertTrue(values.contains("-11"));
            Assert.assertTrue(values.contains("-10"));
            Assert.assertTrue(values.contains("-14"));

            fieldAgg = groupByAgg.groupBy("9");
            values = fieldAgg.distinctValues();
            Assert.assertEquals(2, values.size());
            Assert.assertTrue(values.contains("-20"));
            Assert.assertTrue(values.contains("-10"));

            groupByAgg = jsonAgg.getGroupBy("a", "b.x");
            fieldAgg = groupByAgg.groupBy("10");
            values = fieldAgg.distinctValues();
            Assert.assertEquals(1, values.size());
            Assert.assertTrue(values.contains("true"));
        }
    }

    @Test
    public void testFieldNamesOnJsonArrayWithPrimitive() throws IOException {
        for (JsonAgg jsonAgg : getJsonAggList()) {
            InputStream stream = getClass().getClassLoader().getResourceAsStream(SAMPLE_02);

            jsonAgg.loadNDJSON(stream);

            Assert.assertEquals(5, jsonAgg.getFieldAggCount());
            Assert.assertEquals(20, jsonAgg.getGroupByAggCount());

            Set<String> names = jsonAgg.fieldNames();
            Assert.assertTrue(names.contains("a"));
            Assert.assertTrue(names.contains("b_0"));
            Assert.assertTrue(names.contains("b_1"));
            Assert.assertTrue(names.contains("b_2"));
            Assert.assertTrue(names.contains("b_3"));
            Assert.assertFalse(names.contains("c"));
        }
    }

    @Test
    public void testGetFieldAggOnJsonArrayWithPrimitive() throws IOException {
        for (JsonAgg jsonAgg : getJsonAggList()) {
            InputStream stream = getClass().getClassLoader().getResourceAsStream(SAMPLE_02);

            jsonAgg.loadNDJSON(stream);

            Assert.assertEquals(5, jsonAgg.getFieldAggCount());
            Assert.assertEquals(20, jsonAgg.getGroupByAggCount());

            Assert.assertNull(jsonAgg.getFieldAgg("a").getMin());
            Assert.assertNull(jsonAgg.getFieldAgg("a").getMax());
            Assert.assertNull(jsonAgg.getFieldAgg("a").getSum());

            Assert.assertEquals(2, jsonAgg.getFieldAgg("b_0").getMin().intValue());
            Assert.assertEquals(9, jsonAgg.getFieldAgg("b_0").getMax().intValue());
            Assert.assertEquals(24, jsonAgg.getFieldAgg("b_0").getSum().intValue());

            Assert.assertEquals(-8, jsonAgg.getFieldAgg("b_1").getMin().intValue());
            Assert.assertEquals(-1, jsonAgg.getFieldAgg("b_1").getMax().intValue());
            Assert.assertEquals(-15, jsonAgg.getFieldAgg("b_1").getSum().intValue());

            Assert.assertEquals(10, jsonAgg.getFieldAgg("b_2").getMin().intValue());
            Assert.assertEquals(10, jsonAgg.getFieldAgg("b_2").getMax().intValue());
            Assert.assertEquals(40, jsonAgg.getFieldAgg("b_2").getSum().intValue());

            Assert.assertEquals(0, jsonAgg.getFieldAgg("b_3").getMin().intValue());
            Assert.assertEquals(0, jsonAgg.getFieldAgg("b_3").getMax().intValue());
            Assert.assertEquals(0, jsonAgg.getFieldAgg("b_3").getSum().intValue());
        }
    }

    @Test
    public void testGetGroupByOnJsonArrayWithPrimitive() throws IOException {
        for (JsonAgg jsonAgg : getJsonAggList()) {
            InputStream stream = getClass().getClassLoader().getResourceAsStream(SAMPLE_02);

            jsonAgg.loadNDJSON(stream);

            Assert.assertEquals(5, jsonAgg.getFieldAggCount());
            Assert.assertEquals(20, jsonAgg.getGroupByAggCount());

            IGroupByAgg groupByAgg = jsonAgg.getGroupBy("b_0", "a");
            IFieldAgg fieldAgg = groupByAgg.groupBy("true");
            Set<String> values = fieldAgg.distinctValues();
            Assert.assertEquals(2, values.size());
            Assert.assertTrue(values.contains("9"));
            Assert.assertTrue(values.contains("8"));


            groupByAgg = jsonAgg.getGroupBy("b_0", "b_2");
            fieldAgg = groupByAgg.groupBy("10");
            values = fieldAgg.distinctValues();
            Assert.assertEquals(4, values.size());
            Assert.assertTrue(values.contains("9"));
            Assert.assertTrue(values.contains("9"));
            Assert.assertTrue(values.contains("2"));
            Assert.assertTrue(values.contains("5"));
        }
    }

    @Test
    public void testFieldNamesOnJsonWithObject() throws IOException {
        for (JsonAgg jsonAgg : getJsonAggList()) {
            InputStream stream = getClass().getClassLoader().getResourceAsStream(SAMPLE_03);

            jsonAgg.loadNDJSON(stream);

            Assert.assertEquals(3, jsonAgg.getFieldAggCount());
            Assert.assertEquals(6, jsonAgg.getGroupByAggCount());

            Set<String> names = jsonAgg.fieldNames();
            Assert.assertTrue(names.contains("a"));
            Assert.assertTrue(names.contains("b.x"));
            Assert.assertTrue(names.contains("b.y"));
            Assert.assertFalse(names.contains("c"));
        }
    }

    @Test
    public void testGetFieldAggOnJsonWithObject() throws IOException {
        for (JsonAgg jsonAgg : getJsonAggList()) {
            InputStream stream = getClass().getClassLoader().getResourceAsStream(SAMPLE_03);

            jsonAgg.loadNDJSON(stream);

            Assert.assertEquals(3, jsonAgg.getFieldAggCount());
            Assert.assertEquals(6, jsonAgg.getGroupByAggCount());

            Assert.assertNull(jsonAgg.getFieldAgg("a").getMin());
            Assert.assertNull(jsonAgg.getFieldAgg("a").getMax());
            Assert.assertNull(jsonAgg.getFieldAgg("a").getSum());

            Assert.assertEquals(-1, jsonAgg.getFieldAgg("b.x").getMin().intValue());
            Assert.assertEquals(1, jsonAgg.getFieldAgg("b.x").getMax().intValue());
            Assert.assertEquals(0, jsonAgg.getFieldAgg("b.x").getSum().intValue());

            Assert.assertEquals(2, jsonAgg.getFieldAgg("b.y").getMin().intValue());
            Assert.assertEquals(6, jsonAgg.getFieldAgg("b.y").getMax().intValue());
            Assert.assertEquals(12, jsonAgg.getFieldAgg("b.y").getSum().intValue());
        }
    }

    @Test
    public void testGetGroupByOnJsonWithObject() throws IOException {
        for (JsonAgg jsonAgg : getJsonAggList()) {
            InputStream stream = getClass().getClassLoader().getResourceAsStream(SAMPLE_03);

            jsonAgg.loadNDJSON(stream);

            Assert.assertEquals(3, jsonAgg.getFieldAggCount());
            Assert.assertEquals(6, jsonAgg.getGroupByAggCount());

            IGroupByAgg groupByAgg = jsonAgg.getGroupBy("b.x", "a");
            IFieldAgg fieldAgg = groupByAgg.groupBy("true");
            Set<String> values = fieldAgg.distinctValues();
            Assert.assertEquals(3, values.size());
            Assert.assertTrue(values.contains("-1"));
            Assert.assertTrue(values.contains("0"));
            Assert.assertTrue(values.contains("1"));

            groupByAgg = jsonAgg.getGroupBy("b.y", "b.x");
            fieldAgg = groupByAgg.groupBy("0");
            values = fieldAgg.distinctValues();
            Assert.assertEquals(1, values.size());
            Assert.assertTrue(values.contains("4"));
        }
    }

    @Test
    public void testLargeFileZips() throws IOException {
        for (JsonAgg jsonAgg : getJsonAggList()) {
            InputStream stream = getClass().getClassLoader().getResourceAsStream(ZIPS_FILE_PATH);
            ZipInputStream zipStream = new ZipInputStream(stream);
            zipStream.getNextEntry();

            long time = System.currentTimeMillis();
            jsonAgg.loadNDJSON(zipStream);
            System.out.println("zips takes (seconds): " + (System.currentTimeMillis() - time) / 1000);

            Assert.assertEquals(6, jsonAgg.getFieldAggCount());
            Assert.assertEquals(30, jsonAgg.getGroupByAggCount());

            IFieldAgg popAgg = jsonAgg.getFieldAgg("pop");
            Assert.assertEquals(29467, popAgg.count());
            Assert.assertEquals(0, popAgg.getMin().longValue());
            Assert.assertEquals(112047, popAgg.getMax().longValue());
            Assert.assertEquals(248706415, popAgg.getSum().longValue());

            IGroupByAgg popByCityAgg = jsonAgg.getGroupBy("pop", "city");
            IFieldAgg popParishAgg = popByCityAgg.groupBy("PARISH");
            Assert.assertEquals(1, popParishAgg.count());
            Assert.assertEquals(3039, popParishAgg.getMin().longValue());
            Assert.assertEquals(3039, popParishAgg.getMax().longValue());
            Assert.assertEquals(3039, popParishAgg.getSum().longValue());

            IFieldAgg popBurbankAgg = popByCityAgg.groupBy("BURBANK");
            Assert.assertEquals(9, popBurbankAgg.count());
            Assert.assertEquals(293, popBurbankAgg.getMin().longValue());
            Assert.assertEquals(27870, popBurbankAgg.getMax().longValue());
            Assert.assertEquals(124594, popBurbankAgg.getSum().longValue());
        }
    }

    //    @Test
    public void testLargeFileStocks() throws IOException {
        for (JsonAgg jsonAgg : getJsonAggList()) {
            InputStream stream = getClass().getClassLoader().getResourceAsStream(STOCKS_FILE_PATH);
            ZipInputStream zipStream = new ZipInputStream(stream);
            zipStream.getNextEntry();

            long time = System.currentTimeMillis();
            jsonAgg.loadNDJSON(zipStream);
            System.out.println("stocks takes (seconds): " + (System.currentTimeMillis() - time) / 1000);
            Assert.assertEquals(69, jsonAgg.getFieldAggCount());
            Assert.assertEquals(4692, jsonAgg.getGroupByAggCount());
        }
    }

    private List<JsonAgg> getJsonAggList() {
        List<JsonAgg> list = new ArrayList<>();
        list.add(new JsonAgg());

        try {
            JedisPool pool = new JedisPool(new JedisPoolConfig(), "localhost");
            Jedis jedis = pool.getResource();
            list.add(new JsonAgg(jedis));
            System.out.println("WARNING: using redis at localhost for unit tests!");
        } catch (JedisConnectionException e) {
            System.out.println("WARNING: executing unit tests without redis!");
        }
        return list;
    }

}
