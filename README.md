# Mayor o Menor en red — ejemplo de RMI para POO UNLu

Juego de cartas de ejemplo para la **autoguía de la Clase 12** de Programación Orientada a Objetos (UNLu): cómo llevar un juego hecho con **JavaFX + MVC + Observer** a **cliente-servidor con RMI**, usando la librería `LibreriaRMIMVC` de la asignatura.

Este repositorio **arranca donde terminó el de la Clase 11**: el primer commit es una copia del estado final de [ejemplo-mayor-menor-fx](https://github.com/onofrionico/ejemplo-mayor-menor-fx) (commit `4486177`), y cada commit posterior muestra solo los cambios para aplicar RMI. El repositorio de la Clase 11 sigue intacto.

## La librería RMIMVC

Está en `lib/LibreriaRMIMVC.jar` y se declara en el `pom.xml` como dependencia `system`. No hay que descargar nada más.

## Créditos de imágenes

- **Cartas** (`src/main/resources/img/cartas/`): mazo de Heraclio Fournier de 1878, de Ignacio Díaz Olano, Heraclio Fournier González y Emilio Soubrier. **Dominio público.** Fuente: [Wikimedia Commons — Heraclio Fournier’s 1878 card deck](https://commons.wikimedia.org/wiki/Category:Heraclio_Fournier%E2%80%99s_1878_card_deck). Reducidas a 200 px de ancho y renombradas (`espada-1.png`, `reverso.png`, ...).
- **Íconos** de los botones (`src/main/resources/img/iconos/`): generados por el equipo docente de la asignatura.
