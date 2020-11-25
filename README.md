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
Access `http://localhost:8080/api/this` via GET or `http://localhost:8080/api/that` via POST to see the demo running. For example, like this:
```
curl --request GET http://localhost:8080/api/this
```
Or this:
```
curl --request POST http://localhost:8080/api/that
```

## Configuration
There is configured using a JSON-file. By default the server will look for a file named `config.json` in the fold it was started from. If the file is in another location the server can be told about it like this:
```
java -Dapi.config=/some/directory/somefile.json -jar target/bara-midlara.jar

```
If you look in the example configuration-file in this repo, the format of the configuration-file should be self explanatory.

