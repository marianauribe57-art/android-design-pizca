# Pizca — Arquitectura y flujo de ejecución

Este documento explica cómo está organizada la app **Pizca**, qué hace cada archivo y cómo viajan los datos desde que se abre la app hasta que el usuario cocina con Chef Teo.

## 1. Estructura del proyecto

```text
app/src/main/java/com/adso/pizca/
│
├── data/                                <-- CAPA DE DATOS
│    ├── network/                        (.gitkeep - preparado para Retrofit)
│    ├── api/                            (.gitkeep - preparado para TheMealDB, USDA y Gemini)
│    ├── dto/                            (modelos de datos)
│    │    ├── Receta.kt                  (Receta + Ingrediente)
│    │    ├── Mensaje.kt                 (mensajes del chat con Chef Teo)
│    │    └── PerfilUsuario.kt           (perfil, Objetivo y MetaDiaria)
│    └── repository/
│         └── RecetaRepository.kt        (recetas de prueba mientras llegan las APIs)
│
├── presentation/                        <-- CAPA DE PRESENTACIÓN
│    ├── viewmodel/                      (estado de cada pantalla con StateFlow)
│    │    ├── InicioViewModel.kt
│    │    ├── DetalleViewModel.kt
│    │    ├── BusquedaViewModel.kt
│    │    ├── ChefViewModel.kt
│    │    └── PerfilViewModel.kt
│    ├── navigation/                     (navegación type-safe con @Serializable)
│    │    ├── Destinations.kt
│    │    └── AppNavigation.kt
│    ├── screens/                        (pantallas Jetpack Compose)
│    │    ├── InicioScreen.kt
│    │    ├── DetalleScreen.kt
│    │    ├── BusquedaScreen.kt
│    │    ├── ChefScreen.kt
│    │    └── PerfilScreen.kt
│    └── components/                     (componentes de UI reutilizables)
│         ├── RecetaCard.kt
│         ├── StatCard.kt
│         ├── ChipPildora.kt
│         ├── IconoReceta.kt
│         └── ChefTeoImagen.kt
│
├── ui/theme/                            <-- TEMA (MATERIAL 3, PALETA PASTEL)
│    ├── Color.kt
│    ├── Theme.kt
│    └── Type.kt
│
└── MainActivity.kt                      <-- PUNTO DE ENTRADA

docs/                                    <-- DOCUMENTACIÓN
├── tutorial_arquitectura.md             (este archivo)
└── capturas/                            (imágenes del README)
```

## 2. Diagrama del flujo

![Flujo de navegación de Pizca](capturas/flujo-pizca.png)

## 3. Archivo por archivo

### 3.1 Capa de datos (`data/`)

| Archivo | Qué hace |
|---|---|
| `dto/Receta.kt` | Modelo de una receta: id, nombre, categoría, país, ingredientes (con calorías y proteínas) y pasos. |
| `dto/Mensaje.kt` | Un mensaje del chat: texto y si lo escribió Chef Teo o el usuario. |
| `dto/PerfilUsuario.kt` | Datos del usuario (peso, estatura, edad, sexo), su `Objetivo` y la `MetaDiaria` calculada. |
| `repository/RecetaRepository.kt` | Entrega las recetas (`obtenerRecetas()`) y busca una por id (`buscarPorId(id)`). Hoy usa datos de prueba; luego llamará a las APIs. |
| `network/` y `api/` | Carpetas preparadas para Retrofit y los endpoints de TheMealDB, USDA y Gemini. |

### 3.2 ViewModels (`presentation/viewmodel/`)

Cada ViewModel guarda el estado de su pantalla en un `MutableStateFlow` privado y lo expone como `StateFlow` de solo lectura. La pantalla lo lee con `collectAsState()` y se vuelve a dibujar sola cuando el valor cambia.

