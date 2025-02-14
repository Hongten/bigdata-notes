/**
 * @Author : wzdnzd
 * @Time : 2019-06-08
 * @Project : bigdata
 */


package com.wzdnzd.hadoop;

import com.sun.istack.NotNull;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IOUtils;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.log4j.Logger;

import java.io.*;

public class Statistics {
    private static Logger logger = Logger.getLogger(Statistics.class);

    public static void main(String[] args) throws IOException, ClassNotFoundException, InterruptedException {
        Configuration conf = new Configuration();

        String path = "/learn/data/temperature";
        Path inputPath = new Path(path);
        Path outputPath = new Path("/learn/result/temperature");

        if (outputPath.getFileSystem(conf).exists(outputPath))
            outputPath.getFileSystem(conf).delete(outputPath, true);

        String dataPath = Statistics.class.getResource("/").getPath() + "data";
        File dir = new File(dataPath);

        if (dir.exists() && dir.isDirectory()) {
            File[] files = dir.listFiles();

            assert files != null && files.length > 0;

            for (File file : files)
                fileUpload(conf, file, new Path(path + "/" + file.getName()));
        }

        conf.set("mapreduce.app-submission.cross-platform", "true");

        Job job = Job.getInstance(conf, "TemperatureStatics");
        job.setJarByClass(Statistics.class);

        // must be set before running
        String jarPath = "";
        job.setJar(jarPath);

        job.setMapperClass(TemperatureMapper.class);
        job.setMapOutputKeyClass(Temperature.class);
        job.setMapOutputValueClass(IntWritable.class);
        job.setPartitionerClass(TemperaturePartitioner.class);
        job.setSortComparatorClass(TemperatureSort.class);
        job.setGroupingComparatorClass(TemperatureGroup.class);
        job.setReducerClass(TemperatureReduce.class);

        FileInputFormat.addInputPath(job, inputPath);
        FileOutputFormat.setOutputPath(job, outputPath);

        System.exit(job.waitForCompletion(true) ? 0 : 1);
    }

    private static void fileUpload(@NotNull Configuration conf,
                                   @NotNull File src, @NotNull Path target) throws IOException {
        if (!src.exists() || target.getFileSystem(conf).exists(target)) {
            logger.info("+++++ file " + src.getName() + " upload failed. +++++");
            return;
        }

        FSDataOutputStream outputStream = target.getFileSystem(conf).create(target);
        InputStream inputStream = new BufferedInputStream(new FileInputStream(src));

        IOUtils.copyBytes(inputStream, outputStream, 1024, true);
    }
}
