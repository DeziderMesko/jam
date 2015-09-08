
package jira.agile.monitor


import static groovyx.net.http.ContentType.*
import static groovyx.net.http.Method.*
import static org.fusesource.jansi.Ansi.*
import groovyx.net.http.*

import org.apache.http.HttpRequest
import org.apache.http.HttpRequestInterceptor
import org.apache.http.protocol.HttpContext

/*
 * java -cp target/jam-0.0.1-SNAPSHOT-jar-with-dependencies.jar jira.agile.monitor.Labels -s DDT-797,DDT-1003,DDT-1087,PCI-4248,PCI-3249,SEC-634,SEC-676,SEC-693 -l tr/operations,scrum7 -u dezider.mesko
 * mvn clean compile assembly:single
 * @author dezider.mesko
 *
 */


//@Grab(group='org.codehaus.groovy.modules.http-builder', module='http-builder', version='0.7.2' )
class Labels {

    def static JIRA_REST_URL = "https://jira.intgdc.com/rest"
    def static JIRA_API_URL = JIRA_REST_URL + "/api/latest/"

    public static void main(String[] args) {
        def jam = new Labels()
        def showLabelsOnly = false;
        def subtract = false;

        def stories = getArgument(args, "-s", "Stories are required parameter in format: XXX-nnn,XXX-mmm,XXX-ooo...")
        def labels
        if(args.contains("--showLabelsOnly")){
            showLabelsOnly = true;
            println "Only showing current labels for given stories"
            labels = ""
        } else {
            labels = getArgument(args, "-l", "Labels are required parameter in format: label1,label2...")
        }
        if (stories == null || labels == null) {
            println "Usage: Labels.groovy <-s story> <-l labels> [-u] [-p]\n"+
                    "-s stories i.e. -s DDT-523,DDT-234,PCI-3242\n-u username\n-p password\n"+
                    "-l labels i.e. -l label1,label2\n"+
                    "--skipSubtasks\n"+
                    "--showLabelsOnly just show current labels and do nothing"+
                    "--subtract remove given labels"
            System.exit(1)
        }



        stories = stories.split(',').toList();
        labels = labels.split(',').toList();
        def bothStoriesAndSubtasks = true;
        if(args.contains("--skipSubtasks")){
            bothStoriesAndSubtasks = false;
            println "Subtask won't be labeled"
        }

        if(args.contains("--subtract")){
            subtract = true;
            println "Labels will be subtracted"
        }

        def String authString = getAuthString(args)
        def jira = new RESTClient(JIRA_API_URL);
        jira.handler.failure = { resp ->
            println "Unexpected failure: ${resp.statusLine}"; System.exit(1)
        }
        setupAuthorization(jira, authString)

        jam.labelStories(stories, labels, bothStoriesAndSubtasks, jira, showLabelsOnly, subtract)
    }

    def labelStories(stories, labels, subtasks, jira, showLabelsOnly, subtract){
        stories.each {
            def story = jira.get(path: "issue/${it}", requestContentType: JSON).getData()
            def storyLabels
            if (subtract){
                storyLabels = (story.fields.labels - labels)
            } else {
                storyLabels = (story.fields.labels + labels).unique()
            }
            if(showLabelsOnly){
                println "${story.key}: ${storyLabels}"
            } else {
                println "Adding/removing labels: ${labels} to/from ${story.key}. Expected result: ${storyLabels}"
                jira.put(path: "issue/${story.key}", body:[fields:["labels":storyLabels]], requestContentType: JSON)
            }

            if(subtasks){
                def rawSubTasks = jira.get(path: 'search', query: ["jql":"parent=${it}"], requestContentType: JSON)
                rawSubTasks.getData().issues.each{ it2 ->
                    def subtaskLabels = (it2.fields.labels + labels).unique();
                    if(showLabelsOnly){
                        println "\t${it2.key}: ${subtaskLabels}"
                    } else {
                        println "\tAdding/removing labels: ${labels} to/from sub-task ${it2.key} as well. Expected result: ${subtaskLabels}"
                        jira.put(path: "issue/${it2.key}", body:[fields:["labels":subtaskLabels]], requestContentType: JSON)
                    }
                }
            }
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
}
