FROM gradle:8.7-jdk21

WORKDIR /workspace

RUN apt-get update \
    && apt-get install -y --no-install-recommends awscli jq \
    && rm -rf /var/lib/apt/lists/*

COPY . /workspace

CMD ["/bin/bash"]

