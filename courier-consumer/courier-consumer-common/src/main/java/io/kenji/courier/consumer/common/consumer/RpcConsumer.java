package io.kenji.courier.consumer.common.consumer;

import io.kenji.courier.common.exception.RpcException;
import io.kenji.courier.common.helper.RpcServiceHelper;
import io.kenji.courier.common.threadpool.ClientThreadPool;
import io.kenji.courier.common.utils.IpUtils;
import io.kenji.courier.constants.RpcConstants;
import io.kenji.courier.consumer.common.future.RpcFuture;
import io.kenji.courier.consumer.common.handler.RpcConsumerHandler;
import io.kenji.courier.consumer.common.helper.RpcConsumerHandlerHelper;
import io.kenji.courier.consumer.common.initializer.RpcConsumerInitializer;
import io.kenji.courier.consumer.common.manager.ConsumerConnectionManager;
import io.kenji.courier.loadbalancer.api.context.ConnectionsContext;
import io.kenji.courier.protocol.RpcProtocol;
import io.kenji.courier.protocol.meta.ServiceMeta;
import io.kenji.courier.protocol.request.RpcRequest;
import io.kenji.courier.registry.api.RegistryService;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static io.kenji.courier.constants.RpcConstants.RPC_MULTI_DIRECT_SERVER_URL_SEPARATOR;

/**
 * @Author Kenji Peng
 * @Description
 * @Date 2023-12-13
 **/
@Slf4j
public class RpcConsumer implements Consumer {
    private final Bootstrap bootstrap;
    private final EventLoopGroup eventLoopGroup;

    private static volatile RpcConsumer instance;

    private final String localIp;

    private ScheduledExecutorService executorService;
    // heartbeatInterval
    private int heartbeatInterval = 30;
    private TimeUnit heartbeatIntervalTimeUnit = TimeUnit.SECONDS;
    // scanNotActiveChannelInterval
    private int scanNotActiveChannelInterval = 60;
    private TimeUnit scanNotActiveChannelIntervalTimeUnit = TimeUnit.SECONDS;

    private int retryIntervalInMillisecond = 1000;
    private int maxRetryTime = 3;

    private boolean enableDirectServer;

    private String directServerUrl;

    private boolean enableDelayConnection = false;

    private volatile boolean isConnectionInitiated = false;

    public RpcConsumer setEnableDelayConnection(boolean enableDelayConnection) {
        this.enableDelayConnection = enableDelayConnection;
        return this;
    }

    public RpcConsumer buildConnection(RegistryService registryService) {
        if (!enableDelayConnection && !isConnectionInitiated) {
            this.initConnection(registryService);
            this.isConnectionInitiated = true;
        }
        return this;
    }

    private RpcConsumer(int heartbeatInterval, TimeUnit heartbeatIntervalTimeUnit, int scanNotActiveChannelInterval, TimeUnit scanNotActiveChannelIntervalTimeUnit, int retryIntervalInMillisecond, int maxRetryTime, boolean enableDirectServer, String directServerUrl) {
        this.bootstrap = new Bootstrap();
        this.eventLoopGroup = new NioEventLoopGroup(4);
        localIp = IpUtils.getLocalHostIp();
        if (heartbeatInterval > 0 && heartbeatIntervalTimeUnit != null) {
            this.heartbeatInterval = heartbeatInterval;
            this.heartbeatIntervalTimeUnit = heartbeatIntervalTimeUnit;
        }
        if (scanNotActiveChannelInterval > 0 && scanNotActiveChannelIntervalTimeUnit != null) {
            this.scanNotActiveChannelInterval = scanNotActiveChannelInterval;
            this.scanNotActiveChannelIntervalTimeUnit = scanNotActiveChannelIntervalTimeUnit;
        }
        this.retryIntervalInMillisecond = retryIntervalInMillisecond > 0 ? retryIntervalInMillisecond : this.retryIntervalInMillisecond;
        this.maxRetryTime = maxRetryTime > 0 ? maxRetryTime : this.maxRetryTime;
        bootstrap.group(eventLoopGroup).channel(NioSocketChannel.class).handler(new RpcConsumerInitializer(this.heartbeatInterval, this.heartbeatIntervalTimeUnit));
        this.startHeartBeat();
        this.enableDirectServer = enableDirectServer;
        this.directServerUrl = directServerUrl;
    }


