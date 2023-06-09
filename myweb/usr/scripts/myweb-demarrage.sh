#!/bin/bash

# On vérifie que le fichier existe, et si ce n'est pas le cas on le crée 
# et on le met à une valeur par défaut 0 pour être sûr que la compilation 
# va bien être faite. 
# Cette condition va servir à éviter un bug juste après que l'installation
# des sources soit faite mais que personne n'ait encore compilé.
if [ $(ls -l /var/log/myweb/ | grep logPoids.txt | wc -l) -eq 0 ]
then 
    echo 0 | sudo tee /usr/local/sbin/myweb/Ressources/logs/logPoids.txt
fi

# Je stocke le poids total enregistré dans le fichier log dans une variable logPoids
logPoids=$(cat /var/log/myweb/logPoids.txt)
# Je stocke le poids total actuel des fichiers sources dans une autre variable poidsSrcActuel
poidsSrcActuel=$(ls -l /usr/local/sbin/myweb/src | grep ".java" | awk '{sum += $5} END {print sum}')
echo Le poids total des sources enregistré dans le fichier de log : $logPoids octets
echo Le poids total des sources calculé à linstant : $poidsSrcActuel octets

if [ "$logPoids" != "$poidsSrcActuel" ]
then
    echo "Les sources se compilent"
    sudo javac -classpath /usr/local/sbin/myweb/jsoup-1.16.1.jar /usr/local/sbin/myweb/src/*.java
fi

# Je lance le fichier HTTPServer (je lance avec l'option -cp pour donner la localisation de la librairie dont j'ai besoin avant le : 
# et la localisation du dossier contenant la classe à exécuter après le : 
# Enfin je donne le nom de la classe à exécuter 
sudo java -classpath /usr/local/sbin/myweb/jsoup-1.16.1.jar:/usr/local/sbin/myweb/src HTTPServer

exit 0

