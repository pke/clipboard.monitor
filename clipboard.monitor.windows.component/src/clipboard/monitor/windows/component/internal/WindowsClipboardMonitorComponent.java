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
package clipboard.monitor.windows.component.internal;

import clipboard.monitor.ClipboardEvent;
import clipboard.monitor.ClipboardListener;
import clipboard.monitor.ClipboardMonitor;
import clipboard.monitor.component.ClipboardMonitorComponent;
import clipboard.monitor.windows.AbstractWindowsClipboardMonitor;

/**
 * OSGi component for clipboard monitoring on Windows.
 * 
 * <p>
 * It consumes {@link ClipboardListener} and notifies them about changes in the
 * clipboard. It does not read out the clipboard. That will be the task of the
 * listener service itself.
 * 
 * @author <a href="mailto:phil.kursawe@gmail.com">Philipp Kursawe</a>
 * 
 */
public class WindowsClipboardMonitorComponent extends ClipboardMonitorComponent {
	@Override
	protected ClipboardMonitor getMonitor() {
		return new AbstractWindowsClipboardMonitor() {
			public void onChange(ClipboardEvent event) {
				WindowsClipboardMonitorComponent.this.onChange(event);
			}
		};
	}
}
