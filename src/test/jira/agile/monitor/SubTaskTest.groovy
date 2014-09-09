package jira.agile.monitor;

import static org.junit.Assert.*

import org.junit.Test

class SubTaskTest {

    @Test
    public void testCompareTo() {
        def done = new SubTask(status:SubTask.DONE)
        def todo = new SubTask(status:SubTask.TO_DO)
        def inpg = new SubTask(status:SubTask.IN_PROGRESS)

        assert done.compareTo(todo) == 1
        assert todo.compareTo(todo) == 0
        assert inpg.compareTo(todo) == 1

        assert done.compareTo(inpg) == 1
        assert inpg.compareTo(inpg) == 0
        assert todo.compareTo(inpg) == -1
    }
}