    private void startHeartBeat() {
        executorService = Executors.newScheduledThreadPool(2);
        executorService.scheduleAtFixedRate(() -> {
            log.info("========scan not active channel============");
            ConsumerConnectionManager.scanNotActiveChannel();
        }, 10, scanNotActiveChannelInterval, scanNotActiveChannelIntervalTimeUnit);
        executorService.scheduleAtFixedRate(() -> {
            log.info("========broadcast ping message from consumer============");
            ConsumerConnectionManager.broadcastPingMessageFromConsumer(this);
        }, 3, heartbeatInterval, heartbeatIntervalTimeUnit);
    }

    public static RpcConsumer getInstance(int heartbeatInterval, TimeUnit heartbeatIntervalTimeUnit, int scanNotActiveChannelInterval, TimeUnit scanNotActiveChannelIntervalTimeUnit, int retryIntervalInMillisecond, int maxRetryTime, boolean enableDirectServer, String directServerUrl) {
        if (instance == null) {
            synchronized (RpcConsumer.class) {
                if (instance == null) {
                    instance = new RpcConsumer(heartbeatInterval, heartbeatIntervalTimeUnit, scanNotActiveChannelInterval, scanNotActiveChannelIntervalTimeUnit, retryIntervalInMillisecond, maxRetryTime, enableDirectServer, directServerUrl);
                }
            }
        }
        return instance;
    }


    public void close() {
        eventLoopGroup.shutdownGracefully();
        ClientThreadPool.shutdown();
        RpcConsumerHandlerHelper.closeConsumerHandler();
        executorService.shutdown();
    }

    private void initConnection(RegistryService registryService) {
        Set<ServiceMeta> serviceMetaList = new HashSet<>();
        try {
            if (enableDirectServer) {
                serviceMetaList = Set.copyOf(this.getDirectServiceMeta());
            } else {
                serviceMetaList = Set.copyOf(registryService.discoveryAll());
            }
        } catch (Exception e) {
            log.error("Hit exception for loading connection metadata", e);
        }

        for (ServiceMeta serviceMeta : serviceMetaList) {
            try {
                this.getRpcConsumerHandler(serviceMeta.serviceAddr(), serviceMeta.servicePort());
            } catch (InterruptedException e) {
                log.warn("Hit exception during init connection", e);
            }
        }
    }

    @Override
    public RpcFuture sendRequest(RpcProtocol<RpcRequest> protocol, RegistryService registryService) throws Exception {
        RpcRequest body = protocol.getBody();
        String serviceKey = RpcServiceHelper.buildServiceKey(body.getClassName(), body.getVersion(), body.getGroup());
        Object[] parameters = body.getParameters();
        int invokerHashCode = (parameters == null || parameters.length == 0) ? serviceKey.hashCode() : parameters[0].hashCode();
        Optional<ServiceMeta> serviceMetaOptional = getServiceMeta(registryService, serviceKey, invokerHashCode);
        if (serviceMetaOptional.isPresent()) {
            ServiceMeta serviceMeta = serviceMetaOptional.get();
            RpcConsumerHandler handler = getRpcConsumerHandlerWithRetry(serviceMeta);
            if (handler != null) {
                return handler.sendRequest(protocol, body.getAsync(), body.getOneway());
            }
        }
        return null;
    }

    private RpcConsumerHandler getRpcConsumerHandlerWithRetry(ServiceMeta serviceMeta) throws InterruptedException {
        int count = 0;
        RpcConsumerHandler handler = null;
        while (count++ < maxRetryTime) {
            try {
                handler = getRpcConsumerHandlerInCache(serviceMeta);
                break;
            } catch (Exception e) {
                //do retry
                log.warn("Retry to connect due to exception, retry count: {}", count, e);
                Thread.sleep(retryIntervalInMillisecond);
            }
        }
        return handler;
    }

    private RpcConsumerHandler getRpcConsumerHandlerInCache(ServiceMeta serviceMeta) throws InterruptedException {
        RpcConsumerHandler handler = RpcConsumerHandlerHelper.get(serviceMeta);
        if (handler == null) {
            handler = getRpcConsumerHandler(serviceMeta.serviceAddr(), serviceMeta.servicePort());
        } else if (!handler.getChannel().isActive()) {
            handler.close();
            handler = getRpcConsumerHandler(serviceMeta.serviceAddr(), serviceMeta.servicePort());
        }
        return handler;
    }

