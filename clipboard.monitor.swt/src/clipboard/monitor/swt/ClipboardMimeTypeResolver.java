/**
 * 
 */
package clipboard.monitor.swt;

import java.util.Set;

import org.eclipse.swt.dnd.TransferData;

/**
 * Resolves MIME-Types from SWT Clipboard TransferData.
 * 
 * @author <a href="mailto:phil.kursawe@gmail.com">Philipp Kursawe</a>
 *
 */
public interface ClipboardMimeTypeResolver {
	/**
	 * Extracts possible MIME-Types out of a TransferData.
	 * 
	 * @param transferData to be examined for MIME-Types
	 * @param mimeTypes set to add new MIME-Types to.
	 */
	void resolveMimeTypes(TransferData transferData, Set<String> mimeTypes);
}
