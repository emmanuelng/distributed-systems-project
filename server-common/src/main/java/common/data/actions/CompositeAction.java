package common.data.actions;

import java.util.Stack;

public class CompositeAction implements DataAction {

	private Stack<DataAction> actions;

	public CompositeAction() {
		this.actions = new Stack<>();
	}

	public void add(DataAction action) {
		if (action != null) {
			actions.add(action);
		}
	}

	@Override
	public void undo() {
		while (!actions.isEmpty()) {
			DataAction action = actions.pop();
			action.undo();
		}
	}

}
