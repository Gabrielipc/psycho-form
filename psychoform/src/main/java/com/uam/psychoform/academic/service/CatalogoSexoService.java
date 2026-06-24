package com.uam.psychoform.academic.service;

import com.uam.psychoform.academic.entity.CatalogoSexo;
import com.uam.psychoform.academic.repository.CatalogoSexoRepository;
import org.springframework.stereotype.Service;

@Service
public class CatalogoSexoService extends CatalogoService<CatalogoSexo, Short> {

    public CatalogoSexoService(CatalogoSexoRepository repository) {
        super(repository);
    }
}
