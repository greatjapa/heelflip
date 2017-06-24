package skatepark.heelflip;

import com.google.gson.JsonPrimitive;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.exceptions.JedisConnectionException;
import skatepark.heelflip.inmem.InMemFieldAgg;
import skatepark.heelflip.redis.RedisFieldAgg;

public class FieldAggTest {

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
    public void testDoubleValues() {
        for (IFieldAgg fieldAgg : getFieldImplList()) {
            fieldAgg.agg(new JsonPrimitive(2.3));
            fieldAgg.agg(new JsonPrimitive(2.1));
            fieldAgg.agg(new JsonPrimitive(2.0));
            fieldAgg.agg(new JsonPrimitive(2.0));

            Assert.assertEquals("a", fieldAgg.getFieldName());

            Assert.assertEquals(4, fieldAgg.count());
            Assert.assertEquals(3, fieldAgg.cardinality());
            Assert.assertEquals(0, fieldAgg.getStringCount());
            Assert.assertEquals(0, fieldAgg.getBooleanCount());
            Assert.assertEquals(4, fieldAgg.getNumberCount());
            Assert.assertEquals(2.0, fieldAgg.getMin().doubleValue(), 10E-10);
            Assert.assertEquals(2.3, fieldAgg.getMax().doubleValue(), 10E-10);
            Assert.assertEquals(8.4, fieldAgg.getSum().doubleValue(), 10E-10);

            Assert.assertEquals(4, fieldAgg.count());
            Assert.assertEquals(1, fieldAgg.count("2.3"));
            Assert.assertEquals(1, fieldAgg.count("2.1"));
            Assert.assertEquals(2, fieldAgg.count("2.0"));
            Assert.assertEquals(0, fieldAgg.count("1.0"));
        }
    }

    @Test
    public void testIntValues() {
        for (IFieldAgg fieldAgg : getFieldImplList()) {
            fieldAgg.agg(new JsonPrimitive(10));
            fieldAgg.agg(new JsonPrimitive(11));
            fieldAgg.agg(new JsonPrimitive(12));
            fieldAgg.agg(new JsonPrimitive(12));

            Assert.assertEquals("a", fieldAgg.getFieldName());

            Assert.assertEquals(4, fieldAgg.count());
            Assert.assertEquals(3, fieldAgg.cardinality());
            Assert.assertEquals(0, fieldAgg.getStringCount());
            Assert.assertEquals(0, fieldAgg.getBooleanCount());
            Assert.assertEquals(4, fieldAgg.getNumberCount());
            Assert.assertEquals(10, fieldAgg.getMin().intValue());
            Assert.assertEquals(12, fieldAgg.getMax().intValue());
            Assert.assertEquals(45, fieldAgg.getSum().intValue());

            Assert.assertEquals(4, fieldAgg.count());
            Assert.assertEquals(1, fieldAgg.count("10"));
            Assert.assertEquals(1, fieldAgg.count("11"));
            Assert.assertEquals(2, fieldAgg.count("12"));
            Assert.assertEquals(0, fieldAgg.count("13"));
        }
    }

    @Test
    public void testLongValues() {
        for (IFieldAgg fieldAgg : getFieldImplList()) {
            fieldAgg.agg(new JsonPrimitive(10L));
            fieldAgg.agg(new JsonPrimitive(11L));
            fieldAgg.agg(new JsonPrimitive(12L));
            fieldAgg.agg(new JsonPrimitive(12L));

            Assert.assertEquals("a", fieldAgg.getFieldName());

            Assert.assertEquals(4, fieldAgg.count());
            Assert.assertEquals(3, fieldAgg.cardinality());
            Assert.assertEquals(0, fieldAgg.getStringCount());
            Assert.assertEquals(0, fieldAgg.getBooleanCount());
            Assert.assertEquals(4, fieldAgg.getNumberCount());
            Assert.assertEquals(10, fieldAgg.getMin().intValue());
            Assert.assertEquals(12, fieldAgg.getMax().intValue());
            Assert.assertEquals(45, fieldAgg.getSum().intValue());

            Assert.assertEquals(4, fieldAgg.count());
            Assert.assertEquals(1, fieldAgg.count("10"));
            Assert.assertEquals(1, fieldAgg.count("11"));
            Assert.assertEquals(2, fieldAgg.count("12"));
            Assert.assertEquals(0, fieldAgg.count("13"));
        }
    }

