# Prodotto
Periferico 

# Descrizione del prodotto
Software sviluppato in Java che gestisce la stazione di misura. 

Il software di stazione ha il compito di acquisire le misure e le informazioni di stato dagli analizzatori, le informazioni di stato della stazione stessa e del PC, di effettuare il calcolo delle aggregazioni (medie orarie, giornaliere, etc...) a partire dalle misure istantanee, di comunicare tali informazioni al centro operativo e di offrire un'interfaccia web per la consultazione di queste informazioni e per effettuare le operazioni di configurazione.

Il software del periferico puo' essere esguito su:
* un pc Linux
* un pc Windows
* un mini-computer Raspberry

# Configurazioni iniziali 
Per eseguire il periferico e' necessario assicurarsi che l'utente periferico abbia nel PATH la JAVA_HOME; in seguito si puo' eseguire lo script `run.sh` o `run.bat` (a seconda del sistema operativo su cui si esegue il periferico) dopo aver eventualmente personalizzato le seguenti opzioni nel file:
* `IP_PORT`: porta alla quale deve rispondere l'interfaccia grafica del periferico; la porta di default e' la 55000
* `COMEDI_ENABLE`: flag che indica se il periferico deve avviarsi con il supporto comedi (true) oppure no (false). Questa opzione è presente soltanto nel file `run.sh` poiche' su Windows il supporto alle librerie COMEDI non è supportato.

Ad esempio si puo' editare il file `run.sh` come segue:

```bash
>cd /home/periferico/periferico
>vi run.sh
```
e mettere ad esempio:

```bash
#### User configurable variables - BEGIN
IP_PORT=55001
COMEDI_ENABLE="false"
#### User configurable variables - END
```

salvare il file.

Lo script e' in grado di riconoscere automaticamente se e' necessario il supporto per le librerie COMEDI o no.

Dopo queste modifiche è possibile avviare il periferico in questo modo:

```bash
>cd /home/periferico/periferico
>./run.sh
```
Lo script richiede la password dell'utente periferico.

E' possibile consultare i log nell cartella `/home/periferico/periferico/log`

Se il software di stazione ha acquisito dati nel futuro a causa di un malfunzionamento dell'orologio interno e' possibile avviare il periferico con l'opzione per cancellare i dati nel futuro come segue:

```bash
>cd /home/periferico/periferico
>./run.sh -delete_future_data
```

Se il periferico ha gia' tutte le configurazioni degli analizzatori fatte e viene spostato in un'altra stazione e' possibile copiare la configurazione assegnando degli UUID nuovi avviando il periferico con l'opzione opportuna:

```bash
>cd /home/periferico/periferico
>./run.sh -as_new_station
```

# Getting Started 
Eseguire il target **release** di ANT (tramite OpenJDK 1.8).

Per impostare eventuali parametri tipici di ambienti di test o produzione e' possibile specificare la proprieta':
* `-Dtarget=prod`: per deployare su ambiente di produzione (file di properties `prod.properties`)

L'esecuzione di questo target crea un elenco di file tgz nella cartella `dist/prod` del workspace.


# Prerequisiti di sistema 
Fare riferimento al file BOM.csv per verificare l'elenco delle librerie esterne utilizzate in questo software.

Lo script di avvio `run.sh` supporta le seguenti combinazioni di Linux/Java (per AriaLinux)

* i386 con Java Sun 1.6
* x86_64 con Java OpenJDK 1.8
* x86_64 con Java OpenJDK > 1.8 e jaxb installato a parte
* armv7l con Java OpenJDK > 11 e jaxb installato a parte

Per l'installazione su Raspberry e' necessaria una Raspberry PI4 model B, >= 2GB RAM, >= 16GB uSD con Raspbian GNU/Linux 10 (buster).

# Installazione 

## Ambiente Linux
Creare sulla macchina su cui si vuole eseguire il software del periferico un utente linux chiamato `periferico` che abbia la cartella home.

Copiare i seguenti archivi creati con il target di ANT nella cartella `/home/periferico`

* periferico_bin_....tgz
* periferico_cfg_...tgz
* periferico_common_....tgz
* periferico_drv_libs_...tgz

Estrarre gli archivi come nell'esempio:

```bash
>tar -xvzf periferico_bin_V3.4.0_20211020.tgz
>tar -xvzf periferico_cfg_V3.4.0_20211020.tgz
>tar -xvzf periferico_common_V3.4.0_20211020.tgz
>tar -xvzf periferico_drv-libs_V3.4.0_20211020.tgz
```
In questo modo si creera' la cartella `/home/periferico/periferico` e questa conterra' tutto il software necessario.

## Ambiente Windows
Per l'instazzlazione su un pc Windows è necessario consultare il file `docs/windows-install.txt`

##  Raspberry
Per l'installazione su Raspberry è necessario consultare il file `docs/raspberry-install.txt`

## Avvio interfaccia grafica
L'interfaccia grafica del periferico è accessibile all'indirizzo:

`http://IP:porta/PerifericoUI.html`

ad esempio con i parametri di default:

[http://localhost:55000/PerifericoUI.html](http://localhost:55000/PerifericoUI.html) 

Per accedere è necessario inserire una password che si trova nel file di configurazione `conf/login.xml`

# Esecuzione dei test
Sono stati eseguiti test di vulnerabilità DAST e SAST e non sono state rilevate vulnerabilita' gravi.

# Versioning
Per il versionamento del software si usa la tecnica Semantic Versioning (http://semver.org).

# Authors
La lista delle persone che hanno partecipato alla realizzazione del software sono  elencate nel file AUTHORS.txt.

# Copyrights
L'elenco dei titolari del software sono indicati nel file Copyrights.txt

# License 
SPDX-License-Identifier: EUPL-1.2-or-later

Vedere il file LICENSE per i dettagli.