package org.openhab.binding.coap.internal;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.jetty.client.HttpAuthenticationStore;
import org.eclipse.jetty.client.api.AuthenticationStore;

/**
 * @author IL
 *         contains all additional information that was used in the initial binding but is not implemented in the
 *         CoapClient by Californium
 *
 *         note: is not necessary because AuthenticationStore is not needed
 */
public class CoAPClientWrapper {

    private CoapClient coapClient = new CoapClient();

    // AuthenticationStore is not needed in fact
    private AuthenticationStore authenticationStore = new HttpAuthenticationStore();

    public CoapClient getCoapClient() {
        return coapClient;
    }

    public void setCoapClient(CoapClient coapClient) {
        this.coapClient = coapClient;
    }

    public AuthenticationStore getAuthenticationStore() {
        return authenticationStore;
    }

    public void setAuthenticationStore(AuthenticationStore authenticationStore) {
        this.authenticationStore = authenticationStore;
    }

}
