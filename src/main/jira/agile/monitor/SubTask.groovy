package jira.agile.monitor

import static org.fusesource.jansi.Ansi.*
import static org.fusesource.jansi.Ansi.Color.*

import org.joda.time.DateTime
import org.joda.time.DateTimeConstants
import org.joda.time.Hours
import org.joda.time.format.DateTimeFormat

class SubTask extends Task implements Comparable{

    private static final int LAST_WORKING_HOUR = 18

    private static final int FIRST_WORKING_HOUR = 9

    private static final int NUMBER_OF_WORKING_HOURS = 7

    private static final String DATETIME_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSSZ"

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

    def countHours(){
        def blah = ""
        def startDay, endDay
        raw.changelog.histories.each{ history ->
            history.items.each{ item ->
                if(item.field.equals("status")){
                    if(item.toString.equals("In Progress")){
                        startDay = history.created
                        endDay = null
                    }
                    if(item.toString.equals("Done")){
                        endDay = history.created
                    }
                    blah += "${history.created} ${item.fromString} ${item.toString}\n"
                }
            }
        }
        if(startDay == null){
            return "??"
        }
        if(endDay == null){
            def dtf = DateTimeFormat.forPattern(DATETIME_PATTERN);
            endDay = dtf.print(DateTime.now());
        }
        //println blah
        return ((String)getHoursInDays(startDay, endDay)).padLeft(2, ' ');
    }

    def public getHoursInDays(startTextDate, endTextDate){
        def startDate = DateTimeFormat.forPattern(DATETIME_PATTERN).parseDateTime(startTextDate).toLocalDateTime()
        def endDate = DateTimeFormat.forPattern(DATETIME_PATTERN).parseDateTime(endTextDate).toLocalDateTime()
        def hours = 0
        if(startDate.toLocalDate().isEqual(endDate.toLocalDate())){
            return Hours.hoursBetween(startDate,endDate).getHours()
        }
        if(startDate.getHourOfDay() < LAST_WORKING_HOUR){
            hours = LAST_WORKING_HOUR - startDate.getHourOfDay()
            startDate = startDate.plusDays(1);
        }
        while (startDate.toLocalDate().compareTo(endDate.toLocalDate()) < 0){
            if(startDate.dayOfWeek().get() < DateTimeConstants.SATURDAY){
                hours += NUMBER_OF_WORKING_HOURS
            }
            startDate = startDate.plusDays(1);
        }
        if(endDate.getHourOfDay() > FIRST_WORKING_HOUR){
            hours += endDate.getHourOfDay() - FIRST_WORKING_HOUR
        }
        return hours
    }

    def String toString(){
        return "${getColorStatus()} ${getAbbreivation()} ${getEstimateInHours()}h/${countHours()}h ${id}: ${description}"
    }
}
