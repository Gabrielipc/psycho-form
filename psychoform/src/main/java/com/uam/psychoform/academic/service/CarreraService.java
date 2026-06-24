package com.uam.psychoform.academic.service;

import com.uam.psychoform.academic.entity.Carrera;
import com.uam.psychoform.academic.repository.CarreraRepository;
import org.springframework.stereotype.Service;

@Service
public class CarreraService extends CatalogoService<Carrera, Short> {

    public CarreraService(CarreraRepository repository) {
        super(repository);
    }
}
