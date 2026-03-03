Contexto para el Asistente - Desafío Técnico Java Senior (Arquitecto Financiero)
Instrucción principal
Eres un arquitecto de software senior en una empresa financiera. Debes generar un proyecto Java completo que resuelva el siguiente desafío técnico. El proyecto debe ser totalmente funcional, incluir código fuente, pruebas unitarias, documentación y diagramas, siguiendo las mejores prácticas de desarrollo, código limpio, principios SOLID, y con una arquitectura preparada para escalar y facilitar la integración de nuevos servicios financieros en el futuro.

Descripción del desafío
Formas parte de un equipo encargado de diseñar la arquitectura de un sistema conversacional distribuido para una entidad financiera (banco, fintech, gestora de inversiones). Este sistema será un asistente virtual empresarial capaz de recibir, procesar y responder consultas de clientes sobre productos financieros, integrándose con servicios internos (cuentas, tarjetas) y externos (mercados, APIs públicas financieras).

Objetivo
Diseñar e implementar un microservicio en Java que represente un componente clave dentro de esta arquitectura financiera. El microservicio debe simular ser el "orquestador conversacional" que entiende intenciones financieras y consulta datos relevantes. La solución debe ser extensible para añadir fácilmente nuevas intenciones financieras, nuevos proveedores de datos y nuevos canales de conversación.

Requisitos funcionales y técnicos (obligatorios)
1. Diseño de API Financiera
Propón una interfaz de comunicación RESTful para el microservicio.

Justificación: REST es estándar en el sector bancario para Open Banking y APIs financieras, y facilita la integración con otros servicios.

Define contratos de entrada/salida en JSON para consultas financieras típicas.

Buenas prácticas: Versionado de API (ej. /api/v1/chat), uso correcto de códigos HTTP, documentación con OpenAPI/Swagger (opcional pero recomendado).

2. Procesamiento de Consultas Financieras (Extensible)
Implementa una lógica que simule un asistente virtual financiero.

Diseño pensado para extensibilidad: Utiliza un patrón de diseño como Strategy o Chain of Responsibility para manejar diferentes intenciones. Debe ser fácil agregar una nueva intención (ej. simular_prestamo) sin modificar el código existente (abierto/cerrado).

Intenciones iniciales a implementar (basadas en reglas simples, usando palabras clave o expresiones regulares):

consultar_saldo: el usuario pregunta por su saldo (respuesta simulada).

consultar_tipo_cambio: el usuario pregunta el valor de una divisa (consume API externa).

info_producto: el usuario pide información de un producto financiero (respuesta predefinida).

recomendacion_inversion: el usuario pide una recomendación según un perfil de riesgo simulado.

3. Integración con API Pública Financiera
Realiza al menos una llamada a una API pública financiera real y utiliza su respuesta en el procesamiento.

API recomendada: Frankfurter (tipos de cambio, gratuita, sin clave). Úsala para la intención consultar_tipo_cambio.

Manejo de fallos: Implementa un patrón de circuit breaker simulado o al menos un try-catch con fallback (devolver un mensaje amigable y posiblemente datos en caché si los hubiera).

Preparación para el futuro: La integración con esta API debe estar encapsulada en un cliente dedicado, de modo que si en el futuro se cambia a otro proveedor (ej. XE, Oanda), solo haya que modificar una clase.

4. Persistencia Financiera
Guarda el historial de conversación de forma estructurada.

Usa H2 en memoria con JPA.

Entidad: Conversation con campos: id, customerId (o sessionId), userMessage, assistantResponse, intention (tipo de consulta detectada), timestamp.

Extensibilidad: El diseño de la entidad debe permitir añadir campos adicionales en el futuro sin romper el código existente (ej. rating, feedback).

5. Gestión de Errores
Implementa manejo robusto para entradas inválidas, errores internos y fallos en servicios externos.

Contexto financiero: Los errores deben ser informativos pero sin exponer datos sensibles. Usa un @ControllerAdvice global que devuelva respuestas de error consistentes (código HTTP, mensaje, timestamp).

Buenas prácticas: Diferenciar entre errores de cliente (4xx) y errores de servidor (5xx). Incluir un identificador de correlación en las respuestas de error para facilitar el debugging.

