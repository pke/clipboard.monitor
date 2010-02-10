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
package clipboard.monitor.swt;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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

import clipboard.monitor.ClipboardEvent;
import clipboard.monitor.ClipboardListener;

/**
 * ClipboardListener implementation that queries the clipboard for the available
 * formats.
 * 
 * <p>
 * Subclasses must decide what to do with the available formats by implementing
 * {@link #processEvent(String[], String[])}.
 * 
 * @author <a href="mailto:phil.kursawe@gmail.com">Philipp Kursawe</a>
 * 
 */
public abstract class AbstractSWTClipboardListener implements ClipboardListener {
	private Map<Transfer, String> mimeTypesMapping;

	protected interface TypeRunnable {
		void run(TransferData[] transferData, String[] typeNames);
	}

	public void onEvent(ClipboardEvent event) {
		getTypes(new TypeRunnable() {
			public void run(TransferData[] transferData, String[] typeNames) {
				final Set<String> mimeTypes = new HashSet<String>();
				resolveMimeTypes(transferData, mimeTypes);

				processEvent(mimeTypes.toArray(new String[mimeTypes.size()]),
						typeNames);
			}
		});
	}

	/**
	 * Processes the given MIME-Types and native type names.
	 * 
	 * @param mimeTypes
	 *            Unique list of detected MIME-Types.
	 * @param typeNames
	 */
	protected abstract void processEvent(String[] mimeTypes, String[] typeNames);

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

	/**
	 * Logs a message and (optionally) a Throwable to the standard system output
	 * stream.
	 * 
	 * <p>
	 * Errors are logged to the system error output stream.
	 * 
	 * @param message
	 *            to log
	 * @param t
	 *            Throwable that is associated with the message
	 * @override
	 */
	protected void log(String message, Throwable t) {
		if (t != null) {
			System.err.println(message);
			t.printStackTrace(System.err);
		} else {
			System.out.println(message);
		}
	}

	/**
	 * Resolves some standard MIME-Types from the given transfer dates.
	 * 
	 * @param transferData
	 * @param mimeTypes
	 * @subclass.override
	 */
	protected void resolveMimeTypes(TransferData[] transferData,
			Set<String> mimeTypes) {
		if (mimeTypesMapping == null) {
			mimeTypesMapping = new HashMap<Transfer, String>();
			mimeTypesMapping.put(TextTransfer.getInstance(), "text/plain"); //$NON-NLS-1$
			mimeTypesMapping.put(URLTransfer.getInstance(), "text/url"); //$NON-NLS-1$
			mimeTypesMapping.put(ImageTransfer.getInstance(), "image"); //$NON-NLS-1$
			mimeTypesMapping.put(RTFTransfer.getInstance(), "text/richtext"); //$NON-NLS-1$
			mimeTypesMapping.put(HTMLTransfer.getInstance(), "text/html"); //$NON-NLS-1$
			mimeTypesMapping
					.put(FileTransfer.getInstance(), "application/file"); //$NON-NLS-1$
		}
		for (Transfer transfer : mimeTypesMapping.keySet()) {
			for (TransferData data : transferData) {
				if (transfer.isSupportedType(data)) {
					mimeTypes.add(mimeTypesMapping.get(transfer));
				}
			}
		}
	}
}
