# Thincloud SDK

##### Table of Contents
1. [Getting Started](#getting-started)
2. [Components](#components)
3. [Javadocs](#javadocs)


## Getting Started

This SDK is used for interacting with Thincloud v1 as well as providing a Virtual Gateway implementation.

Setting up your IDE:

1. Download [Android Studio](androidStudio)
2. [Install Lombok Plugin](lombokForJetbrains)
3. [Enable Annotation Processing](annotationProcessing)
4. Open Project


## Components


There are four primary components to be concerned with:

1. [ThincloudConfig](#thincloudconfig)
2. [ThincloudSDK](#thincloudSDK)
3. [ThincloudAPI](#thincloudAPI)
4. ThincloudException



### ThincloudConfig

A configuration POJO including the following properties. Uses [Lombok](lombokLib) with `@Data` and `@Accessors(fluent = true)` for interacting with POJO. See [@Data](lombokData) and [@Accessors](lombokAccessor) for more information.


|	Type	|	Variable	|	Usage	|
| ------- | ---------- | ------- |
| String | username | Thincloud Username |
| String | password | Thincloud Password |
| String | environment | Environment, used for API URL generation |
| String | company | Company name, used for API URL generation |
| String | appName | Name of the implementing application |
| String | appVersion | Version of the implementing application | 
| String | apiKey | Thincloud API Key |
| String | fcmTopic | Firebase Topic |
| String | clientId | oAuth Client ID |


### ThincloudSDK

Primary singleton interface for dealing with Thincloud. Ideally, this is all that is needed for a basic Virtual Gateway implementation.


| Return Type | Method | Parameters | Usage |
| ---- | ---- | ----- | ---- |
| ThincloudSDK | *initialize* | Context, ThincloudConfig | Initializes the ThincloudSDK and ThincloudAPI. <br>*Requires Android Context to configure Firebase.* |
| void | *setHandler* | CommandHandler | Provides an event handler to be called for each command that is sent. |
| boolean | *isInitialized* |  | Determines if the SDK has been initialized yet |


### ThincloudAPI

An API initializer and manager singleton. Receives a configuration object when the SDK is initialized. Uses [Retrofit](retrofit) behind the scenes for easy API management. Wraps all authentication handling to make interacting with the API easy. When run in an Android App, all API interaction should be called asynchronously to prevent network on the main thread.

#### API Interaction Sample

```java
try {
    ThincloudAPI.getInstance().spec(new TCAPIFuture() {
        @Override
        public boolean complete(APISpec spec) {
            spec.getSelf().enqueue(new Callback<User>() {
                @Override
                public void onResponse(Call<User> call, Response<User> response) {
                }

                @Override
                public void onFailure(Call<User> call, Throwable t) {
                }
            });
            return true; // Return true means this command is safe to exit
        }

        @Override
        public boolean completeExceptionally(Throwable e){
        	// Failed to generate API spec.
            return true; // Return true means this command is safe to exit
        }
    });
} catch(ThincloudException e){
	// An error occured
}
```

## Javadocs

Javadocs can be found [here](./javadoc/).




[javadocs]: ./javadocs
[androidStudio]: https://developer.android.com/studio/index.html
[lombokForJetbrains]: https://plugins.jetbrains.com/plugin/6317-lombok-plugin
[annotationProcessing]: https://www.jetbrains.com/help/idea/compiler-annotation-processors.html
[lombokLib]: https://projectlombok.org
[lombokAccessor]: https://projectlombok.org/features/experimental/Accessors
[lombokData]: https://projectlombok.org/features/Data
[retrofit]: http://square.github.io/retrofit/