6. Pruebas
Incluye pruebas unitarias con JUnit 5 y Mockito.

Prueba al menos:

El servicio de procesamiento de consultas (con mocking del repositorio y del cliente HTTP).

El controlador REST.

Casos de error (API externa caída, entrada inválida, intención no reconocida).

Cobertura: Apunta a una cobertura de al menos el 80% en las capas de servicio y controlador (puedes mencionarlo como objetivo, no es necesario medirlo).

7. Documentación
Crea un archivo README.md que contenga:

Arquitectura propuesta: Diagrama de componentes, explicación de capas, patrones utilizados (Strategy para intenciones, Cliente HTTP separado, etc.).

Interfaces y contratos: Descripción de endpoints con ejemplos de request/response (incluyendo ejemplos financieros).

Decisiones de diseño: Justificación de tecnologías, por qué se eligió ese patrón para las intenciones, cómo se preparó el sistema para futuras integraciones.

Instrucciones de ejecución: Cómo compilar, ejecutar y probar el proyecto.

Mejoras futuras: Explica brevemente cómo se podrían añadir nuevas intenciones o cambiar la API externa.

8. Observabilidad (Contexto Financiero)
Define qué métricas técnicas y funcionales expondrías.

Métricas relevantes:

Técnicas: tasa de error de llamadas a API externa, latencia del endpoint /chat, tiempo de respuesta del asistente.

Funcionales: consultas por tipo de intención (contadores), tasa de éxito (intención reconocida vs. no reconocida).

Justificación: En banca digital, es crítico monitorear la disponibilidad de datos de mercado y el comportamiento de los usuarios para priorizar mejoras.

Implementación opcional pero recomendada: Endpoint /metrics que devuelva un JSON con algunas métricas en memoria (puedes usar Counter de Micrometer o simples AtomicLong).

9. Diagramas
Incluye en el README.md (usando Mermaid o bloques de texto):

Diagrama de componentes: Muestra los componentes internos (Controller, Service con Strategy, Repository, ExternalApiClient) y sus relaciones.

Diagrama de secuencia: Para el caso de uso "Consulta de tipo de cambio", mostrando la interacción entre el usuario, el controller, el service, el cliente HTTP y la API externa.

Extras Opcionales (altamente valorados en el contexto financiero)
Seguridad: Implementa autenticación básica con JWT simulando un token de cliente. Protege el endpoint de historial (/api/v1/conversations).

Escalabilidad: Explica en el README cómo escalarías horizontalmente este microservicio:

Stateless, por lo tanto, múltiples instancias detrás de un balanceador.

Uso de caché distribuida (ej. Redis) para almacenar respuestas de la API de tipos de cambio y reducir latencia y llamadas externas.

Colas (RabbitMQ/Kafka) para procesamiento asíncrono de consultas que requieran más tiempo (simulación de préstamos, etc.).

Preparación para integración futura: Describe cómo añadirías un nuevo proveedor de datos financieros (ej. una API de acciones) sin cambiar el núcleo del asistente.

Stack tecnológico obligatorio
Java 17 o superior.

Spring Boot 3.x (Spring Web, Spring Data JPA, Spring Boot Actuator opcional).

Maven (preferiblemente) o Gradle.

H2 Database (en memoria).

JUnit 5 y Mockito.

Cliente HTTP: RestClient (nuevo en Spring Boot 3.2) o WebClient (reactivo pero usado de forma bloqueante si se prefiere). Se recomienda RestClient por su simplicidad y modernidad.

Formato de entrega
Debes proporcionar todos los archivos del proyecto en formato de texto, claramente identificados con su ruta y nombre. Por ejemplo:

text
// filename: pom.xml
contenido...

// filename: src/main/java/com/financial/assistant/FinancialAssistantApplication.java
contenido...
Incluye el README.md con toda la documentación y diagramas.

Instrucciones adicionales sobre calidad y extensibilidad
Código limpio: Sigue las convenciones de nomenclatura, comenta solo lo necesario, mantén las clases y métodos pequeños y con una sola responsabilidad (SRP).

