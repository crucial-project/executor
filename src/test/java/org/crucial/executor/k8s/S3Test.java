/*
package org.crucial.executor.k8s;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import org.crucial.executor.ServerlessExecutorService;
import org.testng.annotations.Test;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import org.apache.commons.io.FileUtils;



public class S3Test {
    @Test
    public void testSubmitS3() throws ExecutionException, InterruptedException {


        final String ret = "test";
        String FILE_NAME = "output.csv";
        String bucketName = "sshell-bucket";


        long start = System.currentTimeMillis();

        //     job-a
        //     cmd: curl url

        ServerlessExecutorService esK8s1 = new KubernetesExecutorService("job-b", "tmsquare/executor-image");
        esK8s1.setLocal(false);
        Future<String> future1 = esK8s1.submit((Serializable & Callable<String>) () -> {

            String FILE_URL = "https://perso.telecom-paristech.fr/eagan/class/igr204/data/nat1900-2017.tsv";

            try (BufferedInputStream in = new BufferedInputStream(new URL(FILE_URL).openStream());
                 FileOutputStream fileOutputStream = new FileOutputStream(FILE_NAME)) {
                byte dataBuffer[] = new byte[1024];
                int bytesRead;
                while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
                    fileOutputStream.write(dataBuffer, 0, bytesRead);
                }

                AWSCredentials credentials = new BasicAWSCredentials(
                        "AKIAUSCEEJS5EVJHJ2PL",
                        "hLMR5aXJ/MhSkC1fwLX90FhJ0GLx60sBEh/mXxMf"
                );

                AmazonS3 s3client = AmazonS3ClientBuilder
                        .standard()
                        .withCredentials(new AWSStaticCredentialsProvider(credentials))
                        .withRegion(Regions.US_EAST_1)
                        .build();

                // PUT object to S3
                s3client.putObject(
                        bucketName,
                        "Document/" + FILE_NAME,
                        new File("./"+FILE_NAME)
                );
                System.out.println("Operation PUT successful");

            } catch (IOException | AmazonServiceException e) {
                System.out.println("Error");
            }

            return ret;
        });



        //     job-b
        //     cmd: grep perl

        ServerlessExecutorService esK8s2 = new KubernetesExecutorService("job-a", "tmsquare/executor-image");
        esK8s2.setLocal(false);
        Future<String> future2 = esK8s2.submit((Serializable & Callable<String>) () -> {

            try {

                AWSCredentials credentials = new BasicAWSCredentials(
                        "AKIAUSCEEJS5EVJHJ2PL",
                        "hLMR5aXJ/MhSkC1fwLX90FhJ0GLx60sBEh/mXxMf"
                );

                AmazonS3 s3client = AmazonS3ClientBuilder
                        .standard()
                        .withCredentials(new AWSStaticCredentialsProvider(credentials))
                        .withRegion(Regions.US_EAST_1)
                        .build();


                List<String> data = new ArrayList<>();
                String grep = "Test";

                // GET object from S3
                S3Object s3object = null;
                while (s3object == null) {
                    try {
                        s3object = s3client.getObject(bucketName, "Document/" + FILE_NAME);
                    } catch (AmazonS3Exception e) {
                        System.out.println("Waiting for file...");
                    }
                }

                S3ObjectInputStream inputStream = s3object.getObjectContent();
                FileUtils.copyInputStreamToFile(inputStream, new File(FILE_NAME));

                try(BufferedReader br = new BufferedReader(new FileReader(FILE_NAME))) {
                    for(String line; (line = br.readLine()) != null; ) {
                        boolean val = line.contains(grep);
                        if (val) data.add(line);
                    }
                }
                System.out.println(data.toString());
            } catch (IOException | AmazonServiceException e) {
                System.out.println("Error");
            }

            return ret;
        });

        System.out.println(future2.get());
        System.out.println(future1.get());

        long end = System.currentTimeMillis();
        long elapsedTime = end - start;

        System.out.println("Elapsed time: " + elapsedTime/1000.0);

        esK8s2.deleteAllJobs();

    }
}
*/