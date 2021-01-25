package replication;

import java.util.Arrays;

public class Action implements Comparable<Action>{

    public enum ActionType {
        CREATE_FILE,CREATE_DIR,WRITE_FILE,DELETE_FILE,RENAME_FILE
    };

    public Action(ActionType actionType, Object[] data) {
        this(actionType, TestClient.getNextStep(),data);
    }

    public Action(ActionType actionType, int time, Object[] data) {
        this.actionType = actionType;
        this.time = time;
        this.data = data;
    }

    @Override
    public int compareTo(Action action) {
        return this.time-action.time;
    }

    private final ActionType actionType;
    private final int time;
    private final Object[] data;

    public ActionType getActionType() {
        return actionType;
    }

    public Object[] getData() {
        return data;
    }

    @Override
    public String toString() {
        return actionType.name()+" @ "+time+" "+ Arrays.toString(data);
    }
}
