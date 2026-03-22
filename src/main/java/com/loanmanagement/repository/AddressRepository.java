package com.loanmanagement.repository;

import com.loanmanagement.entity.Address;
import com.loanmanagement.enums.AddressType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AddressRepository extends JpaRepository<Address, Long> {
    
    List<Address> findByUserId(Long userId);

    Optional<Address> findByUserIdAndAddressType(
            Long userId, AddressType addressType);

    boolean existsByUserIdAndAddressType(
            Long userId, AddressType addressType);

}
