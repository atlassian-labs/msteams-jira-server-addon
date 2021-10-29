# Microsoft Teams for Jira Server

[![Atlassian license](https://img.shields.io/badge/license-Apache%202.0-blue.svg?style=flat-square)](LICENSE) [![PRs Welcome](https://img.shields.io/badge/PRs-welcome-brightgreen.svg?style=flat-square)](CONTRIBUTING.md)

Official plugin for Jira Server that integrates with [Microsoft Teams](https://www.microsoft.com/en-ww/microsoft-teams/group-chat-software).

[![jdk version](https://img.shields.io/badge/jdk-1.8-blue.svg?style=flat-square)](https://www.npmjs.com/package/react-beautiful-dnd) [![Build Status](https://img.shields.io/travis/stricter/stricter/master?style=flat-square)](https://travis-ci.org/stricter/stricter)


## Usage

Jira administrator can install Microsoft Teams plugin to his system via embedded Marketplace (**\<Configuration gear\>** -> **Find new apps/plugins**)
or by manually downloading plugin JAR files from Marketplace pages for [Jira](https://marketplace.atlassian.com/apps/1217836/microsoft-teams-for-jira?hosting=server&tab=overview) plugin.
Links to the official documentation are specified on Marketplace pages.

Supported products (on 5 Mar, 2021). See [EOL policy](https://confluence.atlassian.com/support/atlassian-support-end-of-life-policy-201851003.html).
* Jira Server 7.0.0 - 8.19.0 JDK 8.

## A note on future development plans

In order to [accelerate our journey to the cloud, together](https://www.atlassian.com/blog/announcements/journey-to-cloud), Atlassian will continue to maintain these apps' _compatibility_ with our Server products. However, we will not be creating new features or matching feature parity with our Cloud integrations. If you would like to add your own customizations/features to the integration, we encourage you to fork this repository and customize it as you wish like we’ve seen many customers do.

When Jira Server releases new versions, we will validate the compatibility of these apps and release new versions within four weeks of their public release.


## Installation

### Prerequisites
1. JDK 8 is installed on your machine.
1. Standalone Jira server is installed on your machine or you have installed Atlassian SDK (https://developer.atlassian.com/server/framework/atlassian-sdk/install-the-atlassian-sdk-on-a-windows-system/).
1. You have Atlassian plugin SDK on your machine. It should be installed automatically on step #1. Please check if you have folder C:\Applications\Atlassian on your machine. [ATLASSIAN_PLUGIN_FOLDER]
1. Repository is cloned to local folder [ADDON_FOLDER] on your machine.

### Build addon
1. Go to [ADDON_FOLDER]\src\main\resources and edit integration.properties file:
    - Add value for aud_claim=**__JIRA_BOT_ID__**, where **__JIRA_BOT_ID__** uniques ID of your JIRA Bot Application
    - Add value for signalr_hub_url=https://**__NGROK_URL__**/JiraGateway?atlasId=%s&atlasUrl=%s&pluginVersion=%s, where **__NGROK_URL__** url of your local instance of Jira app run on ngrok.
    - Save changes.
1. Run next command from the root folder [ADDON_FOLDER]: "[ATLASSIAN_PLUGIN_FOLDER]\ [APACHE_MAVEN_FOLDER]\bin\mvn package -DskipTests". For example: C:\Applications\Atlassian\atlassian-plugin-sdk-8.0.16\apache-maven-3.5.4\bin\mvn package -DskipTests
1. Wait until command successfully finished.
1. Go to [ADDON_FOLDER]\target folder. You should see new file microsoft-teams-integration-0.0.0.0.jar.
1. Please use this addon file to proceed with JIRA <-> MS Teams integration.

### Run addon
1. To run plugin just use `atlas-run --product jira` from root folder.
1. To enable quick reload find and change property `<enableQuickReload>` to true in pom.xml. Full documentation is here: https://developer.atlassian.com/server/framework/atlassian-sdk/modify-the-plugin-using-quickreload/.
1. To change jira version: find and change `<refapp.version>` & `<amps.version>` in pom.xml.

### Debug addon
1. Create Remote JVM Debug with default settings and start via `atlas-debug --product jira`.
1. To recompile plugin use `atlas-mvn package`.

## Documentation

Full documentation is always available at:
https://developer.atlassian.com/display/DOCS/Introduction+to+the+Atlassian+Plugin+SDK

## Tests

Run all unit tests (from the root project folder):
```bash
atlas-mvn clean test
```
Run unit tests in a specific module:
```bash
cd <that-module>
atlas-mvn clean test
```

## Contributions

Contributions to Microsoft Teams for Jira Server are welcome! Please see [CONTRIBUTING.md](CONTRIBUTING.md) for details.

## License

Copyright (c) 2019 - 2021 Atlassian and others.
Apache 2.0 licensed, see [LICENSE](LICENSE) file.

[![With ❤️ from Atlassian](https://raw.githubusercontent.com/atlassian-internal/oss-assets/master/banner-with-thanks.png)](https://www.atlassian.com)
