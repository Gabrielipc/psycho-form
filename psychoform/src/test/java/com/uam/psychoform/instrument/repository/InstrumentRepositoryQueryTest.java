package com.uam.psychoform.instrument.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.repository.Query;

class InstrumentRepositoryQueryTest {
    @Test
    void officialConfigurationQueriesBindEstadoConfiguracionInsteadOfEmbeddingEnumLiterals() {
        assertThat(queriesFrom(ClaveRespuestaLookupRepository.class, OpcionPuntajeDimensionRepository.class,
                BaremoRepository.class, ReglaCalificacionPublicationRepository.class,
                BaremoPublicationRepository.class))
                .doesNotContain("EstadoConfiguracion.APROBADO")
                .doesNotContain("EstadoConfiguracion.PUBLICADO");
    }

    private static String queriesFrom(Class<?>... repositoryTypes) {
        StringBuilder queries = new StringBuilder();
        for (Class<?> repositoryType : repositoryTypes) {
            for (Method method : repositoryType.getDeclaredMethods()) {
                Query query = method.getAnnotation(Query.class);
                if (query != null) {
                    queries.append(query.value()).append('\n');
                }
            }
        }
        return queries.toString();
    }
}
