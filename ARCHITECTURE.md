# Architecture Overview

This project follows a layered structure to keep responsibilities and dependencies clear:

- **Domain layer (`app/src/main/kotlin/com/spothit/core`)**
  - Owns business models, use cases, and interfaces (e.g., repositories, auth/session gateways, real-time sockets).
  - No platform or framework dependencies; other layers depend on these interfaces.
- **Data layer (`app/src/main/kotlin/com/spothit/network`, `app/src/main/kotlin/com/spothit/auth`)**
  - Implements domain interfaces using Retrofit, WebSocket, storage, and platform APIs.
  - May define DTOs/mappers but should not leak concrete networking/storage classes to higher layers.
- **Presentation layer (`app/src/main/kotlin/com/spothit/ui`, view models/controllers)**
  - Consumes domain interfaces via dependency injection and avoids direct knowledge of data implementations.

### Dependency Direction
Presentation ➜ Domain interfaces ➜ Data implementations.

- WebSocket, Retrofit services, and authentication managers should be referenced through domain interfaces (`LobbySocketClient`, `AuthSessionManager`, repositories, etc.).
- Data implementations can be swapped or mocked by providing different bindings in DI (e.g., `AppContainer`).
- When adding new features, introduce contracts in the domain layer first, then provide data implementations and inject them into presentation code.
