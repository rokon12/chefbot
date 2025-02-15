package ca.bazlur.chefbot.config;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EnvironmentConfigTest {

    @AfterEach
    void tearDown() {
        EnvironmentConfig.clearTestEnv();
    }

    @Test
    void requireEnv_shouldThrowException_whenEnvironmentVariableNotSet() {
        assertThatThrownBy(() -> EnvironmentConfig.requireEnv("NON_EXISTENT_VAR"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Required environment variable 'NON_EXISTENT_VAR' is not set");
    }

    @Test
    void requireEnv_shouldReturnValue_whenEnvironmentVariableIsSet() {
        EnvironmentConfig.setTestEnv("TEST_VAR", "test-value");

        String value = EnvironmentConfig.requireEnv("TEST_VAR");

        assertThat(value).isEqualTo("test-value");
    }

    @Test
    void getEnv_shouldReturnDefaultValue_whenEnvironmentVariableNotSet() {
        String value = EnvironmentConfig.getEnv("NON_EXISTENT_VAR", "default-value");

        assertThat(value).isEqualTo("default-value");
    }

    @Test
    void getEnv_shouldReturnValue_whenEnvironmentVariableIsSet() {
        EnvironmentConfig.setTestEnv("TEST_VAR", "test-value");

        String value = EnvironmentConfig.getEnv("TEST_VAR", "default-value");

        assertThat(value).isEqualTo("test-value");
    }

    @Test
    void getEnvAsInt_shouldReturnDefaultValue_whenEnvironmentVariableNotSet() {
        int value = EnvironmentConfig.getEnvAsInt("NON_EXISTENT_VAR", 42);

        assertThat(value).isEqualTo(42);
    }

    @Test
    void getEnvAsInt_shouldReturnValue_whenEnvironmentVariableIsSet() {
        EnvironmentConfig.setTestEnv("TEST_VAR", "123");

        int value = EnvironmentConfig.getEnvAsInt("TEST_VAR", 42);

        assertThat(value).isEqualTo(123);
    }

    @Test
    void getEnvAsInt_shouldThrowException_whenEnvironmentVariableIsNotInteger() {
        EnvironmentConfig.setTestEnv("TEST_VAR", "not-a-number");

        assertThatThrownBy(() -> EnvironmentConfig.getEnvAsInt("TEST_VAR", 42))
                .isInstanceOf(NumberFormatException.class);
    }

    @Test
    void getEnvAsDouble_shouldReturnDefaultValue_whenEnvironmentVariableNotSet() {
        double value = EnvironmentConfig.getEnvAsDouble("NON_EXISTENT_VAR", 3.14);

        assertThat(value).isEqualTo(3.14);
    }

    @Test
    void getEnvAsDouble_shouldReturnValue_whenEnvironmentVariableIsSet() {
        EnvironmentConfig.setTestEnv("TEST_VAR", "3.14");

        double value = EnvironmentConfig.getEnvAsDouble("TEST_VAR", 42.0);

        assertThat(value).isEqualTo(3.14);
    }

    @Test
    void getEnvAsDouble_shouldThrowException_whenEnvironmentVariableIsNotDouble() {
        EnvironmentConfig.setTestEnv("TEST_VAR", "not-a-number");

        assertThatThrownBy(() -> EnvironmentConfig.getEnvAsDouble("TEST_VAR", 3.14))
                .isInstanceOf(NumberFormatException.class);
    }
}