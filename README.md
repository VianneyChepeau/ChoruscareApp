# Projet Open Innovation - Choruscare Application Mobile

## Contexte du projet

Le but du projet est de mettre en place une application mobile permettant de répertorier des paroles de chansons, de chants, de musiques. Une première version assez simple est presque prête et permettra de voir les différentes possibilités que l’on pourra ajouter à l’application.

## Versions

L’aboutissement du projet serai d’avoir 2 versions :
1 version qui serai gratuite. Le but de celle-ci est que tout le monde puisse avoir l’application et ajouter les paroles qu’il souhaite. Dans cette version, il n’y aura que certaines fonctionnalités accessibles, les autres étant disponibles dans la version payantes. SQLite permettra de stocker les différentes chansons en local sur le portable et nous permettra de nous affranchir d’une connexion internet pour cette version.
1 version payante. Celle-ci offrirait plus de possibilités à l’utilisateur. Une base de données distante permettra à l’utilisateur de stocker ses chansons sans être restreint à la capacité de son smartphone. La base de données local servira aussi afin de pouvoir sauvegarder les données s’il n’y a pas de réseau.

Voici la liste des fonctionnalités auxquelles nous avons pensé pour l’instant (toutes idées supplémentaires sont à réfléchir et pourquoi pas à ajouter) :
Liste de chansons (Page d’accueil)
Ajout d’une chanson
Ajout des paroles soit par détection de texte (librairie OpenCV) soit en écrivant le texte avec le clavier du portable
Internet pour fonctionner (version payante)
Partition de la musique pour les musiciens (version payante)
Lecture de la chanson au sein de l’application (version payante) 

## Modifications souhaitées

Certaines fonctionnalités n’ont pas encore été mises en place. A l’avenir, afin que l’application soit optimal et puisse fournir une expérience parfaite à l’utilisateur, il faudra mettre en place les fonctionnalités suivantes :
Détection de texte : Les formulaires ne sont pas quelque chose que l’utilisateur apprécie. De plus, s’il faut qu’il tape les paroles d’une chansons ou faire du copier coller, il va vite quitter l’application. Ainsi, il est préférable voir nécessaire de mettre en place de la détection de texte grâce à la librairie OpenCV.
Sauvegarde des données à distance : la mise en place d’un serveur permettant de stocker les données des différents utilisateurs permettra de pouvoir proposer un service payant mais efficace à l’utilisateur.
Mise à disposition de données : cette  fonctionnalité dépend de la précédente. Si l’utilisateur possède la version payante de l’application, on pourra lui fournir toutes une série de chansons déjà configurées avec les paroles et la partitions.