| ViewModel | Estado que maneja | Acciones |
|---|---|---|
| `InicioViewModel` | recetas, categoría seleccionada | `seleccionarCategoria()` |
| `DetalleViewModel` | receta, raciones, favoritas | `cargarReceta()`, `sumarRacion()`, `restarRacion()`, `cambiarFavorita()` |
| `BusquedaViewModel` | modo, texto, ingredientes, resultados | `cambiarModo()`, `cambiarTexto()`, `agregarIngrediente()`, `quitarIngrediente()` |
| `ChefViewModel` | receta, paso actual, mensajes | `cargarReceta()`, `siguientePaso()`, `decirIngredientes()`, `enviarMensaje()` |
| `PerfilViewModel` | perfil, meta diaria, error | `calcular()` |

### 3.3 Componentes reutilizables (`presentation/components/`)

| Componente | Qué dibuja | Dónde se usa |
|---|---|---|
| `RecetaCard` | Tarjeta de receta con icono, nombre, país · categoría y flecha | Inicio |
| `StatCard` | Tarjeta de un dato con número grande (kcal, proteína, metas) | Detalle y Perfil |
| `ChipPildora` | Chip redondo que se pone rosado al seleccionarlo | Inicio y Perfil |
| `IconoReceta` | Cuadrito de color según la categoría | RecetaCard y Búsqueda |
| `ChefTeoImagen` | La imagen de Chef Teo | Inicio, Detalle, Búsqueda y Chef |

### 3.4 Navegación (`presentation/navigation/`)

- **`Destinations.kt`**: todas las rutas dentro de `sealed interface Destination`. `Inicio`, `Busqueda` y `Perfil` son `data object` (no llevan datos). `Detalle(recetaId)` y `Chef(recetaId?)` son `data class` porque llevan el id de la receta.
- **`AppNavigation.kt`**: crea el `NavController`, la barra inferior de 4 pestañas y el `NavHost` que conecta cada `Destination` con su pantalla. Con `toRoute()` recupera el `recetaId` que viaja en la ruta. La barra se oculta en el Detalle.

Las pantallas no reciben el `NavController`: reciben eventos como `onRecetaClick`, `onVolver` y `onCocinar`. Así cada pantalla no depende de la navegación.

### 3.5 Punto de entrada (`MainActivity.kt`)

Activa `enableEdgeToEdge()`, aplica `PizcaTheme` y llama a `AppNavigation()`.

## 4. Caso práctico: de Inicio a cocinar con Chef Teo

1. **Apertura:** `MainActivity` carga `AppNavigation()`, que muestra `InicioScreen` (`startDestination = Destination.Inicio`).
2. **Filtrar:** el usuario toca la categoría "Pollo". Se llama a `InicioViewModel.seleccionarCategoria("Pollo")`, el `StateFlow` de recetas emite la lista filtrada e `InicioScreen` se redibuja con `RecetaCard`.
3. **Ir al detalle:** el usuario toca "Arroz con Pollo". `RecetaCard` llama a `onRecetaClick(id)` y `AppNavigation` ejecuta `navController.navigate(Destination.Detalle(id))`.
4. **Recibir el dato:** `AppNavigation` recupera el id con `entrada.toRoute<Destination.Detalle>()` y se lo pasa a `DetalleScreen`.
5. **Cargar la receta:** `DetalleScreen` llama a `DetalleViewModel.cargarReceta(id)`, que la busca con `RecetaRepository.buscarPorId(id)`.
6. **Cambiar raciones:** con los botones − y + cambia `raciones` y las dos `StatCard` recalculan kcal y proteína.
7. **Cocinar:** el botón "Cocinar con Chef Teo" ejecuta `navController.navigate(Destination.Chef(id))`. `ChefScreen` carga la receta y Chef Teo guía paso a paso con `siguientePaso()`.

## Conclusión

- **Capas separadas:** datos (`data`), presentación (`presentation`), tema (`ui`) y documentación (`docs`).
- **Navegación type-safe:** las rutas son clases, así que un error de ruta se detecta al compilar y no al usar la app.
- **Componentes reutilizables:** `RecetaCard`, `StatCard` y `ChipPildora` evitan repetir código entre pantallas.
- **Estado reactivo:** `StateFlow` + `collectAsState()` mantienen la pantalla sincronizada con los datos.