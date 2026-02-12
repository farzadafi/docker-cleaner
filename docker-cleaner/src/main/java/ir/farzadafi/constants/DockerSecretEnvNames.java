package ir.farzadafi.constants;

import java.util.Set;

public final class DockerSecretEnvNames {

    private DockerSecretEnvNames() {
        throw new AssertionError("can't instantiate utility class!");
    }

    public static final Set<String> SECRET_ENV_NAMES = Set.of(
            // passwords
            "PASSWORD",
            "PASS",
            "PASSWD",
            "DB_PASSWORD",
            "DATABASE_PASSWORD",
            "MYSQL_PASSWORD",
            "POSTGRES_PASSWORD",
            "POSTGRESQL_PASSWORD",
            "MONGO_PASSWORD",
            "MONGODB_PASSWORD",
            "REDIS_PASSWORD",
            "RABBITMQ_PASSWORD",
            "ELASTIC_PASSWORD",
            "OPENSEARCH_PASSWORD",
            "ORACLE_PASSWORD",
            "SA_PASSWORD",

            // generic secrets
            "SECRET",
            "SECRET_KEY",
            "SECRET_TOKEN",
            "SECRET_VALUE",
            "PRIVATE_KEY",
            "PRIVATEKEY",
            "SSH_PRIVATE_KEY",
            "SSL_PRIVATE_KEY",
            "ENCRYPTION_KEY",

            // API keys
            "API_KEY",
            "APIKEY",
            "APP_KEY",
            "APPLICATION_KEY",
            "SERVICE_KEY",
            "CLIENT_SECRET",
            "CLIENTSECRET",

            // cloud providers
            "AWS_ACCESS_KEY",
            "AWS_ACCESS_KEY_ID",
            "AWS_SECRET_ACCESS_KEY",
            "AWS_SECRET_KEY",
            "AWS_SESSION_TOKEN",
            "GCP_API_KEY",
            "GCP_SECRET",
            "GOOGLE_API_KEY",
            "AZURE_CLIENT_SECRET",
            "AZURE_STORAGE_KEY",
            "AZURE_ACCOUNT_KEY",

            // tokens
            "TOKEN",
            "ACCESS_TOKEN",
            "REFRESH_TOKEN",
            "AUTH_TOKEN",
            "AUTHORIZATION_TOKEN",
            "BEARER_TOKEN",
            "SESSION_TOKEN",
            "OAUTH_TOKEN",
            "OAUTH_SECRET",

            // VCS / CI
            "GITHUB_TOKEN",
            "GITLAB_TOKEN",
            "BITBUCKET_TOKEN",
            "CI_JOB_TOKEN",
            "NPM_TOKEN",
            "NPM_AUTH_TOKEN",
            "PYPI_TOKEN",
            "DOCKER_TOKEN",

            // databases & services
            "DB_PASS",
            "DB_SECRET",
            "DATABASE_SECRET",
            "REDIS_SECRET",
            "MONGO_SECRET",
            "MYSQL_SECRET",
            "POSTGRES_SECRET",

            // messaging / integrations
            "SLACK_TOKEN",
            "SLACK_WEBHOOK",
            "DISCORD_TOKEN",
            "TELEGRAM_TOKEN",
            "TWILIO_AUTH_TOKEN",
            "STRIPE_SECRET_KEY",
            "PAYPAL_SECRET",

            // misc
            "JWT_SECRET",
            "JWT_PRIVATE_KEY",
            "SIGNING_KEY",
            "SIGNATURE_KEY",
            "CRYPTO_KEY",
            "VAULT_TOKEN",
            "CONSUL_TOKEN",
            "KAFKA_PASSWORD",
            "KAFKA_SECRET",

            // generic fallbacks seen in dataset
            "KEY",
            "SECRETKEY",
            "SECRETS",
            "PASSWORD_HASH",
            "MASTER_PASSWORD",
            "ROOT_PASSWORD",
            "ADMIN_PASSWORD",
            "ADMIN_SECRET"
    );
}
