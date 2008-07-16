/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.axis2.jaxws.server.dispatcher;

import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.jaxws.Constants;
import org.apache.axis2.jaxws.WebServiceExceptionLogger;
import org.apache.axis2.jaxws.ExceptionFactory;
import org.apache.axis2.jaxws.core.MessageContext;
import org.apache.axis2.jaxws.server.EndpointCallback;
import org.apache.axis2.jaxws.server.EndpointInvocationContext;
import org.apache.axis2.jaxws.server.InvocationHelper;
import org.apache.axis2.jaxws.server.InvocationListener;
import org.apache.axis2.jaxws.server.InvocationListenerBean;
import org.apache.axis2.jaxws.utility.ClassUtils;
import org.apache.axis2.jaxws.utility.JavaUtils;
import org.apache.axis2.transport.TransportUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.Callable;
/**
 * JavaDispatcher is an abstract class that can be extended to implement an EndpointDispatcher to a
 * Java object.
 */
public abstract class JavaDispatcher implements EndpointDispatcher {

    private static final Log log = LogFactory.getLog(JavaDispatcher.class);

    protected Class serviceImplClass;
    protected Object serviceInstance;

    protected JavaDispatcher(Class impl, Object serviceInstance) {
        this.serviceImplClass = impl;
        this.serviceInstance = serviceInstance;
    }

    public abstract MessageContext invoke(MessageContext request) throws Exception;
    
    public abstract void invokeOneWay(MessageContext request);
    
    public abstract void invokeAsync(MessageContext request, EndpointCallback callback);

    protected abstract MessageContext createResponse(MessageContext request, Object[] input, Object output);
    
    protected abstract MessageContext createFaultResponse(MessageContext request, Throwable fault);
    
    
    public Class getServiceImplementationClass() {
        return serviceImplClass;
    }
    
    protected final Object invokeTargetOperation(Method method, Object[] args) throws Throwable {
        Object output = null;
        try {
            output = method.invoke(serviceInstance, args);
        } catch (Throwable t) {
            
            // Delegate logging the exception to the WebServiceExceptionLogger.
            // Users can specifiy debug tracing of the WebServiceExceptionLogger to see
            // all exceptions.
            // Otherwise the WebServiceExceptionLogger only logs errors for non-checked exceptions
            WebServiceExceptionLogger.log(method, 
                                          t,
                                          false,
                                          serviceImplClass,
                                          serviceInstance,
                                          args);
                                                         
            throw t;
        }
        
        return output;
    }
    
    
    protected class AsyncInvocationWorker implements Callable {
        
        private Method method;
        private Object[] params;
        private ClassLoader classLoader;
        private EndpointInvocationContext eic;
        
        public AsyncInvocationWorker(Method m, Object[] p, ClassLoader cl, EndpointInvocationContext ctx) {
            method = m;
            params = p;
            classLoader = cl;
            eic = ctx;
        }
        
