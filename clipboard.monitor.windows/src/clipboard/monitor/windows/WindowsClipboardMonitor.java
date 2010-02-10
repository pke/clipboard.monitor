package clipboard.monitor.windows;

import java.util.HashSet;
import java.util.Set;

import clipboard.monitor.ClipboardEvent;
import clipboard.monitor.ClipboardListener;

/**
 * @noosgi
 * This class can be used without OSGi.
 * @author <a href="mailto:phil.kursawe@gmail.com">Philipp Kursawe</a>
 */
public class WindowsClipboardMonitor extends AbstractWindowsClipboardMonitor {

	private Set<ClipboardListener> listeners;
	
	public void onChange(final ClipboardEvent event) {
		if (listeners != null) {
		for (ClipboardListener listener : listeners) {
			try {
				listener.onEvent(event);
			} catch (Throwable t) {
				t.printStackTrace();
			}
		}
		}
	}
	
	void addListener(ClipboardListener listener) {
		getListeners().add(listener);
	}
	
	void removeListener(ClipboardListener listener) {
		getListeners().remove(listener);
	}
	
	private Set<ClipboardListener> getListeners() {
		if (null == listeners) {
			listeners = new HashSet<ClipboardListener>();
		}
		return listeners;
	}
}
