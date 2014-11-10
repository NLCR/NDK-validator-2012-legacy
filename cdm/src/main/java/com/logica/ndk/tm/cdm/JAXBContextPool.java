package com.logica.ndk.tm.cdm;

import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

public class JAXBContextPool {

		private final static Map<String, JAXBContext> JAXB_CONTEXT_MAP = new HashMap<String, JAXBContext>();
		
		public static synchronized JAXBContext getContext(String contextPath) throws JAXBException {
			JAXBContext context = JAXB_CONTEXT_MAP.get(contextPath);
			if (context == null) {
				context = JAXBContext.newInstance(contextPath);
				JAXB_CONTEXT_MAP.put(contextPath, context);
			}
			return context;
		}

		public static synchronized JAXBContext getContext(Class clazz) throws JAXBException {
			JAXBContext context = JAXB_CONTEXT_MAP.get(clazz.getName());
			if (context == null) {
				context = JAXBContext.newInstance(clazz);
				JAXB_CONTEXT_MAP.put(clazz.getName(), context);
			}
			return context;
		}
	}


