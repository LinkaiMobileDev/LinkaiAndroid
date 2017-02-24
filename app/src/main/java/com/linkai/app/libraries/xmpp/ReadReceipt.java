package com.linkai.app.libraries.xmpp;

import org.jivesoftware.smack.packet.ExtensionElement;
import org.jivesoftware.smack.provider.EmbeddedExtensionProvider;

import java.util.List;
import java.util.Map;

/**
 * Created by LP1001 on 17-02-2017.
 */ //    class for read receipt
public class ReadReceipt implements ExtensionElement {

    private String receiptId;
    public static final String NAMESPACE = "urn:xmpp:read";
    public static final String ELEMENT = "read";

    public ReadReceipt(String _receiptId) {
        this.receiptId = _receiptId;
    }

    @Override
    public String getNamespace() {
        return NAMESPACE;
    }

    @Override
    public String getElementName() {
        return ELEMENT;
    }

    @Override
    public CharSequence toXML() {
        return "<read xmlns='" + NAMESPACE + "' id='" + this.receiptId + "'/>";
    }

    public String getReceiptId(){
        return this.receiptId;
    }

    public static class ReadReceiptProvider extends EmbeddedExtensionProvider {
        @Override
        protected ExtensionElement createReturnExtension(String currentElement, String currentNamespace, Map attributeMap, List content) {
            return new ReadReceipt(attributeMap.get("id").toString());
        }
    }

}
