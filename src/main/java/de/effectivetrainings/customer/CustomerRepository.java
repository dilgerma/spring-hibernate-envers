package de.effectivetrainings.customer;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author Martin Dilger
 * @since: 19.05.13
 */
public interface CustomerRepository extends JpaRepository<Customer, Long> {
}
