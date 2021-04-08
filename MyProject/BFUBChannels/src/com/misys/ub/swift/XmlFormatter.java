package com.misys.ub.swift;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Node;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSOutput;
import org.w3c.dom.ls.LSSerializer;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class XmlFormatter {
	private static final String XML_LINEARIZATION_REGEX = "(>|&gt;){1,1}(\\t)*(\\n|\\r)+(\\s)*(<|&lt;){1,1}";

	private static final String XML_LINEARIZATION_REPLACEMENT = "$1$5";

	public String format(String xml) throws ClassNotFoundException,
			InstantiationException, IllegalAccessException, ClassCastException,
			SAXException, IOException, ParserConfigurationException {
		Writer stringWriter = new StringWriter();
		if (xml != null) {
			xml = xml.trim().replaceAll(XML_LINEARIZATION_REGEX,
					XML_LINEARIZATION_REPLACEMENT);
			final InputSource src = new InputSource(new StringReader(xml));
			final Node document = DocumentBuilderFactory.newInstance()
					.newDocumentBuilder().parse(src).getDocumentElement();
			final Boolean keepDeclaration = Boolean
					.valueOf(xml.startsWith("<?xml"));

			
			final DOMImplementationRegistry registry = DOMImplementationRegistry
					.newInstance();
			final DOMImplementationLS impl = (DOMImplementationLS) registry
					.getDOMImplementation("LS");
			final LSSerializer writer = impl.createLSSerializer();

			writer.getDomConfig().setParameter("format-pretty-print", Boolean.TRUE); 
			writer.getDomConfig().setParameter("xml-declaration", keepDeclaration); 
			LSOutput lsOutput = impl.createLSOutput();
			lsOutput.setEncoding("UTF-8");
			lsOutput.setCharacterStream(stringWriter);
			writer.write(document, lsOutput);
		}
		return stringWriter.toString();
	}

}
