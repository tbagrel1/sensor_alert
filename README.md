# SensorAlert

Application Android permettant de lire les données de capteurs distant de luminosité et d'établir un
système d'alerting sur les changements d'état des capteurs.

## Affichage

L'onglet principal présente la liste des capteurs en marche connectés à l'IoTLab et des informations générales.

![Onglet principal](https://raw.githubusercontent.com/tbagrel1/sensor_alert/master/.readme_resources/OngletPrincipal.png)

Chaque capteur est représenté sur une ligne avec différentes informations (de gauche à droite):
+ Lumière allumé ou éteinte :
    - ampoule allumée : luminosité de la pièce au dessus du seuil
    - ampoule éteinte :  luminosité de la pièce en dessous du seuil
+ Identifiant/nom du capteur (par défaut son adresse)

![Capteurs](https://raw.githubusercontent.com/tbagrel1/sensor_alert/master/.readme_resources/CapteurAllumeCapteurEteint.png)

+ Le bouton permettant de modifier l'identifiant/nom du capteur
(remarque : lorsqu'un capteur est renommé son identifiant reste affiché en plus petit en dessous du
nouveau nom. Le nouveu nom du capteur sera celui utilisé lors des alertes par notification et par mail.)

![Renommage capteur](https://raw.githubusercontent.com/tbagrel1/sensor_alert/master/.readme_resources/RenommageCapteur.png)

+ Le niveau de luminosité relevé par le capteur
+ Le temps écoulé depuis la dernière mise à jour du capteur


Il est possible d'obtenir l'historique des luminosités enregistrées pour un capteur donné.
L'historique se présente sous forme d'un graphique, il expose tout les niveaux de luminosité relevés
par un capteur lors des dernières 24 heures. Il s'obtient en cliquant sur un capteur.

![Graph](https://raw.githubusercontent.com/tbagrel1/sensor_alert/master/.readme_resources/Graph.png)

En bas à gauche de l'écran on trouve la durée depuis la mise à jour la plus récente.
En bas à droite de l'écran on trouve l'état de la dernière mise à jour :
+ "success" si la dernière mise à jour a été possible.
+ "failure" sinon.

![Indications generales](https://raw.githubusercontent.com/tbagrel1/sensor_alert/master/.readme_resources/IndicationsGenerales.png)

## Alerting

En plus de permettre l'affichage de l'état passé et présent des capteurs, l'application propose un
système d'alerting. Il se décompose en trois modes :
+ Vibration : Si un capteur change d'état pendant la plage horaire de vibration, une vibration est produite
              sur le téléphone indiquant quel capteur a changé d'état et quel est son nouvel état.
+ Notification : Si un capteur change d'état pendant la plage horaire de notification, une notification est émise
sur le téléphone indiquant quel capteur a changé d'état et quel est son nouvel état.
+ Mail : Si un capteur change d'état pendant la plage horaire de mail, un mail est envoyé à l'adresse définit.
Le contient les informations suivantes : le nom (si définit) et l'identifiant du capteur ayant changé d'état, la date du changement, sa nouvelle valeur et son nouvel état.

## Export des données

Le menu "export" est accessible en haut à droite de l'écran.
Il permet d'envoyer les données des capteurs pour une période définis.
Les données seront envoyées par mail à l'adresse mail renseignée, au format JSON.

![Export](https://raw.githubusercontent.com/tbagrel1/sensor_alert/master/.readme_resources/Export.png)

## Préférence

Le menu "préférences" est accessible en haut à droite de l'écran.
Il permet de configurer les fonctionnalités décrites ci dessus en définissant:

+ la plage horaire pendant laquelle une vibration sera produite en cas de changement d'état d'un
capteur.

![Preference vibration](https://raw.githubusercontent.com/tbagrel1/sensor_alert/master/.readme_resources/PreferenceVibration.png)

+ la plage horaire pendant laquelle une notification sera émise en cas de changement d'état d'un
capteur.

![Preference notification](https://raw.githubusercontent.com/tbagrel1/sensor_alert/master/.readme_resources/PreferenceNotification.png)

+ la plage horaire pendant laquelle un mail sera envoyé en cas de changement d'état d'un capteur.

(Remarque : Pour activer un des modes d'alerting en permanence il faut mettre sa plage horraire de 0h00 à 0h00)

+ les adresses mail auxquelles seront envoyées l'alerte de changement d'état d'un capteur

![Preference mail](https://raw.githubusercontent.com/tbagrel1/sensor_alert/master/.readme_resources/PreferenceMail.png)

+ la information de connexion à l'hôte SMTP
    - nom de l'hôte SMTP (par défaut : gmail.com)
    - port SMTP (par défaut : 587)
    - l'utilisation ou non de StartTLS
    - login du compte mail envoyant les alertes et les données exportées
    - mot de passe du compte mail envoyant les alertes et les données exportées
(Remarque : un compte SMTP sera fournit à l'évaluateur hors du readme)

![Preference SMTP](https://raw.githubusercontent.com/tbagrel1/sensor_alert/master/.readme_resources/PreferenceSMTP.png)

+ la fréquence de raffraichissement des données (limitation android)
+ l'adresse de l'hôte de l'API
+ le temps de conservation des données, ce qui limite la période d'affichage des données dans lors d'un export.
+ le seuil de luminosité au dessus duquel une lumière sera considérée comme allumée.

![Préférence générale](https://raw.githubusercontent.com/tbagrel1/sensor_alert/master/.readme_resources/PreferenceGenerale.png)

## Scnénario de test avec l'IOT Lab Mock API

L'IOT Lab Mock API permet de simuler l'IOT Lab de Telecom NANCY et d'y effectuer les requettes de notre
choix afin de tester les fonctionnalités de l'application Android.

Il est disponible [ici](https://github.com/tbagrel1/iotlab_mock_api).
Une fois l'IOT Lab Mock API lancé avec IntelliJ, il est possible d'effectuer un scnénario "scenario.bash" d'une durée de 3 minutes. Il simulera 4
mises à jour pour chaque capteur, modifiant la valeur et l'état de certains d'entre eux.
Pour le lancer : >>bash scenario.bash

Une fois les préférences de l'application correctement configurées (plages d'alerting, mail),
ce scénario permet de tester :
+ le bon affichage des données à l'écran et leur mise à jour régulière.
+ l'affichage de l'historique de luminosité d'un capteur en cliquant dessus.
+ l'export des données
+ le système d'alerting vibration, notification et mail.

Il est possible de forcer le raffraichissement de l'application en la mettant en pause puis en
reprennant le focus dessus.