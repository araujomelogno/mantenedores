package uy.com.bay.cruds.services;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import uy.com.bay.cruds.data.SamplePerson;
import uy.com.bay.cruds.data.SamplePersonRepository;

@Service
public class SamplePersonService {

    private final SamplePersonRepository repository;

    public SamplePersonService(SamplePersonRepository repository) {
        this.repository = repository;
    }

    public Optional<SamplePerson> get(Long id) {
        return repository.findById(id);
    }

    public SamplePerson save(SamplePerson entity) {
        return repository.save(entity);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }

    public Page<SamplePerson> list(Pageable pageable) {
        return repository.findAll(pageable);
    }

    public Page<SamplePerson> list(Pageable pageable, Specification<SamplePerson> filter) {
        return repository.findAll(filter, pageable);
    }

    public int count() {
        return (int) repository.count();
    }

}
