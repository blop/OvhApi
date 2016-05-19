# OvhApi
![Build Status](https://travis-ci.org/cambierr/OvhApi.svg?branch=master)

RxJava Ovh Api wrapper... soon

##Requesting credentials

To request a consumer key to use in your application, you can use the com.github.cambierr.ovhapi.auth.CredentialRequest object this way:

```java
AccessRules accessRules = new AccessRules().addRule("/cloud/*", Method.GET);

CredentialRequest credRqst = CredentialRequest.build("applicationKey", "applicationSecret", accessRules, "http://youwebsite.ovh").toBlocking().single();

System.out.println(credRqst.getValidationUrl());
```

You can now link your CK to a user account using the validation url. You should be able to use 

```java
credRqst.isLinked()
```

one of these days; I'm waiting for OVH'guys to implement something to be able to test the CK.

Finally, you can retrieve your credentials:

```java
Credential creds = credRqst.getCredential();
```
##Using existing credentials

Nothing more simple:
```java
Credential creds = Credential.build("applicationKey", "applicationSecret", "consumerKey").toBlocking().single();
```

Like for the credential request, you should be able to use

```java
creds.check()
```

as soon as this will be implemented in the OVH API.

##Load your project

```java
Project project = Project.byId(creds, "myProjectId").toBlocking().single();
```

or list all your projects:

```java
List<Project> projects = Project.list(creds).toList().toBlocking().single();
```

##List your instances
```java
Region region = Region.byName(project, "SBG1");

List<Instance> instancesAtSBG1 = Instance.list(project, region).toList().toBlocking().single();

List<Instance> allInstances = Instance.list(project, null).toList().toBlocking().single();
```

##Kill your instances
```java
for(Instance instance:allInstances){
    instance.kill().toBlocking().single();
}
```


##Do it again, but asynchronously !
```java
Credential.build("applicationKey", "applicationSecret", "consumerKey")
                .flatMap((Credential t) -> Project.byId(t, "projectId"))
                .flatMap((Project p) -> Instance.list(p, null))
                .flatMap((Instance i) -> i.kill())
                .subscribe();
```

##Running the tests:
```shell
export ovhCk=your-consumer-key
export ovhAk=your-application-key
export ovhAs=your-application-secret
export epochString=epoch, with your locale. ex: 1970-01-01T00:00:00Z
export ovhProject=project-id-to-run-tests-on
export ovhSnapshot=already-existing-snapshot-or-image-id
mvn test

```

##Maven
```xml
<dependency>
  <groupId>com.github.cambierr</groupId>
  <artifactId>ovhapi</artifactId>
  <version>1.1</version>
</dependency>
```

##Extending
Using not implemented endpoints with this lib is also easy:
```java
Credential creds = Credential.build("applicationKey", "applicationSecret", "consumerKey").toBlocking().single();

JSONObject data = new RequestBuilder("/me", Method.GET, creds)
                .build()
                .flatMap((SafeResponse arg0) -> arg0.validateResponse(JSONObject.class))
                .toBlocking()
                .single();
```
