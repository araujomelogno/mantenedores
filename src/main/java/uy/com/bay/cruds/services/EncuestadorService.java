package uy.com.bay.cruds.services;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import uy.com.bay.cruds.data.Encuestador;
import uy.com.bay.cruds.data.EncuestadorRepository;

@Service
public class EncuestadorService {

    private final EncuestadorRepository repository;

    public EncuestadorService(EncuestadorRepository repository) {
        this.repository = repository;
    }

    public Optional<Encuestador> get(Long id) {
        return repository.findById(id);
    }

    public Encuestador save(Encuestador entity) {
        return repository.save(entity);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }

    public Page<Encuestador> list(Pageable pageable) {
        return repository.findAll(pageable);
    }

    public Page<Encuestador> list(Pageable pageable, Specification<Encuestador> filter) {
        return repository.findAll(filter, pageable);
    }

    public int count() {
        return (int) repository.count();
    }

}
