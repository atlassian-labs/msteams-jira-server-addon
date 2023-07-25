FROM atlassian/default-image:4

# Install PowerShell
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

# Install xmlstarlet command to edit XML files
RUN apt-get -y install xmlstarlet

# Install zip command to compress the output of build
RUN apt-get -y install zip

# Install Atlassian SDK
# https://developer.atlassian.com/server/framework/atlassian-sdk/install-the-atlassian-sdk-on-a-linux-or-mac-system/
# 1. Set up repositories
RUN sh -c 'echo "deb https://packages.atlassian.com/debian/atlassian-sdk-deb/ stable contrib" >>/etc/apt/sources.list'
# 2. Download public key
RUN wget https://packages.atlassian.com/api/gpg/key/public
# 3. Add the public key to apt to verify the package signatures automatically
RUN apt-key add public
# 4. Run install
RUN apt-get update
RUN apt-get install atlassian-plugin-sdk
# 5. Check atlas version
RUN atlas-version