package org.crucial.executor;

public class Config {
    public static final String CONFIG_FILE = "config.properties";

    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_RED = "\u001B[31m";

    public static final String DEBUG = "debug";
    public static final String DEBUG_DEFAULT = "false";

    // aws

    public static final String AWS_LAMBDA_CLIENT_TIMEOUT = "aws.client.timeout";
    public static final String AWS_LAMBDA_CLIENT_TIMEOUT_DEFAULT = "30";

    public static final String AWS_LAMBDA_FUNCTION_ASYNC = "async";
    public static final String AWS_LAMBDA_FUNCTION_ASYNC_DEFAULT = "false";

    public static final String AWS_LAMBDA_REGION = "aws.region";
    public static final String AWS_LAMBDA_REGION_DEFAULT = "AWS_REGION";

    public static final String AWS_LAMBDA_FUNCTION_NAME = "aws.lambda.function.name";
    public static final String AWS_LAMBDA_FUNCTION_NAME_DEFAULT = "serverless-shell";

    public static final String AWS_LAMBDA_FUNCTION_ARN = "aws.lambda.function.arn";
    public static final String AWS_LAMBDA_FUNCTION_ARN_DEFAULT = "arn:aws:lambda:AWS_REGION:ID:function:NAME";

    public static final String AWS_LAMBDA_LOGGING = "logging";
    public static final String AWS_LAMBDA_LOGGING_DEFAULT = "false";

    // k8s

    public static final String K8S_IMAGE = "k8s.image";
    public static final String K8S_IMAGE_DEFAULT = "0track/executor:latest";

    public static final String K8S_JOB_NAME = "k8s.job.name";
    public static final String K8S_JOB_NAME_DEFAULT = "job";

    public static final String K8S_TOKEN = "k8s.token";
    public static final String K8S_TOKEN_DEFAULT = "";

}