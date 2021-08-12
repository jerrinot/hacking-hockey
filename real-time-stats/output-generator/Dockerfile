FROM gcr.io/distroless/java:8
COPY sample-output-generator.jar /opt/sample-output-generator/
WORKDIR /opt/sample-output-generator/
ENTRYPOINT ["/usr/bin/java" ]

# Expose port
EXPOSE 5701

CMD ["-jar", "sample-output-generator.jar"]
