# BFA Digital

## Sistema de Digitalización de la Batería Factorial de Aptitudes

BFA Digital es una aplicación web institucional orientada a digitalizar la aplicación, control, calificación y consulta de resultados de una parte específica de la **Batería Factorial de Aptitudes (BFA)**. El sistema está enfocado en los subtests **Figuras idénticas**, **Desplazamiento** y **Espacial**, con el objetivo de reducir procesos manuales, mejorar la trazabilidad, proteger instrumentos sensibles y generar resultados de forma más rápida y segura.

El proyecto surge como una solución para la carrera de Psicología de la Universidad Americana (UAM), permitiendo administrar sesiones de evaluación controladas, registrar participantes, capturar respuestas, calcular resultados automáticamente y generar reportes básicos para usuarios autorizados.

## Objetivo del proyecto

El objetivo principal de BFA Digital es sustituir el proceso manual de aplicación y corrección de los subtests definidos de la BFA por una plataforma digital segura, controlada y trazable.

El sistema busca:

* Reducir el tiempo de aplicación y corrección.
* Disminuir errores de conteo, transcripción o tabulación.
* Proteger claves, ítems, imágenes, baremos y resultados.
* Facilitar la consulta de resultados individuales y agregados.
* Permitir la generación de reportes exportables.
* Mantener auditoría de accesos, cambios y consultas sensibles.
* Servir como base para futuras ampliaciones del sistema.

## Alcance del sistema

La primera versión del sistema incluye exclusivamente la digitalización de los siguientes subtests de la BFA:

* Figuras idénticas.
* Desplazamiento.
* Espacial.

El alcance funcional contempla:

* Autenticación de usuarios.
* Control de acceso por roles.
* Registro de participantes.
* Configuración de subtests, ítems, opciones, imágenes, claves y tiempos.
* Creación y administración de sesiones de evaluación.
* Aplicación digital de los subtests.
* Guardado incremental de respuestas.
* Control de avance y finalización de evaluación.
* Calificación automática.
* Consulta de resultados individuales.
* Consulta de resultados agregados.
* Exportación de reportes.
* Dashboard básico de indicadores.
* Auditoría de acciones sensibles.
* Respaldo y recuperación de información.

## Fuera del alcance inicial

La primera versión no contempla:

* Digitalización de otros instrumentos psicológicos.
* Pruebas vocacionales completas.
* Entrevistas clínicas.
* Integración completa con sistemas institucionales.
* Diagnósticos psicológicos automáticos definitivos.
* Análisis estadístico avanzado para renormalización de baremos.
* Aplicaciones no supervisadas fuera de condiciones institucionales.

## Usuarios del sistema

El sistema considera los siguientes perfiles de usuario:

### Participante

Usuario que responde los subtests dentro de una sesión autorizada. Puede ser estudiante, aspirante u otro evaluado autorizado.

### Aplicador

Usuario encargado de crear, iniciar, supervisar y cerrar sesiones de evaluación. También controla el avance de los participantes y registra incidencias operativas.

### Psicólogo / Coordinador

Usuario autorizado para consultar resultados, validar reportes, revisar información individual o agregada y supervisar el uso funcional del instrumento.

### Administrador del sistema

Usuario técnico responsable de gestionar cuentas, roles, permisos, parámetros del sistema, respaldos y auditoría.

### Consultor de reportes

Usuario autorizado para consultar únicamente información agregada, según los permisos definidos por la institución.

## Características principales

### Gestión de usuarios, roles y sesiones

Permite autenticar usuarios y asignar permisos según su rol. También permite crear y administrar sesiones de aplicación para controlar cuándo y quién puede responder los subtests.

### Registro de participantes

Permite registrar participantes con datos mínimos necesarios, como código único o identificador, nombre, edad, sexo, carrera o grupo.

### Configuración de la BFA

Permite configurar los subtests incluidos en el alcance, gestionando ítems, imágenes, opciones, claves, tiempos, instrucciones, reglas de calificación, estado de publicación y versión del instrumento.

### Aplicación digital de subtests

Presenta los subtests en una interfaz clara, con instrucciones iniciales, navegación controlada, registro de respuestas por ítem, control de avance y finalización.

### Calificación automática

Calcula puntuaciones directas y resultados conforme a reglas previamente validadas por el área de Psicología.

### Reportes y dashboards

Permite consultar resultados individuales y agregados por sesión, grupo, edad, sexo, subtest u otros criterios aprobados. También permite exportar información en formatos como PDF, Excel o CSV.

### Auditoría, seguridad y respaldo

