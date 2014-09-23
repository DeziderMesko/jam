package jira.agile.monitor

import static org.fusesource.jansi.Ansi.*
import static org.fusesource.jansi.Ansi.Color.*

class SubTask implements Comparable{

    private static final String TO_DO = "To Do"
    private static final String DONE = "Done"
    private static final String IN_PROGRESS = "In Progress"

    def id, name, status, description, raw, estimate=0

    def String getAbbreivation(){
        if (status.equals(TO_DO)) return "..."
        if (name.startsWith("michal.mar")) return "OMG"

        def names = name.split("\\.")
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
        }
        return ansi().bold().fg(color).a(status.padRight(11, ' ')).reset();
    }

    def int compareTo(Object o) {
        def st = ((SubTask) o).status

        if(this.status.equals(st)) return 0
        if(this.status.equals(DONE)) return 1
        if(this.status.equals(TO_DO)) return -1
        if(this.status.equals(IN_PROGRESS)){
            if(st.equals(DONE)) return -1
            if(st.equals(TO_DO)) return 1
        }
        return 0
    }

    def String getEstimateInHours(){
        if(estimate!=null){
            return ((String)estimate/60/60).padLeft(2, ' ')
        } else {
        }
    }

    def String toString(){
        return "${getColorStatus()} ${getAbbreivation()} ${getEstimateInHours()}h ${id}: ${description}"
    }
}
