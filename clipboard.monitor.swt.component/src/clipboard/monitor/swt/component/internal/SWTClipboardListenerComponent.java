package clipboard.monitor.swt.component.internal;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.swt.dnd.TransferData;
import org.osgi.framework.FrameworkUtil;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.osgi.service.log.LogService;

import clipboard.monitor.swt.AbstractSWTClipboardListener;
import clipboard.monitor.swt.ClipboardMimeTypeResolver;

/**
 * OSGi component ClipboardListener implementation that queries the clipboard
 * for the available formats and publishes an event with the
 * <code>clipboard/monitor/swt</code> topic.
 * 
 * <h2>Event properties</h2>
 * <ul>
 * <code>
 * <li>mime-type</li>
 * <li>native-type</li>
 * </code>
 * </ul>
 * 
 * @author <a href="mailto:phil.kursawe@gmail.com">Philipp Kursawe</a>
 * 
 */
public class SWTClipboardListenerComponent extends AbstractSWTClipboardListener {
	private static final String TOPIC = "clipboard/monitor/swt"; //$NON-NLS-1$

	private final AtomicReference<EventAdmin> eventAdminRef = new AtomicReference<EventAdmin>();
	private final AtomicReference<LogService> logRef = new AtomicReference<LogService>();

	private ComponentContext context;

	@Override
	protected void processEvent(String[] mimeTypes, String[] typeNames) {
		EventAdmin eventAdmin = eventAdminRef.get();
		if (eventAdmin != null) {
			Map<String, Object> props = new HashMap<String, Object>();
			props.put("mime-type", mimeTypes);
			props.put("native-type", typeNames);
			eventAdmin.postEvent(new Event(TOPIC, props));
		}
	}

	protected void activate(ComponentContext context) {
		this.context = context;
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

	/**
	 * Logs a message and the (optionally) Throwable with the OSGi LogService
	 * (if available). If the LogService is not available it calls its super
	 * method.
	 */
	@Override
	protected void log(String message, Throwable t) {
		final LogService log = logRef.get();
		if (log != null) {
			log.log(context.getServiceReference(),
					t != null ? LogService.LOG_ERROR : LogService.LOG_DEBUG,
					message, t);
		} else {
			super.log(message, t);
		}
	}

	@Override
	protected void resolveMimeTypes(TransferData[] transferData,
			Set<String> mimeTypes) {
		super.resolveMimeTypes(transferData, mimeTypes);
		// Query additional services that can help to resolve MIME-Types
		Object resolvers[] = context
				.locateServices(ClipboardMimeTypeResolver.class.getSimpleName());
		if (resolvers != null) {
			for (TransferData data : transferData) {
				for (Object resolver : resolvers) {
					try {
						((ClipboardMimeTypeResolver) resolver)
								.resolveMimeTypes(data, mimeTypes);
					} catch (Throwable t) {
						log(
								String.format(
										"Error resolving MIME-Type with resolver %s from %s",
										resolver.getClass().getName(),
										FrameworkUtil.getBundle(
												resolver.getClass())
												.getSymbolicName()), t);
					}
				}
			}
		}
	}
}
