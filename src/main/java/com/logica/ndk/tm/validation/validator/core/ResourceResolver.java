/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.logica.ndk.tm.validation.validator.core;

import com.logica.ndk.tm.cdm.XslInputStream;
import java.io.InputStream;
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSResourceResolver;

/**
 *
 * @author brizat
 */
public class ResourceResolver {
    
    private static ResourceResolver instance;
    
    private LSResourceResolver resolver;
            
    public static ResourceResolver instance(){
        if(instance == null){
            instance = new ResourceResolver();
        }
        return instance;
    }

    protected ResourceResolver() {
        this.resolver = new LSResourceResolver() {

            @Override
            public LSInput resolveResource(String type, String namespaceURI, String publicId, String systemId, String baseURI) {
                InputStream resourceAsStream = ResourceResolver.class.getClassLoader().getResourceAsStream(systemId);
                return new XslInputStream(publicId, systemId, resourceAsStream);
            }
        };
    }

    public LSResourceResolver getResolver() {
        return resolver;
    }
    
}
