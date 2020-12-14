FROM node:12

RUN node -v

# Run together to help with Docker caching
## keep package list alphabetized for easy curation
RUN apt-get update && \
    apt install -y \
        default-jre \
        && rm -rf /var/lib/apt/lists/*
