# Mayor o Menor en red — ejemplo de RMI para POO UNLu

Juego de cartas de ejemplo para la **autoguía de la Clase 12** de Programación Orientada a Objetos (UNLu): cómo llevar un juego hecho con **JavaFX + MVC + Observer** a **cliente-servidor con RMI**, usando la librería `LibreriaRMIMVC` de la asignatura.

Este repositorio **arranca donde terminó el de la Clase 11**: el primer commit es una copia del estado final de [ejemplo-mayor-menor-fx](https://github.com/onofrionico/ejemplo-mayor-menor-fx) (commit `4486177`), y cada commit posterior muestra solo los cambios para aplicar RMI. El repositorio de la Clase 11 sigue intacto.

## El juego

Un servidor tiene **una sola partida**, y cada jugador la juega desde su propio cliente (su propia ventana).

1. Cada jugador entra a la **sala de espera** con su nombre. El primero que entra es el **admin**: es el único que puede empezar la partida (puede hacerlo aunque esté solo).
2. Todos ven la misma carta. En su turno, cada jugador apuesta si la próxima va a ser **mayor** o **menor**. Los demás ven los botones bloqueados y "Esperando a…".
3. Si acertás seguís vivo; si fallás (o sale el mismo número) quedás **eliminado**. El turno pasa al siguiente jugador vivo después de cada apuesta.
4. **Gana el último que queda vivo.** Si se acaba el mazo, ganan todos los que siguen vivos. Jugando de a uno es el juego de la Clase 11.

## Requisitos

Los mismos que en la Clase 11: un JDK 17 o superior (recomendado: **JDK 21**) e **IntelliJ IDEA**. Con IntelliJ no hace falta instalar el JDK aparte (`File → Project Structure → Project → SDK → Download JDK...`), ni Maven, ni JavaFX.

La librería RMIMVC viene en `lib/LibreriaRMIMVC.jar` y se declara en el `pom.xml` como dependencia `system`: no hay que descargar nada más.

## Cómo correrlo (desde IntelliJ)

El proyecto trae dos configuraciones de ejecución (carpeta `.run/`), que aparecen solas en el menú de configuraciones, arriba a la derecha:

1. **Servidor**: ejecutala primero. Aceptá la IP y el puerto que propone (`8888`). Queda abierta una ventanita que dice "Servidor corriendo".
2. **Cliente**: ejecutala una vez por cada jugador. Tiene activada la opción *Allow multiple instances*, así que podés abrir varias. En los diálogos aceptá los valores que propone: si ya hay otro cliente abierto en la misma PC, propone otro puerto solo.

Para jugar entre varias PCs conectadas al mismo router o wifi, en el servidor elegí la **IP privada que el router le dio a esa PC** (casi siempre empieza con `192.168.` o `10.`, por ejemplo `192.168.0.15`; en Windows la ves con `ipconfig`, como "Dirección IPv4" del adaptador de wifi o Ethernet), no `127.0.0.1`. En cada cliente elegí la IP privada de esa PC y escribí la del servidor como "IP del servidor".

Las configuraciones arrancan desde `LanzadorServidor` y `LanzadorCliente`. Si corrés directamente `AppServidor` o `AppCliente`, Java puede quejarse con `JavaFX runtime components are missing`.

Tests (modelo y controlador, sin interfaz gráfica ni red): ejecutá la carpeta `src/test/java` con ▶ (o `./mvnw test`).

## El proyecto paso a paso

Cada paso tiene un *tag*: en IntelliJ, `Git → Branches...`, elegí el tag `paso-3` y **Checkout** para ver el proyecto tal como quedó en ese paso (volvé al final con `main`).

| Tag | Qué cambia |
|---|---|
| `paso-0` | Punto de partida: el juego de la Clase 11, tal cual. |
| `paso-1` | Se agrega la librería RMIMVC (`lib/` + `pom.xml`). |
| `paso-2` | Modelo remoto: `IPartida`, `Partida extends ObservableRemoto`, `Carta` serializable. |
| `paso-3` | Controlador remoto: `IControladorRemoto`, `setModeloRemoto`, errores en la vista. |
| `paso-4` | La vista actualiza la pantalla con `Platform.runLater`. |
| `paso-5` | `AppServidor` y `AppCliente`: dos ventanas comparten la misma partida. |
| `paso-6` | Sala de espera y admin. |
| `paso-7` | Turnos y eliminación para N jugadores. |
| `paso-8` | Al cerrar la ventana, el jugador sale de la partida. |

## Estructura

```
src/main/java/ar/edu/unlu/mayormenor/
├── modelo/        # la partida (sin JavaFX): IPartida, Partida, Jugador, Carta, Mazo...
├── controlador/   # Controlador MVC (controlador remoto: observa a la partida)
├── vista/         # IVista, VistaGrafica y controladores de cada FXML
├── servidor/      # AppServidor + LanzadorServidor
└── cliente/       # AppCliente + LanzadorCliente
src/main/resources/
├── fxml/          # sala.fxml, juego.fxml, resultado.fxml
├── img/           # cartas e íconos
└── styles/juego.css
lib/LibreriaRMIMVC.jar
.run/              # configuraciones de IntelliJ: Servidor y Cliente
```

## Limitaciones

Es un ejemplo para aprender, no un juego robusto en red:

- Si un cliente se cierra de golpe (por ejemplo, matando el proceso o con el botón Stop de IntelliJ) sin pasar por la ventana, el servidor no se entera y los avisos a los demás pueden fallar. Cerrá cada cliente con la X de su ventana.
- Si se cae el servidor, los clientes muestran un error pero no se reconectan.

## Créditos de imágenes

- **Cartas** (`src/main/resources/img/cartas/`): mazo de Heraclio Fournier de 1878, de Ignacio Díaz Olano, Heraclio Fournier González y Emilio Soubrier. **Dominio público.** Fuente: [Wikimedia Commons — Heraclio Fournier’s 1878 card deck](https://commons.wikimedia.org/wiki/Category:Heraclio_Fournier%E2%80%99s_1878_card_deck). Reducidas a 200 px de ancho y renombradas (`espada-1.png`, `reverso.png`, ...).
- **Íconos** de los botones (`src/main/resources/img/iconos/`): generados por el equipo docente de la asignatura.
