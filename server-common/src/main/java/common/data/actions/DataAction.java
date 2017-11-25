package common.data.actions;

import java.io.Serializable;

public interface DataAction extends Serializable {

	/**
	 * Undoes the action.
	 */
	public void undo();
}
