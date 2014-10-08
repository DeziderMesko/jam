package jira.agile.monitor

import static org.fusesource.jansi.Ansi.*
import static org.fusesource.jansi.Ansi.Color.*

class Story extends Task {

    def String toString(){
        return "${id}: ${getAbbreivation()} ${getColorStatus()} ${description}"
    }

    def String getColorStatus(){
        if(status.equals("Completed") || status.equals("Verification")){
            status = "Done"
        }
        return super.getColorStatus()
    }
}
