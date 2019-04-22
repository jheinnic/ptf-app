package name.jchein.portfolio.services.ecosystem.query;
//package name.jchein.portfolio.services.ecosystem.query;
//
//
//import org.apache.curator.x.discovery.ServiceType;
//import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
//import org.springframework.cloud.zookeeper.serviceregistry.ServiceInstanceRegistration;
//import org.springframework.cloud.zookeeper.serviceregistry.ZookeeperRegistration;
//import org.springframework.cloud.zookeeper.serviceregistry.ZookeeperServiceRegistry;
//import org.springframework.context.annotation.Configuration;
//
//
//@Configuration
//@EnableDiscoveryClient(autoRegister = true)
//public class MicroserviceRegistration
//{
//   private final ZookeeperServiceRegistry registry;
//
//
//   public MicroserviceRegistration( ZookeeperServiceRegistry registry )
//   {
//      this.registry = registry;
//   }
//
//
//   // called via some external process, such as an event or a custom actuator endpoint
//   public void register()
//   {
//      ZookeeperRegistration registration = constructRegistration();
//      this.registry.register(registration);
//   }
//
//
//   private ZookeeperRegistration constructRegistration()
//   {
//      return ServiceInstanceRegistration.builder()
//         .defaultUriSpec()
//         .id("resolution.registry")
//         .name("Resolution Registry")
//         .serviceType(ServiceType.DYNAMIC)
//         .address("127.0.0.1")
//         .sslPort(6443)
//         .port(6565)
//         .build();
//   }
//}
