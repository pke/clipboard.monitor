/*******************************************************************************
 * Copyright (c) 2010 Philipp Kursawe.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Philipp Kursawe (phil.kursawe@gmail.com) - initial API and implementation
 ******************************************************************************/
package clipboard.monitor.swt.internal;

import org.eclipse.swt.widgets.Display;

/**
 * An SWT thread that pools the message queue.
 * 
 * @author <a href="mailto:phil.kursawe@gmail.com">Philipp Kursawe</a>
 * 
 */
public class SWTMessagePoller extends Thread {

	private Display display;

	/**
	 * Creates the poller
	 */
	public SWTMessagePoller() {
		super("SWT Main"); //$NON-NLS-1$
	}

	protected void activate() {
		start();
	}

	protected void deactivate() {
		if (display != null) {
			display.syncExec(new Runnable() {
				public void run() {
					display.dispose();
					display = null;
				}
			});
		}
	}

	@Override
	public void run() {
		display = Display.getDefault();
		while (display != null && !display.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
	}
}
