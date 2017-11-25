package common.data.actions;

import java.util.Stack;

public class CompositeAction implements DataAction {

	private static final long serialVersionUID = -7930644094706643798L;
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
