package org.test;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

	List<Customer> findByLastNameStartsWithIgnoreCase(String lastName);
	

//	
//	@Query("select c from Customer c")
//	Stream<Customer> streamAllPaged(Pageable pageable);
}
