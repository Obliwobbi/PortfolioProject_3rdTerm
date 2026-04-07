package app.config;

import jakarta.persistence.EntityManagerFactory;

import java.util.Properties;

public final class HibernateTestConfig
{

    private static volatile EntityManagerFactory emf;

    private HibernateTestConfig()
    {
    }

    public static EntityManagerFactory getEntityManagerFactory()
    {
        if (emf == null || !emf.isOpen())
        {
            synchronized (HibernateTestConfig.class)
            {
                if (emf == null || !emf.isOpen())
                {
                    emf = HibernateEmfBuilder.build(buildTestProps());
                }
            }
        }
        return emf;
    }

    private static Properties buildTestProps()
    {
        Properties props = HibernateBaseProperties.createBase();

        props.put("hibernate.hbm2ddl.auto", "create-drop");
        props.put("hibernate.connection.url", "jdbc:postgresql://localhost:5432/test_member_system");
        props.put("hibernate.connection.username", "postgres");
        props.put("hibernate.connection.password", "postgres");

        return props;
    }

}