# Start with the Debian Bookworm base image
FROM debian:bookworm

# Argument that allows setting the tag name (branch or tag) for the git clone
ARG tag_name

# Set environment variables for GOSU and JDK versions
ENV GOSU_VERSION=1.14
ENV JDK_VERSION=17.0.11.9-1

# Update package lists and install necessary packages
RUN apt-get update \
  && apt-get install --no-install-recommends git curl wget gnupg2 ca-certificates lsb-release software-properties-common unzip -y

# Add the Amazon Corretto repository for the JDK and install it
RUN wget -O - https://apt.corretto.aws/corretto.key | gpg --dearmor -o /usr/share/keyrings/corretto-keyring.gpg && \
    echo "deb [signed-by=/usr/share/keyrings/corretto-keyring.gpg] https://apt.corretto.aws stable main" | tee /etc/apt/sources.list.d/corretto.list && \
    apt-get update && \
    apt-get install --no-install-recommends -y java-17-amazon-corretto-jdk=1:${JDK_VERSION} && \
    apt-get -y install maven

# Set the JAVA_HOME environment variable to the location of the JDK
ENV JAVA_HOME=/usr/lib/jvm/java-17-amazon-corretto

# Set the working directory to /build
WORKDIR /build

# Print the tag_name to the console (useful for debugging)
RUN echo tag_name ${tag_name:-master}

# Clone the QuestDB repository using the specified tag or branch
RUN git clone --depth=1 --progress --branch "${tag_name:-master}" --verbose https://github.com/questdb/questdb.git

# Change the working directory to the cloned repository
WORKDIR /build/questdb

# Build QuestDB using Maven
RUN mvn clean package -Djdk.lang.Process.launchMechanism=vfork -Dmaven.resolver.transport=wagon -Dmaven.wagon.httpconnectionManager.ttlSeconds=30 -DskipTests -P build-web-console,build-binaries

# Change the working directory to the target directory where the build artifacts are located
WORKDIR /build/questdb/core/target

# Extract the QuestDB runtime tarball
RUN tar xvfz questdb-*-rt-*.tar.gz

# Remove the tarball to save space
RUN rm questdb-*-rt-*.tar.gz

# Download and install GOSU, a tool for running commands as different users
RUN dpkgArch="$(dpkg --print-architecture | awk -F- '{ print $NF }')"; \
    wget -O gosu "https://github.com/tianon/gosu/releases/download/$GOSU_VERSION/gosu-$dpkgArch" && \
    wget -O gosu.asc "https://github.com/tianon/gosu/releases/download/$GOSU_VERSION/gosu-$dpkgArch.asc" && \
    export GNUPGHOME="$(mktemp -d)" && \
    gpg --batch --keyserver hkps://keys.openpgp.org --recv-keys B42F6819007F00F88E364FD4036A9C25BF357DD4 && \
    gpg --batch --verify gosu.asc gosu && \
    gpgconf --kill all && \
    rm -rf "$GNUPGHOME" gosu.asc && \
    chmod +x gosu && \
    ./gosu --version && \
    ./gosu nobody true

# Start with a smaller Debian image for the final runtime environment
FROM debian:bookworm-slim

# Set the working directory to /app
WORKDIR /app

# Copy the built QuestDB files from the previous stage
COPY --from=0 /build/questdb/core/target/questdb-*-rt-* .

# Copy the GOSU binary from the previous stage
COPY --from=0 /build/questdb/core/target/gosu /usr/local/bin/gosu

# Copy the Docker entrypoint script and make it executable
COPY docker-entrypoint.sh /docker-entrypoint.sh
RUN chmod +x /docker-entrypoint.sh

# Set the entrypoint to the copied script
ENTRYPOINT ["/docker-entrypoint.sh"]
