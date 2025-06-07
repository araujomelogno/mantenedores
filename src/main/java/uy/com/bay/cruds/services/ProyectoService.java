package uy.com.bay.cruds.services;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import uy.com.bay.cruds.data.Proyecto;
import uy.com.bay.cruds.data.ProyectoRepository;

@Service
public class ProyectoService {

    private final ProyectoRepository repository;

    public ProyectoService(ProyectoRepository repository) {
        this.repository = repository;
    }

    public Optional<Proyecto> get(Long id) {
        return repository.findById(id);
    }

    public Proyecto save(Proyecto entity) {
        return repository.save(entity);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }

    public Page<Proyecto> list(Pageable pageable) {
        return repository.findAll(pageable);
    }

    public Page<Proyecto> list(Pageable pageable, Specification<Proyecto> filter) {
        return repository.findAll(filter, pageable);
    }

    public int count() {
        return (int) repository.count();
    }

    public List<Proyecto> findAll() {
        return repository.findAll();
    }
}
