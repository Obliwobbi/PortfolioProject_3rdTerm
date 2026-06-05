package app.daos;

import app.entities.Company;
import app.interfaces.IDAO;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityNotFoundException;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class CompanyDAO implements IDAO<Company>
{

    private final EntityManagerFactory emf;

    public CompanyDAO(EntityManagerFactory emf)
    {
        this.emf = emf;
    }

    @Override
    public Company create(Company company)
    {
        try (EntityManager em = emf.createEntityManager())
        {
            em.getTransaction().begin();
            em.persist(company);
            em.getTransaction().commit();
            return company;
        }
    }

    @Override
    public Set<Company> getAll()
    {
        try (EntityManager em = emf.createEntityManager())
        {
            return new HashSet<>(
                    em.createQuery("SELECT c FROM Company c", Company.class)
                            .getResultList()
            );
        }
    }

    public Set<Company> getAllPublicRegistrationEnabled()
    {
        try (EntityManager em = emf.createEntityManager())
        {
            return new HashSet<>(
                    em.createQuery(
                                    "SELECT c FROM Company c WHERE c.publicRegistrationEnabled = true",
                                    Company.class
                            )
                            .getResultList()
            );
        }
    }

    @Override
    public Company getById(Long id)
    {
        try (EntityManager em = emf.createEntityManager())
        {
            Company company = em.find(Company.class, id);
            if (company == null)
            {
                throw new EntityNotFoundException("Company not found with id: " + id);
            }
            return company;
        }
    }

    public Optional<Company> findByName(String name)
    {
        try (EntityManager em = emf.createEntityManager())
        {
            return em.createQuery(
                            "SELECT c FROM Company c WHERE c.name = :name",
                            Company.class
                    )
                    .setParameter("name", name)
                    .getResultStream()
                    .findFirst();
        }
    }

    @Override
    public Company update(Company company)
    {
        try (EntityManager em = emf.createEntityManager())
        {
            Company found = em.find(Company.class, company.getId());
            if (found == null)
            {
                throw new EntityNotFoundException("Company not found with id: " + company.getId());
            }

            em.getTransaction().begin();
            Company merged = em.merge(company);
            em.getTransaction().commit();
            return merged;
        }
    }

    @Override
    public Long delete(Company company)
    {
        try (EntityManager em = emf.createEntityManager())
        {
            Company found = em.find(Company.class, company.getId());
            if (found == null)
            {
                throw new EntityNotFoundException("Company not found with id: " + company.getId());
            }

            em.getTransaction().begin();
            em.remove(found);
            em.getTransaction().commit();
            return found.getId();
        }
    }
}