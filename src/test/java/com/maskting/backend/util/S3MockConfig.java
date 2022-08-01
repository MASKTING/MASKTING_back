package com.maskting.backend.util;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.AnonymousAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import io.findify.s3mock.S3Mock;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class S3MockConfig {
    @Value("${cloud.aws.s3.bucket}")
    public String bucket;

    @Value("${cloud.aws.region.static}")
    private String region;

    @Bean
    public S3Mock s3Mock() {
        return new S3Mock.Builder().withPort(8001).withInMemoryBackend().build();
    }

    @Bean
    public AmazonS3Client amazonS3Client(S3Mock s3Mock){
        s3Mock.start();
        AwsClientBuilder.EndpointConfiguration endpoint = new AwsClientBuilder.EndpointConfiguration("http://localhost:8001", region);
        AmazonS3ClientBuilder standard = AmazonS3ClientBuilder
                .standard();
        standard.withPathStyleAccessEnabled(true);
        standard.withEndpointConfiguration(endpoint);
        standard.withCredentials(new AWSStaticCredentialsProvider(new AnonymousAWSCredentials()));
        AmazonS3 client = standard
                .build();
        client.createBucket(bucket);

        return (AmazonS3Client) client;
    }
}
