package com.myorg;

import software.amazon.awscdk.services.apigateway.LambdaRestApi;
import software.amazon.awscdk.services.apigateway.StageOptions;
import software.amazon.awscdk.services.ec2.IVpc;
import software.amazon.awscdk.services.ec2.SubnetSelection;
import software.amazon.awscdk.services.ec2.SubnetType;
import software.amazon.awscdk.services.ec2.Vpc;
import software.amazon.awscdk.services.ec2.VpcLookupOptions;
import software.amazon.awscdk.services.ec2.Subnet;
import software.amazon.awscdk.services.ec2.ISecurityGroup;
import software.amazon.awscdk.services.ec2.SecurityGroup;
import software.amazon.awscdk.services.lambda.Function;
import software.amazon.awscdk.services.lambda.Runtime;
import software.amazon.awscdk.services.lambda.Code;
import java.util.Map;
import java.util.List;
import software.amazon.awscdk.Stack;
import software.constructs.Construct;
import software.amazon.awscdk.StackProps;
import java.nio.file.Paths;

public class DeployStack extends Stack {

		public DeployStack(final Construct scope, final String id) {
				this(scope, id, null);
		}

		public DeployStack(final Construct scope, final String id, final StackProps props) {
				super(scope, id, props);

				// Buscar una VPC existente
				IVpc vpc = Vpc.fromLookup(this, "ExistingVpc", VpcLookupOptions.builder()
								//.vpcId("vpc-06d959fa883f7099f") // Reemplaza con el ID de tu VPC específica
								.vpcId(System.getenv("VPC_ID"))
								.build());

				// Especificar una Subnet específica por ID
				SubnetSelection subnetSelection = SubnetSelection.builder()
								//.subnets(List.of(Subnet.fromSubnetId(this, "LambdaPrivateSubnet", "subnet-048e1b539693ca677"))) // Reemplaza con tu Subnet ID
						.subnets(List.of(Subnet.fromSubnetId(this, "LambdaPrivateSubnet", System.getenv("SUBNET_IDS"))))
								.build();

				// Definir un Security Group específico
				//ISecurityGroup securityGroup = SecurityGroup.fromSecurityGroupId(this, "Default", "sg-069a39550d6ce94b2"); // Reemplaza con tu Security Group ID
				ISecurityGroup securityGroup = SecurityGroup.fromSecurityGroupId(this, "Default", System.getenv("SECURITY_GROUP_IDS"));

				String absolutePath = Paths.get("../apikey-admin/apikey/target/function.zip").toAbsolutePath().toString();
				
				// Crear una Lambda en la VPC y Subnet especificada
				Function lambdaFunction = Function.Builder.create(this, "ApiKeyAdmin")
								.functionName("ApiKeyAdmin")
								.runtime(Runtime.PROVIDED_AL2023) // Cambia si usas otro runtime
								.handler("bootstrap")
								.vpc(vpc) // Apuntar a la VPC especificada
								.vpcSubnets(subnetSelection) // Definir la Subnet específica para la Lambda
                .securityGroups(List.of(securityGroup)) // Asignar el Security Group
								.code(Code.fromAsset(absolutePath)) // Ruta al archivo ZIP de tu Lambda
								//.environment(Map.of("APP_DB_MONGO_URI", "mongodb://t1pnonpci:qK!X5oNqP0b2@t1pnonpcidocumentdbcluster-f6py01dyecc7.cluster-ckh5zkjba5nq.us-east-1.docdb.amazonaws.com:27017/?replicaSet=rs0&readPreference=secondaryPreferred&retryWrites=false"))
								.environment(Map.of("APP_DB_MONGO_URI", System.getenv("MONGODB_CONNECTION_STRING")))
								.allowPublicSubnet(true)
								.build();

				// Crear un API Gateway vinculado a la Lambda
				LambdaRestApi api = LambdaRestApi.Builder.create(this, "ApiKeyAdminGateway")
								.handler(lambdaFunction)
								.deployOptions(StageOptions.builder()
												//.stageName("prod")
												.stageName(System.getenv("STAGE_NAME"))
												.build())
								.build();
		}
}