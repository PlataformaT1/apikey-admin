package com.myorg;

import software.amazon.awscdk.Duration;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.CfnOutput;
import software.amazon.awscdk.services.lambda.*;
import software.amazon.awscdk.services.lambda.Runtime;
import software.amazon.awscdk.services.apigateway.*;
import software.amazon.awscdk.services.iam.*;
import software.amazon.awscdk.services.ec2.*;
import software.amazon.awscdk.services.logs.*;
import software.constructs.Construct;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;


public class DeployStack extends Stack {

	public DeployStack(final Construct scope, final String id, final StackProps props, final Map<String, String> context) {
		super(scope, id, props);

		// Obtener valores del contexto
		String environment = context.getOrDefault("environment", "development");
		String mongodbConnectionString = context.getOrDefault("mongodbConnectionString", "");
		String vpcId = context.getOrDefault("vpcId", "");
		String subnetIdsString = context.getOrDefault("subnetIds", "");
		String securityGroupIdsString = context.getOrDefault("securityGroupIds", "");
		String awsRegion = context.getOrDefault("awsRegion", "us-east-1");
		String stageName = context.getOrDefault("stageName", "dev");
		String lambdaFunctionName = context.getOrDefault("lambdaFunctionName", "apikey-admin-" + environment);

		System.out.println("Desplegando stack para entorno: " + environment);
		System.out.println("Nombre de función Lambda: " + lambdaFunctionName);

		// Variables para configuración de VPC
		IVpc vpc = null;
		List<ISubnet> subnets = new ArrayList<>();
		List<ISecurityGroup> securityGroups = new ArrayList<>();

		// Configurar VPC si se proporciona un ID
		if (vpcId != null && !vpcId.isEmpty()) {
			System.out.println("Configurando VPC: " + vpcId);
			try {
				vpc = Vpc.fromLookup(this, "ImportedVpc", VpcLookupOptions.builder()
						.vpcId(vpcId)
						.build());

				// Configurar subredes si se proporcionan IDs
				if (subnetIdsString != null && !subnetIdsString.isEmpty()) {
					String[] subnetIds = subnetIdsString.split(",");
					for (int i = 0; i < subnetIds.length; i++) {
						String subnetId = subnetIds[i].trim();
						System.out.println("Configurando Subnet: " + subnetId);
						subnets.add(Subnet.fromSubnetId(this, "Subnet" + i, subnetId));
					}
				}

				// Configurar grupos de seguridad si se proporcionan IDs
				if (securityGroupIdsString != null && !securityGroupIdsString.isEmpty()) {
					String[] sgIds = securityGroupIdsString.split(",");
					for (int i = 0; i < sgIds.length; i++) {
						String sgId = sgIds[i].trim();
						System.out.println("Configurando Security Group: " + sgId);
						securityGroups.add(SecurityGroup.fromSecurityGroupId(this, "SG" + i, sgId));
					}
				}
			} catch (Exception e) {
				System.err.println("Error al configurar VPC: " + e.getMessage());
				System.err.println("Se continuará sin configuración de VPC");
			}
		}

		// Crear rol para la función Lambda
		Role lambdaRole = Role.Builder.create(this, "ApiKeyAdminRole")
				.assumedBy(new ServicePrincipal("lambda.amazonaws.com"))
				.managedPolicies(Arrays.asList(
						ManagedPolicy.fromAwsManagedPolicyName("service-role/AWSLambdaBasicExecutionRole"),
						ManagedPolicy.fromAwsManagedPolicyName("service-role/AWSLambdaVPCAccessExecutionRole")
				))
				.build();

		// Definir variables de entorno para la función Lambda
		Map<String, String> environmentVars = new HashMap<>();
		environmentVars.put("QUARKUS_PROFILE", environment);
		if (mongodbConnectionString != null && !mongodbConnectionString.isEmpty()) {
			environmentVars.put("MONGODB_CONNECTION_STRING", mongodbConnectionString);
		}
		environmentVars.put("AWS_REGION", awsRegion);

		// Configurar las propiedades de la función Lambda
		FunctionProps.Builder functionPropsBuilder = FunctionProps.builder()
				.functionName(lambdaFunctionName)
				.runtime(Runtime.JAVA_17)
				.code(Code.fromAsset("./apikey/target/function.zip"))
				.handler("io.quarkus.amazon.lambda.runtime.QuarkusStreamHandler::handleRequest")
				.memorySize(1024)
				.timeout(Duration.seconds(30))
				.role(lambdaRole)
				.environment(environmentVars);

		// Configurar VPC si está disponible
		if (vpc != null) {
			functionPropsBuilder.vpc(vpc);

			if (!subnets.isEmpty()) {
				functionPropsBuilder.vpcSubnets(SubnetSelection.builder()
						.subnets(subnets)
						.build());
			}

			if (!securityGroups.isEmpty()) {
				functionPropsBuilder.securityGroups(securityGroups);
			}
		}

		// Crear la función Lambda
		Function apiKeyFunction = new Function(this, "ApiKeyFunction", functionPropsBuilder.build());

		// Configurar logs para la función Lambda
		LogGroup.Builder.create(this, "ApiKeyFunctionLogs")
				.logGroupName("/aws/lambda/" + lambdaFunctionName)
				.retention(RetentionDays.ONE_WEEK)
				.removalPolicy(software.amazon.awscdk.RemovalPolicy.DESTROY)
				.build();

		// Crear la API REST
		RestApi api = RestApi.Builder.create(this, "ApiKeyApi")
				.restApiName(lambdaFunctionName + "-api")
				.description("API para administración de API keys")
				.deployOptions(StageOptions.builder()
						.stageName(stageName)
						.build())
				.build();

		// Integrar la función Lambda con la API
		LambdaIntegration lambdaIntegration = LambdaIntegration.Builder.create(apiKeyFunction)
				.build();

		// Crear recursos de API
		Resource apiKeyResource = api.getRoot().addResource("apikey");

		// Configurar métodos HTTP
		apiKeyResource.addMethod("GET", lambdaIntegration);
		apiKeyResource.addMethod("POST", lambdaIntegration);
		apiKeyResource.addMethod("PUT", lambdaIntegration);
		apiKeyResource.addMethod("DELETE", lambdaIntegration);

		// Endpoint hello específico
		Resource helloResource = apiKeyResource.addResource("hello");
		helloResource.addMethod("GET", lambdaIntegration);

		// Crear salidas para facilitar el acceso a los recursos
		CfnOutput.Builder.create(this, "ApiUrl")
				.description("URL de la API Gateway")
				.value(api.getUrl())
				.build();

		CfnOutput.Builder.create(this, "FunctionName")
				.description("Nombre de la función Lambda")
				.value(apiKeyFunction.getFunctionName())
				.build();

		CfnOutput.Builder.create(this, "ApiEndpoint")
				.description("Endpoint de la API")
				.value(api.getUrl() + "apikey/hello")
				.build();
	}
}