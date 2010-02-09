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
package clipboard.monitor.component;

import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

import clipboard.monitor.ClipboardEvent;
import clipboard.monitor.ClipboardListener;
import clipboard.monitor.ClipboardMonitor;

/**
 * Abstract base class for clipoard monitor OSGi components.
 * 
 * 
 * @author <a href="mailto:phil.kursawe@gmail.com">Philipp Kursawe</a>
 * 
 */
public abstract class ClipboardMonitorComponent {

	private ClipboardMonitor monitor;
	private BundleContext bundleContext;

	/**
	 * Subclasses must return the monitor to use with this component here. This
	 * method is called only once. The implementation does not need to remember
	 * what it returns. Must <em>not</em> return <code>null</code>.
	 */
	protected abstract ClipboardMonitor getMonitor();

	private ClipboardMonitor _getMonitor() {
		if (null == monitor) {
			monitor = getMonitor();
			if (null == monitor) {
				throw new IllegalArgumentException("monitor must not be null"); //$NON-NLS-1$
			}
		}
		return monitor;
	}

	protected void activate(BundleContext context) {
		this.bundleContext = context;
		_getMonitor().start();
	}

	protected void deactivate() {
		_getMonitor().stop();
	}

	protected void onChange(ClipboardEvent event) {
		ServiceReference[] references = null;
		try {
			references = bundleContext.getServiceReferences(
					ClipboardListener.class.getName(), null);
		} catch (InvalidSyntaxException e) {
		}
		if (references != null) {
			for (ServiceReference ref : references) {
				ClipboardListener listener = (ClipboardListener) bundleContext
						.getService(ref);
				try {
					if (listener != null) {
						listener.onEvent(event);
					}
				} catch (Throwable t) {
				} finally {
					bundleContext.ungetService(ref);
				}
			}
		}
	}
}