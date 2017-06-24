[![Build Status](https://travis-ci.org/greatjapa/heelflip.svg?branch=master)](https://travis-ci.org/greatjapa/heelflip)
[![license](https://img.shields.io/github/license/mashape/apistatus.svg?maxAge=2592000)](https://github.com/greatjapa/heelflip/blob/master/LICENCE)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.greatjapa/heelflip/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.greatjapa/heelflip)
[![codecov](https://codecov.io/gh/greatjapa/heelflip/branch/master/graph/badge.svg)](https://codecov.io/gh/greatjapa/heelflip)

**Heelflip** is a JSON aggregator library for Java. It's well-known that aggregation processes are useful but very expensive to calculate. Instead of calculating aggregations over JSON files into a relational database or even NoSQL database, we provide a library that performs this task for you. Heelflip works **in-memory** or using **Redis** to aggregate values.

## Maven
Heelflip is available at the Central Maven Repository:

```xml
<dependency>
    <groupId>com.github.greatjapa</groupId>
    <artifactId>heelflip</artifactId>
    <version>1.4</version>
</dependency>
```

## How to use
Consider the following bookstore JSON sample:
```javascript
{"name":"The Odyssey",  "author":"Homer",          "genre":"poem",  "inStock":true, "price":12.50}
{"name":"The Godfather","author":"Mario Puzo",     "genre":"novel", "inStock":true, "price":6.49 }
{"name":"Moby-Dick",    "author":"Herman Melville","genre":"novel", "inStock":false,"price":3.07 }
{"name":"Emma",         "author":"Austen",         "genre":"novel", "inStock":true, "price":30.50}
```
We can read it as follows:
```java
try(InputStream stream = new FileInputStream("bookstore.json")){
    JsonAgg jsonAgg = new JsonAgg();
    jsonAgg.loadNDJSON(zipStream);
    
    ...
}
```
### Global Aggregations

Once you have a `JsonAgg` object you can get global aggregations (min, max and sum) as follows:
```java
IFieldAgg priceAgg = jsonAgg.getFieldAgg("price");
popAgg.getMin(); // 3.07
popAgg.getMax(); // 30.50
popAgg.getSum(); // 52.56
```
Or counting their values (count and cardinality):
```java
IFieldAgg genreAgg = jsonAgg.getFieldAgg("genre");
genreAgg.count();          // 4
genreAgg.cardinality();    // 2
genreAgg.count("novel");   // 3
genreAgg.count("poem");    // 1
```
### Group By Aggregations

We can also get group aggregations as follows:
```java
IGroupByAgg groupByAgg = jsonAgg.getGroupBy("price", "inStock");
IFieldAgg priceBystockAgg = groupByAgg.groupBy("true");
priceBystockAgg.getMin(); // 6.49
priceBystockAgg.getMax(); // 30.50
priceBystockAgg.getSum(); // 49.49
```
or
```java
IGroupByAgg groupByAgg = jsonAgg.getGroupBy("name", "inStock");
IFieldAgg namesInStockAgg = groupByAgg.groupBy("true");

namesInStockAgg.distinctValues(); 
//"The Odyssey"
//"The Godfather"
//"Emma"
```
### Dump Report
You can also generate a report with all accumulated aggregations. You just need to do:
```java
jsonAgg.dumpReport("report", true);
```
This code snippet will create a directory with the following structure:
```
report
│   __missingGroupBy.json
└───name
│   │   __name.json
│   │   name_groupBy_author.json
│   │   name_groupBy_genre.json
│   │   name_groupBy_inStock.json
│   │   name_groupBy_price.json
└───author
└───genre
└───inStock
└───price
```
Each JSON field has its own directory, for instance, `name`, `author` etc. This directory has the following items:
1. `__<field_name>.json` file with field global aggregations;
2. All combinations of group by aggregations separated in different files in this format `<field_name>_groupBy_<field_name>.json`.

Here is an example of dump result for the field author (file `__author.json`):
```javascript
{
  "fieldName": "author",
  "count": 4,
  "cardinality": 4,
  "string": 4,
  "boolean": 0,
  "number": 0,
  "values": [
    "Homer",
    "Mario Puzo",
    "Herman Melville",
    "Austen"
  ]
}
```
The group by files have the following structure (file `author_groupBy_genre.json`):
```javascript
{
  "groupBy": "genre",
  "fieldName": "author",
  "values": [
    {
      "poem": {
        "fieldName": "author",
        "count": 1,
        "cardinality": 1,
        "string": 1,
        "boolean": 0,
        "number": 0,
        "values": [
          "Homer"
        ]
      }
    },
    {
      "novel": {
        "fieldName": "author",
        "count": 3,
        "cardinality": 3,
        "string": 3,
        "boolean": 0,
        "number": 0,
        "values": [
          "Mario Puzo",
          "Herman Melville",
          "Austen"
        ]
      }
    }
  ]
}
```

Finally, the root directory contains a file `__missingGroupBy.json` with a list of missing group by combination.

### How to scale?
Creating `JsonAgg` with the default constructor means that you will aggregate JSON files in-memory. If you try to load several JSONs, the JVM may raise an `OutOfMemoryError`. To avoid this kind of error, we provide a Redis based alternative to the in-memory based process aggregations. See code below:

```java
JedisPool pool = new JedisPool(new JedisPoolConfig(), "localhost");
try (Jedis jedis = pool.getResource()) {
    JsonAgg jsonAgg = new JsonAgg(jedis);
    ...
}
```

### How aggregation works over non-flat JSON
There is no problem if you have JSONs with arrays or nested objects. What Heelflip does is calculate aggregations over fields and their values. However, Heelflip API is based on field names so we need to rename JSON fields when arrays or objects appear.

For instance, the following JSON entry:
```javascript
{"name":"Steve", "age":30, "address":{"street":"8nd Street", "city":"New York"}}
```
will be read as:
```javascript
{"name":"Steve", "age":30, "address.street":"8nd Street", "address.city":"New York"}}
```
To retrieve the information about the field "city" we need to concatenate the field names as shown below:
```java
IFieldAgg cityAgg = agg.getFieldAgg("address.city");
cityAgg.count();
```

Similarly, the following JSON entry with arrays:
```javascript
{ "city" : "SPRINGFIELD", "loc" : [ -72.577769, 42.128848 ], "pop" : 22115}
```
will be read as:
```javascript
{ "city" : "SPRINGFIELD", "loc_0" : -72.577769, "loc_1": 42.128848, "pop" : 22115}
```

**NOTE**: The examples above are only used to ilustrate how to rename field names for objects and arrays in order to generate unique names at aggregation time. It is important to understand that we do not flatten the JSON.
