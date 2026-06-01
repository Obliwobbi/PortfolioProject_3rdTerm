package app.daos;

import app.entities.Company;
import app.entities.User;
import app.interfaces.IDAO;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.NoResultException;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class UserDAO implements IDAO<User>
{

    private final EntityManagerFactory emf;

    public UserDAO(EntityManagerFactory emf)
    {
        this.emf = emf;
    }

    @Override
    public User create(User user)
    {
        try (EntityManager em = emf.createEntityManager())
        {
            em.getTransaction().begin();

            Company managedCompany = em.merge(user.getCompany());
            user.setCompany(managedCompany);

            em.persist(user);
            em.getTransaction().commit();
            return user;
        }
    }

    @Override
    public Set<User> getAll()
    {
        try (EntityManager em = emf.createEntityManager())
        {
            return new HashSet<>(
                    em.createQuery("SELECT u FROM User u", User.class)
                            .getResultList()
            );
        }
    }

    public Set<User> getAllWithCompany()
    {
        try (EntityManager em = emf.createEntityManager())
        {
            return new HashSet<>(
                    em.createQuery("SELECT u FROM User u JOIN FETCH u.company", User.class)
                            .getResultList()
            );
        }
    }

    @Override
    public User getById(Long id)
    {
        try (EntityManager em = emf.createEntityManager())
        {
            User user = em.find(User.class, id);
            if (user == null)
            {
                throw new EntityNotFoundException("User not found with id: " + id);
            }
            return user;
        }
    }

    public User getByIdWithCompany(Long id)
    {
        try (EntityManager em = emf.createEntityManager())
        {
            try
            {
                return em.createQuery(
                                "SELECT u FROM User u JOIN FETCH u.company WHERE u.id = :id",
                                User.class
                        )
                        .setParameter("id", id)
                        .getSingleResult();
            } catch (NoResultException e)
            {
                throw new EntityNotFoundException("User not found with id: " + id);
            }
        }
    }

    @Override
    public User update(User user)
    {
        try (EntityManager em = emf.createEntityManager())
        {
            User found = em.find(User.class, user.getId());
            if (found == null)
            {
                throw new EntityNotFoundException("User not found with id: " + user.getId());
            }

            em.getTransaction().begin();

            Company managedCompany = em.merge(user.getCompany());
            user.setCompany(managedCompany);

            User merged = em.merge(user);
            em.getTransaction().commit();
            return merged;
        }
    }

    @Override
    public Long delete(User user)
    {
        try (EntityManager em = emf.createEntityManager())
        {
            User found = em.find(User.class, user.getId());
            if (found == null)
            {
                throw new EntityNotFoundException("User not found with id: " + user.getId());
            }

            em.getTransaction().begin();
            em.remove(found);
            em.getTransaction().commit();
            return found.getId();
        }
    }

    public Optional<User> findByEmail(String email)
    {
        try (EntityManager em = emf.createEntityManager())
        {
            return em.createQuery(
                            "SELECT u FROM User u WHERE u.email = :email",
                            User.class
                    )
                    .setParameter("email", email)
                    .getResultStream()
                    .findFirst();
        }
    }

    public Set<User> findByCompanyIdWithCompany(Long companyId)
    {
        try (EntityManager em = emf.createEntityManager())
        {
            return new HashSet<>(
                    em.createQuery(
                                    "SELECT u FROM User u JOIN FETCH u.company WHERE u.company.id = :companyId",
                                    User.class
                            )
                            .setParameter("companyId", companyId)
                            .getResultList()
            );
        }
    }

    // TODO: Add more queries, for example findByRole or pagination.
}