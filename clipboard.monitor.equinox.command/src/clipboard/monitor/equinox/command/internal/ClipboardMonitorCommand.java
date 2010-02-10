package clipboard.monitor.equinox.command.internal;

import org.eclipse.osgi.framework.console.CommandInterpreter;
import org.eclipse.osgi.framework.console.CommandProvider;
import org.osgi.service.component.ComponentContext;

import clipboard.monitor.ClipboardMonitor;

public class ClipboardMonitorCommand implements CommandProvider {

	private ComponentContext context;

	public void _cbm(CommandInterpreter ci) {
		String arg = ci.nextArgument();
		if ("start".equals(arg)) { //$NON-NLS-1$
			start(ci);
		} else if ("stop".equals(arg)) { //$NON-NLS-1$
			stop(ci);
		} else {
			ci.print(getHelp());
			return;
		}
	}

	public String getHelp() {
		return "---Clipboard Monitor---\n\tcbm [start|stop] - Starts or stops all clipboard monitors\n"; //$NON-NLS-1$
	}

	protected void activate(ComponentContext context) {
		this.context = context;
	}

	private interface Visitor {
		void visit(ClipboardMonitor monitor);
	}

	private void stop(CommandInterpreter ci) {
		visitMonitors(ci, new Visitor() {
			public void visit(ClipboardMonitor monitor) {
				monitor.stop();
			}
		});
	}

	private void start(CommandInterpreter ci) {
		visitMonitors(ci, new Visitor() {
			public void visit(ClipboardMonitor monitor) {
				monitor.start();
			}
		});
	}

	private void visitMonitors(CommandInterpreter ci, Visitor visitor) {
		Object monitors[] = context.locateServices(ClipboardMonitor.class
				.getSimpleName());
		if (monitors != null) {
			for (Object monitor : monitors) {
				try {
					visitor.visit((ClipboardMonitor) monitor);
				} catch (Throwable t) {
					ci.printStackTrace(t);
				}
			}
		}
	}

}
