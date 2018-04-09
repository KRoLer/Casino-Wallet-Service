FROM openjdk:latest

WORKDIR /opt/docker

ADD --chown=daemon:daemon opt /opt

USER daemon

ENTRYPOINT ["bin/wallet-webservice"]

CMD []