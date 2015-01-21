package jira.agile.monitor


import static groovyx.net.http.ContentType.*
import static groovyx.net.http.Method.*
import static org.fusesource.jansi.Ansi.*
import groovyx.net.http.*

import java.text.SimpleDateFormat

import org.apache.http.HttpRequest
import org.apache.http.HttpRequestInterceptor
import org.apache.http.protocol.HttpContext
import org.fusesource.jansi.AnsiConsole


/**
 * java -jar jam-0.0.1-SNAPSHOT-jar-with-dependencies.jar -t DDT-525,DDT-493,DDT-495,DDT-697,DDT-686,DDT-683 -u dezider.mesko
 * mvn clean compile assembly:single
 * @author dezider.mesko
 *
 */
class JAM {

    def static JIRA_REST_URL = "https://jira.intgdc.com/rest"
    def static JIRA_API_URL = JIRA_REST_URL + "/api/latest/"
    def static JIRA_AGILE = JIRA_REST_URL + "/greenhopper/1.0/"

    public static void main(String[] args) {

        def jam = new JAM()

        def stories = getArgument(args, "-s", "Stories are required parameter in format: XXX-nnn,XXX-mmm,XXX-ooo...")
        if (stories == null) {
            println "Usage: jam <-s story> [-u] [-p]\n"+
                    "-s stories i.e. -s DDT-523,DDT-234,PCI-3242\n-u username\n-p password"
            System.exit(1)
        }

        stories = stories.split(',');

        def String authString = getAuthString(args)
        def jira = new RESTClient(JIRA_API_URL);
        jira.handler.failure = { resp -> println "Unexpected failure: ${resp.statusLine}"; System.exit(1) }
        setupAuthorization(jira, authString)


        System.setProperty("jansi.passthrough", "true");
        AnsiConsole.systemInstall();

        def sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm")
        println "Status at " + sdf.format(new Date())

        stories.each {
            def story = jira.get(path: "issue/${it}", requestContentType: JSON).getData()
            def rawSubTasks = jira.get(path: 'search', query: ["jql":"parent=${it}", "expand":"changelog"]).getData()
            jam.processSubtasks(rawSubTasks, story)
        }

        AnsiConsole.systemUninstall();
    }

    def processSubtasks(rawSubTasks, story) {
        def subTasksList = []
        def stry = new Story(raw:story, id:story.key, status:story.fields.status.name, description:story.fields.summary, user:story.fields.assignee.name)

        rawSubTasks.issues.each{ it ->
            def st = new SubTask(raw:it, description:it.fields.summary, user:it.fields.assignee.name, status:it.fields.status.name, id:it.key, estimate:it.fields.timeoriginalestimate)
            subTasksList.add(st)
        }

        if(subTasksList.isEmpty()) {
            println stry
        } else {
            println "\n"+story.key+": "+story.fields.summary
            Collections.sort(subTasksList)
            subTasksList.each { println it }
        }
    }

    def static setupAuthorization(RESTClient jira, String authString) {
        jira.ignoreSSLIssues()

        jira.client.addRequestInterceptor(
                new HttpRequestInterceptor() {
                    void process(HttpRequest httpRequest,
                            HttpContext httpContext) {
                        httpRequest.addHeader('Authorization', "Basic "+authString)
                    }
                })
    }



    private static getArgument(args, String option, String errorMessage){
        def uIndex = args.findIndexOf { it.equals(option) } + 1
        if (uIndex != 0){
            if (uIndex >= args.length){
                println errorMessage
                return null
            }
            return args[uIndex];
        }
        return null
    }
}
