//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 generiert 
// Siehe <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2023.05.14 um 03:43:47 PM CEST 
//


package org.omg.spec.bpmn._20100524.model;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse für tChoreographyLoopType.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * <p>
 * <pre>
 * &lt;simpleType name="tChoreographyLoopType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="None"/>
 *     &lt;enumeration value="Standard"/>
 *     &lt;enumeration value="MultiInstanceSequential"/>
 *     &lt;enumeration value="MultiInstanceParallel"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "tChoreographyLoopType")
@XmlEnum
public enum TChoreographyLoopType {

    @XmlEnumValue("None")
    NONE("None"),
    @XmlEnumValue("Standard")
    STANDARD("Standard"),
    @XmlEnumValue("MultiInstanceSequential")
    MULTI_INSTANCE_SEQUENTIAL("MultiInstanceSequential"),
    @XmlEnumValue("MultiInstanceParallel")
    MULTI_INSTANCE_PARALLEL("MultiInstanceParallel");
    private final String value;

    TChoreographyLoopType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static TChoreographyLoopType fromValue(String v) {
        for (TChoreographyLoopType c: TChoreographyLoopType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
