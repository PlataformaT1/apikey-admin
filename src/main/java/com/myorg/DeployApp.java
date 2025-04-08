package com.myorg;

import software.amazon.awscdk.App;
import software.amazon.awscdk.Environment;
import software.amazon.awscdk.StackProps;

import java.util.Arrays;
import java.util.Map;
import java.util.HashMap;

public class DeployApp {
        public static void main(final String[] args) {
                App app = new App();

                // Obtener variables de entorno
                String environment = getEnvOrDefault("ENVIRONMENT", "development");
                String awsAccount = getEnvOrDefault("AWS_ACCOUNT_ID", "");
                String awsRegion = getEnvOrDefault("AWS_REGION", "us-east-1");

                // Crear un mapa con todas las variables de configuración
                Map<String, String> contextMap = new HashMap<>();
                contextMap.put("environment", environment);
                contextMap.put("mongodbConnectionString", getEnvOrDefault("MONGODB_CONNECTION_STRING", ""));
                contextMap.put("vpcId", getEnvOrDefault("VPC_ID", ""));
                contextMap.put("subnetIds", getEnvOrDefault("SUBNET_IDS", ""));
                contextMap.put("securityGroupIds", getEnvOrDefault("SECURITY_GROUP_IDS", ""));
                contextMap.put("awsRegion", awsRegion);
                contextMap.put("stageName", getEnvOrDefault("STAGE_NAME", "dev"));
                contextMap.put("lambdaFunctionName", getEnvOrDefault("LAMBDA_FUNCTION_NAME", "apikey-admin-" + environment));

                // Configurar el entorno AWS
                Environment awsEnvironment = Environment.builder()
                        .account(awsAccount.isEmpty() ? null : awsAccount)
                        .region(awsRegion)
                        .build();

                // Propiedades del stack
                StackProps stackProps = StackProps.builder()
                        .env(awsEnvironment)
                        .build();

                // Crear el stack principal
                //String stackName = "ApiKeyAdmin" + (environment.equals("production") ? "Prod" : "Dev");
                String stackName = "ApiKeyDeployStack";
                new DeployStack(app, stackName, stackProps, contextMap);

                app.synth();
        }
        /**
         * Obtiene una variable de entorno o devuelve un valor predeterminado si no existe
         */
        private static String getEnvOrDefault(String key, String defaultValue) {
                String value = System.getenv(key);
                if (value == null || value.isEmpty()) {
                        System.out.println("Variable de entorno " + key + " no encontrada, usando valor predeterminado: " + defaultValue);
                        return defaultValue;
                }
                // No imprimir valores confidenciales como cadenas de conexión
                if (key.contains("CONNECTION_STRING") || key.contains("PASSWORD") || key.contains("SECRET")) {
                        System.out.println("Variable de entorno " + key + " encontrada [valor oculto]");
                } else {
                        System.out.println("Variable de entorno " + key + " encontrada: " + value);
                }
                return value;
        }
}
