FROM docker-registry.intr/user/javabox
ADD ./"${jarBuildName}" app.jar
RUN sh -c 'touch /app.jar'
RUN adduser -u 32151 -D rc-staff
USER rc-staff
ENTRYPOINT [ ${entryPoint} ]
