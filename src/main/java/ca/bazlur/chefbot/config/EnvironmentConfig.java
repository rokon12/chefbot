package ca.bazlur.chefbot.config;

import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for handling environment variable configurations.
 */
public class EnvironmentConfig {

    /**
     * Retrieves a required environment variable.
     *
     * @param name The name of the environment variable
     * @return The value of the environment variable
     * @throws IllegalStateException if the environment variable is not set
     */
    public static String requireEnv(String name) {
        String value = getEnvValue(name);
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalStateException("Required environment variable '" + name + "' is not set");
        }
        return value;
    }

    /**
     * Retrieves an optional environment variable with a default value.
     *
     * @param name         The name of the environment variable
     * @param defaultValue The default value to return if the environment variable is not set
     * @return The value of the environment variable or the default value
     */
    public static String getEnv(String name, String defaultValue) {
        String value = getEnvValue(name);
        return (value != null && !value.trim().isEmpty()) ? value : defaultValue;
    }

    /**
     * Retrieves an optional integer environment variable with a default value.
     *
     * @param name         The name of the environment variable
     * @param defaultValue The default value to return if the environment variable is not set
     * @return The value of the environment variable or the default value
     * @throws NumberFormatException if the environment variable is set but cannot be parsed as an integer
     */
    public static int getEnvAsInt(String name, int defaultValue) {
        String value = getEnvValue(name);
        return (value != null && !value.trim().isEmpty()) ? Integer.parseInt(value) : defaultValue;
    }

    /**
     * Retrieves an optional double environment variable with a default value.
     *
     * @param name         The name of the environment variable
     * @param defaultValue The default value to return if the environment variable is not set
     * @return The value of the environment variable or the default value
     * @throws NumberFormatException if the environment variable is set but cannot be parsed as a double
     */
    public static double getEnvAsDouble(String name, double defaultValue) {
        String value = getEnvValue(name);
        return (value != null && !value.trim().isEmpty()) ? Double.parseDouble(value) : defaultValue;
    }

    // For testing purposes
    private static final Map<String, String> testEnv = new HashMap<>();

    public static void setTestEnv(String name, String value) {
        testEnv.put(name, value);
    }

    public static void clearTestEnv() {
        testEnv.clear();
    }

    private static String getEnvValue(String name) {
        return testEnv.containsKey(name) ? testEnv.get(name) : System.getenv(name);
    }
}
