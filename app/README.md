# Thicloud Demo App

##### Table of Contents
1. [Getting Started](#getting-started)
2. [Configuration](#configuration)


## Getting Started

This SDK is used for interacting with Thincloud v1 as well as providing a Virtual Gateway implementation.

Setting up your IDE:

1. Download [Android Studio](androidStudio)
2. [Install Lombok Plugin](lombokForJetbrains)
3. [Enable Annotation Processing](annotationProcessing)
4. Open Project


## Configuration

### Configurator Class

The Configurator class is used to manage loading of configuration from a key-value store flat file located in `./src/main/assets/`. By default on load it will look for two files in that directory, `app.properties` and `dev.properties` in that order. Below are the required properties in order to run this demo application:

| Property | Description | 
| ---- | ---- |
| thincloud.clientId | OAuth2 ClientID; Provided by Yonomi |
| thincloud.apiKey | Thincloud API Key; Provided by Yonomi |
| thincloud.instanceName | Thincloud Instance Name; Provided by Yonomi |
| thincloud.fcmTopic | Firebase Topic to Subscribe to |
| thincloud.defaultTestUser | Default Username pre-filled when the Demo App starts |
| thincloud.defaultTestPass | Default Password pre-filled when the Demo App starts |

