package skatepark.heelflip.inmem;

import com.google.gson.JsonPrimitive;

import org.junit.Assert;
import org.junit.Test;

import java.util.Set;

public class InMemGroupByAggTest {

    @Test
    public void testGroupByValues() {
        InMemGroupByAgg groupByAgg = new InMemGroupByAgg("a", "b");
        groupByAgg.agg(new JsonPrimitive(10), new JsonPrimitive(true));
        groupByAgg.agg(new JsonPrimitive(20), new JsonPrimitive(true));
        groupByAgg.agg(new JsonPrimitive(-1), new JsonPrimitive(false));
        groupByAgg.agg(new JsonPrimitive(-1), new JsonPrimitive(false));

        Assert.assertEquals("a", groupByAgg.getFieldName());
        Assert.assertEquals("b", groupByAgg.getGroupBy());

        Set<String> groupByValues = groupByAgg.groupByValues();
        Assert.assertEquals(2, groupByValues.size());
        Assert.assertTrue(groupByValues.contains("true"));
        Assert.assertTrue(groupByValues.contains("false"));
    }

    @Test
    public void testValues() {
        InMemGroupByAgg groupByAgg = new InMemGroupByAgg("a", "b");
        groupByAgg.agg(new JsonPrimitive(10), new JsonPrimitive(true));
        groupByAgg.agg(new JsonPrimitive(20), new JsonPrimitive(true));
        groupByAgg.agg(new JsonPrimitive(-1), new JsonPrimitive(false));
        groupByAgg.agg(new JsonPrimitive(-1), new JsonPrimitive(false));

        Assert.assertEquals("a", groupByAgg.getFieldName());
        Assert.assertEquals("b", groupByAgg.getGroupBy());

        Set<String> values = groupByAgg.values();
        Assert.assertEquals(3, values.size());
        Assert.assertTrue(values.contains("10"));
        Assert.assertTrue(values.contains("20"));
        Assert.assertTrue(values.contains("-1"));
    }

    @Test
    public void testGroupBy() {
        InMemGroupByAgg groupByAgg = new InMemGroupByAgg("a", "b");
        groupByAgg.agg(new JsonPrimitive(10), new JsonPrimitive(true));
        groupByAgg.agg(new JsonPrimitive(20), new JsonPrimitive(true));
        groupByAgg.agg(new JsonPrimitive(-1), new JsonPrimitive(false));
        groupByAgg.agg(new JsonPrimitive(-1), new JsonPrimitive(false));

        Assert.assertEquals("a", groupByAgg.getFieldName());
        Assert.assertEquals("b", groupByAgg.getGroupBy());
        
        InMemFieldAgg fieldAgg = groupByAgg.groupBy("true");
        Set<String> distinctValues = fieldAgg.distinctValues();
        Assert.assertEquals(2, distinctValues.size());
        Assert.assertTrue(distinctValues.contains("10"));
        Assert.assertTrue(distinctValues.contains("20"));

        Assert.assertEquals(2, fieldAgg.cardinality());
        Assert.assertEquals(2, fieldAgg.count());
        Assert.assertEquals(2, fieldAgg.getNumberCount());
        Assert.assertEquals(10, fieldAgg.getMin().intValue());
        Assert.assertEquals(20, fieldAgg.getMax().intValue());
        Assert.assertEquals(30, fieldAgg.getSum().intValue());

        fieldAgg = groupByAgg.groupBy("false");
        distinctValues = fieldAgg.distinctValues();
        Assert.assertEquals(1, distinctValues.size());
        Assert.assertTrue(distinctValues.contains("-1"));

        Assert.assertEquals(1, fieldAgg.cardinality());
        Assert.assertEquals(2, fieldAgg.count());
        Assert.assertEquals(2, fieldAgg.getNumberCount());
        Assert.assertEquals(-1, fieldAgg.getMin().intValue());
        Assert.assertEquals(-1, fieldAgg.getMax().intValue());
        Assert.assertEquals(-2, fieldAgg.getSum().intValue());
    }
}