Principios SOLID: Aplícalos de forma evidente. En particular, el principio de abierto/cerrado en el procesamiento de intenciones.

Preparación para el futuro: El código debe estar estructurado de manera que añadir una nueva intención financiera (ej. "simular préstamo") implique solo crear una nueva clase que implemente una interfaz IntentionHandler y registrarla en una fábrica o mapa, sin modificar el flujo principal.

Pruebas como documentación: Las pruebas deben mostrar cómo se comporta el sistema en casos normales y de error.

Manejo de configuración: Externaliza las URLs de APIs, timeouts, etc., en application.properties o application.yml. Usa @ConfigurationProperties si es necesario.



Incluye también:
- `application.yml` o `application.properties`
- Todas las clases necesarias (controladores, servicios, repositorios, entidades, DTOs, manejadores de excepciones, clientes HTTP, configuraciones, etc.)
- Las pruebas unitarias en `src/test/java/...`
- El `README.md` con la documentación y diagramas.

### Índice de archivos
Antes de mostrar los archivos, proporciona un **índice numerado** de todos los archivos que vas a generar, con una breve descripción de su propósito. Ejemplo:

1. `pom.xml` - Configuración de Maven con dependencias.
2. `src/main/resources/application.yml` - Configuración de Spring Boot, H2, API externa.
3. `src/main/java/com/financial/assistant/FinancialAssistantApplication.java` - Clase principal.
4. `src/main/java/com/financial/assistant/controller/ChatController.java` - Endpoint REST para chat.
5. `src/main/java/com/financial/assistant/dto/ChatRequest.java` - DTO para petición.
...

## Reglas de estilo y buenas prácticas
- **Código en inglés**: nombres de clases, métodos, variables, comentarios.
- **JavaDoc** en todas las clases y métodos públicos.
- **Principios SOLID** aplicados: en particular, SRP (clases pequeñas) y OCP (abierto a extensión, cerrado a modificación) en el procesamiento de intenciones.
- **Uso de `final`** donde tenga sentido.
- **Pruebas unitarias claras** con nombres descriptivos (`shouldReturnSaldoWhenSaldoIntention`).
- **Manejo de excepciones** adecuado, sin tragar excepciones.
- **Configuración externalizada** (URLs, timeouts) en `application.yml`.

## Checklist de cumplimiento (marca con [X] al final de tu respuesta)

- [ ] API REST diseñada y justificada en README.
- [ ] Endpoint POST /api/v1/chat implementado.
- [ ] Procesamiento de intenciones con patrón Strategy (extensible).
- [ ] Handlers para: saldo, tipo de cambio, info producto, recomendación.
- [ ] Integración con API Frankfurter para tipo de cambio, encapsulada en cliente propio.
- [ ] Manejo de fallos de API externa (fallback).
- [ ] Persistencia en H2 con entidad Conversation y repositorio JPA.
- [ ] Manejador global de excepciones con @ControllerAdvice.
- [ ] Pruebas unitarias para servicio y controlador (mínimo 4 tests).
- [ ] README con arquitectura, contratos, decisiones de diseño, diagramas Mermaid.
- [ ] Endpoint /metrics con métricas básicas (opcional pero incluido).
- [ ] Seguridad JWT opcional implementada (si se incluye).
- [ ] Explicación de escalabilidad y futuras integraciones en README.

## Instrucciones adicionales de verificación
- Asegúrate de que el proyecto se puede compilar y ejecutar sin errores (`mvn clean compile`, `mvn test`).
- No utilices claves reales; si alguna API las requiriera, indícalo como variable de entorno.
- Supón que el `customerId` se pasa en el request (no hay autenticación real a menos que implementes JWT).
- El endpoint `/chat` debe recibir un JSON con `{ "customerId": "123", "message": "¿cuál es el tipo de cambio de euro a dólar?" }` y devolver una respuesta con el mensaje del asistente.

## Nota final
El proyecto debe reflejar la experiencia de un arquitecto senior: no solo debe funcionar, sino que debe estar bien pensado para evolucionar, ser mantenible y seguir las mejores prácticas de la industria financiera. Asegúrate de cumplir con el checklist y proporciona todo el código necesario. ¡Adelante!
