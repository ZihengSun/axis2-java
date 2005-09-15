package org.apache.axis2.interopt.whitemesa.round2.util.soap12;

import org.apache.axis2.soap.SOAPEnvelope;
import org.apache.axis2.soap.SOAPFactory;
import org.apache.axis2.soap.SOAPBody;
import org.apache.axis2.om.OMAbstractFactory;
import org.apache.axis2.om.OMElement;
import org.apache.axis2.interopt.whitemesa.round2.util.SunRound2ClientUtil;

/**
 * Created by IntelliJ IDEA.
 * User: Gayan
 * Date: Sep 6, 2005
 * Time: 10:35:41 AM
 * To change this template use File | Settings | File Templates.
 */
public class GroupbSoap12EchoNestedArrayUtil implements SunRound2ClientUtil {

     public SOAPEnvelope getEchoSoapEnvelope() {

        SOAPFactory omfactory = OMAbstractFactory.getSOAP12Factory();
        SOAPEnvelope reqEnv = omfactory.createSOAPEnvelope();
        //reqEnv.declareNamespace("http://schemas.xmlsoap.org/soap/envelope/", "soapenv");
        //reqEnv.declareNamespace("http://schemas.xmlsoap.org/wsdl/", "xmlns");
        //reqEnv.declareNamespace("http://schemas.xmlsoap.org/wsdl/soap/", "soap");
        reqEnv.declareNamespace("http://www.w3.org/2001/XMLSchema", "xsd");
        reqEnv.declareNamespace("http://schemas.xmlsoap.org/soap/encoding/", "SOAP-ENC");
        reqEnv.declareNamespace("http://soapinterop.org/", "tns");
        reqEnv.declareNamespace("http://soapinterop.org/xsd", "s");
        //reqEnv.declareNamespace("http://schemas.xmlsoap.org/wsdl/", "wsdl");
        reqEnv.declareNamespace("http://www.w3.org/2001/XMLSchema-instance","xsi");

        OMElement operation = omfactory.createOMElement("echoNestedArray", "http://soapinterop.org/", null);
        SOAPBody body = omfactory.createSOAPBody(reqEnv);
        body.addChild(operation);
        operation.addAttribute("soapenv:encodingStyle", "http://www.w3.org/2003/05/soap-encoding", null);

        OMElement part = omfactory.createOMElement("inputStruct", "", null);
        part.addAttribute("xsi:type", "s:SOAPStruct", null);


        OMElement value0 = omfactory.createOMElement("varString", "", null);
        value0.addAttribute("xsi:type", "xsd:string", null);
        value0.addChild(omfactory.createText("strss fdfing1"));

        OMElement value1 = omfactory.createOMElement("varInt", "", null);
        value1.addAttribute("xsi:type", "xsd:int", null);
        value1.addChild(omfactory.createText("25"));

        OMElement value2 = omfactory.createOMElement("varFloat", "", null);
        value2.addAttribute("xsi:type", "xsd:float", null);
        value2.addChild(omfactory.createText("25.23"));

        OMElement value3 = omfactory.createOMElement("varArray", "", null);
        part.addAttribute("xsi:type", "s:SOAPArrayStruct", null);
        value3.addAttribute("SOAP-ENC:arrayType", "xsd:string[3]", null);

        OMElement value30 = omfactory.createOMElement("item", "", null);
        value30.addAttribute("xsi:type", "xsd:string", null);
        value30.addChild(omfactory.createText("strss fdfing1"));

        OMElement value31 = omfactory.createOMElement("item", "", null);
        value31.addAttribute("xsi:type", "xsd:string", null);
        value31.addChild(omfactory.createText("strss fdfing2"));

        OMElement value32 = omfactory.createOMElement("item", "", null);
        value32.addAttribute("xsi:type", "xsd:string", null);
        value32.addChild(omfactory.createText("strss fdfing3"));



        value3.addChild(value30);
        value3.addChild(value31);
        value3.addChild(value32);

        part.addChild(value0);
        part.addChild(value1);
        part.addChild(value2);
        part.addChild(value3);


        operation.addChild(part);

        //reqEnv.getBody().addChild(method);
        return reqEnv;

    }
}
