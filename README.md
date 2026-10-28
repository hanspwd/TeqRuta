# TeqRuta

TeqRuta es una plataforma diseñada para digitalizar y optimizar el registro de traslados, odometría y peajes de técnicos en terreno, permitiendo un control financiero eficiente, validación de jefatura y gestión de flotas vehiculares.

## Arquitectura del Proyecto

La solución se divide en dos componentes principales:

### 1. Aplicación Móvil (Android Nativo)
Diseñada con un enfoque **Offline-First**, asegurando que los técnicos puedan operar sin interrupciones en zonas con baja cobertura de red (ej. rutas hacia Chillán, Los Ángeles o Rancagua).

*   **Lenguaje:** Kotlin.
*   **Interfaz de Usuario:** Jetpack Compose, priorizando accesibilidad, alto contraste y controles adaptados para uso en terreno.
*   **Patrón de Diseño:** **MVVM (Model-View-ViewModel)**.
    *   **Model:** Representa los datos y la lógica de negocio (entidad `Viaje`, DAO de Room).
    *   **View:** Interfaces construidas con Jetpack Compose que observan los cambios de estado reactivos.
    *   **ViewModel (`ViajeViewModel`):** Gestiona el estado de la UI, validaciones en tiempo real y la persistencia local mediante flujos `StateFlow`.
*   **Almacenamiento Local:** SQLite a través de la librería Room, permitiendo registrar viajes, peajes con comprobantes fotográficos y bitácoras sin depender de conexión constante.
*   **Capacidades Nativas:** Integración con la cámara del dispositivo (`ActivityResultContracts.TakePicturePreview`) para la captura y previsualización de odómetros (inicial y final), boletas de peaje y evidencias en ruta.

### 2. Backend (API REST)
Sistema centralizado para la validación, almacenamiento y cálculo de costos.

*   **Framework:** Flask (Python).
*   **Estructura:** Patrón de Blueprints para modularizar la aplicación (Auth, Viajes, Reportes, Mantenedores).
*   **Base de Datos:** PostgreSQL/MySQL a través de SQLAlchemy (ORM).
*   **Seguridad:** Autenticación basada en JSON Web Tokens (JWT) y contraseñas encriptadas con Bcrypt. Control de acceso basado en roles (**RBAC**) diferenciando perfiles para:
    *   *Técnicos:* Registro de rutas, odometría, peajes y eventos.
    *   *Jefatura:* Validación, aprobación o rechazo de kilometrajes con notificación simulada por correo.
    *   *Gerencia:* Reportes financieros y exportación a CSV de costos mensuales.
    *   *Administradores:* Gestión global de flota vehicular, asignación de móviles a técnicos y mantenedor de destinos médicos.

## Estructura del Repositorio

*   `mobile/`: Contiene el código fuente de la app Android bajo patrón MVVM.
    *   `data/`: Configuración de Room Database y DAOs.
    *   `model/`: Definición de entidades (`Viaje`).
    *   `viewmodel/`: Lógica de interacción y manejo de estado (`ViajeViewModel`).
    *   `ui/screens/`: Pantallas declarativas (`InitialTripScreen`, `ActiveTripInProgressContent`, dashboards por rol).
    *   `ui/components/`: Componentes reutilizables (chips de estado, tarjetas de métricas, previsualizadores de cámara).
*   `backend/`: API Flask estructurada con Blueprints.
    *   `app/auth/`: Rutas de inicio de sesión y control de roles.
    *   `app/viajes/`: Rutas para la sincronización y gestión de viajes.
    *   `app/models.py`: Modelos de base de datos (SQLAlchemy).

## Estado de Desarrollo y Siguientes Pasos
El sistema actual cuenta con un MVP robusto e interactivo implementado tanto en el cliente Android como en la estructura de backend. Las siguientes iteraciones contemplan:
1.  Integrar `WorkManager` en Android para automatizar la sincronización en segundo plano con la API REST.
2.  Despliegue formal del Dashboard Web administrativo.
3.  Conexión con servicios externos de telemetría y precios de combustibles.

---

**Nota:** Este es un proyecto de desarrollo académico orientado a la digitalización de procesos de terreno. El uso comercial de este software se encuentra estrictamente restringido.