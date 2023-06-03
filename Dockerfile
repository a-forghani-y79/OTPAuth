FROM quay.io/keycloak/keycloak:21.1.1
ENV KEYCLOAK_ADMIN=admin
ENV KEYCLOAK_ADMIN_PASSWORD=admin
ENV KEYCLOAK_LOGLEVEL=ALL
#ADD --chown=keycloak:keycloak src/main/java/themes/* /opt/keycloak/themes
ADD --chown=keycloak:keycloak ./target/*.jar /opt/keycloak/providers/
EXPOSE 8080
ENTRYPOINT ["/opt/keycloak/bin/kc.sh","start-dev"]