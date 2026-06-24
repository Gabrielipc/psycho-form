package com.uam.psychoform.academic.service;

import com.uam.psychoform.academic.entity.GrupoAcademico;
import com.uam.psychoform.academic.repository.GrupoAcademicoRepository;
import org.springframework.stereotype.Service;

@Service
public class GrupoAcademicoService extends CatalogoService<GrupoAcademico, Short> {

    public GrupoAcademicoService(GrupoAcademicoRepository repository) {
        super(repository);
    }
}
