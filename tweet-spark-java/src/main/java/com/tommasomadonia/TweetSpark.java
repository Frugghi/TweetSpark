/** Created by Tommaso Madonia */
package com.tommasomadonia;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.spark.sql.DataFrame;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SQLContext;
import scala.Tuple2;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.FlatMapFunction;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.api.java.function.PairFunction;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

public class TweetSpark {

    private static final String appName = "com.tommasomadonia.TweetSpark";
    private static final Pattern WORDS = Pattern.compile("[^\\w#/:\\.]+");

    public static void main(String[] args) {

        if (args.length < 1) {
            System.err.println("Usage: " + appName + " <file>");
            System.exit(1);
        }

        Configuration hadoopConfiguration = new Configuration();
        Path path = new Path("hdfs://" + args[0]);
        FileSystem fileSystem;
        try {
            fileSystem = FileSystem.get(hadoopConfiguration);
            if (!fileSystem.exists(path)) {
                System.err.println("Path '" + args[0] + "' does not exists");
                System.exit(1);
            }
        } catch (IOException e) {
            System.err.println("Path '" + args[0] + "' does not exists");
            e.printStackTrace();
            System.exit(1);
        }

        SparkConf sparkConfiguration = new SparkConf().setAppName(appName);
        JavaSparkContext context = new JavaSparkContext(sparkConfiguration);
        SQLContext sqlContext = new SQLContext(context);

        DataFrame tweets = sqlContext.read().json(path.toString());
        tweets.registerTempTable("tweets");

        JavaRDD<String> words = sqlContext.sql("SELECT text FROM tweets").toJavaRDD().flatMap(new FlatMapFunction<Row, String>() {
            @Override
            public Iterable<String> call(Row row) {
                String text = row.getString(0);
                return Arrays.asList(WORDS.split(text));
            }
        });

        JavaPairRDD<String, Integer> ones = words.mapToPair(new PairFunction<String, String, Integer>() {
            @Override
            public Tuple2<String, Integer> call(String s) {
                return new Tuple2<>(s, 1);
            }
        });

        JavaPairRDD<String, Integer> counts = ones.reduceByKey(new Function2<Integer, Integer, Integer>() {
            @Override
            public Integer call(Integer i1, Integer i2) {
                return i1 + i2;
            }
        });

        List<Tuple2<String, Integer>> output = counts.collect();
        for (Tuple2<?,?> tuple : output) {
            System.out.println(tuple._1() + ": " + tuple._2());
        }

        System.out.println("Count Tweets: " + sqlContext.sql("SELECT COUNT(*) FROM tweets").collect()[0].getLong(0));

        context.stop();
    }
}
