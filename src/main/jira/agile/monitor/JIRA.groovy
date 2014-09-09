package jira.agile.monitor


import static groovyx.net.http.ContentType.*
import static groovyx.net.http.Method.*
import static org.fusesource.jansi.Ansi.*
import groovyx.net.http.*

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

        def tasks = getArgument(args, "-t", "Stories are required parameter in format: XXX-nnn,XXX-mmm,XXX-ooo...")
        if (tasks == null) {
            println "Usage: jam <-t task> [-u] [-p]\n"+
                    "-t tasks i.e. -t DDT-523,DDT-234,PCI-3242\n-u username\n-p password"
            System.exit(1)
        }

        tasks = tasks.split(',');

        def authString = getAuthString(args)

        System.setProperty("jansi.passthrough", "true");
        AnsiConsole.systemInstall();

        tasks.each {
            jam.go(it, authString)
        }

        AnsiConsole.systemUninstall();
    }

    def go(taskId, authString) {
        def jira = new RESTClient(JIRA_API_URL);
        setupAuthorization(jira, authString)

        def rawSubTasks = jira.get(path: 'search', query: ["jql":"parent=${taskId}"])

        def subTasksList = []

        rawSubTasks.getData().issues.each{ it ->
            def st = new SubTask(raw:it, description:it.fields.summary, name:it.fields.assignee.name, status:it.fields.status.name, id:it.key)
            subTasksList.add(st)
        }
        if(subTasksList.isEmpty()) return

            println subTasksList.first().raw.fields.parent.key+": "+subTasksList.first().raw.fields.parent.fields.summary

        Collections.sort(subTasksList)
        subTasksList.each { println it }
        println ""
    }

    def setupAuthorization(RESTClient jira, String authString) {
        jira.ignoreSSLIssues()

        jira.client.addRequestInterceptor(
                new HttpRequestInterceptor() {
                    void process(HttpRequest httpRequest,
                            HttpContext httpContext) {
                        httpRequest.addHeader('Authorization', "Basic "+authString)
                    }
                })
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

    def getInfo(RESTClient jira){
        def serverInfo = jira.get(path: 'serverInfo')
        println serverInfo.getData()
    }
}
