# bara-midlara
A tiny server that serves up static JSON from files. "Bara miðlara" is Icelandic and means "just a server".

## Build with Maven
```
mvn clean package
```

## Run
```
java -Dapi.config=demo/demo.config -jar target/bara-midlara.jar
```
Access http://localhost:8080/api/this via GET or http://localhost:8080/another-api/that via POST to see the demo running. For example, like this:
```
curl --request GET http://localhost:8080/api/this
```
Or this:
```
curl --request POST http://localhost:8080/another-api/that
```
