package uy.com.bay.cruds.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uy.com.bay.cruds.data.Encuestador;
import uy.com.bay.cruds.services.EncuestadorService;

import java.util.List;

@RestController
@RequestMapping("/api/encuestadores")
public class EncuestadorController {

    private final EncuestadorService encuestadorService;

    public EncuestadorController(EncuestadorService encuestadorService) {
        this.encuestadorService = encuestadorService;
    }

    @GetMapping
    public List<Encuestador> getAllEncuestadores() {
        return encuestadorService.findAll();
    }
}