    @Test
    public void testStringValues() {
        for (IFieldAgg fieldAgg : getFieldImplList()) {
            fieldAgg.agg(new JsonPrimitive("foo"));
            fieldAgg.agg(new JsonPrimitive("foo"));
            fieldAgg.agg(new JsonPrimitive("boo"));
            fieldAgg.agg(new JsonPrimitive("call"));

            Assert.assertEquals("a", fieldAgg.getFieldName());

            Assert.assertEquals(4, fieldAgg.count());
            Assert.assertEquals(3, fieldAgg.cardinality());
            Assert.assertEquals(4, fieldAgg.getStringCount());
            Assert.assertEquals(0, fieldAgg.getBooleanCount());
            Assert.assertEquals(0, fieldAgg.getNumberCount());
            Assert.assertNull(fieldAgg.getMin());
            Assert.assertNull(fieldAgg.getMax());
            Assert.assertNull(fieldAgg.getSum());

            Assert.assertEquals(4, fieldAgg.count());
            Assert.assertEquals(2, fieldAgg.count("foo"));
            Assert.assertEquals(1, fieldAgg.count("boo"));
            Assert.assertEquals(1, fieldAgg.count("call"));
            Assert.assertEquals(0, fieldAgg.count("other"));
        }
    }

    @Test
    public void testBooleanValues() {
        for (IFieldAgg fieldAgg : getFieldImplList()) {
            fieldAgg.agg(new JsonPrimitive(true));
            fieldAgg.agg(new JsonPrimitive(true));
            fieldAgg.agg(new JsonPrimitive(true));
            fieldAgg.agg(new JsonPrimitive(false));

            Assert.assertEquals("a", fieldAgg.getFieldName());

            Assert.assertEquals(4, fieldAgg.count());
            Assert.assertEquals(2, fieldAgg.cardinality());
            Assert.assertEquals(0, fieldAgg.getStringCount());
            Assert.assertEquals(4, fieldAgg.getBooleanCount());
            Assert.assertEquals(0, fieldAgg.getNumberCount());
            Assert.assertNull(fieldAgg.getMin());
            Assert.assertNull(fieldAgg.getMax());
            Assert.assertNull(fieldAgg.getSum());

            Assert.assertEquals(4, fieldAgg.count());
            Assert.assertEquals(3, fieldAgg.count("true"));
            Assert.assertEquals(1, fieldAgg.count("false"));
            Assert.assertEquals(0, fieldAgg.count("other"));
        }
    }

    @Test
    public void testMixedValues() {
        for (IFieldAgg fieldAgg : getFieldImplList()) {
            fieldAgg.agg(new JsonPrimitive("foo"));
            fieldAgg.agg(new JsonPrimitive("true"));
            fieldAgg.agg(new JsonPrimitive("1.2"));
            fieldAgg.agg(new JsonPrimitive("10"));
            fieldAgg.agg(new JsonPrimitive("20"));
            fieldAgg.agg(new JsonPrimitive(1.2));
            fieldAgg.agg(new JsonPrimitive(10));
            fieldAgg.agg(new JsonPrimitive(15L));

            Assert.assertEquals("a", fieldAgg.getFieldName());

            Assert.assertEquals(8, fieldAgg.count());
            Assert.assertEquals(6, fieldAgg.cardinality());
            Assert.assertEquals(5, fieldAgg.getStringCount());
            Assert.assertEquals(0, fieldAgg.getBooleanCount());
            Assert.assertEquals(3, fieldAgg.getNumberCount());
            Assert.assertEquals(1.2, fieldAgg.getMin().doubleValue(), 10E-10);
            Assert.assertEquals(15, fieldAgg.getMax().longValue());
            Assert.assertEquals(26.2, fieldAgg.getSum().doubleValue(), 10E-10);

            Assert.assertEquals(8, fieldAgg.count());
            Assert.assertEquals(1, fieldAgg.count("foo"));
            Assert.assertEquals(1, fieldAgg.count("true"));
            Assert.assertEquals(2, fieldAgg.count("1.2"));
            Assert.assertEquals(2, fieldAgg.count("10"));
            Assert.assertEquals(1, fieldAgg.count("20"));
            Assert.assertEquals(1, fieldAgg.count("15"));
            Assert.assertEquals(0, fieldAgg.count("13"));
        }
    }

    private List<IFieldAgg> getFieldImplList() {
        List<IFieldAgg> list = new ArrayList<>();
        list.add(new InMemFieldAgg("a"));

        try {
            JedisPool pool = new JedisPool(new JedisPoolConfig(), "localhost");
            Jedis jedis = pool.getResource();
            list.add(new RedisFieldAgg("a", jedis));
            System.out.println("WARNING: using redis at localhost for unit tests!");
        } catch (JedisConnectionException e) {
            System.out.println("WARNING: executing unit tests without redis!");
        }
        return list;
    }
}
