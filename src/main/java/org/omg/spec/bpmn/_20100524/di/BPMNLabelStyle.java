//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 generiert 
// Siehe <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2023.05.14 um 03:43:47 PM CEST 
//


package org.omg.spec.bpmn._20100524.di;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import org.omg.spec.dd._20100524.dc.Font;
import org.omg.spec.dd._20100524.di.Style;


/**
 * <p>Java-Klasse für BPMNLabelStyle complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="BPMNLabelStyle">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.omg.org/spec/DD/20100524/DI}Style">
 *       &lt;sequence>
 *         &lt;element ref="{http://www.omg.org/spec/DD/20100524/DC}Font"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "BPMNLabelStyle", propOrder = {
    "font"
})
public class BPMNLabelStyle
    extends Style
{

    @XmlElement(name = "Font", namespace = "http://www.omg.org/spec/DD/20100524/DC", required = true)
    protected Font font;

    /**
     * Ruft den Wert der font-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Font }
     *     
     */
    public Font getFont() {
        return font;
    }

    /**
     * Legt den Wert der font-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Font }
     *     
     */
    public void setFont(Font value) {
        this.font = value;
    }

}
