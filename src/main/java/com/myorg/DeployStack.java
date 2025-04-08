package com.myorg;

import software.amazon.awscdk.services.apigateway.LambdaRestApi;
import software.amazon.awscdk.services.apigateway.StageOptions;
import software.amazon.awscdk.services.ec2.IVpc;
import software.amazon.awscdk.services.ec2.SubnetSelection;
import software.amazon.awscdk.services.ec2.Vpc;
import software.amazon.awscdk.services.ec2.VpcLookupOptions;
import software.amazon.awscdk.services.ec2.Subnet;
import software.amazon.awscdk.services.ec2.ISecurityGroup;
import software.amazon.awscdk.services.ec2.SecurityGroup;
import software.amazon.awscdk.services.lambda.Function;
import software.amazon.awscdk.services.lambda.Runtime;
import software.amazon.awscdk.services.lambda.Code;
import software.amazon.awscdk.App;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.constructs.Construct;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.nio.file.Paths;


public class DeployStack extends Stack {
	public DeployStack(final Construct scope, final String id) {
		this(scope, id, null);
	}

	public DeployStack(final Construct scope, final String id, final StackProps props) {
		super(scope, id, props);

		// Obtener valores de variables de entorno o contexto
		String vpcId = getEnvOrContext(scope, "VPC_ID");
		String subnetIdsString = getEnvOrContext(scope, "SUBNET_IDS");
		String securityGroupIdsString = getEnvOrContext(scope, "SECURITY_GROUP_IDS");
		String stageName = getEnvOrContext(scope, "STAGE_NAME", "prod");
		String mongodbConnectionString = getEnvOrContext(scope, "MONGODB_CONNECTION_STRING");
		String logLevel = getEnvOrContext(scope, "LOG_LEVEL", "INFO");
		String logChan = getEnvOrContext(scope, "LOG_CHAN", "app");

		// Buscar una VPC existente
		IVpc vpc = Vpc.fromLookup(this, "ExistingVpc", VpcLookupOptions.builder()
				.vpcId(vpcId)
				.build());

		// Procesar lista de IDs de subnets
		SubnetSelection subnetSelection = SubnetSelection.builder()
				.subnets(List.of(Subnet.fromSubnetId(this, "LambdaPrivateSubnet", subnetIdsString))) // Reemplaza con tu Subnet ID
				.build();


		// Definir un Security Group específico
		ISecurityGroup securityGroup = SecurityGroup.fromSecurityGroupId(this, "Default", securityGroupIdsString); // Reemplaza con tu Security Group ID

		String absolutePath = Paths.get("../apikey-admin/apikey/target/function.zip").toAbsolutePath().toString();

		// Crear un mapa para las variables de entorno de la función Lambda
		Map<String, String> lambdaEnvVars = new HashMap<>();
		lambdaEnvVars.put("APP_DB_MONGO_URI", mongodbConnectionString);
		lambdaEnvVars.put("LOG_LEVEL", logLevel);
		lambdaEnvVars.put("LOG_CHAN", logChan);

		// Crear una Lambda en la VPC y Subnet especificada
		Function lambdaFunction = Function.Builder.create(this, "ApiKeyAdmin")
				.functionName("ApiKeyAdmin")
				.runtime(Runtime.PROVIDED_AL2023)
				.handler("bootstrap")
				.vpc(vpc)
				.vpcSubnets(subnetSelection)
				.securityGroups(List.of(securityGroup))
				.code(Code.fromAsset(absolutePath))
				.environment(lambdaEnvVars)
				.allowPublicSubnet(true)
				.build();

		// Crear un API Gateway vinculado a la Lambda
		LambdaRestApi api = LambdaRestApi.Builder.create(this, "ApiKeyAdminGateway")
				.handler(lambdaFunction)
				.deployOptions(StageOptions.builder()
						.stageName(stageName)
						.build())
				.build();
	}

	// Método auxiliar para obtener valor de variable de entorno o contexto
	private String getEnvOrContext(Construct scope, String key) {
		return getEnvOrContext(scope, key, null);
	}

	private String getEnvOrContext(Construct scope, String key, String defaultValue) {
		// Primero intentar obtener de variable de entorno
		String value = System.getenv(key);

		// Si no está disponible en variable de entorno, intentar obtener del contexto
		if (value == null || value.isEmpty()) {
			App app = (App) scope.getNode().getRoot();
			Object contextValue = app.getNode().tryGetContext(key);
			if (contextValue != null) {
				value = contextValue.toString();
			}
		}

		// Si aún no está disponible, usar valor predeterminado
		if (value == null || value.isEmpty()) {
			value = defaultValue;
		}

		return value;
	}
}
