# SensorAlert

Application Android permettant de lire les données de capteurs de luminosité distants et d'établir un
système d'alerting sur les changements d'état des capteurs.

## Affichage

L'onglet principal présente la liste des capteurs en connectés à l'IoTLab ainsi que des informations générales.

![Onglet principal](https://raw.githubusercontent.com/tbagrel1/sensor_alert/master/.readme_resources/OngletPrincipal.png)

Chaque capteur est représenté sur une ligne avec différentes informations (de gauche à droite):

+ Lumière allumé ou éteinte :
    - ampoule allumée : luminosité de la pièce au dessus du seuil fixé
    - ampoule éteinte :  luminosité de la pièce en dessous du seuil fixé
+ Nom du capteur et/ou identifiant du capteur

![Capteurs](https://raw.githubusercontent.com/tbagrel1/sensor_alert/master/.readme_resources/CapteurAllumeCapteurEteint.png)

+ Le bouton permettant de nommer un capteur
(remarque : lorsqu'un capteur est nommé son identifiant reste affiché en plus petit en dessous du
nouveau nom. Le nouveau nom du capteur sera égalment utilisé lors des alertes par notification et par email)

![Renommage capteur](https://raw.githubusercontent.com/tbagrel1/sensor_alert/master/.readme_resources/RenommageCapteur.png)

+ Le niveau de luminosité relevé par le capteur
+ Le temps écoulé depuis la dernière valeur relevée par ce capteur

Il est possible d'obtenir l'historique des luminosités enregistrées pour un capteur donné.
L'historique se présente sous forme d'un graphique, il expose tout les niveaux de luminosité relevés
par un capteur lors des dernières 24 heures (au plus). Il s'obtient en cliquant sur la tuile d'un capteur (à n'importe endroit sauf sur le bouton de nommage).

![Graph](https://raw.githubusercontent.com/tbagrel1/sensor_alert/master/.readme_resources/Graph.png)

En bas à gauche de l'écran on trouve la durée écoulée depuis la dernière tentative de lecture de l'API.

En bas à droite de l'écran on trouve l'état de la dernière lecture de l'API :
+ "Success" si la dernière mise-à-jour des données a réussi
+ "Failure" sinon

![Indications generales](https://raw.githubusercontent.com/tbagrel1/sensor_alert/master/.readme_resources/IndicationsGenerales.png)

## Alerting

En plus de permettre l'affichage de l'état passé et présent des capteurs, l'application propose un
système d'alerting. Il se décompose en trois modes :

+ Vibration : si un capteur change d'état pendant la plage horaire de vibration, une vibration est produite sur le téléphone indiquant quel capteur a changé d'état et quel est son nouvel état. Une vibration est également produite si une erreur de lecture de l'API se produit pendant la plage horaire de vibration
+ Notification : Si un capteur change d'état pendant la plage horaire de notification, une notification est émise sur le téléphone indiquant quel capteur a changé d'état et quel est son nouvel état. Une notification est également émise si une erreur de lecture de l'API se produit pendant la plage horaire de notification
+ Email : Si un capteur change d'état pendant la plage horaire d'envoi d'email, un email est envoyé à l'adresse (ou aux adresses) définie(s) dans les paramètres. Il contient alors les informations suivantes : le nom (si défini) et l'identifiant du capteur ayant changé d'état, la date du changement, sa nouvelle valeur et son nouvel état

## Export des données

Le menu "Export" est accessible grâce au menu en haut à droite de l'écran.

Il permet d'exporter les données des capteurs enregistrées par l'application sur une période définie. Les données seront envoyées par mail à l'adresse mail spécifiées, au format JSON.

![Export](https://raw.githubusercontent.com/tbagrel1/sensor_alert/master/.readme_resources/Export.png)

## Préférences

Le menu "Settings" est accessible grâce au menu en haut à droite de l'écran.

Il permet de configurer les fonctionnalités décrites ci-dessus en définissant:

+ La plage horaire pendant laquelle une vibration sera produite en cas de changement d'état d'un
capteur ou d'une erreur de lecture de l'API

![Preference vibration](https://raw.githubusercontent.com/tbagrel1/sensor_alert/master/.readme_resources/PreferencesVibration.png)

+ La plage horaire pendant laquelle une notification sera émise en cas de changement d'état d'un
capteur ou d'une erreur de lecture de l'API

![Preference notification](https://raw.githubusercontent.com/tbagrel1/sensor_alert/master/.readme_resources/PreferencesNotification.png)

+ La plage horaire pendant laquelle un mail sera envoyé en cas de changement d'état d'un capteur

**Remarque** : Pour activer un des modes d'alerting en permanence (toute la journée) il faut suffit de choisir 00h00 comme heure de début et de fin pour ce mode.

+ Les adresses email auxquelles sera envoyée l'alerte de changement d'état d'un capteur

![Preference mail](https://raw.githubusercontent.com/tbagrel1/sensor_alert/master/.readme_resources/PreferencesMail.png)

+ Les informations de connexion au serveur SMTP
    - le nom d'hôte / adresse IP du serveur SMTP (par défaut : gmail.com)
    - le port du serveur SMTP (par défaut : 587)
    - l'utilisation ou non de StartTLS
    - le login du compte email envoyant les alertes et les données exportées par l'intermédiaire du SMTP
    - le mot de passe du compte email envoyant les alertes et les données exportées par l'intermédiaire du SMTP

**Remarque** : un compte email d'exemple sera fourni à l'évaluateur par un autre canal pour permettre le test de l'application

![Preference SMTP](https://raw.githubusercontent.com/tbagrel1/sensor_alert/master/.readme_resources/PreferencesSMTP.png)

+ La fréquence de rafraîchissement des données par lecture de l'API (Attention, pour les valeurs inférieures à la minute, le système Android peut décider de limiter la fréquence d'exécution du service en passant outre le paramètre choisi)
+ Le nom d'hôte / adresse IP de l'API
+ Le temps de conservation des données par l'application (ce qui peut limiter la quantité de données disponibles à l'export)
+ Le seuil de luminosité au dessus duquel une lumière sera considérée comme allumée (par défaut : 250)

![Préférence générale](https://raw.githubusercontent.com/tbagrel1/sensor_alert/master/.readme_resources/PreferencesGenerales.png)

## Scénario de test avec l'IoTLab Mock API

### Présentation de l'IoTLab Mock API

Comme le réseau de capteurs n'était pas complètement opérationnel dès le début du projet, et qu'il a arrêté de fournir des données à partir du 19 décembre 2020, nous avons mis en place une API mockant l'IOTLab déployé à TELECOM Nancy afin de réaliser facilement les tests de notre application.

L'IOTLab Mock API est disponible à l'adresse [github.com/tbagrel1/iotlab_mock_api](https://github.com/tbagrel1/iotlab_mock_api), et inclue un guide d'utilisation détaillée pour satisfaire aux besoins de ce projet. Il est fortement recommandé de l'utiliser pour tester notre application (sinon, comme les capteurs n'émettent plus de nouvelles valeurs de luminosité à l'heure actuelle, certaines fonctionnalités de notre application seront limitées, en particulier l'affichage des données détaillées de capteur sous forme de graph pour les dernières 24h).

Le moyen le plus simple d'utliser l'IOTLab Mock API est de cloner le *repository* depuis GitHub, puis d'ouvrir ce dernier avec IntelliJ IDEA Ultimate. Le projet suit une structure classique d'application Spring Boot ; il est par conséquent possible de l'utiliser sans IDE si besoin. Le lancement de l'API se fait en exécutant la méthode `main` de la classe `IotlabMockApiApplication`.

Une fois l'API lancée, il est possible d'utiliser le fichier de requêtes d'exemple `requests.http` situé à la racine pour exécuter directement depuis IntelliJ lesdites requêtes. Au lancement, l'API n'expose aucun capteur. Ajouter un capteur et modifier la valeur mesurée par un capteur se font tout deux par une requête `PUT` comme indiqué dans le `README.md` du projet. Sinon, il est également possible d'intéragir avec l'API par tout autre client HTTP (Postman, `curl`, `wget`...).

### Utilisation pour la démo de notre application

**Prérequis** :

+ Application Android lancée (il est conseillé de lancer l'application dans l'émulateur, pour pouvoir accéder facilement au log sur lequel nous ajoutons beaucoup d'informations de déboguage)
+ Paramètres réglés dans l'application :
    - temps de rafraîchissement de 15 secondes
    - Paramètres SMTP bien définis
    - Paramètres de notification bien définis
    - Racine de l'API : `http://10.0.2.2:8080`

Une fois les prérequis remplis, il est possible de lancer le script Bash `scenario.sh` situé à la racine du projet Android, qui va mettre-à-jour pendant 3 minutes les capteurs de l'IoTLab Mock API afin de simuler une évolution des valeurs mesurées par les capteurs et plusieurs changements d'états. Pendant l'éxécution, l'application Android devrait lire les données de l'API à plusieurs reprises, permettant de tester toutes les fonctionnalités de l'application :

+ le bon affichage des données à l'écran et leur mise à jour régulière.
+ l'affichage de l'historique de luminosité d'un capteur en cliquant dessus.
+ l'export des données
+ le système d'alerting par vibration, notification et email

## Auteurs

+ Thomas BAGREL [<thomas.bagrel@telecomnancy.eu>](mailto:thomas.bagrel@telecomnancy.eu)
+ Timothée ADAM [<timothee.adam@telecomnancy.eu>](mailto:timothee.adam@telecomnancy.eu)