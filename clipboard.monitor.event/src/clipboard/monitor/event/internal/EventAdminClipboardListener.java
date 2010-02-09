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
package clipboard.monitor.event.internal;

import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;

import clipboard.monitor.ClipboardEvent;
import clipboard.monitor.ClipboardListener;

/**
 * ClipboardListener implementation that publishes an event with the
 * <code>eclipseutils/ui/clipboard/monitor/event</code> topic with the
 * EventAdmin. The event has no properties.
 * 
 * @author <a href="mailto:phil.kursawe@gmail.com">Philipp Kursawe</a>
 * 
 */
public class EventAdminClipboardListener implements ClipboardListener {
	private static final String TOPIC = "eclipseutils/ui/clipboard/monitor/event"; //$NON-NLS-1$

	private final AtomicReference<EventAdmin> ref = new AtomicReference<EventAdmin>();

	protected void bind(EventAdmin eventAdmin) {
		ref.set(eventAdmin);
	}

	protected void unbind(EventAdmin eventAdmin) {
		ref.compareAndSet(eventAdmin, null);
	}

	public void onEvent(ClipboardEvent event) {
		EventAdmin eventAdmin = ref.get();
		if (eventAdmin != null) {
			eventAdmin.postEvent(new Event(TOPIC, (Map<?, ?>) null));
		}
	}
}
