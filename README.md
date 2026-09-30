<a name="readme-top"></a>

<div align="center">
  <img src="_assets/images/logo01.png" width="300" alt="Logo">
  <img src="_assets/images/logo02.png" width="300" alt="Logo">
</div>

# xtr-toolkit-hosts

Quando lavori con decine di host sparsi fra applicazioni e ambienti diversi,
tenere in ordine il file `hosts` a mano diventa scomodo e si sbaglia facilmente. xtr-toolkit-hosts è
un'applicazione desktop JavaFX che raggruppa gli host per applicazione e
ambiente e te li fa modificare da un'interfaccia grafica.

> Stato: versione multipiattaforma di riferimento, non più sviluppata attivamente (ultime modifiche al codice nel 2024). Lo sviluppo attivo è in `xtr-toolkit-hosts-macos`.

## Info sul progetto

È nato come mio banco di prova personale, per sperimentare tecnologie e
framework moderni su un problema reale, ed è uno dei moduli di una serie più
ampia che mi piacerebbe crescesse anche con i contributi della community.

### Versione Java e versione macOS

Questa è la versione multipiattaforma (Linux, macOS, Windows). Da qui è nata
[`xtr-toolkit-hosts-macos`](https://github.com/XtremeAlex/xtr-toolkit-hosts-macos),
un'app nativa per Mac oggi molto più avanti: scrittura sicura di `/etc/hosts`
con backup datati e ripristino, audit, gestione dei load balancer, politiche
via MDM. Su Mac conviene usare quella; su Windows e Linux questa resta la
versione di riferimento.

## Stack

- Java 17
- JavaFX 21
- Maven (jpackage)
- Linux, macOS, Windows

## Per iniziare
Il progetto usa Maven per dipendenze e build, ed è sviluppato con Java 17 e
JavaFX: puoi avviarlo e provarlo in locale.

### Prerequisiti

- Git (>= 2.43)
- Java OJDK (GraalVM versione 17)
- Maven (Apache Maven >= 3.9.6)
- JavaFX 21

### Struttura del progetto

```
.
├── pom.xml
└── src
    ├── main
    │   ├── java
    │   │   └── com
    │   │       └── xtremealex
    │   │           └── toolkit
    │   │               └── hosts
    │   │                   ├── IOHostParser.java
    │   │                   ├── MainHostsApp.java
    │   │                   ├── models
    │   │                   │   ├── App.java
    │   │                   │   ├── Host.java
    │   │                   │   └── HostType.java
    │   │                   └── mvp
    │   │                       ├── MusicPlayer.java
    │   │                       ├── controllers
    │   │                       │   ├── AppCell.java
    │   │                       │   ├── HostEditCell.java
    │   │                       │   ├── HostListCell.java
    │   │                       │   ├── IMainViewController.java
    │   │                       │   ├── ModalController.java
    │   │                       │   └── impl
    │   │                       │       └── MainViewControllerImpl.java
    │   │                       └── views
    │   │                           └── presenter
    │   │                               ├── IMainPresenter.java
    │   │                               └── impl
    │   │                                   └── MainPresenterImpl.java
    │   └── resources
    │       ├── css
    │       │   └── styles.css
    │       ├── fonts
    │       │   ├── Comfortaa
    │       │   ├── OpenSans
    │       │   ├── Overpass
    │       │   └── Roboto
    │       ├── fxml
    │       │   └── MainView.fxml
    │       ├── images
    │       └── music
    │           └── background.wav
    └── test
        └── java
```

### Clonare e compilare

1. Clona il repository:
   ```bash
   git clone https://github.com/XtremeAlex/xtr-toolkit-hosts.git
   cd xtr-toolkit-hosts
   ```

2. Compila e crea l'eseguibile con jpackage, scegliendo il profilo della tua piattaforma:
   ```bash
   # macOS (Apple Silicon)
   mvn clean package jpackage:jpackage -Pmac-aarch64

   # Windows
   mvn clean package jpackage:jpackage -Pwindows
   ```
   <img src="_assets/images/mvn-build.png" alt="Build Maven"/>

3. Trovi l'eseguibile in `./target/jpackage`.

### Formato del file hosts

L'app legge il file `hosts` a partire dalla riga `##start-xtr-toolkit-host`:
ricordati di aggiungerla, tutto ciò che sta sopra viene ignorato.

Sotto il marcatore gli host si organizzano così (qui con segnaposto al posto
di host reali):

```
#TEST CLOUD
#APP: TEST KIBANA COLL
#LB: <hostname>
0.0.0.0 <dominio>
```

## Roadmap

- [ ] Testare il tutto e riportare i risultati

Nelle [open issues](https://github.com/XtremeAlex/xtr-toolkit-hosts/issues) trovi l'elenco completo delle funzionalità proposte e dei bug noti.

## Come contribuire

Ogni contributo è benvenuto.

1. Fai un fork del progetto
2. Crea il tuo feature branch (`git checkout -b feature/nome-feature`)
3. Fai commit delle modifiche (`git commit -m "Aggiunge nome-feature"`)
4. Fai push sul branch (`git push origin feature/nome-feature`)
5. Apri una Pull Request

Hai un'idea? Apri pure una issue con il tag giusto. E se il progetto ti è utile, lascia una stella.

## Licenza
Distribuito sotto licenza Apache 2.0. Vedi il file [`LICENSE`](LICENSE) per i dettagli.

## Contatti

Andrei Alexandru Dabija (XtremeAlex) · [alexdabi92@gmail.com](mailto:alexdabi92@gmail.com) · [2ad.bubume.it](https://2ad.bubume.it/) · [LinkedIn](https://www.linkedin.com/in/andrei-alexandru-dabija/) · [github.com/XtremeAlex](https://github.com/XtremeAlex)
