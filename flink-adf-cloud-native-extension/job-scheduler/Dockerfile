FROM xpayregistry.azurecr.io/alpine:3.15.5

WORKDIR /
COPY app /app
COPY config/ config/
COPY deployment/ deployment/

ENV PORT 8080
ENV GIN_MODE release
EXPOSE 8080

ENTRYPOINT ["./app"]