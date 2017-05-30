package com.skatepark.heelflip;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

import com.skatepark.heelflip.util.Extractor;
import com.skatepark.heelflip.util.JsonDumper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class JsonAgg {

    private Map<String, FieldAgg> fieldAggMap;

    private Map<String, Map<String, GroupByAgg>> groupByAggMap;

    public JsonAgg() {
        this.fieldAggMap = new HashMap<>();
        this.groupByAggMap = new HashMap<>();
    }

    public void add(JsonObject json) {
        Objects.requireNonNull(json, "json should not be null.");
        aggregate(Extractor.extract(json));
    }

    public int numberOfFieldAgg() {
        return fieldAggMap.size();
    }

    public int numberOfGroupByAgg() {
        return groupByAggMap.values().stream()
                .mapToInt(m -> m.size())
                .sum();
    }

    public Set<String> fieldNames() {
        return fieldAggMap.keySet();
    }

    public boolean hasFieldAgg(String fieldName) {
        return fieldName != null && fieldAggMap.containsKey(fieldName);
    }

    public FieldAgg getFieldAgg(String fieldName) {
        return !hasFieldAgg(fieldName) ? null : fieldAggMap.get(fieldName);
    }

    public boolean hasGroupBy(String fieldName) {
        return fieldName != null && groupByAggMap.containsKey(fieldName);
    }

    public GroupByAgg getGroupBy(String fieldName, String groupBy) {
        return !hasGroupBy(fieldName) ? null : groupByAggMap.get(fieldName).get(groupBy);
    }

    /**
     * Load {@link InputStream} with JSON newline delimited format.
     *
     * @param stream stream.
     * @throws IOException if IO errors occurs.
     */
    public void loadNDJSON(InputStream stream) throws IOException {
        Objects.requireNonNull(stream, "stream should not be null.");

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream))) {
            JsonParser parser = new JsonParser();

            reader.lines()
                    .map(line -> parser.parse(line))
                    .map(elem -> elem.getAsJsonObject())
                    .forEach(this::add);
        }
    }

    /**
     * Load {@link InputStream} with JSON array where each element is the data itself.
     *
     * @param stream stream.
     * @throws IOException if IO errors occurs.
     */
    public void loadJSONArray(InputStream stream) throws IOException {
        Objects.requireNonNull(stream, "stream should not be null.");

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream))) {
            JsonElement result = new JsonParser().parse(reader);
            if (!result.isJsonArray()) {
                throw new IllegalArgumentException("result is not a JSON array.");
            }
            for (JsonElement elem : result.getAsJsonArray()) {
                add(elem.getAsJsonObject());
            }
        }
    }

    /**
     * Dump field aggregations in a single JSON file.
     *
     * @param filePath      file path.
     * @param includeValues flag used to include values (the result JSON can increase
     *                      dramatically).
     * @throws IOException if IO errors occurs.
     */
    public void dumpFieldAgg(String filePath, boolean includeValues) throws IOException {
        JsonDumper.dumpFieldAgg(this, filePath, includeValues);
    }

    /**
     * Dump group by aggregations in a single JSON file.
     *
     * @param filePath      file path.
     * @param includeValues flag used to include values (the result JSON can increase
     *                      dramatically).
     * @throws IOException if IO errors occurs.
     */
    public void dumpGroupByAgg(String filePath, boolean includeValues) throws IOException {
        JsonDumper.dumpGroupByAgg(this, filePath, includeValues);
    }

    private void aggregate(Map<String, List<JsonPrimitive>> valueMap) {
        for (Map.Entry<String, List<JsonPrimitive>> entry : valueMap.entrySet()) {
            String fieldName = entry.getKey();
            List<JsonPrimitive> valueList = entry.getValue();
            FieldAgg fieldAgg = fieldAggMap.computeIfAbsent(fieldName, key -> new FieldAgg(key));

            valueList.stream().forEach(fieldAgg::agg);
        }

        for (String fieldName : valueMap.keySet()) {
            for (String groupBy : valueMap.keySet()) {
                if (fieldName.equals(groupBy)) {
                    continue;
                }

                Map<String, GroupByAgg> map = groupByAggMap.computeIfAbsent(fieldName, key -> new HashMap<>());
                GroupByAgg groupByAgg = map.computeIfAbsent(groupBy, key -> new GroupByAgg(fieldName, groupBy));

                for (JsonPrimitive fieldValue : valueMap.get(fieldName)) {
                    for (JsonPrimitive groupByValue : valueMap.get(groupBy)) {
                        groupByAgg.agg(fieldValue, groupByValue);
                    }
                }
            }
        }
    }
}