package cljwxpay;

import com.github.wxpay.sdk.WXPayConfig;
import com.github.wxpay.sdk.IWXPayDomain;
import com.github.wxpay.sdk.WXPayConstants;
import java.io.*;

public class AppWxpayConfig extends WXPayConfig{

    private byte[] certData;
    private String appID;
    private String mchID;
    private String key;
    private int httpConnectTimeoutMs = 6*1000;
    private int httpReadTimeoutMs = 8*1000;

    public AppWxpayConfig(String appID, String mchID, String key){
        this.appID = appID;
        this.mchID = mchID;
        this.key = key;
    }

    public String getAppID() {
        return this.appID;
    }

    public String getMchID() {
        return this.mchID;
    }

    public String getKey() {
        return this.key;
    }

    public void setCert(String certPath) throws Exception {
        //String certPath = "/path/to/apiclient_cert.p12";
        File file = new File(certPath);
        InputStream certStream = new FileInputStream(file);
        this.certData = new byte[(int) file.length()];
        certStream.read(this.certData);
        certStream.close();

    }

    public InputStream getCertStream() {
        ByteArrayInputStream certBis = new ByteArrayInputStream(this.certData);
        return certBis;
    }

    public void setHttpConnectTimeoutMs(int ms){
        this.httpConnectTimeoutMs = ms;
    }
    public int getHttpConnectTimeoutMs() {
        return this.httpConnectTimeoutMs;
    }

    public void setHttpReadTimeoutMs(int ms) {
        this.httpReadTimeoutMs = ms;
    }
    public int getHttpReadTimeoutMs() {
        return this.httpReadTimeoutMs;
    }

    public boolean shouldAutoReport() {
        return false;
    }
}