    private Optional<ServiceMeta> getServiceMeta(RegistryService registryService, String serviceKey, Integer invokerHashCode) throws InterruptedException {
        Optional<ServiceMeta> serviceMeta;
        if (enableDirectServer) {
            List<ServiceMeta> directServiceMetaList = this.getDirectServiceMeta();
            if (directServiceMetaList != null && directServiceMetaList.size() > 1) {
                serviceMeta = Optional.of(getDirectServiceMetaFromMultiServerUrl(directServiceMetaList, registryService, invokerHashCode));
            } else if (directServiceMetaList != null && directServiceMetaList.size() == 1) {
                serviceMeta = directServiceMetaList.stream().findFirst();
            } else {
                throw new RpcException("Can't find url from given url");
            }
        } else {
            serviceMeta = this.getServiceMetaDataFromRegister(registryService, serviceKey, invokerHashCode);
        }
        return serviceMeta;
    }

    private List<ServiceMeta> getDirectServiceMeta() {
        List<ServiceMeta> serviceMetaList = null;
        if (!this.directServerUrl.contains(RPC_MULTI_DIRECT_SERVER_URL_SEPARATOR)) {
            directServerUrlChecking(this.directServerUrl);
            serviceMetaList = List.of(getDirectServiceMetaFromSingleServerUrl(this.directServerUrl));
        } else {
            String[] directServerUrls = directServerUrl.split(RPC_MULTI_DIRECT_SERVER_URL_SEPARATOR);
            if (directServerUrls.length > 0) {
                Arrays.stream(directServerUrls).forEach(this::directServerUrlChecking);
                return Arrays.stream(directServerUrls).map(this::getDirectServiceMetaFromSingleServerUrl).toList();
            }
        }
        return serviceMetaList;
    }

    private ServiceMeta getDirectServiceMetaFromMultiServerUrl(List<ServiceMeta> serviceMetaList, RegistryService registryService, int invokerHashCode) {
        Optional<ServiceMeta> serviceMetaOptional = registryService.select(serviceMetaList, invokerHashCode, localIp);
        serviceMetaOptional.orElseThrow(() -> new RpcException("Can't select service meta data from registry with MultiServerUrl"));
        return serviceMetaOptional.get();
    }

    private void directServerUrlChecking(String singleDirectServerUrl) {
        if (StringUtils.isEmpty(singleDirectServerUrl)) {
            throw new RpcException("Direct server url is null...");
        }
        if (!singleDirectServerUrl.contains(RpcConstants.IP_PORT_SPLIT)) {
            throw new RpcException("Direct server url doesn't contains : ");
        }
    }

    private ServiceMeta getDirectServiceMetaFromSingleServerUrl(String singleDirectServerUrl) {
        log.info("Service provider connect with connect directly, {}", singleDirectServerUrl);
        String[] directServerUrlArray = singleDirectServerUrl.split(RpcConstants.IP_PORT_SPLIT);
        return new ServiceMeta(null, null, directServerUrlArray[0], Integer.parseInt(directServerUrlArray[1]), null, 0);
    }

    private Optional<ServiceMeta> getServiceMetaDataFromRegister(RegistryService registryService, String serviceKey, int invokerHashCode) throws InterruptedException {
        log.info("Try to fetch service meta data by serviceKey: {}", serviceKey);
        int count = 0;
        while (count < maxRetryTime) {
            try {
                count++;
                Optional<ServiceMeta> serviceMetaOptional = registryService.discovery(serviceKey, invokerHashCode, this.localIp);
                if (serviceMetaOptional.isPresent()) {
                    return serviceMetaOptional;
                }
                log.info("Can't fetch service meta data from discovery, retry again. Retry count: {}", count);
            } catch (Exception e) {
                log.warn("Hit exception during retrieving service metadata from discovery. Going to retry to fetch server metadata, retry count: {}", count, e);
            }
            Thread.sleep(retryIntervalInMillisecond);
        }
        return Optional.empty();
    }

    public RpcConsumerHandler getRpcConsumerHandler(String serviceAddr, int servicePort) throws InterruptedException {
        ChannelFuture channelFuture = bootstrap.connect(serviceAddr, servicePort).sync();
        channelFuture.addListener(listener -> {
            if (channelFuture.isSuccess()) {
                log.info("Connect rpc provider server {} on port {} success", serviceAddr, servicePort);
                ConnectionsContext.add(serviceAddr, servicePort);
            } else {
                log.error("Failed to connect rpc provider server {} on port {}", serviceAddr, servicePort, channelFuture.cause());
                eventLoopGroup.shutdownGracefully();
            }
        });
        RpcConsumerHandler rpcConsumerHandler = channelFuture.channel().pipeline().get(RpcConsumerHandler.class);
        RpcConsumerHandlerHelper.put(serviceAddr, servicePort, rpcConsumerHandler);
        return rpcConsumerHandler;
    }

}
