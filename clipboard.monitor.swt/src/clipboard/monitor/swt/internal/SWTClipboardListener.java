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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.HTMLTransfer;
import org.eclipse.swt.dnd.ImageTransfer;
import org.eclipse.swt.dnd.RTFTransfer;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.swt.dnd.URLTransfer;
import org.eclipse.swt.widgets.Display;
import org.osgi.framework.ServiceReference;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.osgi.service.log.LogService;

import clipboard.monitor.ClipboardEvent;
import clipboard.monitor.ClipboardListener;

/**
 * ClipboardListener implementation that queries the clipboard for the available
 * formats and publishes specialized events under the parent
 * <code>eclipseutils/ui/clipboard/monitor/swt/</code> topic.
 * 
 * <ul>
 * <code>
 * <li>eclipseutils/ui/clipboard/monitor/swt/TEXT</li>
 * <li>eclipseutils/ui/clipboard/monitor/swt/URL</li>
 * <li>eclipseutils/ui/clipboard/monitor/swt/IMAGE</li>
 * <li>eclipseutils/ui/clipboard/monitor/swt/RTF</li>
 * <li>eclipseutils/ui/clipboard/monitor/swt/HTML</li>
 * <li>eclipseutils/ui/clipboard/monitor/swt/FILE</li>
 * </code>
 * </ul>
 * 
 * @author <a href="mailto:phil.kursawe@gmail.com">Philipp Kursawe</a>
 * 
 */
public class SWTClipboardListener implements ClipboardListener {

	private static final String BASE_TOPIC = "eclipseutils/ui/clipboard/monitor/swt"; //$NON-NLS-1$
	private final AtomicReference<EventAdmin> eventAdminRef = new AtomicReference<EventAdmin>();
	private final AtomicReference<LogService> logRef = new AtomicReference<LogService>();
	private Map<Transfer, String> transferTopicMap;
	private ServiceReference ref;

	interface TypeRunnable {
		void run(TransferData[] transferData, String[] typeNames);
	}

	public void onEvent(ClipboardEvent event) {
		final EventAdmin eventAdmin = eventAdminRef.get();
		if (eventAdmin != null) {
			getTypes(new TypeRunnable() {
				public void run(TransferData[] transferData, String[] typeNames) {
					Set<Event> events = new HashSet<Event>();
					createTransferDataEvents(events, transferData);
					createTypeEvents(events, typeNames);
					logEvents(events);
					for (Event event : events) {
						eventAdmin.postEvent(event);
					}
				}
			});
		}
	}

	protected void activate(ServiceReference ref) {
		this.ref = ref;
	}

	protected void bind(EventAdmin eventAdmin) {
		eventAdminRef.set(eventAdmin);
	}

	protected void unbind(EventAdmin eventAdmin) {
		eventAdminRef.compareAndSet(eventAdmin, null);
	}

	protected void bind(LogService logService) {
		logRef.set(logService);
	}

	protected void unbind(LogService logService) {
		logRef.compareAndSet(logService, null);
	}

	private void getTypes(final TypeRunnable runnable) {
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				Clipboard clipboard = new Clipboard(Display.getDefault());
				final TransferData[] data;
				final String[] types;
				try {
					data = clipboard.getAvailableTypes();
					types = clipboard.getAvailableTypeNames();
				} catch (Throwable t) {
					log("Could not query clipboard", t); //$NON-NLS-1$
					return;
				} finally {
					clipboard.dispose();
				}
				// Spawn a new thread, so the UI thread is no longer busy when
				// we process the clipboard data
				Thread thread = new Thread(new Runnable() {
					public void run() {
						runnable.run(data, types);
					}
				}, "Clipboard Change Dispatcher"); //$NON-NLS-1$
				thread.setDaemon(true);
				thread.start();
			}
		});
	}

	private void log(String message) {
		log(message, null);
	}

	private void log(String message, Throwable t) {
		final LogService log = logRef.get();
		if (log != null) {
			log.log(ref, t != null ? LogService.LOG_ERROR
					: LogService.LOG_DEBUG, message, t);
		}
	}

	private Event getEventForTransferData(TransferData data) {
		if (transferTopicMap == null) {
			transferTopicMap = new HashMap<Transfer, String>();
			transferTopicMap.put(TextTransfer.getInstance(), "TEXT"); //$NON-NLS-1$
			transferTopicMap.put(URLTransfer.getInstance(), "URL"); //$NON-NLS-1$
			transferTopicMap.put(ImageTransfer.getInstance(), "IMAGE"); //$NON-NLS-1$
			transferTopicMap.put(RTFTransfer.getInstance(), "RTF"); //$NON-NLS-1$
			transferTopicMap.put(HTMLTransfer.getInstance(), "HTML"); //$NON-NLS-1$
			transferTopicMap.put(FileTransfer.getInstance(), "FILE"); //$NON-NLS-1$
		}
		for (Transfer transfer : transferTopicMap.keySet()) {
			if (transfer.isSupportedType(data)) {
				return new Event(BASE_TOPIC + '/'
						+ transferTopicMap.get(transfer), (Map<?, ?>) null);
			}
		}
		return null;
	}

	private void createTransferDataEvents(Set<Event> events,
			TransferData[] transferData) {
		for (TransferData data : transferData) {
			Event event = getEventForTransferData(data);
			if (event != null) {
				events.add(event);
			}
		}
	}

	private void createTypeEvents(Set<Event> events, String[] typeNames) {
		for (String type : typeNames) {
			try {
				events.add(new Event(BASE_TOPIC + '/' + type.replace(' ', '_'),
						(Map<?, ?>) null));
			} catch (IllegalArgumentException e) {
				log("Could not generate valid event topic", e); //$NON-NLS-1$
			}
		}
	}

	private void logEvents(Set<Event> events) {
		final LogService log = logRef.get();
		if (log != null) {
			for (Event event : events) {
				log(String.format("Sending event: %s", event.getTopic())); //$NON-NLS-1$
			}
		}
	}
}