Registra accesos, cambios, consultas de información sensible, configuraciones de instrumentos, sesiones y eventos relevantes. Además, contempla mecanismos de respaldo y recuperación para evitar pérdida de información.

## Requisitos generales del sistema

El sistema debe contemplar:

* Aplicación web accesible desde navegadores modernos.
* Interfaz responsiva, priorizando computadoras o tablets por la naturaleza visual de los subtests.
* Base de datos relacional para usuarios, participantes, sesiones, respuestas, resultados y auditoría.
* API segura para la comunicación entre interfaz, lógica de negocio y almacenamiento.
* Almacenamiento protegido para imágenes o elementos gráficos de los subtests.
* Mecanismos de respaldo y recuperación.
* Capacidad de despliegue en servidor institucional o nube, según decisión de la universidad.

## Requisitos de desempeño

* La navegación entre ítems debe sentirse inmediata.
* El guardado de respuestas debe ejecutarse de forma incremental.
* La calificación debe generarse en segundos después de finalizar la evaluación.
* El sistema debe soportar al menos una sesión grupal de laboratorio con decenas de participantes simultáneos.
* Los reportes básicos deben generarse sin esperas prolongadas.

## Seguridad y privacidad

BFA Digital maneja información sensible, por lo que debe aplicar medidas de protección como:

* Autenticación obligatoria.
* Control de acceso por roles.
* Principio de mínimo privilegio.
* Protección de claves, ítems, imágenes, baremos y resultados.
* Registro de auditoría para acciones sensibles.
* Acceso restringido a resultados individuales.
* Respaldo y recuperación de datos.
* Validación institucional de reglas de calificación y reportes.

El sistema no debe exponer claves, baremos ni instrumentos completos a participantes o usuarios no autorizados.

## Restricciones

* El MVP se limita a los subtests Figuras idénticas, Desplazamiento y Espacial.
* No se deben emitir diagnósticos psicológicos definitivos de forma automática.
* La interpretación final debe quedar bajo criterio profesional del personal autorizado.
* La aplicación debe realizarse preferiblemente en sesiones supervisadas.
* Los resultados individuales deben tratarse como información confidencial.
* La infraestructura final, presupuesto, políticas institucionales y número exacto de usuarios simultáneos están pendientes de confirmación.

## Instalación

La instalación dependerá de la tecnología definida para la implementación final. Como guía general, el sistema deberá contar con:

1. Un entorno de ejecución para la aplicación web.
2. Una base de datos relacional configurada.
3. Variables de entorno para conexión a base de datos, autenticación y almacenamiento.
4. Scripts de inicialización o migración de base de datos.
5. Usuario administrador inicial.
6. Mecanismo de respaldo configurado.
7. Ambiente de pruebas previo al uso real.

Ejemplo general:

```bash
# Clonar el repositorio
git clone <url-del-repositorio>

# Ingresar al proyecto
cd bfa-digital

# Instalar dependencias
<instalar-dependencias>

# Configurar variables de entorno
cp .env.example .env

# Ejecutar migraciones o scripts de base de datos
<ejecutar-migraciones>

# Iniciar el servidor
<iniciar-aplicacion>
```

## Uso general

1. El administrador crea usuarios y asigna roles.
2. El aplicador crea una sesión de evaluación.
3. Se registran o habilitan los participantes.
4. El participante ingresa a la sesión autorizada.
5. El participante responde los subtests habilitados.
6. El sistema guarda las respuestas durante la evaluación.
7. Al finalizar, el sistema calcula los resultados.
8. El psicólogo o coordinador consulta resultados autorizados.
9. Se generan reportes individuales o agregados.
10. Las acciones sensibles quedan registradas en auditoría.

## Estado del proyecto

Versión preliminar del producto basada en el Documento Visión del proyecto BFA Digital.

Estado actual:

* Alcance definido.
* Subtests principales identificados.
* Usuarios y stakeholders definidos.
* Características principales propuestas.
* Restricciones iniciales documentadas.
* Reglas exactas de calificación, tiempos, baremos e infraestructura pendientes de confirmación institucional.

## Autores

Proyecto académico desarrollado para la Universidad Americana (UAM).

Autores de la propuesta:

* Gabriel Pérez.
* Silvio Mora.
* Christopher Ibarra.

## Licencia

Este proyecto corresponde a una propuesta académica. El uso, distribución y modificación del sistema dependerá de las políticas definidas por la Universidad Americana y por el equipo responsable del proyecto.

## Confidencialidad

Este sistema puede manejar instrumentos, claves de respuesta, baremos, resultados individuales y datos personales. Toda información relacionada con la BFA y sus resultados debe tratarse como confidencial y solo debe estar disponible para usuarios autorizados.
