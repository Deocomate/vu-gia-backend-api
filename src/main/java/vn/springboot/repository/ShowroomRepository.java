package vn.springboot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import vn.springboot.entity.showroom.ShowroomEntity;

@Repository
public interface ShowroomRepository
        extends JpaRepository<ShowroomEntity, Long>, JpaSpecificationExecutor<ShowroomEntity> {
}
