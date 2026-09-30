# TeqRuta

TeqRuta es una plataforma diseñada para digitalizar y optimizar el registro de traslados, odometría y peajes de técnicos en terreno, al igual que constatar gastos.

## Arquitectura del Proyecto

La solución se divide en dos componentes principales:

### 1. Aplicación Móvil (Android Nativo)
Diseñada para un funcionamiento **Offline-First**, asegurando que los técnicos puedan operar sin interrupciones en zonas con baja cobertura de red (ej. rutas a Chillán o Los Ángeles).

*   **Lenguaje:** Kotlin.
*   **Interfaz de Usuario:** Jetpack Compose, priorizando accesibilidad, alto contraste y botones grandes para uso en terreno.
*   **Patrón de Diseño:** **MVVM (Model-View-ViewModel)**.
    *   **Model:** Representa los datos y la lógica de negocio (entidad `Viaje`, DAO de Room).
    *   **View:** Interfaces construidas con Jetpack Compose (`ViajeScreen`) que observan los cambios de estado.
    *   **ViewModel:** Gestiona el estado de la UI y se comunica con el Model (`ViajeViewModel`), recolectando datos mediante `StateFlow`.
*   **Almacenamiento Local:** SQLite a través de la librería Room. Permite guardar los viajes generados de manera local hasta que exista conexión.
*   **Sincronización (Próximos pasos):** Envío de los datos al servidor en segundo plano utilizando `WorkManager` (Req 3.2.6).
*   **Captura de Imágenes (Próximos pasos):** Invocación de la cámara nativa para fotografiar y comprimir comprobantes de peaje (Req 3.2.4).

### 2. Backend (API REST)
Sistema centralizado para la validación, almacenamiento y cálculo de costos.

*   **Framework:** Flask (Python).
*   **Estructura:** Patrón de Blueprints para modularizar la aplicación (Auth, Viajes, Reportes, Mantenedores).
*   **Base de Datos:** PostgreSQL/MySQL a través de SQLAlchemy (ORM).
*   **Seguridad:** Autenticación basada en JSON Web Tokens (JWT) y contraseñas encriptadas con Bcrypt. Manejo de roles (RBAC) para Técnicos, Jefatura, Gerencia y Administradores.
*   **Flujo de Datos:** Expone endpoints como `/api/viajes/sync` para recibir los viajes de forma masiva (JSON) provenientes de la app móvil.

## Estructura del Repositorio

El código generado en este MVP sienta las bases de:
*   `mobile/`: Contiene el código fuente de la app Android bajo patrón MVVM.
    *   `model/`: Definición de la entidad y los métodos de base de datos local.
    *   `viewmodel/`: Lógica de interacción y manejo de estado.
    *   `ui/screens/`: Pantallas declarativas de captura de odómetro y peajes.
*   `backend/`: API Flask estructurada con Blueprints.
    *   `app/auth/`: Rutas de inicio de sesión.
    *   `app/viajes/`: Rutas para la sincronización y gestión de viajes.
    *   `app/models.py`: Modelos de base de datos (SQLAlchemy).

## Estado de Desarrollo y Siguientes Pasos
Este MVP demuestra la estructura arquitectónica y los flujos principales (MVVM y Blueprints). Para iteraciones futuras se debe:
1.  Integrar `WorkManager` en Android para la sincronización automática.
2.  Desarrollar el Dashboard Web para Jefatura y Finanzas.
3.  Conectar la API externa de combustibles para el cálculo de costos.

---

**Nota:** Este es un proyecto de estudiantes desarrollado con fines académicos. El uso comercial de este software se encuentra estrictamente restringido.
