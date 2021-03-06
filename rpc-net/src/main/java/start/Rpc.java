package start;

import com.google.common.base.Strings;
import config.Config;
import config.ConfigTem;
import config.SimpleConfig;
import remote.server.NettyServer;
import remote.server.RpcServer;
import remote.handler.SimpleRequestHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import proxy.InvocationHandlerFactory;
import regsitry.Registry;
import regsitry.RegistryFactory;
import util.FileUtils;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Objects;
import java.util.Properties;

/**
 * Created by JackJ on 2021/1/16.
 */
public class Rpc {

    private static final Logger LOGGER = LoggerFactory.getLogger(Rpc.class);

    private String version;

    private String group;


    private Rpc(){

    }

    public Rpc version(String version) {
        SimpleConfig.INSTANCE.put("version", version);
        this.version = version;
        return this;
    }

    public Rpc group(String group){
        SimpleConfig.INSTANCE.put("group", group);
        this.group = group;
        return this;
    }

    public void start() {
        LOGGER.info("start server");
        NettyServer rpcServer = new NettyServer(Integer.parseInt(SimpleConfig.INSTANCE.get("provider.port")), new SimpleRequestHandler());
        Thread thread = new Thread(rpcServer::start);
        thread.start();
    }

    public Rpc registry(Object registryService) {

        if (Objects.isNull(registryService)) {
            throw new RuntimeException("注册的服务不能为null");
        }

        Registry registry = RegistryFactory.getRegistry();

        String serviceName = genServiceName(registryService);
        InetSocketAddress address = null;

        try {
            address = new InetSocketAddress(InetAddress.getLocalHost().getHostAddress(), Integer.parseInt(SimpleConfig.INSTANCE.get("provider.port")));
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        registry.registry(serviceName, address);


        return this;
    }

    private String genServiceName(Object service){
        String className = service.getClass().getInterfaces()[0].getCanonicalName();

        return className + this.group + this.version;
    }


    public <T> T getBean(Class<T> tClass) {
        return (T) InvocationHandlerFactory.getRpcProxy(tClass);
    }

}
