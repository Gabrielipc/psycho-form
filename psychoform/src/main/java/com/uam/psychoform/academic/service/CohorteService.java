package com.uam.psychoform.academic.service;

import com.uam.psychoform.academic.entity.Cohorte;
import com.uam.psychoform.academic.repository.CohorteRepository;
import org.springframework.stereotype.Service;

@Service
public class CohorteService extends CatalogoService<Cohorte, Short> {

    public CohorteService(CohorteRepository repository) {
        super(repository);
    }
}
