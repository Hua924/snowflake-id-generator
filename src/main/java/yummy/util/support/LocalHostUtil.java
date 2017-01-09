package yummy.util.support;

import org.apache.commons.lang3.StringUtils;

import java.net.*;
import java.util.Enumeration;

/**
 * Created by jinqinghua on 2017/1/3.
 */
public class LocalHostUtil {


    public static String hostAddress() throws UnknownHostException, SocketException {
        String osType = (String) System.getProperties().get("os.name");
        if (StringUtils.isBlank(osType)) {
            throw new RuntimeException("snowflake sequence generator initialization failed:unsupport os");
        }
        if (osType.toLowerCase().indexOf("windows") > -1) {
            return InetAddress.getLocalHost().getHostAddress();
        }

        if (osType.toLowerCase().indexOf("mac") > -1) {
            return InetAddress.getLocalHost().getHostAddress();
        }

        if (osType.toLowerCase().indexOf("linux") > -1) {
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            NetworkInterface networkInterface;
            Enumeration<InetAddress> inetAddresses;
            InetAddress inetAddress;
            String ip;
            while (networkInterfaces.hasMoreElements()) {
                networkInterface = networkInterfaces.nextElement();
                inetAddresses = networkInterface.getInetAddresses();
                while (inetAddresses.hasMoreElements()) {
                    inetAddress = inetAddresses.nextElement();
                    if (inetAddress != null && inetAddress instanceof Inet4Address) { // IPV4
                        ip = inetAddress.getHostAddress();
                        if (!inetAddress.isLoopbackAddress()) {
                            return inetAddress.getHostAddress();
                        }
                    }
                }
            }
        }

        throw new RuntimeException("can not get host address");
    }
}
