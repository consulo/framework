/*
 * Copyright 2000-2013 Vaadin Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.vaadin.server.communication;

import java.io.IOException;
import java.io.Serializable;
import java.io.StringWriter;
import java.io.Writer;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.atmosphere.cpr.AtmosphereResource;
import org.json.JSONException;

import com.vaadin.ui.UI;

/**
 * {@link PushConnection} implementation using the Atmosphere push support that
 * is by default included in Vaadin.
 * 
 * @author Vaadin Ltd
 * @since 7.1
 */
public class AtmospherePushConnection implements Serializable, PushConnection {

    private UI ui;
    private transient AtmosphereResource resource;
    private Future<String> lastMessage;

    public AtmospherePushConnection(UI ui) {
        this.ui = ui;
    }

    @Override
    public void push() {
        assert isConnected();
        try {
            push(true);
        } catch (IOException e) {
            // TODO Error handling
            throw new RuntimeException("Push failed", e);
        }
    }

    /**
     * Pushes pending state changes and client RPC calls to the client.
     * 
     * @param async
     *            True if this push asynchronously originates from the server,
     *            false if it is a response to a client request.
     * @throws IOException
     */
    protected void push(boolean async) throws IOException {
        Writer writer = new StringWriter();
        try {
            new UidlWriter().write(getUI(), writer, false, false, async);
        } catch (JSONException e) {
            throw new IOException("Error writing UIDL", e);
        }
        sendMessage("for(;;);[{" + writer.toString() + "}]");
    }

    /**
     * Sends the given message to the current client.
     * 
     * @param message
     *            The message to send
     */
    void sendMessage(String message) {
        // "Broadcast" the changes to the single client only
        lastMessage = getResource().getBroadcaster().broadcast(message,
                getResource());
    }

    /**
     * Associates this connection with the given AtmosphereResource. If there is
     * a push pending, commits it.
     * 
     * @param resource
     *            The AtmosphereResource representing the push channel.
     * @throws IOException
     */
    protected void connect(AtmosphereResource resource) throws IOException {
        this.resource = resource;
    }

    /**
     * Returns whether this connection is currently open.
     */
    @Override
    public boolean isConnected() {
        return resource != null
                && resource.getBroadcaster().getAtmosphereResources()
                        .contains(resource);
    }

    /**
     * @return the UI associated with this connection.
     */
    protected UI getUI() {
        return ui;
    }

    /**
     * @return The AtmosphereResource associated with this connection or null if
     *         connection not open.
     */
    protected AtmosphereResource getResource() {
        return resource;
    }

    @Override
    public void disconnect() {
        if (lastMessage != null) {
            // Wait for the last message to be sent before closing the
            // connection (assumes that futures are completed in order)
            try {
                lastMessage.get(1000, TimeUnit.MILLISECONDS);
            } catch (TimeoutException e) {
                getLogger()
                        .log(Level.INFO,
                                "Timeout waiting for messages to be sent to client before disconnect");
            } catch (Exception e) {
                getLogger()
                        .log(Level.INFO,
                                "Error waiting for messages to be sent to client before disconnect");
            }
            lastMessage = null;
        }

        resource.resume();
        assert !resource.getBroadcaster().getAtmosphereResources()
                .contains(resource);
        resource = null;
    }

    /**
     * @since
     * @return
     */
    private static Logger getLogger() {
        return Logger.getLogger(AtmospherePushConnection.class.getName());
    }
}
