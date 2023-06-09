Ce dossier contient bien 
- Un fichier ou archive de source java (./src)
- Un fichier de configuration (./src/config.xml)
- Les scripts créés pour la partie debian (./Scripts) qui sont indépendant de ce dépôt mais mis puisqu'ils sont demandés
- Le fichier binôme.txt

## Le projet est réalisé sous JDK 17

La version contenant le package debian n'est pas dans ce dossier puisque ce dossier, y compris le fichier config.xml est configuré de sorte à ce qu'on puisse directement lancer le serveur via ces deux commandes :
-- sudo javac -cp jsoup-1.16.1.jar src/*.java
-- sudo java -cp jsoup-1.16.1.jar:src HTTPServer

Ce dossier est donc un projet IntelliJ Idea, il y a donc un dossier Ressources contenant les fichiers web. Un dossier supposé contenir les logs (vide lors du premier téléchargement)

Voici donc ce que contient ce dossier et ce à quoi ça sert

- binome.txt -- Fichier contenant nos deux noms
- Javadoc -- Dossier contenant la javadoc du code générée par IntelliJ 
- jsoup-1.16.1.jar -- Fichier qui regroupe la librairie Jsoup
- Rapport -- Dossier qui contient le diagramme de classe (.puml / .png) ainsi que notre rapport au format pdf
- README.txt -- Ce fichier qui contient toutes les informations sur le rendu
- Ressources -- Dossier qui contient donc les ressources web (.html /images)
- Scripts -- Les scripts que nous avons écrits
- src -- Le dossier qui comporte les sources java ainsi que config.XML (qui n'est pas à modifier au niveau des chemins, car tout est configuré pour que ça marche via chemins relatifs)
- tests -- Dossier contenant des tests que nous avons faits sur certaines méthodes juste pour vérifier certaines situations

Encore désolés du retard pour le rendu...

