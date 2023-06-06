#FROM quay.io/keycloak/keycloak:latest as builder
#
## Enable health and metrics support
#ENV KC_HEALTH_ENABLED=true
#ENV KC_METRICS_ENABLED=true
#
## Configure a database vendor
#ENV KC_DB=postgres
#
#WORKDIR /opt/keycloak
#ADD --chown=keycloak:keycloak ./target/*.jar providers/
## for demonstration purposes only, please make sure to use proper certificates in production instead
#RUN keytool -genkeypair -storepass password -storetype PKCS12 -keyalg RSA -keysize 2048 -dname "CN=server" -alias server -ext "SAN:c=DNS:localhost,IP:127.0.0.1" -keystore conf/server.keystore
#RUN /opt/keycloak/bin/kc.sh build
#
#FROM quay.io/keycloak/keycloak:latest
#COPY --from=builder /opt/keycloak/ /opt/keycloak/
#
## change these values to point to a running postgres instance
#EXPOSE 8080
#ENTRYPOINT ["/opt/keycloak/bin/kc.sh","start-dev"]
#
#
#
#
FROM quay.io/keycloak/keycloak:21.1.1
ENV KEYCLOAK_ADMIN=admin
ENV KEYCLOAK_ADMIN_PASSWORD=admin
ENV KEYCLOAK_LOGLEVEL=ALL
ADD --chown=keycloak:keycloak ./target/*.jar /opt/keycloak/providers/
EXPOSE 8080
ENTRYPOINT ["/opt/keycloak/bin/kc.sh","start-dev"]