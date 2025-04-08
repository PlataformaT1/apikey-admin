package com.myorg;

import software.amazon.awscdk.App;
import software.amazon.awscdk.Environment;
import software.amazon.awscdk.StackProps;


public class DeployApp {
        public static void main(final String[] args) {
                App app = new App();

                String account = System.getenv("CDK_DEFAULT_ACCOUNT");
                if (account == null || account.isEmpty()) {
                        account = (String) app.getNode().tryGetContext("CDK_DEFAULT_ACCOUNT");
                }

                String region = System.getenv("CDK_DEFAULT_REGION");
                if (region == null || region.isEmpty()) {
                        region = (String) app.getNode().tryGetContext("CDK_DEFAULT_REGION");
                }

                Environment env = Environment.builder()
                        .account(account)
                        .region(region)
                        .build();

                StackProps stackProps = StackProps.builder()
                        .env(env)
                        .build();

                new DeployStack(app, "ApiKeyDeployStack", stackProps);

                app.synth();
        }
}
