package jira.agile.monitor

import static org.fusesource.jansi.Ansi.*
import static org.fusesource.jansi.Ansi.Color.*

class SubTask extends Task implements Comparable{

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
            return "??"
        }
    }

    def String toString(){
        return "${getColorStatus()} ${getAbbreivation()} ${getEstimateInHours()}h ${id}: ${description}"
    }
}
