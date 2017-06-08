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
Go to http://localhost:8080/api/this or http://localhost:8080/another-api/that to see the demo running.
