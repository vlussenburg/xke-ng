package com.xebia.xcoss.axcv.logic;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
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

import android.os.Build;

public class EC2TrustedSocketFactory extends SSLSocketFactory {

	private SSLContext sslContext = SSLContext.getInstance("TLS");

	public EC2TrustedSocketFactory() throws GeneralSecurityException, IOException {
		super(createKeyStore());

		X509TrustManager easyTrustManager = new X509TrustManager() {
			public void checkClientTrusted(X509Certificate[] xcs, String string) throws CertificateException {
				// for (X509Certificate x509Certificate : xcs) {
				// Log.e("debug", "check Client: " + string + " * " +
				// x509Certificate.getIssuerDN());
				// }
			}

			public void checkServerTrusted(X509Certificate[] xcs, String string) throws CertificateException {
				// for (X509Certificate x509Certificate : xcs) {
				// x509Certificate.checkValidity();
				// Log.e("debug", "check issuer : " +
				// x509Certificate.getIssuerDN());
				// Log.e("debug", "check subject: " +
				// x509Certificate.getSubjectDN());
				// }
			}

			public X509Certificate[] getAcceptedIssuers() {
				// TODO : Add the Comodo certificate
				return null;
			}
		};
		sslContext.init(null, new TrustManager[] { easyTrustManager }, null);
		setHostnameVerifier(SSLSocketFactory.BROWSER_COMPATIBLE_HOSTNAME_VERIFIER);
	}

	private static KeyStore createKeyStore() throws KeyStoreException, NoSuchAlgorithmException, CertificateException,
			IOException {
		try {
			KeyStore trustStore;

			if (Build.VERSION.SDK_INT >= 14) {
				trustStore = KeyStore.getInstance("AndroidCAStore");
				trustStore.load(null, null);
			} else {
				trustStore = KeyStore.getInstance("BKS");
				trustStore.load(new BufferedInputStream(new FileInputStream(getTrustStorePath())),
						getTrustStorePassword().toCharArray());
			}

			return trustStore;
		} catch (NoSuchAlgorithmException nsae) {
			throw new AssertionError(nsae);
		} catch (KeyStoreException e) {
			throw new AssertionError(e);
		} catch (CertificateException e) {
			throw new AssertionError(e);
		} catch (FileNotFoundException e) {
			throw new AssertionError(e);
		} catch (IOException e) {
			throw new AssertionError(e);
		}
//
//		KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
//		FileInputStream is = new FileInputStream("/system/etc/security/cacerts.bks");
//		trustStore.load(is, null);
//		// Enumeration<String> e = trustStore.aliases();
//		// while ( e.hasMoreElements() ) {
//		// String key = e.nextElement();
//		// Certificate certificate = trustStore.getCertificate(key);
//		// String message = "?";
//		// if ( certificate instanceof X509Certificate ) {
//		// message = ((X509Certificate)certificate).getSubjectDN().toString();
//		// }
//		// Log.v("Security", message);
//		// }
//		return trustStore;
	}

	private static String getTrustStorePath() {
		String path = System.getProperty("javax.net.ssl.trustStore");

		if (path == null) {
			path = System.getProperty("java.home") + File.separator + "etc" + File.separator + "security"
					+ File.separator + "cacerts.bks";
		}

		return path;
	}

	private static String getTrustStorePassword() {
		String password = System.getProperty("javax.net.ssl.trustStorePassword");

		if (password == null)
			password = "changeit";

		return password;
	}

	@Override
	public Socket createSocket(Socket socket, String host, int port, boolean autoClose) throws IOException,
			UnknownHostException {
		return sslContext.getSocketFactory().createSocket(socket, host, port, autoClose);
	}

	@Override
	public Socket createSocket() throws IOException {
		return sslContext.getSocketFactory().createSocket();
	}
}
