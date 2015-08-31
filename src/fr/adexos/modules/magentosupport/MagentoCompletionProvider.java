package fr.adexos.modules.magentosupport;

import fr.adexos.modules.magentosupport.Magento.MagentoModel;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.event.DocumentListener;
import javax.swing.event.UndoableEditListener;
import javax.swing.text.AbstractDocument.Content;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.JTextComponent;
import javax.swing.text.PlainDocument;
import javax.swing.text.Position;
import javax.swing.text.Segment;
import javax.swing.text.StyledDocument;
import jdk.nashorn.internal.objects.NativeString;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.modules.php.editor.completion.PhpTypeCompletionProviderWrapper;
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

    
    public MagentoCompletionProvider() {
        
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
                        System.out.println("MageSupport : Text is not related - " + filter);
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

                if (filter.startsWith("Mage::")) {
                    try {
                        Pattern p = Pattern.compile("Mage::(.*)\\('(.*)'\\)(.*)");
                        String s =  filter;
                        Matcher m = p.matcher(s) ;
                        String completionClass, completionType;
                        
                        if( m.find() && m.groupCount() > 0) {
                            System.out.println("groupes " + m.groupCount());
                            for (int i = 0; i <= m.groupCount(); ++i) {
                                System.out.println("groupe " + i + " :" + m.group(i));
                            }
                            completionType = m.group(1);
                            completionClass = m.group(2);
                            
                            // Mimic completion for original class
                            JTextComponent PHPJTC = new JTextComponent();
                            PHPJTC.setText("Mage_Core_Helper_Data");
                            CompletionProvider PHPProvider = new PhpTypeCompletionProviderWrapper();
                            System.out.println("MageSupport : Fetching for " + completionClass + " of type " + completionType);
                            completionResultSet.addItem(new MageCompletionItem("", startOffset, caretOffset));
                        } else {
                            System.out.println("No match found");
                        }
                    } catch (Exception ex) {
                        Exceptions.printStackTrace(ex);
                    }
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
