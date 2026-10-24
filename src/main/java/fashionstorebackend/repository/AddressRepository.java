package fashionstorebackend.repository;

import fashionstorebackend.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AddressRepository extends JpaRepository<Address, Long> {

    List<Address> findByUserId(Long userId);

    List<Address> findByUserIdOrderByDefaultAddressDesc(Long userId);
}