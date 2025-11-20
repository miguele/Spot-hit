# SpotHit Android (Kotlin + Jetpack Compose)

Aplicación Android que replica el flujo de Spot-Hit usando Jetpack Compose y una arquitectura sencilla basada en casos de uso. Todo el código está escrito en Kotlin para facilitar la extensión y despliegue en Android Studio.

## Puntos clave
- Arquitectura limpia: repositorio, casos de uso y `ViewModel` desacoplados.
- UI con Jetpack Compose y navegación declarativa.
- Repositorio en memoria para poder iterar sin dependencias externas.
- Sin assets binarios: evita el error `binary_files_not_supported` al crear el PR.

## Estructura
- `app/src/main/kotlin/com/spothit/core`: modelos, repositorio y casos de uso.
- `app/src/main/kotlin/com/spothit/ui`: pantallas Compose y navegación.
- `MainActivity` y `SpotHitApp`: punto de entrada y tema.

## Ejecutar
1. Abre el proyecto en Android Studio Hedgehog o superior.
2. Usa JDK 17.
3. Sincroniza Gradle y lanza la app en un emulador o dispositivo con Android 7.0+ (API 24).

## Próximos pasos sugeridos
- Sustituir el repositorio en memoria por uno conectado a tu backend/Firebase.
- Añadir reproducción real de Spotify y pagos IAP respetando SOLID a través de interfaces.
- Incluir pruebas de UI con `ui-test-junit4`.
