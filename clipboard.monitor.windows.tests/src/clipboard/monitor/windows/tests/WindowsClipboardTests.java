package clipboard.monitor.windows.tests;

import org.junit.Assert;
import org.junit.Test;

import clipboard.monitor.ClipboardEvent;
import clipboard.monitor.ClipboardListener;
import clipboard.monitor.windows.WindowsClipboardMonitor;

public class WindowsClipboardTests {

	class WindowsClipboardTester extends WindowsClipboardMonitor {
		int getListenerSize() {
			return this.getListeners().size();
		}
	}

	@Test
	public void testListenerAddingAndRemoving() {
		ClipboardListener listener1 = new ClipboardListener() {
			public void onEvent(ClipboardEvent event) {
			}
		};

		ClipboardListener listener2 = new ClipboardListener() {
			public void onEvent(ClipboardEvent event) {
			}
		};

		WindowsClipboardTester tester = new WindowsClipboardTester();
		tester.addListener(listener1);
		Assert.assertEquals(1, tester.getListenerSize());

		tester.addListener(listener2);
		Assert.assertEquals(2, tester.getListenerSize());

		tester.addListener(listener1);
		Assert.assertEquals(2, tester.getListenerSize());

		tester.removeListener(listener1);
		Assert.assertEquals(1, tester.getListenerSize());

		tester.removeListener(listener1);
		Assert.assertEquals(1, tester.getListenerSize());
	}
}
