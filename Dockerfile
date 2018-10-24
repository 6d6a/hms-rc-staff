FROM docker-registry.intr/base/javabox:master

ENV XMS 256M
ENV XMX 256M
ENV XMN 64M
ENV DEBUG ""
#ENV DEBUG "-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=6006"

COPY ./build/libs/rc*jar /

ENTRYPOINT [ "/entrypoint.sh" ]