        public Object call() throws Exception {
            try {
                if (log.isDebugEnabled()) {
                    log.debug("Invoking target endpoint via the async worker.");
                }
                
                // Set the proper class loader so that we can properly marshall the
                // outbound response.
                ClassLoader currentLoader = Thread.currentThread().getContextClassLoader();
                if (classLoader != null && (classLoader != currentLoader)) {
                    Thread.currentThread().setContextClassLoader(classLoader);
                    if (log.isDebugEnabled()) {
                        log.debug("Context ClassLoader set to:" + classLoader);
                    }
                }
                
                // We have the method that is going to be invoked and the parameter data to invoke it 
                // with, so just invoke the operation.
                Object output = null;
                boolean faultThrown = false;
                Throwable fault = null;
                try {
                    output = invokeTargetOperation(method, params);
                } 
                catch (Exception e) {
                    fault = ClassUtils.getRootCause(e);
                    Throwable newFault = InvocationHelper.determineMappedException(fault, eic);
                    if(newFault != null) {
                        fault = newFault;
                    }
                    faultThrown = true;
                }
                
                // If this is a one way invocation, we are done and just need to return.
                if (eic.isOneWay()) {
                    if (log.isDebugEnabled()) {
                        log.debug("Invocation pattern was one way, work complete.");
                    }
                    
                    responseReady(eic);
                    return null;
                }
                
                // Create the response MessageContext
                MessageContext request = eic.getRequestMessageContext();
                MessageContext response = null;
                if (faultThrown) {
                    // If a fault was thrown, we need to create a slightly different
                    // MessageContext, than in the response path.
                    response = createFaultResponse(request, fault);
                    setCheckedExceptionProperty(response, method, fault);
                } else {
                    if (log.isDebugEnabled()) {
                        log.debug("Async invocation of the endpoint was successful.  Creating response message.");
                    }
                    response = createResponse(request, params, output);
                }

                EndpointInvocationContext eic = null;
                if (request.getInvocationContext() != null) {
                    eic = (EndpointInvocationContext) request.getInvocationContext();
                    eic.setResponseMessageContext(response);                
                }
                
                EndpointCallback callback = eic.getCallback();
                boolean handleFault = response.getMessage().isFault();
                if (!handleFault) {
                    if (log.isDebugEnabled()) {
                        log.debug("No fault detected in response message, sending back application response.");
                    }
                    callback.handleResponse(eic);
                }
                else {
                    if (log.isDebugEnabled()) {
                        log.debug("A fault was detected.  Sending back a fault response.");
                    }
                    callback.handleFaultResponse(eic);
                }
                
                // Set the thread's ClassLoader back to what it originally was.
                Thread.currentThread().setContextClassLoader(currentLoader);
                
                // Clean up the cached attachments from the request and the response.
                TransportUtils.deleteAttachments(eic.getRequestMessageContext().getAxisMessageContext());
                TransportUtils.deleteAttachments(eic.getResponseMessageContext().getAxisMessageContext());                
            } catch (Throwable e) {
                // Exceptions are swallowed, there is no reason to rethrow them
                if (log.isDebugEnabled()) {
                    log.debug("AN UNEXPECTED ERROR OCCURRED IN THE ASYNC WORKER THREAD");
                    log.debug("Exception is:" + e, e);
                }
            }

            return null;
        }
    }
    
    /** 
     * This will call the InvocationListener instances that were called during
     * the request processing for this message.
     */
    protected void responseReady(EndpointInvocationContext eic)  {
        List<InvocationListener> listenerList = eic.getInvocationListeners();
        if(listenerList != null) {
            InvocationListenerBean bean = new InvocationListenerBean(eic, InvocationListenerBean.State.RESPONSE);
            for(InvocationListener listener : listenerList) {
                try {
                    listener.notify(bean); 
                }
                catch(Exception e) {
                    throw ExceptionFactory.makeWebServiceException(e);
                }
            }
        }
    }

    protected static void setFaultResponseAction(Throwable exception, 
                                                 MessageContext request,
                                                 MessageContext response) {
         AxisOperation operation = request.getOperationDescription().getAxisOperation();
         if (operation != null) {
             exception = ClassUtils.getRootCause(exception);        
             String action = operation.getFaultAction(exception.getClass().getName());
             response.getAxisMessageContext().setWSAAction(action);
         }
    }
    
    /**
     * Determine if the thrown exception is a checked exception.
     * If so, then set the name of the checked exception on the response context
     * @param response MessageContext
     * @param m Method
     * @param t Throwable
     */
    protected static void setCheckedExceptionProperty(MessageContext response, Method m, Throwable t) {
        // Get the root of the exception
        if (t instanceof InvocationTargetException) {
            t = ((InvocationTargetException) t).getTargetException();
        }
        
        // Determine if the thrown exception is checked
        Class checkedException = JavaUtils.getCheckedException(t, m);
        
        // Add the property
        if (checkedException != null) {
            response.setProperty(Constants.CHECKED_EXCEPTION, checkedException.getCanonicalName());
        }
    }
    
}
