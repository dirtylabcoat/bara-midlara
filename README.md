# bara-midlara
A tiny server that serves up static JSON from files. "Bara miðlara" is Icelandic and means "just a server".

## Build with Maven
```
mvn clean package
```

## Run
```
java -jar target/bara-midlara.jar
```
Access http://localhost:8080/api/this via GET or http://localhost:8080/another-api/that via POST to see the demo running. For example, like this:
```
curl http://localhost:8080/api/this
```
Or this:
```
curl --request POST http://localhost:8080/another-api/that
```
Default location for the configuration file is `config.json` in the directory you're starting the server from. Another location can be set by adding parameter `-Dapi.config=/path/alternate-config.json` when starting, like this:
```
java -Dapi.config=/path/custom.json -jar target/bara-midlara.jar
```

