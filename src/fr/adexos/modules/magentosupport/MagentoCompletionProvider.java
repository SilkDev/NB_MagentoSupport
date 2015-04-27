package fr.adexos.modules.magentosupport;

import fr.adexos.modules.magentosupport.Magento.MagentoModel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.JTextComponent;
import javax.swing.text.StyledDocument;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.spi.editor.completion.CompletionProvider;
import org.netbeans.spi.editor.completion.CompletionResultSet;
import org.netbeans.spi.editor.completion.CompletionTask;
import org.netbeans.spi.editor.completion.support.AsyncCompletionQuery;
import org.netbeans.spi.editor.completion.support.AsyncCompletionTask;
import org.openide.util.Exceptions;

/**
 *
 * @author Loïc HALL <lhall@adexos.fr>
 */
@MimeRegistration(mimeType = "text/x-php5", service = CompletionProvider.class)
public class MagentoCompletionProvider implements CompletionProvider {

    
    /**
     * On startup, build the main collection of Magento classes
     */
    public MagentoCompletionProvider() {
        // Models
        HashMap MageModels = new HashMap<String, MagentoModel>();
        MageModels.put("catalog/product", new MagentoModel("catalog/product"));
        
        // Helpers
        
    }

    @Override
    public CompletionTask createTask(int queryType, JTextComponent jtc) {

        if (queryType != CompletionProvider.COMPLETION_QUERY_TYPE) {
            return null;
        }

        return new AsyncCompletionTask(new AsyncCompletionQuery() {
            @Override
            protected void query(CompletionResultSet completionResultSet, Document document, int caretOffset) {
                String filter = null;
                int startOffset = caretOffset - 1;

                try {
                    final StyledDocument bDoc = (StyledDocument) document;
                    final int lineStartOffset = getRowFirstNonWhite(bDoc, caretOffset);
                    final char[] line = bDoc.getText(lineStartOffset, caretOffset - lineStartOffset).toCharArray();
                    final int whiteOffset = indexOfWhite(line);
                    filter = new String(line, whiteOffset + 1, line.length - whiteOffset - 1);
                    // Filter out all non Magento stuff
                    if (!filter.startsWith("Mage::")) {
                        System.out.println("MageSupport : Text is not related");
                        completionResultSet.finish();
                        return;
                    }
                    System.out.println(filter);
                    if (whiteOffset > 0) {
                        startOffset = lineStartOffset + whiteOffset + 1;
                    } else {
                        startOffset = lineStartOffset;
                    }
                } catch (BadLocationException ex) {
                    Exceptions.printStackTrace(ex);
                }

                if (filter.matches("/Mage::getModel/")) {
                    // Find DocBlock
                    
                    completionResultSet.addItem(new MageCompletionItem("", startOffset, caretOffset));
                } else if (filter.matches("/Mage::helper/")) {
                    
                } else {
                    System.out.println("MageSupport : Nothing to do");
                }
                
                completionResultSet.finish();
            }
        }, jtc);

    }

    @Override
    public int getAutoQueryTypes(JTextComponent component, String string) {
        return 0;
    }

    static int getRowFirstNonWhite(StyledDocument doc, int offset)
            throws BadLocationException {
        Element lineElement = doc.getParagraphElement(offset);
        int start = lineElement.getStartOffset();
        while (start + 1 < lineElement.getEndOffset()) {
            try {
                if (doc.getText(start, 1).charAt(0) != ' ') {
                    break;
                }
            } catch (BadLocationException ex) {
                throw (BadLocationException) new BadLocationException(
                        "calling getText(" + start + ", " + (start + 1)
                        + ") on doc of length: " + doc.getLength(), start
                ).initCause(ex);
            }
            start++;
        }
        return start;
    }

    static int indexOfWhite(char[] line) {
        int i = line.length;
        while (--i > -1) {
            final char c = line[i];
            if (Character.isWhitespace(c)) {
                return i;
            }
        }
        return -1;
    }
}
