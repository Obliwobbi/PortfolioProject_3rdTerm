package app;

import app.config.HibernateConfig;

public class Main {
    public static void main(String[] args) {
        int port = getPort();
        ApplicationConfig.startApp(port, HibernateConfig.getEntityManagerFactory());
    }

    private static int getPort() {
        String portEnv = System.getenv("PORT");
        if (portEnv != null && !portEnv.isBlank()) {
            return Integer.parseInt(portEnv);
        }
        return 7000;
    }
}