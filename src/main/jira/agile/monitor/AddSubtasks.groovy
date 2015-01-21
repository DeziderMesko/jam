package jira.agile.monitor


import static com.xlson.groovycsv.CsvParser.parseCsv
import static groovyx.net.http.ContentType.*
import static groovyx.net.http.Method.*
import static org.fusesource.jansi.Ansi.*
import groovy.json.JsonBuilder
import groovyx.net.http.*

import org.apache.http.HttpRequest
import org.apache.http.HttpRequestInterceptor
import org.apache.http.protocol.HttpContext


/**
 * java -jar jam-0.0.1-SNAPSHOT-jar-with-dependencies.jar -t DDT-525,DDT-493,DDT-495,DDT-697,DDT-686,DDT-683 -u dezider.mesko
 * mvn clean compile assembly:single
 * @author dezider.mesko
 *
 */
class AddSubtasks {

    def static JIRA_REST_URL = "https://jira.intgdc.com/rest"
    def static JIRA_API_URL = JIRA_REST_URL + "/api/latest/"

    public static void main(String[] args) {

        def jam = new AddSubtasks()

        def String authString = getAuthString(args)
        def jira = new RESTClient(JIRA_API_URL);
        jira.handler.failure = { resp ->
            println "Unexpected failure: ${resp.statusLine}\n ${resp.data}"; System.exit(1)
        }
        jira.handler.success = {   resp, reader ->
            println "Response: ${resp.statusLine}\n ${resp.data}"
            System.out << reader
        }
        setupAuthorization(jira, authString)

        def data = parseCsv(new FileReader(new File("subtask15.csv")))
        def bulk = []
        data.each { subtask ->
            def st
            println "${subtask.parent} ${subtask.summary} "
            st = [fields:[
                    timetracking:["originalEstimate":subtask.estimate],
                    "project":["key":subtask.parent.substring(0, subtask.parent.indexOf("-"))],
                    "issuetype":["id":5],
                    "parent":["key":subtask.parent],
                    "description":subtask.description,
                    "summary": subtask.summary,
                    "assignee": ["name":subtask.assignee],
                    "labels":subtask.labels.split(":")]
            ]
            bulk.add(st)
        }
        def bulkWrap = ["issueUpdates":bulk]
        println new JsonBuilder( bulkWrap ).toPrettyString()
        //jira.post(path: "issue/bulk", body:bulkWrap, requestContentType: JSON)
    }

    private static String getAuthString(String[] args) {
        def user = null
        def pw = null

        user = getArgument(args, "-u", "Username parameter missing")
        pw = getArgument(args, "-p", "Password parameter missing")

        if(user == null){
            user = System.console().readLine("Your JIRA username: ")
        } else {
            println "Using username: "+user
        }
        if(pw == null){
            pw = System.console().readPassword("Your JIRA password: ");
        }

        def authString = "${user}:${pw}".getBytes().encodeBase64().toString()
        return authString
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
}
