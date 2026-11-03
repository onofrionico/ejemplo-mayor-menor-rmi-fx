# Mayor o Menor — ejemplo JavaFX para POO UNLu

Juego de cartas de ejemplo para la **autoguía de la Clase 11** de Programación Orientada a Objetos (UNLu): cómo pasar de un modelo ya armado a una interfaz gráfica con **JavaFX + FXML**, aplicando **MVC** y **Observer**.

Se muestra una carta del mazo español; apostás si la siguiente va a ser **mayor** o **menor**. Si acertás sumás un punto; si fallás (o sale una carta del mismo número) se termina la partida. Si se acaba el mazo, ganaste.

## Requisitos

- JDK 17 o superior (recomendado: **JDK 21**). Con IntelliJ no hace falta instalarlo aparte: `File → Project Structure → Project → SDK → Download JDK...`. Para usar `mvnw` desde una terminal sí necesitás un JDK instalado en el sistema (por ejemplo [Eclipse Temurin](https://adoptium.net/temurin/releases/?version=21), que es un JDK, no el IDE Eclipse).
- No hace falta instalar Maven ni descargar JavaFX: el proyecto trae el *Maven Wrapper* (`mvnw`) y Maven baja JavaFX solo.

## Cómo correrlo

```bash
./mvnw javafx:run
```

En Windows: `.\mvnw.cmd javafx:run` en PowerShell, o `mvnw.cmd javafx:run` en cmd.

Desde IntelliJ: abrí la carpeta como proyecto Maven y ejecutá la clase `Lanzador`.

Tests (modelo y controlador, sin interfaz gráfica): `./mvnw test`.

## El proyecto paso a paso

La historia del repo está armada para recorrerla con la autoguía. Cada paso tiene un *tag*: con `git checkout paso-3` ves el proyecto tal como quedó en ese paso (volvé al final con `git checkout main`).

| Tag | Qué agrega |
|---|---|
| `paso-0` | Proyecto Maven con JavaFX: una ventana mínima para verificar la instalación. |
| *(demos)* | Cuatro demos de componentes: layouts, menús, eventos y diálogos (paquete `demos`). |
| `paso-1` | Modelo del juego (`Carta`, `Mazo`, `Partida`) y Observer (`IObservable`/`IObservador`), sin JavaFX, con tests. |
| `paso-2` | Primera ventana en FXML (`menu.fxml` + `MenuController`). |
| `paso-3` | Botones con imagen y las imágenes de las cartas. |
| `paso-4` | MVC + Observer: el `Controlador` observa a la `Partida` y la carta se actualiza sola. |
| `paso-5` | Cambio de escena: menú → mesa → resultado → menú. |
| `paso-6` | Barra de menú, diálogo modal para abandonar y ventana no modal de reglas. |
| `paso-7` | Estilo con CSS. |

### Demos de componentes

Desde IntelliJ: abrí `Lanzador`, comentá la línea del juego, descomentá la demo que quieras ver y ejecutá `Lanzador` con ▶. No corras la demo con ▶ desde su propia clase: da `JavaFX runtime components are missing`.

Desde una terminal:

```bash
./mvnw javafx:run -Djavafx.mainClass=ar.edu.unlu.mayormenor.demos.DemoLayouts
./mvnw javafx:run -Djavafx.mainClass=ar.edu.unlu.mayormenor.demos.DemoMenus
./mvnw javafx:run -Djavafx.mainClass=ar.edu.unlu.mayormenor.demos.DemoEventos
./mvnw javafx:run -Djavafx.mainClass=ar.edu.unlu.mayormenor.demos.DemoDialogos
```

En PowerShell, el `-D...` va entre comillas:

```powershell
.\mvnw.cmd javafx:run "-Djavafx.mainClass=ar.edu.unlu.mayormenor.demos.DemoLayouts"
```

## Estructura

```
src/main/java/ar/edu/unlu/mayormenor/
├── App.java, Lanzador.java   # arranque
├── modelo/                   # el juego (sin JavaFX)
├── observer/                 # IObservable, IObservador
├── controlador/              # Controlador MVC (observa al modelo)
├── vista/                    # IVista, VistaGrafica y controladores de cada FXML
└── demos/                    # demos de componentes
src/main/resources/
├── fxml/                     # menu.fxml, juego.fxml, resultado.fxml
├── img/                      # cartas e íconos
└── styles/juego.css
```

## Créditos de imágenes

- **Cartas** (`src/main/resources/img/cartas/`): mazo de Heraclio Fournier de 1878, de Ignacio Díaz Olano, Heraclio Fournier González y Emilio Soubrier. **Dominio público.** Fuente: [Wikimedia Commons — Heraclio Fournier’s 1878 card deck](https://commons.wikimedia.org/wiki/Category:Heraclio_Fournier%E2%80%99s_1878_card_deck). Reducidas a 200 px de ancho y renombradas (`espada-1.png`, `reverso.png`, ...).
- **Íconos** de los botones (`src/main/resources/img/iconos/`): generados por el equipo docente de la asignatura.
