package uy.com.bay.cruds.data;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface SamplePersonRepository
		extends JpaRepository<SamplePerson, Long>, JpaSpecificationExecutor<SamplePerson> {

}
