FROM atlassian/default-image:5

# Install PowerShell and JDK prerequisites
# 1. Update the list of packages
RUN apt-get update
# 2. Install pre-requisite packages.
RUN apt-get install -y wget apt-transport-https software-properties-common
# 3. Download the Microsoft repository GPG keys
RUN wget -q "https://packages.microsoft.com/config/ubuntu/$(lsb_release -rs)/packages-microsoft-prod.deb"
# 4. Register the Microsoft repository GPG keys
RUN dpkg -i packages-microsoft-prod.deb
# 5. Delete the the Microsoft repository GPG keys file
RUN rm packages-microsoft-prod.deb
# 6. Update the list of packages after we added packages.microsoft.com
RUN apt-get update
# 7. Install PowerShell
RUN apt-get install -y powershell
# 8. Start PowerShell
RUN pwsh

# Install jq command for processing JSON files
RUN apt-get -y install jq

# Install xmlstarlet command to edit XML files
RUN apt-get -y install xmlstarlet

# Install zip command to compress the output of build
RUN apt-get -y install zip

# Install JDK 21
RUN wget -O /tmp/openjdk-21.tar.gz https://download.oracle.com/java/21/latest/jdk-21_linux-x64_bin.tar.gz && \
    mkdir -p /opt/jdk-21 && \
    tar -xzf /tmp/openjdk-21.tar.gz -C /opt/jdk-21 --strip-components=1 && \
    rm /tmp/openjdk-21.tar.gz

# Set up JDK environment variables
ENV JDK21_HOME=/opt/jdk-21
ENV JAVA_HOME=$JDK21_HOME
ENV PATH=$JAVA_HOME/bin:$PATH

# Verify JDK installation
RUN echo "=== JDK 21 ===" && \
    $JDK21_HOME/bin/java -version

# Install Homebrew
# Install Homebrew prerequisites
RUN apt-get install -y build-essential procps curl file git sudo
# Create a non-root user for Homebrew installation
RUN useradd -m -s /bin/bash linuxbrew && \
    echo 'linuxbrew ALL=(ALL) NOPASSWD:ALL' >> /etc/sudoers
# Install Homebrew as non-root user
USER linuxbrew
WORKDIR /home/linuxbrew
# Set environment variables for non-interactive installation
ENV NONINTERACTIVE=1
ENV CI=1
# Install Homebrew
RUN /bin/bash -c "$(wget -qO- https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"

# Install Atlassian SDK using Homebrew
RUN /home/linuxbrew/.linuxbrew/bin/brew tap atlassian/tap
RUN /home/linuxbrew/.linuxbrew/bin/brew install atlassian/tap/atlassian-plugin-sdk
# Switch back to root user
USER root

# Add Homebrew to system PATH
ENV PATH="/home/linuxbrew/.linuxbrew/bin:/home/linuxbrew/.linuxbrew/sbin:$PATH"
# Make Homebrew available system-wide
RUN echo 'eval "$(/home/linuxbrew/.linuxbrew/bin/brew shellenv)"' >> /etc/bash.bashrc
# Verify Atlassian SDK installation
RUN atlas-version