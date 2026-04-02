package app.config;

import app.entities.*;
import org.hibernate.cfg.Configuration;

final class EntityRegistry
{

    private EntityRegistry()
    {
    }

    static void registerEntities(Configuration configuration)
    {
        // TODO: Add entities here...
        configuration.addAnnotatedClass(User.class);
        configuration.addAnnotatedClass(Company.class);
//        configuration.addAnnotatedClass(Membership.class);
//        configuration.addAnnotatedClass(MembershipType.class);
//        configuration.addAnnotatedClass(Location.class);
//        configuration.addAnnotatedClass(CheckIn.class);
    }
}