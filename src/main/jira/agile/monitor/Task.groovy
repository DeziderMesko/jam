package jira.agile.monitor

import static org.fusesource.jansi.Ansi.*
import static org.fusesource.jansi.Ansi.Color.*

class Task {

    public static final String TO_DO = "To Do"
    public static final String DONE = "Done"
    public static final String IN_PROGRESS = "In Progress"

    public String id, user, status, description
    public raw = [:], labels = []
    public Integer estimate=0

    def String getAbbreivation(){
        if (status.equals(TO_DO)) return "..."
        if (user.startsWith("michal.mar")) return "OMG"

        def names = user.split("\\.")
        return (names[0].substring(0,1)+names[1].substring(0,2)).toUpperCase()
    }

    def String getColorStatus(){
        def color = BLACK
        switch(status.trim()){
            case TO_DO:
                color = RED
                break;
            case DONE:
                color = GREEN
                break;
            case IN_PROGRESS:
                color = YELLOW
                break;
            default:
                color = CYAN
                break;
        }
        return ansi().bold().fg(color).a(status.padRight(11, ' ')).reset();
    }

    def getLabels(){
        raw.fields.labels
    }
}
