### JAM

Lists subtasks for tasks specified on command line. You can specify username and password as a parameter. If not specified, script asks for them.

### Build jar

```
mvn clean compile assembly:single
```
### Run script

```
java -jar target/jam-0.0.1-SNAPSHOT-jar-with-dependencies.jar -t DDT-525,DDT-493 -u dezider.mesko
```
