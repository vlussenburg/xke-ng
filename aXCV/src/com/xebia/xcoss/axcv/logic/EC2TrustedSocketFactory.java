package com.xebia.xcoss.axcv.logic;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.conn.ssl.SSLSocketFactory;

import android.util.Log;
import com.xebia.xcoss.axcv.R;

public class EC2TrustedSocketFactory extends SSLSocketFactory {
	
    private SSLContext sslContext = SSLContext.getInstance("TLS");

    public EC2TrustedSocketFactory() throws GeneralSecurityException, IOException {
        super(createKeyStore());

		X509TrustManager easyTrustManager = new X509TrustManager() {
			public void checkClientTrusted(X509Certificate[] xcs, String string) throws CertificateException {
				for (X509Certificate x509Certificate : xcs) {
					Log.e("debug", "check Client: " + string + " * " + x509Certificate.getIssuerDN());
				}
			}

			public void checkServerTrusted(X509Certificate[] xcs, String string) throws CertificateException {
				for (X509Certificate x509Certificate : xcs) {
					Log.e("debug", "check Server: " + string + " * " + x509Certificate.getIssuerDN());
				}
			}

			public X509Certificate[] getAcceptedIssuers() {
				return null;
			}
		};
        sslContext.init(null, new TrustManager[] { easyTrustManager }, null);
        setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
    }

    private static KeyStore createKeyStore() throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException {
        KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
        trustStore.load(null, null);
        return trustStore;
	}

	@Override
    public Socket createSocket(Socket socket, String host, int port, boolean autoClose) throws IOException, UnknownHostException {
        return sslContext.getSocketFactory().createSocket(socket, host, port, autoClose);
    }

    @Override
    public Socket createSocket() throws IOException {
        return sslContext.getSocketFactory().createSocket();
    }
}
