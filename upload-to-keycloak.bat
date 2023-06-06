docker exec keycloak rm /opt/keycloak/providers/keycloak-otp-authenticator.jar
docker exec keycloak /opt/keycloak/bin/kc.sh build
docker cp .\target\keycloak-otp-authenticator.jar  keycloak:/opt/keycloak/providers/
docker exec keycloak /opt/keycloak/bin/kc.sh build