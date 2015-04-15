### JAM
Few utilities for better manipulation with JIRA's issues and subtasks.

* Lists subtasks for tasks specified on command line. You can specify username and password as a parameter. If not specified, script asks for them.
* Load subtasks to JIRA with CSV as a source
* Label issues with given labels (it preserves old labels)

### Build jar

```
mvn clean compile assembly:single
```
### Run script

Display stories and subtasks
```
java -jar target/jam-0.0.1-SNAPSHOT-jar-with-dependencies.jar -s DDT-525,DDT-493 -u dezider.mesko
```

Load subtasks to JIRA
```
java -cp target/jam-0.0.1-SNAPSHOT-jar-with-dependencies.jar jira.agile.monitor.AddSubtasks -f subtasks-example.csv -u dezider.mesko
```



