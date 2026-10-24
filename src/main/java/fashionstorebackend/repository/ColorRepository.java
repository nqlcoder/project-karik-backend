package fashionstorebackend.repository;

import fashionstorebackend.entity.Color;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ColorRepository extends JpaRepository<Color, Long> {

    List<Color> findAllByOrderByNameAsc();
}