package de.effectivetrainings;

import de.effectivetrainings.customer.Address;
import de.effectivetrainings.customer.Customer;
import de.effectivetrainings.customer.CustomerRepository;
import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.hibernate.envers.query.AuditQuery;
import org.hibernate.envers.query.criteria.IdentifierEqAuditExpression;
import org.junit.Test;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;
import org.springframework.test.context.transaction.TransactionConfiguration;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;
import javax.sql.DataSource;
import java.util.List;

import static org.junit.Assert.*;

/**
 * @author Martin Dilger
 * @since: 19.05.13
 */
@ContextConfiguration(value = "classpath:spring-context.xml")
@TransactionConfiguration
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class SpringBootstrapTest extends AbstractJUnit4SpringContextTests {

    @Inject
    private CustomerRepository customerRepository;
    @PersistenceUnit
    private EntityManagerFactory entityManagerFactory;
    @Inject
    private DataSource ds;

    @Test
    public void configure() {
        assertNotNull(customerRepository);
        assertNotNull(entityManagerFactory);
    }

    @Test
    public void saveCustomer() {

        assertTrue(customerRepository.findAll().isEmpty());

        Address address = new Address("test-street", "80805", "München");
        Customer customer = new Customer("Hans", "test@test.de", address);
        Customer persistedCustomer = customerRepository.save(customer);

        persistedCustomer = customerRepository.findOne(persistedCustomer.getId());
        assertNotNull(persistedCustomer);

        //some simple sanity checks
        assertEquals(persistedCustomer.getName(), "Hans");
        assertEquals(persistedCustomer.getAddress().getStreet(), "test-street");
    }

    @Test
    public void checkAudit() throws Exception {

        assertTrue(customerRepository.findAll().isEmpty());

        Address address = new Address("test-street", "80805", "München");
        Customer customer = new Customer("Hans", "test@test.de", address);
        Customer persistedCustomer = customerRepository.save(customer);

        Address anotherAddres = new Address("test-street", "80805", "München");
        Customer anotherCustomer = new Customer("Heinz","test@test.de", anotherAddres);
        customerRepository.save(anotherCustomer);

        //make some changes
        persistedCustomer.setName("Georg");
        persistedCustomer = customerRepository.save(persistedCustomer);


        AuditReader auditReader = createAuditReader();

        List<Number> revisions = auditReader.getRevisions(Customer.class, persistedCustomer.getId());
        assertEquals(revisions.size(), 2);

        //delete customer
        customerRepository.delete(persistedCustomer);

        AuditQuery query=auditReader.createQuery().forRevisionsOfEntity(Customer.class, true, true);
        query.add(new IdentifierEqAuditExpression(persistedCustomer.getId(),true));
        List<Customer> result = query.getResultList();
        assertEquals(result.size(),3);

    }

    private AuditReader createAuditReader() {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        return AuditReaderFactory.get(entityManager);
    }

    @Test
    public void saveRevisionIfEmbeddedEntityChanges() throws Exception{
        Address address = new Address("test-street", "80805", "München");
        Customer customer = new Customer("Hans", "test@test.de", address);
        Customer persistedCustomer = customerRepository.save(customer);

        persistedCustomer.getAddress().setStreet("Teststraße 4");
        customerRepository.save(persistedCustomer);

        AuditReader auditReader = createAuditReader();
        assertEquals(2, auditReader.getRevisions(Customer.class, persistedCustomer.getId()).size());
    }
}
